#include "CipherBase.h"

#include <dlfcn.h>

#include "Cipher.h"
#include <Canvas/Canvas.h>

#include "../../include/misc/visibility.h"

PRIVATE_API std::vector<CipherBase *> CipherBase::s_InstanceVec;

const char* CipherBase::get_libName() {
    return this->m_libName;
}

bool CipherBase::get_Lock() {
    return this->m_isLocked;
}

std::uintptr_t CipherBase::get_address() {
    return this->p_Address;
}

CipherBase::CipherBase()
    : p_Address(0),
    p_Backup(0),
    m_libName(Canvas::libName),
    m_isLocked(false) {

}

CipherBase::~CipherBase() {

}

CipherBase *CipherBase::set_libName(const char* _libName) {
    this->m_libName = _libName;
    return this;
}

CipherBase* CipherBase::set_Address(const char* _symbol) { //sets address via symbol
    if (this->m_libName == Canvas::libName || Canvas::isLibLoaded(this->m_libName)) {
        this->p_Address = (std::uintptr_t)dlsym(dlopen(this->m_libName, RTLD_LOCAL), _symbol);
    }
    return this;
}

CipherBase* CipherBase::set_Address(std::uintptr_t _address, bool _isLocal) {
    if (!_isLocal) {
        p_Address = _address;
        return this;
    }else if (Canvas::isLibLoaded(this->m_libName)) {
        std::uintptr_t libBase = std::strcmp(this->m_libName, Canvas::libName) == 0 ? Canvas::libBase : Canvas::findLib(this->m_libName);
        this->p_Address = libBase + _address;
    }
    return this;
}

CipherBase *CipherBase::set_Address(const char* _bytes, const char* _mask) {
    this->p_Address = Cipher::CipherScan(_bytes, _mask);
    return this;
}

CipherBase *CipherBase::set_Lock(bool _isLocked) { //sets if multiple functions can hook
    this->m_isLocked = _isLocked;
    return this;
}



