#pragma once

#include <cstdint>

#include "OtherAudio.h"

namespace OtherAudio {

struct Located {
    std::uintptr_t base = 0;
    Chain chain;
    std::uint32_t systemIoOffset = 0;
    std::uintptr_t systemIoGlobal = 0;
};

bool Locate(const char *libName);
const Located *Result();

void SetPlaying(bool playing);
bool IsPlaying();

}
