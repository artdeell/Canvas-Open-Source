#include "OtherAudioLocate.h"

#include <jni.h>
#include <link.h>

#include <atomic>
#include <cstring>
#include <mutex>
#include <vector>

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

std::mutex g_mutex;
bool g_done = false;
bool g_found = false;
OtherAudio::Located g_result;
std::atomic<bool> g_playing{false};

}

namespace OtherAudio {

bool Locate(const char *libName) {
    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_done)
        return g_found;

    Search search{libName, 0, {}};
    dl_iterate_phdr(CollectRanges, &search);
    if (search.ranges.empty()) {
        LOGD("Other audio: %s not loaded yet", libName);
        return false;
    }
    g_done = true;

    auto read = [&search](std::uintptr_t at, void *out, std::size_t size) {
        for (const Range &range : search.ranges) {
            if (at >= range.begin && size <= range.size && at - range.begin <= range.size - size) {
                std::memcpy(out, reinterpret_cast<const void *>(at), size);
                return true;
            }
        }
        return false;
    };

    int chains = 0, fmods = 0, setters = 0;
    Chain chain;
    FmodSelect fmod;
    Setter setter;
    for (const Range &range : search.ranges) {
        if (!range.executable)
            continue;
        Chain c;
        FmodSelect f;
        Setter s;
        const int nc = FindChain(read, range.begin, range.size, c);
        const int nf = FindFmod(read, range.begin, range.size, f);
        const int ns = FindSetter(read, range.begin, range.size, s);
        if (nc && !chains)
            chain = c;
        if (nf && !fmods)
            fmod = f;
        if (ns && !setters)
            setter = s;
        chains += nc;
        fmods += nf;
        setters += ns;
    }

    if (chains != 1) {
        LOGW("Other audio: %d headphone update sites, not supported", chains);
        return false;
    }
    if (fmods > 1 || setters > 1 || (fmods == 0 && setters == 0)) {
        LOGW("Other audio: SystemIO not located (fmod %d, setter %d)", fmods, setters);
        return false;
    }
    if (fmods == 1 && fmod.slot != chain.slot) {
        LOGW("Other audio: slot mismatch (update 0x%x, fmod 0x%x)", chain.slot, fmod.slot);
        return false;
    }
    if (fmods == 1 && setters == 1 && fmod.systemIoOffset != setter.systemIoOffset) {
        LOGW("Other audio: SystemIO offset mismatch (fmod 0x%x, setter 0x%x)", fmod.systemIoOffset,
             setter.systemIoOffset);
        return false;
    }

    g_result.base = search.base;
    g_result.chain = chain;
    g_result.systemIoOffset = fmods == 1 ? fmod.systemIoOffset : setter.systemIoOffset;
    g_result.systemIoGlobal = setters == 1 ? setter.systemIoGlobal : 0;
    g_found = true;
    LOGI("Other audio: update RVA 0x%lx slot 0x%x gate RVA 0x%lx flag +0x%x fade slot 0x%x RVA 0x%lx",
         static_cast<unsigned long>(chain.site - search.base), chain.slot,
         static_cast<unsigned long>(chain.gateReturn - search.base), chain.flagOffset, chain.fadeSlot,
         static_cast<unsigned long>(chain.fadeReturn - search.base));
    LOGI("Other audio: audio global RVA 0x%lx SystemIO at +0x%x, SystemIO global RVA 0x%lx",
         static_cast<unsigned long>(chain.audioGlobal - search.base), g_result.systemIoOffset,
         static_cast<unsigned long>(g_result.systemIoGlobal ? g_result.systemIoGlobal - search.base : 0));
    return true;
}

const Located *Result() {
    std::lock_guard<std::mutex> lock(g_mutex);
    return g_found ? &g_result : nullptr;
}

void SetPlaying(bool playing) {
    g_playing.store(playing, std::memory_order_relaxed);
}

bool IsPlaying() {
    return g_playing.load(std::memory_order_relaxed);
}

}

extern "C" JNIEXPORT void JNICALL
Java_git_artdeell_skymodloader_MainActivity_nativeSetOtherAudioPlaying(JNIEnv *, jclass, jboolean playing) {
    OtherAudio::SetPlaying(playing == JNI_TRUE);
}
