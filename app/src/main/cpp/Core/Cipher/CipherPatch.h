#pragma once

#include <string>

#include "../../Utils/artpatch/artpatch.h"
#include "CipherBase.h"

class CipherPatch : public CipherBase {
    bool m_fired = false;
    patch_t patch;

public:
    CipherPatch();
    ~CipherPatch() override;
    CipherPatch* Fire() override;
    CipherPatch* set_Opcode(std::string _hex);
    void Restore() override;
};

