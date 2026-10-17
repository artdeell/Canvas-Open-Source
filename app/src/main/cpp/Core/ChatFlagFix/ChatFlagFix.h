#pragma once

#include <cstddef>
#include <cstdint>
#include <cstring>

namespace ChatFlagFix {

struct Site {
    std::uintptr_t address = 0;
    std::uint32_t original = 0;
    std::uint32_t replacement = 0;
    std::uint32_t flagOffset = 0;
};

enum class Decision { Write, AlreadyApplied, Foreign };

inline Decision Decide(std::uint32_t current, const Site &site) {
    if (current == site.original)
        return Decision::Write;
    if (current == site.replacement)
        return Decision::AlreadyApplied;
    return Decision::Foreign;
}

namespace Detail {

constexpr std::uint32_t kAndClearBit1 = 0x121e7800u;
constexpr std::uint32_t kAndKeepBits5To7 = 0x121b7000u;
constexpr std::uint32_t kAndKeepBits0To1 = 0x12000400u;
constexpr int kLoadWindow = 4;
constexpr int kStepWindow = 4;
constexpr int kCallWindow = 6;
constexpr int kAddWindow = 12;
constexpr int kAdrpWindow = 5;

inline std::uint32_t Rd(std::uint32_t w) { return w & 0x1fu; }
inline std::uint32_t Rn(std::uint32_t w) { return (w >> 5) & 0x1fu; }

inline bool AndImmediate(std::uint32_t w, std::uint32_t base, std::uint32_t reg) {
    return (w & 0xfffffc00u) == base && Rd(w) == reg && Rn(w) == reg;
}

inline bool OrrShifted(std::uint32_t w, std::uint32_t reg, std::uint32_t shift) {
    return (w & 0xffe0fc00u) == (0x2a000000u | (shift << 10)) && Rd(w) == reg && Rn(w) == reg;
}

inline bool ByteAccess(std::uint32_t w, bool load, std::uint32_t &reg, std::uint32_t &base,
                       std::uint32_t &offset) {
    if ((w & 0xffc00000u) != (load ? 0x39400000u : 0x39000000u))
        return false;
    reg = Rd(w);
    base = Rn(w);
    offset = (w >> 10) & 0xfffu;
    return true;
}

inline bool IsCall(std::uint32_t w) { return (w >> 26) == 0x25u; }

inline std::uintptr_t AdrpTarget(std::uint32_t w, std::uintptr_t pc) {
    std::int64_t page = (static_cast<std::int64_t>((w >> 5) & 0x7ffffu) << 2) | ((w >> 29) & 3u);
    if (page & (std::int64_t{1} << 20))
        page -= std::int64_t{1} << 21;
    return (pc & ~static_cast<std::uintptr_t>(0xfffu)) + static_cast<std::uintptr_t>(page * 4096);
}

template <class Read>
bool Word(Read &read, std::uintptr_t at, std::uint32_t &w) {
    return read(at, &w, sizeof(w));
}

template <class Read>
bool PassesChat(Read &read, std::uintptr_t call) {
    for (int k = 1; k <= kAddWindow; ++k) {
        std::uint32_t add = 0;
        if (!Word(read, call - 4u * k, add))
            return false;
        if ((add & 0xffc003ffu) != 0x91000021u)
            continue;
        for (int j = 1; j <= kAdrpWindow; ++j) {
            const std::uintptr_t at = call - 4u * (k + j);
            std::uint32_t adrp = 0;
            if (!Word(read, at, adrp))
                return false;
            if ((adrp & 0x9f00001fu) != 0x90000001u)
                continue;
            char text[5] = {};
            const std::uintptr_t string = AdrpTarget(adrp, at) + ((add >> 10) & 0xfffu);
            return read(string, text, sizeof(text)) && std::memcmp(text, "chat", sizeof(text)) == 0;
        }
        return false;
    }
    return false;
}

template <class Read>
bool Candidate(Read &read, std::uintptr_t at, Site &out) {
    std::uint32_t w = 0;
    if (!Word(read, at, w) || (w & 0xfffffc00u) != kAndClearBit1 || Rd(w) != Rn(w))
        return false;
    const std::uint32_t reg = Rd(w);

    std::uintptr_t load = 0;
    std::uint32_t record = 0, offset = 0;
    for (int k = 1; k <= kLoadWindow && load == 0; ++k) {
        std::uint32_t x = 0, t = 0, n = 0, imm = 0;
        if (!Word(read, at - 4u * k, x))
            return false;
        if (ByteAccess(x, true, t, n, imm) && t == reg) {
            load = at - 4u * k;
            record = n;
            offset = imm;
        }
    }
    if (load == 0)
        return false;

    std::uintptr_t cursor = at, site = 0;
    for (int step = 0; step < 5; ++step) {
        bool matched = false;
        for (int k = 1; k <= kStepWindow && !matched; ++k) {
            std::uint32_t x = 0, t = 0, n = 0, imm = 0;
            if (!Word(read, cursor + 4u * k, x))
                return false;
            switch (step) {
            case 0:
                matched = OrrShifted(x, reg, 1);
                break;
            case 1:
            case 4:
                matched = ByteAccess(x, false, t, n, imm) && t == reg && n == record && imm == offset;
                break;
            case 2:
                matched = AndImmediate(x, kAndKeepBits5To7, reg);
                break;
            case 3:
                matched = OrrShifted(x, reg, 2);
                break;
            }
            if (matched) {
                cursor += 4u * k;
                if (step == 2)
                    site = cursor;
            }
        }
        if (!matched)
            return false;
    }

    for (int k = 1; k <= kCallWindow; ++k) {
        std::uint32_t x = 0;
        if (!Word(read, load - 4u * k, x))
            return false;
        if (!IsCall(x))
            continue;
        std::uint32_t original = 0;
        if (!PassesChat(read, load - 4u * k) || !Word(read, site, original))
            return false;
        out.address = site;
        out.original = original;
        out.replacement = kAndKeepBits0To1 | (reg << 5) | reg;
        out.flagOffset = offset;
        return true;
    }
    return false;
}

}

template <class Read>
int Find(Read &&read, std::uintptr_t begin, std::size_t size, Site &out) {
    int found = 0;
    for (std::size_t i = 0; i + 4 <= size; i += 4) {
        std::uint32_t w = 0;
        if (!Detail::Word(read, begin + i, w) || (w & 0xfffffc00u) != Detail::kAndClearBit1)
            continue;
        Site site;
        if (Detail::Candidate(read, begin + i, site) && ++found == 1)
            out = site;
    }
    return found;
}

}
