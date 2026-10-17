#pragma once

#include <cstddef>
#include <cstdint>
#include <cstring>

#include "OtherAudioLocate.h"

namespace OtherAudio {

constexpr std::uint32_t kReturnFalseStub[5] = {0xa9bf7bfdu, 0x910003fdu, 0x2a1f03e0u, 0xa8c17bfdu, 0xd65f03c0u};
constexpr std::uint32_t kPtLoad = 1;
constexpr std::uint32_t kPtGnuRelro = 0x6474e552u;
constexpr std::uint32_t kPfX = 1;

struct Segment {
    std::uint32_t type = 0;
    std::uint32_t flags = 0;
    std::uintptr_t vaddr = 0;
    std::uintptr_t memsz = 0;
};

struct Image {
    std::uintptr_t codeBegin = 0;
    std::uintptr_t codeEnd = 0;
    std::uintptr_t relroBegin = 0;
    std::uintptr_t relroEnd = 0;
};

inline bool Layout(std::uintptr_t base, const Segment *segments, int count, Image &out) {
    int code = 0, relro = 0;
    Image image;
    for (int i = 0; i < count; ++i) {
        const Segment &s = segments[i];
        if (s.type == kPtLoad && (s.flags & kPfX) != 0) {
            image.codeBegin = base + s.vaddr;
            image.codeEnd = image.codeBegin + s.memsz;
            ++code;
        } else if (s.type == kPtGnuRelro) {
            image.relroBegin = base + s.vaddr;
            image.relroEnd = image.relroBegin + s.memsz;
            ++relro;
        }
    }
    if (code != 1 || relro != 1 || image.codeBegin >= image.codeEnd || image.relroBegin >= image.relroEnd)
        return false;
    out = image;
    return true;
}

enum class Decision {
    Swap,
    AlreadyApplied,
    Disabled,
    NotLocated,
    NotReady,
    Unreadable,
    SystemIoMismatch,
    NotAVtable,
    SlotOutsideRelro,
    TargetOutsideCode,
    NotTheStub,
};

struct Plan {
    Decision decision = Decision::NotLocated;
    std::uintptr_t at = 0;
    std::uintptr_t systemIo = 0;
    std::uintptr_t otherSystemIo = 0;
    std::uintptr_t vtable = 0;
    std::uintptr_t slotAddress = 0;
    std::uintptr_t current = 0;
};

namespace Detail {

inline Plan Stop(Plan plan, Decision decision, std::uintptr_t at) {
    plan.decision = decision;
    plan.at = at;
    return plan;
}

inline bool Inside(std::uintptr_t at, std::size_t size, std::uintptr_t begin, std::uintptr_t end) {
    return at >= begin && at < end && end - at >= size;
}

template <class Read>
bool Pointer(Read &read, std::uintptr_t at, std::uintptr_t &out) {
    return read(at, &out, sizeof(out));
}

}

template <class Read>
Plan Decide(Read &&read, const Located *located, bool enabled, const Image &image, std::uintptr_t replacement) {
    using Detail::Stop;
    Plan plan;
    if (!enabled)
        return Stop(plan, Decision::Disabled, 0);
    if (located == nullptr)
        return Stop(plan, Decision::NotLocated, 0);

    const Chain &chain = located->chain;
    std::uintptr_t audio = 0;
    if (!Detail::Pointer(read, chain.audioGlobal, audio))
        return Stop(plan, Decision::Unreadable, chain.audioGlobal);
    if (audio == 0)
        return Stop(plan, Decision::NotReady, chain.audioGlobal);
    const std::uintptr_t field = audio + located->systemIoOffset;
    if (!Detail::Pointer(read, field, plan.systemIo))
        return Stop(plan, Decision::Unreadable, field);
    if (plan.systemIo == 0)
        return Stop(plan, Decision::NotReady, field);
    if (located->systemIoGlobal != 0) {
        if (!Detail::Pointer(read, located->systemIoGlobal, plan.otherSystemIo))
            return Stop(plan, Decision::Unreadable, located->systemIoGlobal);
        if (plan.otherSystemIo == 0)
            return Stop(plan, Decision::NotReady, located->systemIoGlobal);
        if (plan.otherSystemIo != plan.systemIo)
            return Stop(plan, Decision::SystemIoMismatch, located->systemIoGlobal);
    }

    if (!Detail::Pointer(read, plan.systemIo, plan.vtable))
        return Stop(plan, Decision::Unreadable, plan.systemIo);
    std::uintptr_t header[2] = {1, 1};
    if (plan.vtable % sizeof(std::uintptr_t) != 0 || plan.vtable < sizeof(header))
        return Stop(plan, Decision::NotAVtable, plan.vtable);
    if (!read(plan.vtable - sizeof(header), header, sizeof(header)))
        return Stop(plan, Decision::Unreadable, plan.vtable - sizeof(header));
    if (header[0] != 0 || header[1] != 0)
        return Stop(plan, Decision::NotAVtable, plan.vtable);

    plan.slotAddress = plan.vtable + chain.slot;
    if (!Detail::Inside(plan.slotAddress, sizeof(std::uintptr_t), image.relroBegin, image.relroEnd))
        return Stop(plan, Decision::SlotOutsideRelro, plan.slotAddress);
    if (!Detail::Pointer(read, plan.slotAddress, plan.current))
        return Stop(plan, Decision::Unreadable, plan.slotAddress);
    if (plan.current == replacement)
        return Stop(plan, Decision::AlreadyApplied, plan.slotAddress);
    if (!Detail::Inside(plan.current, sizeof(kReturnFalseStub), image.codeBegin, image.codeEnd))
        return Stop(plan, Decision::TargetOutsideCode, plan.current);
    std::uint32_t words[5] = {};
    if (!read(plan.current, words, sizeof(words)))
        return Stop(plan, Decision::Unreadable, plan.current);
    if (std::memcmp(words, kReturnFalseStub, sizeof(words)) != 0)
        return Stop(plan, Decision::NotTheStub, plan.current);
    return Stop(plan, Decision::Swap, plan.slotAddress);
}

inline bool Answer(std::uintptr_t returnAddress, std::uintptr_t gate, bool enabled, bool playing) {
    return enabled && playing && gate != 0 && returnAddress == gate;
}

}
