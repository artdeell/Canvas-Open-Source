#pragma once


#include "CipherBase.h"

class CipherHook : public CipherBase {
private:
    std::uintptr_t p_Callback;
    std::uintptr_t p_Hook;
    void m_Restore();

public:
    CipherHook();
    ~CipherHook() override;
    CipherHook* set_Hook(std::uintptr_t _hook);
    CipherHook* set_Callback(std::uintptr_t _callback);
    CipherHook* Fire() override;
    void Restore() override;
};
