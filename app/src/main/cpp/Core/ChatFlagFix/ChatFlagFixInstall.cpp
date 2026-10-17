#include "ChatFlagFixInstall.h"

#include "ChatFlagFix.h"

#include <link.h>

#include <cstdio>
#include <cstring>
#include <mutex>
#include <vector>

#include "../Cipher/Cipher.h"
#include "../../include/misc/Logger.h"

namespace {

struct Range {
    std::uintptr_t begin;
    std::size_t size;
    bool executable;
};

struct Search {
    const char *libName;
    std::uintptr_t base;
    std::vector<Range> ranges;
};

bool EndsWith(const char *text, const char *suffix) {
    const std::size_t a = std::strlen(text), b = std::strlen(suffix);
    return a >= b && std::strcmp(text + a - b, suffix) == 0;
}

int CollectRanges(dl_phdr_info *info, std::size_t, void *data) {
    auto *search = static_cast<Search *>(data);
    if (info->dlpi_name == nullptr || !EndsWith(info->dlpi_name, search->libName))
        return 0;
    search->base = info->dlpi_addr;
    for (int i = 0; i < info->dlpi_phnum; ++i) {
        const ElfW(Phdr) &ph = info->dlpi_phdr[i];
        if (ph.p_type != PT_LOAD || (ph.p_flags & PF_R) == 0)
            continue;
        search->ranges.push_back({info->dlpi_addr + ph.p_vaddr, static_cast<std::size_t>(ph.p_filesz),
                                  (ph.p_flags & PF_X) != 0});
    }
    return 1;
}

std::mutex g_installMutex;
bool g_finished = false;

}

namespace ChatFlagFix {

bool Install(const char *libName) {
    std::lock_guard<std::mutex> lock(g_installMutex);
    if (g_finished)
        return true;

    Search search{libName, 0, {}};
    dl_iterate_phdr(CollectRanges, &search);
    if (search.ranges.empty()) {
        LOGD("Chat fix: %s not loaded yet", libName);
        return false;
    }

    auto read = [&search](std::uintptr_t at, void *out, std::size_t size) {
        for (const Range &range : search.ranges) {
            if (at >= range.begin && size <= range.size && at - range.begin <= range.size - size) {
                std::memcpy(out, reinterpret_cast<const void *>(at), size);
                return true;
            }
        }
        return false;
    };

    int found = 0;
    Site site;
    for (const Range &range : search.ranges) {
        if (!range.executable)
            continue;
        Site candidate;
        const int count = Find(read, range.begin, range.size, candidate);
        if (count > 0 && found == 0)
            site = candidate;
        found += count;
    }

    g_finished = true;
    if (found != 1) {
        LOGW("Chat fix: %d candidate sites in %s, not patching", found, libName);
        return false;
    }

    std::uint32_t current = 0;
    if (!read(site.address, &current, sizeof(current))) {
        LOGW("Chat fix: site %p unreadable, not patching", reinterpret_cast<void *>(site.address));
        return false;
    }

    switch (Decide(current, site)) {
    case Decision::AlreadyApplied:
        LOGI("Chat fix: already applied at %p", reinterpret_cast<void *>(site.address));
        return true;
    case Decision::Foreign:
        LOGW("Chat fix: %p holds %08x, expected %08x, not patching",
             reinterpret_cast<void *>(site.address), current, site.original);
        return false;
    case Decision::Write:
        break;
    }

    char hex[9] = {};
    const auto *bytes = reinterpret_cast<const unsigned char *>(&site.replacement);
    std::snprintf(hex, sizeof(hex), "%02x%02x%02x%02x", bytes[0], bytes[1], bytes[2], bytes[3]);
    auto *patch = new CipherPatch();
    patch->set_Address(site.address, false);
    patch->set_Opcode(hex);
    patch->Fire();
    __builtin___clear_cache(reinterpret_cast<char *>(site.address), reinterpret_cast<char *>(site.address + 4));

    std::uint32_t written = 0;
    if (!read(site.address, &written, sizeof(written)) || written != site.replacement) {
        LOGW("Chat fix: patch at %p did not take (%08x)", reinterpret_cast<void *>(site.address), written);
        return false;
    }
    LOGI("Chat fix: flag mask patched at %p (RVA 0x%lx, flag byte +0x%x)", reinterpret_cast<void *>(site.address),
         static_cast<unsigned long>(site.address - search.base), site.flagOffset);
    return true;
}

}
