#pragma once

#include <cstddef>
#include <cstdint>

namespace OtherAudio {

struct Chain {
    std::uintptr_t site = 0;
    std::uint32_t slot = 0;
    std::uintptr_t gateReturn = 0;
    std::uint32_t flagOffset = 0;
    std::uintptr_t accessor = 0;
    std::uintptr_t audioGlobal = 0;
    std::uint32_t fadeSlot = 0;
    std::uintptr_t fadeReturn = 0;
};

struct FmodSelect {
    std::uintptr_t site = 0;
    std::uint32_t slot = 0;
    std::uint32_t systemIoOffset = 0;
};

struct Setter {
    std::uintptr_t site = 0;
    std::uint32_t systemIoOffset = 0;
    std::uintptr_t systemIoGlobal = 0;
};

namespace Detail {

inline std::uint32_t Rd(std::uint32_t w) { return w & 31u; }
inline std::uint32_t Rn(std::uint32_t w) { return (w >> 5) & 31u; }
inline std::uint32_t Rm(std::uint32_t w) { return (w >> 16) & 31u; }
inline std::uint32_t Imm12(std::uint32_t w) { return (w >> 10) & 0xfffu; }

inline bool Strb(std::uint32_t w) { return (w >> 22) == 0xe4u; }
inline bool Ldrb(std::uint32_t w) { return (w >> 22) == 0xe5u; }
inline bool LdrX(std::uint32_t w) { return (w >> 22) == 0x3e5u; }
inline bool Blr(std::uint32_t w) { return (w & 0xfffffc1fu) == 0xd63f0000u; }
inline bool Bl(std::uint32_t w) { return (w >> 26) == 0x25u; }
inline bool Adrp(std::uint32_t w) { return (w & 0x9f000000u) == 0x90000000u; }
inline bool AddImm(std::uint32_t w) { return (w & 0xffc00000u) == 0x91000000u; }
inline bool AddImmHi(std::uint32_t w) { return (w & 0xffc00000u) == 0x91400000u; }
inline bool MovzW(std::uint32_t w, std::uint32_t imm) { return (w & 0xffffffe0u) == (0x52800000u | (imm << 5)); }
inline bool DefaultBlock(std::uint32_t w) { return MovzW(w, 0x80) || MovzW(w, 0x100); }

inline std::uintptr_t BlTarget(std::uint32_t w, std::uintptr_t pc) {
    std::int64_t imm = w & 0x3ffffffu;
    if (imm & (std::int64_t{1} << 25))
        imm -= std::int64_t{1} << 26;
    return pc + static_cast<std::uintptr_t>(imm * 4);
}

inline std::uintptr_t AdrpPage(std::uint32_t w, std::uintptr_t pc) {
    std::int64_t page = (static_cast<std::int64_t>((w >> 5) & 0x7ffffu) << 2) | ((w >> 29) & 3u);
    if (page & (std::int64_t{1} << 20))
        page -= std::int64_t{1} << 21;
    return (pc & ~static_cast<std::uintptr_t>(0xfffu)) + static_cast<std::uintptr_t>(page * 4096);
}

template <class Read>
bool Words(Read &read, std::uintptr_t at, std::uint32_t *out, int n) {
    return read(at, out, sizeof(std::uint32_t) * static_cast<std::size_t>(n));
}

template <class Read>
std::uintptr_t AccessorGlobal(Read &read, std::uintptr_t accessor) {
    std::uint32_t w[10];
    if (!Words(read, accessor, w, 10))
        return 0;
    for (int i = 0; i < 10; ++i) {
        if (!Adrp(w[i]))
            continue;
        for (int j = i + 1; j < 10; ++j) {
            if (LdrX(w[j]) && Rd(w[j]) == 0 && Rn(w[j]) == Rd(w[i]))
                return AdrpPage(w[i], accessor + 4u * i) + Imm12(w[j]) * 8u;
        }
        return 0;
    }
    return 0;
}

template <class Read>
bool ChainAt(Read &read, std::uintptr_t at, Chain &out) {
    std::uint32_t w[16];
    if (!Words(read, at, w, 16) || !Strb(w[0]))
        return false;
    const std::uint32_t record = Rn(w[0]), store = Imm12(w[0]);
    if (!LdrX(w[1]) || Imm12(w[1]) != 0)
        return false;
    const std::uint32_t vtReg = Rd(w[1]);
    if (!LdrX(w[2]) || Rn(w[2]) != vtReg || !Blr(w[3]) || Rn(w[3]) != Rd(w[2]))
        return false;
    if (!Ldrb(w[4]) || Rn(w[4]) != record || Imm12(w[4]) != store)
        return false;
    const std::uint32_t u = Rd(w[4]);
    if (w[5] != (0x7100001fu | (u << 5)) || w[6] != (0x1a9f17e0u | u))
        return false;
    if ((w[7] & 0xffe0fc00u) != 0x0a000000u || Rd(w[7]) != u || Rn(w[7]) != 0 || Rm(w[7]) != u)
        return false;
    if (!Strb(w[8]) || Rn(w[8]) != record || Imm12(w[8]) != store + 1 || Rd(w[8]) != u)
        return false;
    int bl = -1;
    for (int i = 9; i < 12 && bl < 0; ++i)
        if (Bl(w[i]))
            bl = i;
    if (bl < 0)
        return false;
    Chain c;
    c.site = at;
    c.slot = Imm12(w[2]) * 8u;
    c.gateReturn = at + 16u;
    c.flagOffset = store + 1;
    c.accessor = BlTarget(w[bl], at + 4u * bl);
    c.audioGlobal = AccessorGlobal(read, c.accessor);
    if (c.audioGlobal == 0)
        return false;
    std::uint32_t audioVt = 32, fadeSlotReg = 32;
    bool flagArg = false, indexArg = false;
    for (int i = bl + 1; i < 16; ++i) {
        const std::uint32_t x = w[i];
        if (LdrX(x) && Rn(x) == 0 && Imm12(x) == 0)
            audioVt = Rd(x);
        else if (Ldrb(x) && Rd(x) == 2 && Rn(x) == record && Imm12(x) == c.flagOffset)
            flagArg = true;
        else if (MovzW(x, 1) && Rd(x) == 1)
            indexArg = true;
        else if (LdrX(x) && Rn(x) == audioVt)
            c.fadeSlot = Imm12(x) * 8u, fadeSlotReg = Rd(x);
        else if (Blr(x) && Rn(x) == fadeSlotReg) {
            if (!flagArg || !indexArg)
                return false;
            c.fadeReturn = at + 4u * (i + 1);
            out = c;
            return true;
        }
    }
    return false;
}

template <class Read>
bool FmodAt(Read &read, std::uintptr_t at, FmodSelect &out) {
    std::uint32_t w[3];
    if (!Words(read, at, w, 3) || !DefaultBlock(w[0]))
        return false;
    if (!MovzW(w[1] & ~31u, 0x200) && !MovzW(w[2] & ~31u, 0x200))
        return false;
    std::uint32_t p[40];
    const std::uintptr_t from = at - 4u * 40;
    if (!Words(read, from, p, 40))
        return false;
    const std::uint32_t blr = p[39], slot = p[38], vt = p[37], sio = p[36];
    if (!Blr(blr) || !LdrX(slot) || Rn(blr) != Rd(slot) || !LdrX(vt) || Imm12(vt) != 0 || Rn(slot) != Rd(vt))
        return false;
    if (!LdrX(sio) || Rd(sio) != 0 || Rn(vt) != 0)
        return false;
    const std::uint32_t base = Rn(sio);
    for (int j = 35; j >= 0; --j) {
        if (!AddImm(p[j]) || Rd(p[j]) != base)
            continue;
        const std::uint32_t src = Rn(p[j]);
        for (int k = j - 1; k >= 0; --k) {
            if (AddImmHi(p[k]) && Rd(p[k]) == src) {
                if (Rn(p[k]) != 19)
                    return false;
                out.site = at;
                out.slot = Imm12(slot) * 8u;
                out.systemIoOffset = (Imm12(p[k]) << 12) + Imm12(p[j]) + Imm12(sio) * 8u;
                return true;
            }
        }
        return false;
    }
    return false;
}

inline bool MovReg(std::uint32_t w, std::uint32_t &dst, std::uint32_t &src) {
    if ((w & 0xffe0ffe0u) != 0xaa0003e0u)
        return false;
    dst = Rd(w);
    src = Rm(w);
    return true;
}

template <class Read>
bool SetterAt(Read &read, std::uintptr_t at, Setter &out) {
    std::uint32_t w[8];
    if (!Words(read, at, w, 8))
        return false;
    std::uint32_t self = 32, value = 32, d = 0, s = 0;
    for (int i = 0; i < 3; ++i) {
        if (!MovReg(w[i], d, s))
            return false;
        if (s == 0)
            self = d;
        else if (s == 1 && d != 0)
            value = d;
    }
    if (self == 32 || value == 32 || !Bl(w[3]))
        return false;
    const std::uintptr_t callee = BlTarget(w[3], at + 12u);
    std::uint32_t c[6];
    if (!Words(read, callee, c, 6))
        return false;
    std::uintptr_t global = 0;
    for (int i = 0; i < 5 && global == 0; ++i) {
        if (Adrp(c[i]) && (c[i + 1] >> 22) == 0x3e4u && Rd(c[i + 1]) == 0 && Rn(c[i + 1]) == Rd(c[i]))
            global = AdrpPage(c[i], callee + 4u * i) + Imm12(c[i + 1]) * 8u;
    }
    if (global == 0)
        return false;
    const std::uint32_t lo = w[4], hi = w[5], st = w[6];
    if ((lo & 0xffe00000u) != 0x52800000u || (hi & 0xffe00000u) != 0x72a00000u || Rd(hi) != Rd(lo))
        return false;
    if ((st & 0xffe0fc00u) != 0xf8206800u || Rm(st) != Rd(lo) || Rn(st) != self || Rd(st) != value)
        return false;
    out.site = at;
    out.systemIoOffset = ((hi >> 5) & 0xffffu) << 16 | ((lo >> 5) & 0xffffu);
    out.systemIoGlobal = global;
    return true;
}

}

template <class Read>
int FindSetter(Read &&read, std::uintptr_t begin, std::size_t size, Setter &out) {
    int found = 0;
    for (std::size_t i = 0; i + 32 <= size; i += 4) {
        std::uint32_t w = 0;
        if (!read(begin + i, &w, sizeof(w)) || (w & 0xffe0ffe0u) != 0xaa0003e0u)
            continue;
        Setter s;
        if (Detail::SetterAt(read, begin + i, s) && ++found == 1)
            out = s;
    }
    return found;
}

template <class Read>
int FindChain(Read &&read, std::uintptr_t begin, std::size_t size, Chain &out) {
    int found = 0;
    for (std::size_t i = 0; i + 64 <= size; i += 4) {
        std::uint32_t w = 0;
        if (!read(begin + i, &w, sizeof(w)) || !Detail::Strb(w))
            continue;
        Chain c;
        if (Detail::ChainAt(read, begin + i, c) && ++found == 1)
            out = c;
    }
    return found;
}

template <class Read>
int FindFmod(Read &&read, std::uintptr_t begin, std::size_t size, FmodSelect &out) {
    int found = 0;
    for (std::size_t i = 160; i + 12 <= size; i += 4) {
        std::uint32_t w = 0;
        if (!read(begin + i, &w, sizeof(w)) || !Detail::DefaultBlock(w))
            continue;
        FmodSelect f;
        if (Detail::FmodAt(read, begin + i, f) && ++found == 1)
            out = f;
    }
    return found;
}

}
