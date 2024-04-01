#include "CipherHook.hpp"

#include <Canvas/Canvas.hpp>
#include <Cipher/Cipher.hpp>
#include <KittyMemory/MemoryBackup.hpp>

#include "../include/misc/Logger.h"
#include "../Utils/And64InlineHook/And64InlineHook.hpp"

CipherHook::CipherHook()
        : p_Hook(0),
        p_Callback(0) {
    this->m_type = Types::e_hook;
    this->set_libName(Canvas::libName);
}

CipherHook::~CipherHook() {
    this->m_Restore();
    return;
}

//sets detour function
CipherHook* CipherHook::set_Hook(std::uintptr_t _hook) {
    this->p_Hook = _hook;
    return this;
}

//sets callback to original function
CipherHook* CipherHook::set_Callback(std::uintptr_t _callback) {
    this->p_Callback = _callback;
    return this;
}

CipherHook* CipherHook::Fire() {
    const bool invalidHook = this->get_address() == 0 || this->p_Hook == 0;
    const bool invalidCallback = !this->get_Lock() && this->p_Callback == 0;
    if (invalidHook || invalidCallback) {
        return this; //check if fields are set
    }

    if (!this->get_Lock()) {
        for (auto& instance : CipherBase::s_InstanceVec) {
            CipherHook* pInstance = (CipherHook *)instance;
            if (pInstance->get_Lock()) {
                return this;
            } else if (pInstance->get_address() == this->get_address()
                && pInstance->m_type == Types::e_hook) {
                this->set_Address(pInstance->p_Hook, false); //hooks the hooked function instead
            }
        }
    }
    MemoryBackup *backup = new MemoryBackup;
    *backup = MemoryBackup::createBackup(this->get_address(), 8);
    this->p_Backup = (uintptr_t) (backup); //backs up original bytes
    LOGD(
        "address: %p detour: %p callback: %p",
        this->get_address(),
        this->p_Hook,
        this->p_Callback
    );

    A64HookFunction( //hooks
        (void *)this->get_address(),
        (void *)this->p_Hook,
        (void **)this->p_Callback
    );

    CipherBase::s_InstanceVec.push_back((CipherBase *)this);
    return this;
}

void CipherHook::m_Restore() {
    ((MemoryBackup *)this->p_Backup)->Restore();
    delete ((MemoryBackup *)this->p_Backup);
    CipherBase::s_InstanceVec.erase(
        std::find(
            CipherBase::s_InstanceVec.begin(),
            CipherBase::s_InstanceVec.end(),
            (CipherBase *)this
        )
    );

    for (auto& instance : CipherBase::s_InstanceVec) {
        CipherHook* pInstance = (CipherHook *)instance;
        if (pInstance->get_address() == this->p_Hook && pInstance->m_type == Types::e_hook) {
            pInstance->Restore();
            return;
        }

        pInstance->set_Address(this->get_address(), false);
        ((MemoryBackup *) pInstance->p_Backup)->Restore();
        CipherBase::s_InstanceVec.erase(
            std::find(
                CipherBase::s_InstanceVec.begin(),
                CipherBase::s_InstanceVec.end(),
                (CipherBase *)pInstance
            )
        );
        pInstance->Fire();
    }
}

void CipherHook::Restore() {
    this->m_Restore();
}

