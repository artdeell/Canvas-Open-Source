#include "OtherAudio/OtherAudio.h"
#include "OtherAudioFixtures.h"

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <map>
#include <string>
#include <vector>

namespace {

int g_failures = 0;
int g_checks = 0;

void Check(bool ok, const std::string &what) {
    ++g_checks;
    if (!ok) {
        ++g_failures;
        std::printf("FAIL %s\n", what.c_str());
    }
}

struct Memory {
    std::vector<std::pair<std::uintptr_t, std::vector<unsigned char>>> windows;
    std::map<std::uintptr_t, std::uint32_t> patches;

    bool read(std::uintptr_t at, void *out, std::size_t n) const {
        for (const auto &w : windows) {
            if (at >= w.first && n <= w.second.size() && at - w.first <= w.second.size() - n) {
                std::memcpy(out, w.second.data() + (at - w.first), n);
                for (const auto &p : patches)
                    if (p.first >= at && p.first + 4 <= at + n)
                        std::memcpy(static_cast<unsigned char *>(out) + (p.first - at), &p.second, 4);
                return true;
            }
        }
        return false;
    }
};

struct Scan {
    int chains = 0, fmods = 0, setters = 0;
    OtherAudio::Chain chain;
    OtherAudio::FmodSelect fmod;
    OtherAudio::Setter setter;
};

Scan Run(const Memory &mem) {
    Scan s;
    auto read = [&mem](std::uintptr_t at, void *out, std::size_t n) { return mem.read(at, out, n); };
    for (const auto &w : mem.windows) {
        OtherAudio::Chain c;
        OtherAudio::FmodSelect f;
        OtherAudio::Setter t;
        const int nc = OtherAudio::FindChain(read, w.first, w.second.size(), c);
        const int nf = OtherAudio::FindFmod(read, w.first, w.second.size(), f);
        const int ns = OtherAudio::FindSetter(read, w.first, w.second.size(), t);
        if (nc && !s.chains)
            s.chain = c;
        if (nf && !s.fmods)
            s.fmod = f;
        if (ns && !s.setters)
            s.setter = t;
        s.chains += nc;
        s.fmods += nf;
        s.setters += ns;
    }
    return s;
}

Memory FromFixture(const char *tag) {
    Memory m;
    for (const auto &img : Fixtures::All())
        if (std::strcmp(img.tag, tag) == 0)
            for (const auto &w : img.windows)
                m.windows.push_back({w.va, w.bytes});
    return m;
}

struct Expected {
    const char *tag;
    std::uintptr_t site, gate, accessor, global, fadeReturn;
    std::uint32_t flag, systemIo;
    std::uintptr_t systemIoGlobal;
};

const Expected kExpected[] = {
    {"0.35.1", 0x1a2d5e8, 0x1a2d5f8, 0x2a1e81c, 0x3631190, 0x1a2d624, 0xa71, 0x21a868, 0x309ef78},
    {"0.35.0", 0x1a2d5dc, 0x1a2d5ec, 0x2a1e81c, 0x3631180, 0x1a2d618, 0xa71, 0x21a868, 0x309ef68},
    {"0.34.5", 0x197be90, 0x197bea0, 0x2930490, 0x351d7f0, 0x197becc, 0xa71, 0x21a838, 0x2f9a0a8},
    {"0.33.8", 0x183a7a4, 0x183a7b4, 0, 0x32e44c0, 0x183a7e0, 0x821, 0, 0},
};

void ChainDecodesOnEveryFixture() {
    for (const Expected &e : kExpected) {
        const Scan s = Run(FromFixture(e.tag));
        const std::string t = e.tag;
        Check(s.chains == 1, t + ": exactly one update site");
        Check(s.chain.site == e.site, t + ": update site");
        Check(s.chain.slot == 0xa8, t + ": SystemIO slot");
        Check(s.chain.gateReturn == e.gate, t + ": gate return");
        Check(s.chain.flagOffset == e.flag, t + ": flag offset");
        Check(s.chain.audioGlobal == e.global, t + ": audio global");
        Check(!e.accessor || s.chain.accessor == e.accessor, t + ": accessor");
        Check(s.chain.fadeSlot == 0xd8, t + ": fade slot");
        Check(s.chain.fadeReturn == e.fadeReturn, t + ": fade return");
    }
}

void SystemIoRoutesAgree() {
    for (const Expected &e : kExpected) {
        const Scan s = Run(FromFixture(e.tag));
        const std::string t = e.tag;
        if (e.systemIo == 0) {
            Check(s.fmods == 0 && s.setters == 0, t + ": no SystemIO route on the older shape");
            continue;
        }
        Check(s.fmods == 1 && s.setters == 1, t + ": both SystemIO routes found");
        Check(s.fmod.slot == s.chain.slot, t + ": fmod slot equals update slot");
        Check(s.fmod.systemIoOffset == e.systemIo, t + ": fmod SystemIO offset");
        Check(s.setter.systemIoOffset == e.systemIo, t + ": setter SystemIO offset");
        Check(s.setter.systemIoGlobal == e.systemIoGlobal, t + ": SystemIO global");
    }
}

void SingleWordMutantsFailClosed() {
    const Expected &e = kExpected[0];
    const struct {
        const char *what;
        std::uintptr_t at;
        std::uint32_t word;
        int field;
    } mutants[] = {
        {"cset ne instead of eq", e.site + 24, 0x1a9f07e8u, 0},
        {"and with w1 instead of w0", e.site + 28, 0x0a080028u, 0},
        {"flag stored one byte off", e.site + 32, 0x39000000u | (0xa72u << 10) | (19u << 5) | 8u, 0},
        {"slot load without blr", e.site + 12, 0xd503201fu, 0},
        {"fade bus index 2", e.site + 48, 0x52800041u, 0},
        {"fmod 0x300 instead of 0x200", 0xdc3b7cu, 0x52806009u, 1},
        {"setter callee without the global store", 0xdd758cu, 0xd503201fu, 2},
    };
    for (const auto &m : mutants) {
        Memory mem = FromFixture(e.tag);
        mem.patches[m.at] = m.word;
        const Scan s = Run(mem);
        const int count = m.field == 0 ? s.chains : m.field == 1 ? s.fmods : s.setters;
        Check(count == 0, std::string("mutant rejected: ") + m.what);
    }
}

bool LoadElf(const char *path, Memory &mem) {
    FILE *f = std::fopen(path, "rb");
    if (!f)
        return false;
    std::fseek(f, 0, SEEK_END);
    const long size = std::ftell(f);
    std::fseek(f, 0, SEEK_SET);
    std::vector<unsigned char> data(static_cast<std::size_t>(size));
    const std::size_t got = std::fread(data.data(), 1, data.size(), f);
    std::fclose(f);
    if (got != data.size() || size < 64)
        return false;
    std::uint64_t phoff = 0;
    std::uint16_t phentsize = 0, phnum = 0;
    std::memcpy(&phoff, &data[0x20], 8);
    std::memcpy(&phentsize, &data[0x36], 2);
    std::memcpy(&phnum, &data[0x38], 2);
    for (int i = 0; i < phnum; ++i) {
        const unsigned char *ph = &data[phoff + static_cast<std::size_t>(i) * phentsize];
        std::uint32_t type = 0, flags = 0;
        std::uint64_t off = 0, vaddr = 0, filesz = 0;
        std::memcpy(&type, ph, 4);
        std::memcpy(&flags, ph + 4, 4);
        std::memcpy(&off, ph + 8, 8);
        std::memcpy(&vaddr, ph + 16, 8);
        std::memcpy(&filesz, ph + 32, 8);
        if (type == 1 && (flags & 1))
            mem.windows.push_back({static_cast<std::uintptr_t>(vaddr),
                                   std::vector<unsigned char>(data.begin() + static_cast<long>(off),
                                                              data.begin() + static_cast<long>(off + filesz))});
    }
    return !mem.windows.empty();
}

void FullImagesFromEnvironment() {
    const char *list = std::getenv("OTHER_AUDIO_IMAGES");
    if (!list || !*list)
        return;
    std::string all = list;
    std::size_t start = 0;
    while (start <= all.size()) {
        const std::size_t end = all.find(';', start);
        const std::string path = all.substr(start, end == std::string::npos ? std::string::npos : end - start);
        start = end == std::string::npos ? all.size() + 1 : end + 1;
        if (path.empty())
            continue;
        Memory mem;
        if (!LoadElf(path.c_str(), mem)) {
            Check(false, "cannot load " + path);
            continue;
        }
        const Scan s = Run(mem);
        Check(s.chains == 1, path + ": exactly one update site");
        if (s.fmods == 1 && s.setters == 1)
            Check(s.fmod.systemIoOffset == s.setter.systemIoOffset, path + ": SystemIO routes agree");
        std::printf("%s: chains %d fmods %d setters %d\n", path.c_str(), s.chains, s.fmods, s.setters);
    }
}

}

int main() {
    std::setvbuf(stdout, nullptr, _IONBF, 0);
    ChainDecodesOnEveryFixture();
    SystemIoRoutesAgree();
    SingleWordMutantsFailClosed();
    FullImagesFromEnvironment();
    std::printf("%d checks, %d failed\n", g_checks, g_failures);
    return g_failures ? 1 : 0;
}
