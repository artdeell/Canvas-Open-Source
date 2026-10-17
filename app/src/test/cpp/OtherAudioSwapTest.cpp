#include "OtherAudio/OtherAudio.h"
#include "OtherAudio/OtherAudioSwap.h"
#include "OtherAudioFixtures.h"
#include "OtherAudioSwapFixtures.h"

#include <cstddef>
#include <cstdio>
#include <cstdlib>
#include <cstring>
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

using OtherAudio::Decision;

constexpr std::uintptr_t kAudio = 0x7a00000000u;
constexpr std::uintptr_t kSystemIo = 0x7b00000000u;
constexpr std::uintptr_t kReplacement = 0x7c00001000u;

struct Region {
    std::uintptr_t base;
    std::vector<unsigned char> bytes;
};

struct Memory {
    std::vector<Region> regions;
    mutable int reads = 0;

    bool operator()(std::uintptr_t at, void *out, std::size_t size) const {
        ++reads;
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

    void PutPointer(std::uintptr_t at, std::uintptr_t value) { Put(at, &value, sizeof(value)); }

    void Set(std::uintptr_t at, const void *data, std::size_t size) {
        for (Region &r : regions) {
            if (at >= r.base && at + size <= r.base + r.bytes.size()) {
                std::memcpy(r.bytes.data() + (at - r.base), data, size);
                return;
            }
        }
        std::printf("  Set outside fixture at %#zx\n", static_cast<std::size_t>(at));
        std::abort();
    }

    void SetPointer(std::uintptr_t at, std::uintptr_t value) { Set(at, &value, sizeof(value)); }

    void Drop(std::uintptr_t at) {
        for (std::size_t i = 0; i < regions.size(); ++i) {
            if (at >= regions[i].base && at < regions[i].base + regions[i].bytes.size()) {
                regions.erase(regions.begin() + static_cast<std::ptrdiff_t>(i));
                return;
            }
        }
        std::printf("  Drop outside fixture at %#zx\n", static_cast<std::size_t>(at));
        std::abort();
    }
};

struct World {
    Memory memory;
    OtherAudio::Located located;
    OtherAudio::Image image;
    std::uintptr_t vtable = 0;
    std::uintptr_t stub = 0;
    std::uintptr_t fmodReturn = 0;
    bool decoded = false;
};

const SwapFixtures::Image &SwapImage(const char *tag) {
    for (const auto &img : SwapFixtures::All())
        if (std::strcmp(img.tag, tag) == 0)
            return img;
    std::printf("  no swap fixture %s\n", tag);
    std::abort();
}

World Build(const char *tag) {
    World w;
    Memory code;
    for (const auto &img : Fixtures::All())
        if (std::strcmp(img.tag, tag) == 0)
            for (const auto &win : img.windows)
                code.Put(win.va, win.bytes.data(), win.bytes.size());

    int chains = 0, fmods = 0, setters = 0;
    for (const Region &r : code.regions) {
        OtherAudio::Chain c;
        OtherAudio::FmodSelect f;
        OtherAudio::Setter s;
        const int nc = OtherAudio::FindChain(code, r.base, r.bytes.size(), c);
        const int nf = OtherAudio::FindFmod(code, r.base, r.bytes.size(), f);
        const int ns = OtherAudio::FindSetter(code, r.base, r.bytes.size(), s);
        if (nc && !chains)
            w.located.chain = c;
        if (nf && !fmods) {
            w.located.systemIoOffset = f.systemIoOffset;
            w.fmodReturn = f.site;
        }
        if (ns && !setters)
            w.located.systemIoGlobal = s.systemIoGlobal;
        chains += nc;
        fmods += nf;
        setters += ns;
    }
    w.decoded = chains == 1 && fmods == 1 && setters == 1;

    const SwapFixtures::Image &img = SwapImage(tag);
    for (const auto &win : img.windows)
        w.memory.Put(win.va, win.bytes.data(), win.bytes.size());
    w.image.codeBegin = img.codeBegin;
    w.image.codeEnd = img.codeEnd;
    w.image.relroBegin = img.relroBegin;
    w.image.relroEnd = img.relroEnd;
    w.vtable = img.vtable;
    w.stub = img.stub;

    w.memory.PutPointer(w.located.chain.audioGlobal, kAudio);
    w.memory.PutPointer(kAudio + w.located.systemIoOffset, kSystemIo);
    w.memory.PutPointer(w.located.systemIoGlobal, kSystemIo);
    w.memory.PutPointer(kSystemIo, w.vtable);
    return w;
}

OtherAudio::Plan Decide(const World &w, bool enabled = true) {
    return OtherAudio::Decide(w.memory, &w.located, enabled, w.image, kReplacement);
}

void SwapsOnEveryAuditedBuild() {
    for (const char *tag : {"0.35.1", "0.35.0", "0.34.5"}) {
        const World w = Build(tag);
        CHECK(w.decoded);
        const OtherAudio::Plan plan = Decide(w);
        CHECK(plan.decision == Decision::Swap);
        CHECK(plan.systemIo == kSystemIo);
        CHECK(plan.otherSystemIo == kSystemIo);
        CHECK(plan.vtable == w.vtable);
        CHECK(plan.slotAddress == w.vtable + 0xa8u);
        CHECK(plan.at == plan.slotAddress);
        CHECK(plan.current == w.stub);
    }
}

void UsesTheAudioRouteAloneWhenNoGlobalWasDecoded() {
    World w = Build("0.35.1");
    w.located.systemIoGlobal = 0;
    CHECK(Decide(w).decision == Decision::Swap);
}

void ReportsAlreadyAppliedWhenTheSlotHoldsTheReplacement() {
    World w = Build("0.35.1");
    w.memory.SetPointer(w.vtable + 0xa8u, kReplacement);
    const OtherAudio::Plan plan = Decide(w);
    CHECK(plan.decision == Decision::AlreadyApplied);
    CHECK(plan.at == w.vtable + 0xa8u);
}

void ReadsNothingWhenTheSettingIsOff() {
    const World w = Build("0.35.1");
    w.memory.reads = 0;
    CHECK(Decide(w, false).decision == Decision::Disabled);
    CHECK(w.memory.reads == 0);
}

void ReadsNothingWhenNotLocated() {
    const World w = Build("0.35.1");
    w.memory.reads = 0;
    CHECK(OtherAudio::Decide(w.memory, nullptr, true, w.image, kReplacement).decision == Decision::NotLocated);
    CHECK(w.memory.reads == 0);
}

void WaitsWhileTheGameObjectsAreNull() {
    {
        World w = Build("0.35.1");
        w.memory.SetPointer(w.located.chain.audioGlobal, 0);
        const OtherAudio::Plan plan = Decide(w);
        CHECK(plan.decision == Decision::NotReady);
        CHECK(plan.at == w.located.chain.audioGlobal);
    }
    {
        World w = Build("0.35.1");
        w.memory.SetPointer(kAudio + w.located.systemIoOffset, 0);
        const OtherAudio::Plan plan = Decide(w);
        CHECK(plan.decision == Decision::NotReady);
        CHECK(plan.at == kAudio + w.located.systemIoOffset);
    }
    {
        World w = Build("0.35.1");
        w.memory.SetPointer(w.located.systemIoGlobal, 0);
        const OtherAudio::Plan plan = Decide(w);
        CHECK(plan.decision == Decision::NotReady);
        CHECK(plan.at == w.located.systemIoGlobal);
    }
}

void RefusesWhenTheSystemIoRoutesDisagree() {
    World w = Build("0.35.1");
    w.memory.SetPointer(w.located.systemIoGlobal, kSystemIo + 0x100u);
    const OtherAudio::Plan plan = Decide(w);
    CHECK(plan.decision == Decision::SystemIoMismatch);
    CHECK(plan.systemIo == kSystemIo);
    CHECK(plan.otherSystemIo == kSystemIo + 0x100u);
}

void RefusesUnreadableMemory() {
    {
        World w = Build("0.35.1");
        w.memory.Drop(w.located.chain.audioGlobal);
        const OtherAudio::Plan plan = Decide(w);
        CHECK(plan.decision == Decision::Unreadable);
        CHECK(plan.at == w.located.chain.audioGlobal);
    }
    {
        World w = Build("0.35.1");
        w.memory.Drop(kSystemIo);
        const OtherAudio::Plan plan = Decide(w);
        CHECK(plan.decision == Decision::Unreadable);
        CHECK(plan.at == kSystemIo);
    }
    {
        World w = Build("0.35.1");
        w.memory.Drop(w.stub);
        const OtherAudio::Plan plan = Decide(w);
        CHECK(plan.decision == Decision::Unreadable);
        CHECK(plan.at == w.stub);
    }
}

void RefusesASystemIoWithoutAVtableHeader() {
    {
        World w = Build("0.35.1");
        w.memory.SetPointer(w.vtable - 8u, 0x2c00000u);
        CHECK(Decide(w).decision == Decision::NotAVtable);
    }
    {
        World w = Build("0.35.1");
        w.memory.SetPointer(w.vtable - 16u, 8u);
        CHECK(Decide(w).decision == Decision::NotAVtable);
    }
    {
        World w = Build("0.35.1");
        w.memory.SetPointer(kSystemIo, w.vtable + 4u);
        CHECK(Decide(w).decision == Decision::NotAVtable);
    }
}

void RefusesASlotOutsideRelro() {
    {
        World w = Build("0.35.1");
        w.image.relroEnd = w.vtable + 0xa8u + 4u;
        const OtherAudio::Plan plan = Decide(w);
        CHECK(plan.decision == Decision::SlotOutsideRelro);
        CHECK(plan.at == w.vtable + 0xa8u);
    }
    {
        World w = Build("0.35.1");
        w.image.relroBegin = w.vtable + 0xb0u;
        CHECK(Decide(w).decision == Decision::SlotOutsideRelro);
    }
}

void RefusesASlotPointingOutsideTheGamesCode() {
    {
        World w = Build("0.35.1");
        w.memory.SetPointer(w.vtable + 0xa8u, w.image.codeEnd - 8u);
        const OtherAudio::Plan plan = Decide(w);
        CHECK(plan.decision == Decision::TargetOutsideCode);
        CHECK(plan.at == w.image.codeEnd - 8u);
    }
    {
        World w = Build("0.35.1");
        w.memory.SetPointer(w.vtable + 0xa8u, kReplacement + 0x40u);
        CHECK(Decide(w).decision == Decision::TargetOutsideCode);
    }
}

void RefusesAnythingButTheExactStub() {
    for (int word = 0; word < 5; ++word) {
        World w = Build("0.35.1");
        std::uint32_t changed = OtherAudio::kReturnFalseStub[word] ^ 0x20u;
        w.memory.Set(w.stub + 4u * static_cast<unsigned>(word), &changed, sizeof(changed));
        const OtherAudio::Plan plan = Decide(w);
        CHECK(plan.decision == Decision::NotTheStub);
        CHECK(plan.at == w.stub);
    }
    World w = Build("0.35.1");
    const std::uint32_t returnTrue = 0x52800020u;
    w.memory.Set(w.stub + 8u, &returnTrue, sizeof(returnTrue));
    CHECK(Decide(w).decision == Decision::NotTheStub);
}

void AnswersOnlyTheGateCaller() {
    const World w = Build("0.35.1");
    const std::uintptr_t gate = w.located.chain.gateReturn;
    CHECK(w.fmodReturn != 0);
    CHECK(OtherAudio::Answer(gate, gate, true, true));
    CHECK(!OtherAudio::Answer(gate, gate, true, false));
    CHECK(!OtherAudio::Answer(gate, gate, false, true));
    CHECK(!OtherAudio::Answer(w.fmodReturn, gate, true, true));
    CHECK(!OtherAudio::Answer(w.located.chain.fadeReturn, gate, true, true));
    CHECK(!OtherAudio::Answer(gate + 4u, gate, true, true));
    CHECK(!OtherAudio::Answer(0, 0, true, true));
}

void LayoutNeedsOneCodeSegmentAndOneRelro() {
    const std::uintptr_t base = 0x7000000000u;
    std::vector<OtherAudio::Segment> segments = {
        {OtherAudio::kPtLoad, 4, 0x0u, 0xcdb538u},
        {OtherAudio::kPtLoad, 5, 0xce0000u, 0x1f6db70u},
        {OtherAudio::kPtLoad, 6, 0x2c51b70u, 0x1a5490u},
        {OtherAudio::kPtLoad, 6, 0x2dfa520u, 0x9ceb30u},
        {OtherAudio::kPtGnuRelro, 4, 0x2c51b70u, 0x1a5490u},
    };
    OtherAudio::Image image;
    CHECK(OtherAudio::Layout(base, segments.data(), static_cast<int>(segments.size()), image));
    const SwapFixtures::Image &real = SwapImage("0.35.1");
    CHECK(image.codeBegin == base + real.codeBegin);
    CHECK(image.codeEnd == base + real.codeEnd);
    CHECK(image.relroBegin == base + real.relroBegin);
    CHECK(image.relroEnd == base + real.relroEnd);

    std::vector<OtherAudio::Segment> twoCode = segments;
    twoCode.push_back({OtherAudio::kPtLoad, 5, 0x4000000u, 0x1000u});
    OtherAudio::Image untouched;
    CHECK(!OtherAudio::Layout(base, twoCode.data(), static_cast<int>(twoCode.size()), untouched));
    CHECK(untouched.codeBegin == 0);

    std::vector<OtherAudio::Segment> noRelro(segments.begin(), segments.end() - 1);
    CHECK(!OtherAudio::Layout(base, noRelro.data(), static_cast<int>(noRelro.size()), untouched));

    std::vector<OtherAudio::Segment> emptyRelro = segments;
    emptyRelro.back().memsz = 0;
    CHECK(!OtherAudio::Layout(base, emptyRelro.data(), static_cast<int>(emptyRelro.size()), untouched));
}

}

int main() {
    std::setvbuf(stdout, nullptr, _IONBF, 0);
    struct Case {
        const char *name;
        void (*run)();
    };
    const Case cases[] = {
        {"swaps_on_every_audited_build", SwapsOnEveryAuditedBuild},
        {"uses_the_audio_route_alone_when_no_global_was_decoded", UsesTheAudioRouteAloneWhenNoGlobalWasDecoded},
        {"reports_already_applied_when_the_slot_holds_the_replacement",
         ReportsAlreadyAppliedWhenTheSlotHoldsTheReplacement},
        {"reads_nothing_when_the_setting_is_off", ReadsNothingWhenTheSettingIsOff},
        {"reads_nothing_when_not_located", ReadsNothingWhenNotLocated},
        {"waits_while_the_game_objects_are_null", WaitsWhileTheGameObjectsAreNull},
        {"refuses_when_the_systemio_routes_disagree", RefusesWhenTheSystemIoRoutesDisagree},
        {"refuses_unreadable_memory", RefusesUnreadableMemory},
        {"refuses_a_systemio_without_a_vtable_header", RefusesASystemIoWithoutAVtableHeader},
        {"refuses_a_slot_outside_relro", RefusesASlotOutsideRelro},
        {"refuses_a_slot_pointing_outside_the_games_code", RefusesASlotPointingOutsideTheGamesCode},
        {"refuses_anything_but_the_exact_stub", RefusesAnythingButTheExactStub},
        {"answers_only_the_gate_caller", AnswersOnlyTheGateCaller},
        {"layout_needs_one_code_segment_and_one_relro", LayoutNeedsOneCodeSegmentAndOneRelro},
    };
    for (const Case &c : cases) {
        const int before = g_failures;
        c.run();
        std::printf("%s %s\n", g_failures == before ? "ok  " : "FAIL", c.name);
    }
    std::printf("%d failure(s)\n", g_failures);
    return g_failures == 0 ? 0 : 1;
}
