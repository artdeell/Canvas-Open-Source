#include "OtherAudioLocate.h"
#include "OtherAudioSwap.h"

#include <jni.h>
#include <link.h>
#include <sys/mman.h>
#include <unistd.h>

#include <atomic>
#include <cerrno>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <mutex>
#include <system_error>
#include <thread>
#include <vector>

#include "../../include/misc/Logger.h"

namespace {

constexpr int kAttempts = 120;

struct Mapping {
    std::uintptr_t begin;
    std::uintptr_t end;
    char perms[5];
};

std::vector<Mapping> ReadMaps() {
    std::vector<Mapping> maps;
    FILE *f = std::fopen("/proc/self/maps", "re");
    if (f == nullptr)
        return maps;
    char *line = nullptr;
    std::size_t capacity = 0;
    while (getline(&line, &capacity, f) != -1) {
        unsigned long begin = 0, end = 0;
        Mapping m{};
        if (std::sscanf(line, "%lx-%lx %4s", &begin, &end, m.perms) != 3)
            continue;
        m.begin = begin;
        m.end = end;
        maps.push_back(m);
    }
    std::free(line);
    std::fclose(f);
    return maps;
}

const Mapping *Find(const std::vector<Mapping> &maps, std::uintptr_t at, std::size_t size) {
    for (const Mapping &m : maps)
        if (at >= m.begin && at < m.end && m.end - at >= size)
            return &m;
    return nullptr;
}

struct Search {
    std::uintptr_t base;
    bool found;
    std::vector<OtherAudio::Segment> segments;
};

int CollectSegments(dl_phdr_info *info, std::size_t, void *data) {
    auto *search = static_cast<Search *>(data);
    if (info->dlpi_addr != search->base)
        return 0;
    search->found = true;
    for (int i = 0; i < info->dlpi_phnum; ++i) {
        const ElfW(Phdr) &ph = info->dlpi_phdr[i];
        OtherAudio::Segment s;
        s.type = ph.p_type;
        s.flags = ph.p_flags;
        s.vaddr = static_cast<std::uintptr_t>(ph.p_vaddr);
        s.memsz = static_cast<std::uintptr_t>(ph.p_memsz);
        search->segments.push_back(s);
    }
    return 1;
}

std::atomic<bool> g_enabled{false};
std::atomic<std::uintptr_t> g_gate{0};
std::mutex g_mutex;
bool g_started = false;

bool Replacement(void *) {
    return OtherAudio::Answer(reinterpret_cast<std::uintptr_t>(__builtin_return_address(0)),
                              g_gate.load(std::memory_order_acquire), g_enabled.load(std::memory_order_relaxed),
                              OtherAudio::IsPlaying());
}

void *Ptr(std::uintptr_t value) {
    return reinterpret_cast<void *>(value);
}

void Refuse(const OtherAudio::Plan &plan) {
    using OtherAudio::Decision;
    switch (plan.decision) {
    case Decision::Disabled:
        LOGW("Other audio: setting off, not swapping");
        break;
    case Decision::NotLocated:
        LOGW("Other audio: update site not located, not swapping");
        break;
    case Decision::NotReady:
        LOGW("Other audio: %p still null after %d attempts, not swapping", Ptr(plan.at), kAttempts);
        break;
    case Decision::Unreadable:
        LOGW("Other audio: %p unreadable, not swapping", Ptr(plan.at));
        break;
    case Decision::SystemIoMismatch:
        LOGW("Other audio: SystemIO routes disagree (%p from audio, %p from global), not swapping",
             Ptr(plan.systemIo), Ptr(plan.otherSystemIo));
        break;
    case Decision::NotAVtable:
        LOGW("Other audio: SystemIO %p points at %p, not a vtable address point, not swapping", Ptr(plan.systemIo),
             Ptr(plan.vtable));
        break;
    case Decision::SlotOutsideRelro:
        LOGW("Other audio: slot %p is outside RELRO, not swapping", Ptr(plan.slotAddress));
        break;
    case Decision::TargetOutsideCode:
        LOGW("Other audio: slot %p holds %p, outside the game's code, not swapping", Ptr(plan.slotAddress),
             Ptr(plan.current));
        break;
    case Decision::NotTheStub:
        LOGW("Other audio: slot %p holds %p, not the return-false stub, not swapping", Ptr(plan.slotAddress),
             Ptr(plan.current));
        break;
    case Decision::Swap:
    case Decision::AlreadyApplied:
        break;
    }
}

bool Write(const OtherAudio::Plan &plan, std::uintptr_t replacement, const std::vector<Mapping> &maps) {
    const Mapping *m = Find(maps, plan.slotAddress, sizeof(std::uintptr_t));
    if (m == nullptr || std::strcmp(m->perms, "r--p") != 0) {
        LOGW("Other audio: slot %p is mapped %s, expected r--p, not swapping", Ptr(plan.slotAddress),
             m ? m->perms : "nowhere");
        return false;
    }
    const long pageSize = sysconf(_SC_PAGESIZE);
    if (pageSize <= 0) {
        LOGW("Other audio: page size unknown, not swapping");
        return false;
    }
    const std::uintptr_t page = plan.slotAddress & ~(static_cast<std::uintptr_t>(pageSize) - 1);
    if (page < m->begin || m->end - page < static_cast<std::uintptr_t>(pageSize)) {
        LOGW("Other audio: page %p is not inside one mapping, not swapping", Ptr(page));
        return false;
    }
    if (mprotect(Ptr(page), static_cast<std::size_t>(pageSize), PROT_READ | PROT_WRITE) != 0) {
        LOGW("Other audio: mprotect read-write at %p failed (errno %d), not swapping", Ptr(page), errno);
        return false;
    }
    std::uintptr_t expected = plan.current;
    const bool swapped = __atomic_compare_exchange_n(reinterpret_cast<std::uintptr_t *>(plan.slotAddress), &expected,
                                                     replacement, false, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST);
    if (mprotect(Ptr(page), static_cast<std::size_t>(pageSize), PROT_READ) != 0)
        LOGW("Other audio: mprotect read-only at %p failed (errno %d), page left writable", Ptr(page), errno);
    if (!swapped) {
        LOGW("Other audio: slot %p changed to %p before the swap, not swapping", Ptr(plan.slotAddress),
             Ptr(expected));
        return false;
    }
    std::uintptr_t written = 0;
    std::memcpy(&written, reinterpret_cast<const void *>(plan.slotAddress), sizeof(written));
    if (written != replacement) {
        LOGW("Other audio: swap at %p did not stick (%p)", Ptr(plan.slotAddress), Ptr(written));
        return false;
    }
    return true;
}

bool Attempt(OtherAudio::Plan &plan) {
    const OtherAudio::Located *located = OtherAudio::Result();
    OtherAudio::Image image;
    if (located != nullptr) {
        Search search{located->base, false, {}};
        dl_iterate_phdr(CollectSegments, &search);
        if (!search.found || !OtherAudio::Layout(located->base, search.segments.data(),
                                                 static_cast<int>(search.segments.size()), image)) {
            LOGW("Other audio: game library layout unexpected (found %d, %zu segments), not swapping",
                 search.found ? 1 : 0, search.segments.size());
            return true;
        }
    }

    const std::vector<Mapping> maps = ReadMaps();
    auto read = [&maps](std::uintptr_t at, void *out, std::size_t size) {
        const Mapping *m = Find(maps, at, size);
        if (m == nullptr || m->perms[0] != 'r')
            return false;
        std::memcpy(out, reinterpret_cast<const void *>(at), size);
        return true;
    };
    const std::uintptr_t replacement = reinterpret_cast<std::uintptr_t>(&Replacement);
    plan = OtherAudio::Decide(read, located, g_enabled.load(std::memory_order_relaxed), image, replacement);

    switch (plan.decision) {
    case OtherAudio::Decision::NotReady:
        return false;
    case OtherAudio::Decision::AlreadyApplied:
        g_gate.store(located->chain.gateReturn, std::memory_order_release);
        LOGI("Other audio: SystemIO slot already swapped at %p", Ptr(plan.slotAddress));
        return true;
    case OtherAudio::Decision::Swap:
        break;
    default:
        Refuse(plan);
        return true;
    }

    g_gate.store(located->chain.gateReturn, std::memory_order_release);
    if (Write(plan, replacement, maps))
        LOGI("Other audio: SystemIO slot 0x%x swapped at %p (vtable RVA 0x%lx, stub RVA 0x%lx), gate RVA 0x%lx",
             located->chain.slot, Ptr(plan.slotAddress), static_cast<unsigned long>(plan.vtable - located->base),
             static_cast<unsigned long>(plan.current - located->base),
             static_cast<unsigned long>(located->chain.gateReturn - located->base));
    return true;
}

void Run() {
    OtherAudio::Plan plan;
    for (int attempt = 0; attempt < kAttempts; ++attempt) {
        if (Attempt(plan))
            return;
        sleep(1);
    }
    Refuse(plan);
}

void Start() {
    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_started)
        return;
    g_started = true;
    try {
        std::thread(Run).detach();
    } catch (const std::system_error &e) {
        LOGW("Other audio: worker thread not started (%s), not swapping", e.what());
    }
}

}

extern "C" JNIEXPORT void JNICALL
Java_git_artdeell_skymodloader_MainActivity_nativeSetOtherAudioEnabled(JNIEnv *, jclass, jboolean enabled) {
    g_enabled.store(enabled == JNI_TRUE, std::memory_order_relaxed);
    if (enabled == JNI_TRUE)
        Start();
}
