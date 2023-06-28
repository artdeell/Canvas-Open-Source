//
// Created by Lukas on 22/07/2022.
//

#ifndef USERLIB_CIPHER_H
#define USERLIB_CIPHER_H
#include <vector>
#include <utility>
#include <string>
#include "../artpatch/artpatch.h"


uintptr_t* get_adr_val(const char *pattern, const char *mask, uintptr_t *address, uint32_t relOffset = 4, uint32_t patternOffset = 0);
typedef void (*func)();

class Cipher {
public:
    static uint32_t getGameVersion();
    static bool isGameBeta();
    static uintptr_t get_libBase();
    static const char *get_libName();
    static uintptr_t CipherScan(const char *pattern, const char *mask); //librange
    static uintptr_t CipherScan(uintptr_t start, const size_t size, const char *pattern, const char *mask);
    static const char* getConfigPath();
};


enum FLAGS{
    f_global, f_local
};

enum TYPES{
    e_patch,
    e_hook
};


class CipherBase {
private:
    bool m_isLocked = false;
    const char *m_libName;
    uintptr_t p_Address;


protected:
    uint8_t m_type;
    const char *get_libName();
    bool get_Lock();
    uintptr_t get_address();
    static std::vector<CipherBase *> m_InstanceVec;
    uintptr_t p_Backup;

public:
    CipherBase();
    virtual ~CipherBase() = 0;
    CipherBase *set_libName(const char *libName);
    CipherBase *set_Address(const char *Symbol);
    CipherBase *set_Address(uintptr_t address, bool isLocal = true);
    CipherBase *set_Address(const char *pattern, const char *mask);
    CipherBase *set_Lock(bool isLocked);
    virtual CipherBase *Fire() = 0;
    virtual void Restore() = 0;
};

class CipherHook : public CipherBase{
    uintptr_t p_Callback;
    uintptr_t p_Hook;

public:
    CipherHook();
    ~CipherHook() override;
    CipherHook *set_Hook(uintptr_t Hook);
    CipherHook *set_Callback(uintptr_t Callback);
    CipherHook *set_AdressType(bool type);
    CipherHook *Fire() override;
    void Restore() override;
};

class CipherPatch : public CipherBase{
    bool m_fired = false;
    patch_t patch;

public:
    CipherPatch();
    ~CipherPatch() override;
    CipherPatch *Fire() override;
    CipherPatch *set_Opcode(std::string hex);
    void Restore() override;
};











#endif //USERLIB_CIPHER_H