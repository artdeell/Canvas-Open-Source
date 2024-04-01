#pragma once

#include <cstdint>
#include <vector>
#include "Cipher.h"

class CipherBase {
private:
    bool m_isLocked = false;
    const char *m_libName;
    std::uintptr_t p_Address;

protected:
    Types m_type;
    const char* get_libName();
    bool get_Lock();
    std::uintptr_t get_address();
    static std::vector<CipherBase *> s_InstanceVec;
    std::uintptr_t p_Backup;

public:
    CipherBase();
    virtual ~CipherBase() = 0;

    CipherBase* set_libName(const char* _libName);
    CipherBase* set_Address(const char* _symbol);
    CipherBase* set_Address(std::uintptr_t _address, bool _isLocal = true);
    CipherBase* set_Address(const char* _bytes, const char* _mask);

    CipherBase* set_Lock(bool _isLocked);
    virtual CipherBase* Fire() = 0;
    virtual void Restore() = 0;
};
