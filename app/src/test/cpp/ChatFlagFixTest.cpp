#include "ChatFlagFix/ChatFlagFix.h"
#include "ChatFlagFixFixtures.h"

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <string>
#include <vector>

namespace {

int g_failures = 0;

void Check(bool condition, const char *what, int line) {
    if (!condition) {
        std::printf("  FAIL line %d: %s\n", line, what);
        ++g_failures;
    }
}

#define CHECK(x) Check((x), #x, __LINE__)

struct Region {
    std::uintptr_t base;
    std::vector<unsigned char> bytes;
};

struct Memory {
    std::vector<Region> regions;

    bool operator()(std::uintptr_t at, void *out, std::size_t size) const {
        for (const Region &r : regions) {
            if (at >= r.base && size <= r.bytes.size() && at - r.base <= r.bytes.size() - size) {
                std::memcpy(out, r.bytes.data() + (at - r.base), size);
                return true;
            }
        }
        return false;
    }

    void Put(std::uintptr_t base, const void *data, std::size_t size) {
        Region r{base, {}};
        r.bytes.assign(static_cast<const unsigned char *>(data), static_cast<const unsigned char *>(data) + size);
        regions.push_back(std::move(r));
    }

    void SetWord(std::uintptr_t at, std::uint32_t w) {
        for (Region &r : regions) {
            if (at >= r.base && at + 4 <= r.base + r.bytes.size()) {
                std::memcpy(r.bytes.data() + (at - r.base), &w, 4);
                return;
            }
        }
        std::printf("  SetWord outside fixture at %#zx\n", static_cast<std::size_t>(at));
        std::abort();
    }
};

Memory FromWindow(const ChatFlagFixFixtures::Window &w, std::uintptr_t shift = 0) {
    Memory m;
    m.Put(w.codeBase + shift, w.words, w.count * 4);
    m.Put(w.stringAddress + shift, w.string, 5);
    return m;
}

int FindIn(const Memory &m, std::uintptr_t begin, std::size_t size, ChatFlagFix::Site &site) {
    return ChatFlagFix::Find(m, begin, size, site);
}

int FindInWindow(const Memory &m, const ChatFlagFixFixtures::Window &w, ChatFlagFix::Site &site) {
    return FindIn(m, w.codeBase, w.count * 4, site);
}

std::uint32_t Adrp(std::uint32_t rd, std::uintptr_t pc, std::uintptr_t target) {
    const std::int64_t pages = static_cast<std::int64_t>(target >> 12) - static_cast<std::int64_t>(pc >> 12);
    const std::uint32_t imm = static_cast<std::uint32_t>(pages) & 0x1fffffu;
    return 0x90000000u | ((imm & 3u) << 29) | ((imm >> 2) << 5) | rd;
}
std::uint32_t AddImm(std::uint32_t rd, std::uint32_t rn, std::uint32_t imm) {
    return 0x91000000u | (imm << 10) | (rn << 5) | rd;
}
std::uint32_t Bl(std::int32_t words) { return 0x94000000u | (static_cast<std::uint32_t>(words) & 0x3ffffffu); }
std::uint32_t Ldrb(std::uint32_t rt, std::uint32_t rn, std::uint32_t off) {
    return 0x39400000u | (off << 10) | (rn << 5) | rt;
}
std::uint32_t Strb(std::uint32_t rt, std::uint32_t rn, std::uint32_t off) {
    return 0x39000000u | (off << 10) | (rn << 5) | rt;
}
std::uint32_t AndFd(std::uint32_t r) { return 0x121e7800u | (r << 5) | r; }
std::uint32_t AndE3(std::uint32_t r) { return 0x121b7000u | (r << 5) | r; }
std::uint32_t OrrLsl(std::uint32_t rd, std::uint32_t rm, std::uint32_t shift) {
    return 0x2a000000u | (rm << 16) | (shift << 10) | (rd << 5) | rd;
}
constexpr std::uint32_t kNop = 0xd503201fu;

unsigned Builder(unsigned b, unsigned local, unsigned bit15, bool type5, int branch, bool check, unsigned flag,
                 unsigned secondMask) {
    unsigned v = (b & 0xfdu) | (local << 1);
    v = (v & secondMask) | (local << 2);
    v = (v & 0xe6u) | bit15;
    if (type5)
        v |= 4u;
    if (branch == 1) {
        v |= 2u;
        v = (v & 0xfbu) | (check ? 4u : 0u);
        v = (v & 0xf7u) | (flag << 3);
    } else if (branch == 2) {
        v |= 0x10u;
    }
    return v & 0xffu;
}

void FindsTheSiteInEveryAuditedBuild() {
    struct Expect {
        const ChatFlagFixFixtures::Window *window;
        std::uintptr_t site;
    };
    const Expect expects[] = {
        {&ChatFlagFixFixtures::k0_35_1, 0x1466e08u},
        {&ChatFlagFixFixtures::k0_34_5, 0x13f2460u},
        {&ChatFlagFixFixtures::k0_34_3, 0x13c1a04u},
    };
    for (const Expect &e : expects) {
        const Memory m = FromWindow(*e.window);
        ChatFlagFix::Site site;
        CHECK(FindInWindow(m, *e.window, site) == 1);
        CHECK(site.address == e.site);
        CHECK(site.original == 0x121b7108u);
        CHECK(site.replacement == 0x12000508u);
        CHECK(site.flagOffset == 0x14u);
    }
}

void RefusesTheOlderFlagLayout() {
    const Memory m = FromWindow(ChatFlagFixFixtures::k0_33_0);
    ChatFlagFix::Site site;
    CHECK(FindInWindow(m, ChatFlagFixFixtures::k0_33_0, site) == 0);
}

void RefusesWhenTheCallIsNotPassedChat() {
    const auto &w = ChatFlagFixFixtures::k0_35_1;
    Memory m;
    m.Put(w.codeBase, w.words, w.count * 4);
    m.Put(w.stringAddress, "chaX", 5);
    ChatFlagFix::Site site;
    CHECK(FindInWindow(m, w, site) == 0);
}

void RefusesWhenTheStringIsUnreadable() {
    const auto &w = ChatFlagFixFixtures::k0_35_1;
    Memory m;
    m.Put(w.codeBase, w.words, w.count * 4);
    ChatFlagFix::Site site;
    CHECK(FindInWindow(m, w, site) == 0);
}

void RefusesWhenAStoreTargetsAnotherByte() {
    const auto &w = ChatFlagFixFixtures::k0_35_1;
    Memory m = FromWindow(w);
    m.SetWord(0x1466e18u, Strb(8, 19, 0x15));
    ChatFlagFix::Site site;
    CHECK(FindInWindow(m, w, site) == 0);
}

void RefusesWhenTheSecondMaskDiffers() {
    const auto &w = ChatFlagFixFixtures::k0_35_1;
    Memory m = FromWindow(w);
    m.SetWord(0x1466e08u, 0x121c7508u);
    ChatFlagFix::Site site;
    CHECK(FindInWindow(m, w, site) == 0);
}

void CountsEverySiteSoTheCallerCanRefuseTwo() {
    const auto &w = ChatFlagFixFixtures::k0_35_1;
    const std::uintptr_t shift = 0x1000u;
    Memory m;
    std::vector<unsigned char> code(shift + w.count * 4, 0);
    std::memcpy(code.data(), w.words, w.count * 4);
    std::memcpy(code.data() + shift, w.words, w.count * 4);
    m.Put(w.codeBase, code.data(), code.size());
    m.Put(w.stringAddress, w.string, 5);
    m.Put(w.stringAddress + shift, w.string, 5);
    ChatFlagFix::Site site;
    CHECK(FindIn(m, w.codeBase, code.size(), site) == 2);
    CHECK(site.address == 0x1466e08u);
}

void DecodesRegistersAndOffsetInsteadOfPinningThem() {
    const std::uintptr_t base = 0x400000u, string = 0x123456u;
    const std::uint32_t a = 10, b = 21, c = 9, off = 0x18;
    std::vector<std::uint32_t> words = {
        Adrp(1, base + 0, string),
        AddImm(1, 1, static_cast<std::uint32_t>(string & 0xfffu)),
        kNop,
        Bl(0x100),
        Ldrb(a, b, off),
        kNop,
        AndFd(a),
        OrrLsl(a, c, 1),
        Strb(a, b, off),
        AndE3(a),
        kNop,
        OrrLsl(a, c, 2),
        Strb(a, b, off),
    };
    Memory m;
    m.Put(base, words.data(), words.size() * 4);
    m.Put(string, "chat", 5);
    ChatFlagFix::Site site;
    CHECK(FindIn(m, base, words.size() * 4, site) == 1);
    CHECK(site.address == base + 9 * 4);
    CHECK(site.original == AndE3(a));
    CHECK(site.replacement == (0x12000400u | (a << 5) | a));
    CHECK(site.flagOffset == off);
}

void DecidesWhetherToWrite() {
    ChatFlagFix::Site site;
    site.original = 0x121b7108u;
    site.replacement = 0x12000508u;
    CHECK(ChatFlagFix::Decide(0x121b7108u, site) == ChatFlagFix::Decision::Write);
    CHECK(ChatFlagFix::Decide(0x12000508u, site) == ChatFlagFix::Decision::AlreadyApplied);
    CHECK(ChatFlagFix::Decide(0xd503201fu, site) == ChatFlagFix::Decision::Foreign);
}

void PatchMatchesClearingBits5To7AroundTheBuilder() {
    int mismatches = 0, dirty = 0;
    for (unsigned b = 0; b < 256; ++b)
        for (unsigned local = 0; local < 2; ++local)
            for (unsigned bit15 = 0; bit15 < 2; ++bit15)
                for (int type5 = 0; type5 < 2; ++type5)
                    for (int branch = 0; branch < 3; ++branch)
                        for (int check = 0; check < 2; ++check)
                            for (unsigned flag = 0; flag < 2; ++flag) {
                                const unsigned shipped = Builder(b, local, bit15, type5, branch, check, flag, 0xe3u);
                                const unsigned hooked =
                                    Builder(b & 0x1fu, local, bit15, type5, branch, check, flag, 0xe3u) & 0x1fu;
                                const unsigned patched = Builder(b, local, bit15, type5, branch, check, flag, 0x03u);
                                dirty += (shipped & 0xe0u) != 0;
                                mismatches += patched != hooked || patched != (shipped & 0x1fu);
                            }
    CHECK(dirty > 0);
    CHECK(mismatches == 0);
}

bool LoadImage(const std::string &path, Memory &m, std::vector<std::pair<std::uintptr_t, std::size_t>> &code) {
    FILE *f = std::fopen(path.c_str(), "rb");
    if (!f)
        return false;
    std::fseek(f, 0, SEEK_END);
    const long length = std::ftell(f);
    std::fseek(f, 0, SEEK_SET);
    std::vector<unsigned char> data(static_cast<std::size_t>(length));
    const bool ok = std::fread(data.data(), 1, data.size(), f) == data.size();
    std::fclose(f);
    if (!ok || data.size() < 64)
        return false;
    std::uint64_t phoff = 0;
    std::uint16_t phentsize = 0, phnum = 0;
    std::memcpy(&phoff, &data[0x20], 8);
    std::memcpy(&phentsize, &data[0x36], 2);
    std::memcpy(&phnum, &data[0x38], 2);
    for (unsigned i = 0; i < phnum; ++i) {
        const unsigned char *p = &data[phoff + i * phentsize];
        std::uint32_t type = 0, flags = 0;
        std::uint64_t offset = 0, vaddr = 0, filesz = 0;
        std::memcpy(&type, p, 4);
        std::memcpy(&flags, p + 4, 4);
        std::memcpy(&offset, p + 8, 8);
        std::memcpy(&vaddr, p + 16, 8);
        std::memcpy(&filesz, p + 32, 8);
        if (type != 1)
            continue;
        m.Put(static_cast<std::uintptr_t>(vaddr), data.data() + offset, static_cast<std::size_t>(filesz));
        if (flags & 1u)
            code.emplace_back(static_cast<std::uintptr_t>(vaddr), static_cast<std::size_t>(filesz));
    }
    return true;
}

void FindsExactlyOneSiteInListedImages() {
    const char *list = std::getenv("CHAT_FLAG_FIX_IMAGES");
    if (!list || !*list) {
        std::printf("  (CHAT_FLAG_FIX_IMAGES not set, image check skipped)\n");
        return;
    }
    std::string paths(list);
    std::size_t start = 0;
    while (start <= paths.size()) {
        const std::size_t end = paths.find(';', start);
        const std::string path = paths.substr(start, end == std::string::npos ? std::string::npos : end - start);
        start = end == std::string::npos ? paths.size() + 1 : end + 1;
        if (path.empty())
            continue;
        Memory m;
        std::vector<std::pair<std::uintptr_t, std::size_t>> code;
        const bool loaded = LoadImage(path, m, code);
        CHECK(loaded);
        int found = 0;
        ChatFlagFix::Site first;
        for (const auto &range : code) {
            ChatFlagFix::Site site;
            const int n = FindIn(m, range.first, range.second, site);
            if (n > 0 && found == 0)
                first = site;
            found += n;
        }
        std::printf("  %s: %d site(s), first at %#zx\n", path.c_str(), found, static_cast<std::size_t>(first.address));
        CHECK(found == 1);
        CHECK(first.flagOffset == 0x14u);
    }
}

}

int main() {
    std::setvbuf(stdout, nullptr, _IONBF, 0);
    struct Case {
        const char *name;
        void (*run)();
    };
    const Case cases[] = {
        {"finds_the_site_in_every_audited_build", FindsTheSiteInEveryAuditedBuild},
        {"refuses_the_older_flag_layout", RefusesTheOlderFlagLayout},
        {"refuses_when_the_call_is_not_passed_chat", RefusesWhenTheCallIsNotPassedChat},
        {"refuses_when_the_string_is_unreadable", RefusesWhenTheStringIsUnreadable},
        {"refuses_when_a_store_targets_another_byte", RefusesWhenAStoreTargetsAnotherByte},
        {"refuses_when_the_second_mask_differs", RefusesWhenTheSecondMaskDiffers},
        {"counts_every_site_so_the_caller_can_refuse_two", CountsEverySiteSoTheCallerCanRefuseTwo},
        {"decodes_registers_and_offset_instead_of_pinning_them", DecodesRegistersAndOffsetInsteadOfPinningThem},
        {"decides_whether_to_write", DecidesWhetherToWrite},
        {"patch_matches_clearing_bits_5_to_7_around_the_builder", PatchMatchesClearingBits5To7AroundTheBuilder},
        {"finds_exactly_one_site_in_listed_images", FindsExactlyOneSiteInListedImages},
    };
    for (const Case &c : cases) {
        const int before = g_failures;
        c.run();
        std::printf("%s %s\n", g_failures == before ? "ok  " : "FAIL", c.name);
    }
    std::printf("%d failure(s)\n", g_failures);
    return g_failures == 0 ? 0 : 1;
}
