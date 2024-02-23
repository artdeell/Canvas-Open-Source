//
//  KittyMemory.hpp
//
//  Created by MJ (Ruit) on 1/1/19.
//

#pragma once

#include <stdio.h>
#include <string>
#include <unistd.h>
#include <sys/mman.h>
#include <vector>



#include "KittyUtils.h"

#define _SYS_PAGE_SIZE_ (sysconf(_SC_PAGE_SIZE))

#define _PAGE_START_OF_(x) ((uintptr_t)x & ~(uintptr_t)(_SYS_PAGE_SIZE_ - 1))
#define _PAGE_END_OF_(x, len) (_PAGE_START_OF_((uintptr_t)x + len - 1))
#define _PAGE_LEN_OF_(x, len) (_PAGE_END_OF_(x, len) - _PAGE_START_OF_(x) + _SYS_PAGE_SIZE_)
#define _PAGE_OFFSET_OF_(x) ((uintptr_t)x - _PAGE_START_OF_(x))

#define KT_PAGE_SIZE (sysconf(_SC_PAGE_SIZE))

#define KT_PAGE_START(x) (uintptr_t(x) & ~(KT_PAGE_SIZE - 1))
#define KT_PAGE_END(x) (KT_PAGE_START(uintptr_t(x) + KT_PAGE_SIZE - 1))
#define KT_PAGE_OFFSET(x) (uintptr_t(x) - KT_PAGE_START(x))
#define KT_PAGE_LEN(x) (size_t(KT_PAGE_SIZE - KT_PAGE_OFFSET(x)))

#define KT_PAGE_END2(x, len) (KT_PAGE_START(uintptr_t(x) + len - 1))
#define KT_PAGE_LEN2(x, len) (KT_PAGE_END2(x, len) - KT_PAGE_START(x) + KT_PAGE_SIZE)

#define _PROT_RWX_ (PROT_READ | PROT_WRITE | PROT_EXEC)
#define _PROT_RX_ (PROT_READ | PROT_EXEC)
#define _PROT_RW_ (PROT_READ | PROT_WRITE)

#define KITTY_LOG_TAG "KittyMemory"


#include <android/log.h>

#ifdef kITTYMEMORY_DEBUG
#define KITTY_LOGD(fmt, ...) ((void)__android_log_print(ANDROID_LOG_DEBUG, KITTY_LOG_TAG, fmt, ##__VA_ARGS__))
#else
#define KITTY_LOGD(fmt, ...) do {} while(0)
#endif

#define KITTY_LOGI(fmt, ...) ((void)__android_log_print(ANDROID_LOG_INFO, KITTY_LOG_TAG, fmt,  ##__VA_ARGS__))
#define KITTY_LOGE(fmt, ...) ((void)__android_log_print(ANDROID_LOG_ERROR, KITTY_LOG_TAG, fmt, ##__VA_ARGS__))



namespace KittyMemory
{
    class ProcMap {
    public:
        unsigned long long startAddress;
        unsigned long long endAddress;
        size_t length;
        int protection;
        bool readable, writeable, executable, is_private, is_shared, is_ro, is_rw, is_rx;
        unsigned long long offset;
        std::string dev;
        unsigned long inode;
        std::string pathname;std::string perms;

        ProcMap() : startAddress(0), endAddress(0), length(0), protection(0),
		            readable(false), writeable(false), executable(false),
                    is_private(false), is_shared(false),
                    is_ro(false), is_rw(false), is_rx(false),
                    offset(0), inode(0) {}

        inline bool isValid() const { return (startAddress && endAddress && length); }
        inline bool isUnknown() const { return pathname.empty(); }
        inline bool isValidELF() const { return isValid() && length > 4 && readable && memcmp((const void *) startAddress, "\177ELF", 4) == 0; }
        inline bool contains(uintptr_t address) const { return address >= startAddress && address < endAddress; }
        inline std::string toString()
        {
          return KittyUtils::String::Fmt("%llx-%llx %c%c%c%c %llx %s %lu %s",
              startAddress, endAddress,
              readable ? 'r' : '-', writeable ? 'w' : '-', executable ? 'x' : '-', is_private ? 'p' : 's',
              offset, dev.c_str(), inode, pathname.c_str());
        }
    };

    typedef enum
    {
        FAILED = 0,
        SUCCESS = 1,
        INV_ADDR = 2,
        INV_LEN = 3,
        INV_BUF = 4,
        INV_PROT = 5
    } Memory_Status;
    /*
    * mprotect wrapper
    */
    int setAddressProtection(const void *address, size_t length, int protection);

    /*
     * Reads an address content into a buffer
     */
    bool memRead(const std::vector<ProcMap>& getAllMaps, const void *address, void *buffer, size_t len);

    /*
     * Writes buffer content to an address
     */
    bool memWrite(const std::vector<ProcMap>& getAllMaps, void *address, const void *buffer, size_t len);

    /*
     * Writes buffer content to an address
     */
    Memory_Status memWrite(void *addr, const void *buffer, size_t len);

    /*
     * Reads an address content into a buffer
     */
    Memory_Status memRead(void *buffer, const void *addr, size_t len);
    /*
     * /proc/self/cmdline
     */
    std::string getProcessName();

    /*
     * Gets info of all maps in current process
     */
    std::vector<ProcMap> getAllMaps();

    /*
     * Gets info of all maps which pathname equals @name in current process
     */
    std::vector<ProcMap> getMapsEqual(const std::vector<ProcMap> &maps, const std::string& name);

    /*
     * Gets info of all maps which pathname contains @name in current process
     */
    std::vector<ProcMap> getMapsContain(const std::vector<ProcMap> &maps, const std::string& name);

    /*
     * Gets info of all maps which pathname ends with @name in current process
     */
    std::vector<ProcMap> getMapsEndWith(const std::vector<ProcMap> &maps, const std::string& name);

    /*
     * Gets map info of an address in self process
     */
    ProcMap getAddressMap(const std::vector<ProcMap> &maps, const void *address);

    /*
     * Gets info of all maps which pathname equals @name in current process
     */
    inline std::vector<ProcMap> getMapsEqual(const std::string& name) { return getMapsEqual(getAllMaps(), name); }

    /*
     * Gets info of all maps which pathname contains @name in current process
     */
    inline std::vector<ProcMap> getMapsContain(const std::string& name) { return getMapsContain(getAllMaps(), name); }

    /*
     * Gets info of all maps which pathname ends with @name in current process
     */
    inline std::vector<ProcMap> getMapsEndWith(const std::string& name) { return getMapsEndWith(getAllMaps(), name); }

    /*
     * Gets map info of an address in self process
     */
    inline ProcMap getAddressMap(const void *address) { return getAddressMap(getAllMaps(), address); }

    /*
     * Gets the base map of a loaded shared object
     */
    ProcMap getElfBaseMap(const std::string& name);

    std::string ReadHexStr(const std::vector<ProcMap>& getAllMaps, const void *address, size_t len);

    ProcMap getLibraryMap(const char *libraryName);
    std::string read2HexStr(const void *addr, size_t len);
    std::vector<ProcMap> getBootloaderMaps();

    uintptr_t getAbsoluteAddress(const char *libraryName, uintptr_t relativeAddr, bool useMapCache);
}
