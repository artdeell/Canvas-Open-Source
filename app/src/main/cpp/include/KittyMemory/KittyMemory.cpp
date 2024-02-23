//
//  KittyMemory.cpp
//
//  Created by MJ (Ruit) on 1/1/19.
//

#include "KittyMemory.h"

#include <map>
#include <dlfcn.h>

namespace KittyMemory {

    bool ProtectAddr(void *addr, size_t length, int protection)
    {
        uintptr_t pageStart = _PAGE_START_OF_(addr);
        uintptr_t pageLen = _PAGE_LEN_OF_(addr, length);
        return (
                mprotect(reinterpret_cast<void *>(pageStart), pageLen, protection) != -1);
    }

    Memory_Status memWrite(void *addr, const void *buffer, size_t len)
    {
        if (addr == NULL)
            return INV_ADDR;

        if (buffer == NULL)
            return INV_BUF;

        if (len < 1 || len > INT_MAX)
            return INV_LEN;

        if (!ProtectAddr(addr, len, _PROT_RWX_))
            return INV_PROT;

        if (memcpy(addr, buffer, len) != NULL && ProtectAddr(addr, len, _PROT_RX_))
            return SUCCESS;

        return FAILED;
    }

    Memory_Status memRead(void *buffer, const void *addr, size_t len)
    {
        if (addr == NULL)
            return INV_ADDR;

        if (buffer == NULL)
            return INV_BUF;

        if (len < 1 || len > INT_MAX)
            return INV_LEN;

        if (memcpy(buffer, addr, len) != NULL)
            return SUCCESS;

        return FAILED;
    }

    int setAddressProtection(const void *address, size_t length, int protection)
    {
        uintptr_t pageStart = KT_PAGE_START(address);
        uintptr_t pageLen = KT_PAGE_LEN2(address, length);
        int ret = mprotect(reinterpret_cast<void *>(pageStart), pageLen, protection);
        KITTY_LOGD("mprotect(%p, %zu, %d) = %d", address, length, protection, ret);
        return ret;
    }

    bool memRead(const std::vector<ProcMap>& getAllMaps, const void* address, void* buffer, size_t len)
    {
        KITTY_LOGD("memRead(%p, %p, %zu)", address, buffer, len);

        if (!address) {
            KITTY_LOGE("memRead err address (%p) is null", address);
            return false;
        }

        if (!buffer) {
            KITTY_LOGE("memRead err buffer (%p) is null", buffer);
            return false;
        }

        if (!len) {
            KITTY_LOGE("memRead err invalid len");
            return false;
        }

        ProcMap addressMap = getAddressMap(getAllMaps, address);
        if (!addressMap.isValid()) {
            KITTY_LOGE("memRead err couldn't find address (%p) in any map", address);
            return false;
        }

        if (addressMap.protection & PROT_READ) {
            memcpy(buffer, address, len);
            return true;
        }

        if (setAddressProtection(address, len, addressMap.protection | PROT_READ) != 0) {
            KITTY_LOGE("memRead err couldn't add write perm to address (%p, len: %zu, prot: %d)",
                       address, len, addressMap.protection);
            return false;
        }

        memcpy(buffer, address, len);

        if (setAddressProtection(address, len, addressMap.protection) != 0) {
            KITTY_LOGE("memRead err couldn't revert protection of address (%p, len: %zu, prot: %d)",
                       address, len, addressMap.protection);
            return false;
        }

        return true;
    }

    bool memWrite(const std::vector<ProcMap>& getAllMaps, void *address, const void *buffer, size_t len)
    {
        KITTY_LOGD("memWrite(%p, %p, %zu)", address, buffer, len);

        if (!address) {
            KITTY_LOGE("memWrite err address (%p) is null", address);
            return false;
        }

        if (!buffer) {
            KITTY_LOGE("memWrite err buffer (%p) is null", buffer);
            return false;
        }

        if (!len) {
            KITTY_LOGE("memWrite err invalid len");
            return false;
        }

        ProcMap addressMap = getAddressMap(getAllMaps, address);
        if (!addressMap.isValid()) {
            KITTY_LOGE("memWrite err couldn't find address (%p) in any map", address);
            return false;
        }

        if (addressMap.protection & PROT_WRITE) {
            memcpy(address, buffer, len);
            return true;
        }

        if (setAddressProtection(address, len, addressMap.protection | PROT_WRITE) != 0) {
            KITTY_LOGE("memWrite err couldn't add write perm to address (%p, len: %zu, prot: %d)",
                       address, len, addressMap.protection);
            return false;
        }

        memcpy(address, buffer, len);

        if (setAddressProtection(address, len, addressMap.protection) != 0) {
            KITTY_LOGE("memWrite err couldn't revert protection of address (%p, len: %zu, prot: %d)",
                       address, len, addressMap.protection);
            return false;
        }

        return true;
    }

    std::string read2HexStr(const void *addr, size_t len)
    {
        char temp[len];
        memset(temp, 0, len);

        const size_t bufferLen = len * 2 + 1;
        char buffer[bufferLen];
        memset(buffer, 0, bufferLen);

        std::string ret;

        if (memRead(temp, addr, len) != SUCCESS)
            return ret;

        for (int i = 0; i < len; i++)
        {
            sprintf(&buffer[i * 2], "%02X", (unsigned char)temp[i]);
        }

        ret += buffer;
        return ret;
    }

    std::string ReadHexStr(const std::vector<ProcMap>& getAllMaps, const void *address, size_t len)
    {
        std::string temp(len, ' ');
        if (!memRead(getAllMaps, (void*)address, &temp[0], len)) return "";

        std::string ret(len * 2, ' ');
        for (int i = 0; i < len; i++) {
            sprintf(&ret[i * 2], "%02X", (unsigned char) temp[i]);
        }
        return ret;
    }

    std::string getProcessName() {
        const char *file = "/proc/self/cmdline";
        char cmdline[128] = {0};
        FILE *fp = fopen(file, "r");
        if (!fp) {
            KITTY_LOGE("Couldn't open file %s.", file);
            return "";
        }
        fgets(cmdline, sizeof(cmdline), fp);
        fclose(fp);
        return cmdline;
    }

    std::vector<ProcMap> getBootloaderMaps() {
        std::vector<ProcMap> retMaps;
        const char *file = "/proc/self/maps";
        char line[512] = {0};

        FILE *fp = fopen(file, "r");
        if (!fp) {
            KITTY_LOGE("Couldn't open file %s.", file);
            return retMaps;
        }

        while (fgets(line, sizeof(line), fp)) {
            ProcMap map;
            if (strstr(line, "libBootloader.so")) {
                char perms[5] = {0}, dev[11] = {0}, pathname[256] = {0};
                // parse a line in maps file
                // (format) startAddress-endAddress perms offset dev inode pathname
                sscanf(line, "%llx-%llx %s %llx %s %lu %s",
                       &map.startAddress, &map.endAddress,
                       perms, &map.offset, dev, &map.inode, pathname);

                map.length = map.endAddress - map.startAddress;
                map.dev = dev;
                map.pathname = pathname;

                if (perms[0] == 'r') {
                    map.protection |= PROT_READ;
                    map.readable = true;
                }
                if (perms[1] == 'w') {
                    map.protection |= PROT_WRITE;
                    map.writeable = true;
                }
                if (perms[2] == 'x') {
                    map.protection |= PROT_EXEC;
                    map.executable = true;
                }

                map.is_private = (perms[3] == 'p');
                map.is_shared = (perms[3] == 's');

                map.is_rx = (strncmp(perms, "r-x", 3) == 0);
                map.is_rw = (strncmp(perms, "rw-", 3) == 0);
                map.is_ro = (strncmp(perms, "r--", 3) == 0);

                retMaps.push_back(map);
            }
        }

        fclose(fp);

        if (retMaps.empty()) {
            KITTY_LOGE("getAllMaps err couldn't find any map");
        }
        return retMaps;
    }

    std::vector<ProcMap> getAllMaps() {
        std::vector<ProcMap> retMaps;
        const char *file = "/proc/self/maps";
        char line[512] = {0};

        FILE *fp = fopen(file, "r");
        if (!fp) {
            KITTY_LOGE("Couldn't open file %s.", file);
            return retMaps;
        }

        while (fgets(line, sizeof(line), fp)) {
            ProcMap map;

            char perms[5] = {0}, dev[11] = {0}, pathname[256] = {0};
            // parse a line in maps file
            // (format) startAddress-endAddress perms offset dev inode pathname
            sscanf(line, "%llx-%llx %s %llx %s %lu %s",
                   &map.startAddress, &map.endAddress,
                   perms, &map.offset, dev, &map.inode, pathname);

            map.length = map.endAddress - map.startAddress;
            map.dev = dev;
            map.pathname = pathname;

            if (perms[0] == 'r') {
                map.protection |= PROT_READ;
                map.readable = true;
            }
            if (perms[1] == 'w') {
                map.protection |= PROT_WRITE;
                map.writeable = true;
            }
            if (perms[2] == 'x') {
                map.protection |= PROT_EXEC;
                map.executable = true;
            }

            map.is_private = (perms[3] == 'p');
            map.is_shared = (perms[3] == 's');

            map.is_rx = (strncmp(perms, "r-x", 3) == 0);
            map.is_rw = (strncmp(perms, "rw-", 3) == 0);
            map.is_ro = (strncmp(perms, "r--", 3) == 0);

            retMaps.push_back(map);
        }

        fclose(fp);

        if (retMaps.empty()) {
            KITTY_LOGE("getAllMaps err couldn't find any map");
        }
        return retMaps;
    }

    std::vector<ProcMap> getMapsEqual(const std::vector<ProcMap> &maps, const std::string &name) {
        if (name.empty()) return {};

        KITTY_LOGD("getMapsEqual(%s)", name.c_str());

        std::vector<ProcMap> retMaps;

        for (auto &it: maps) {
            if (it.isValid() && !it.isUnknown() && it.pathname == name) {
                retMaps.push_back(it);
            }
        }

        return retMaps;
    }

    std::vector<ProcMap> getMapsContain(const std::vector<ProcMap> &maps, const std::string &name) {
        if (name.empty()) return {};

        KITTY_LOGD("getMapsContain(%s)", name.c_str());

        std::vector<ProcMap> retMaps;

        for (auto &it: maps) {
            if (it.isValid() && !it.isUnknown() && strstr(it.pathname.c_str(), name.c_str())) {
                retMaps.push_back(it);
            }
        }

        return retMaps;
    }

    std::vector<ProcMap> getMapsEndWith(const std::vector<ProcMap> &maps, const std::string &name) {
        if (name.empty()) return {};

        KITTY_LOGD("getMapsEndWith(%s)", name.c_str());

        std::vector<ProcMap> retMaps;

        for (auto &it: maps) {
            if (it.isValid() && !it.isUnknown() &&
                KittyUtils::String::EndsWith(it.pathname, name)) {
                retMaps.push_back(it);
            }
        }

        return retMaps;
    }

    ProcMap getAddressMap(const std::vector<ProcMap> &maps, const void *address) {
        KITTY_LOGD("getAddressMap(%p)", address);

        if (!address) return {};

        ProcMap retMap{};

        for (auto &it: maps) {
            if (it.isValid() && it.contains((uintptr_t) address)) {
                retMap = it;
                break;
            }
        }

        return retMap;
    }

    ProcMap getElfBaseMap(const std::string &name) {
        ProcMap retMap{};

        if (name.empty())
            return retMap;

        bool isZippedInAPK = false;
        auto maps = getMapsEndWith(name);
        if (maps.empty()) {
            // some apps use dlopen on zipped libraries like xxx.apk!/lib/xxx/libxxx.so
            // so we'll search in app's base.apk maps too
            maps = getMapsEndWith(".apk");
            if (maps.empty()) {
                return retMap;
            }
            isZippedInAPK = true;
        }

        for (auto &it: maps) {
            if (!it.readable || it.offset != 0 || it.isUnknown() || it.inode == 0 ||
                !it.is_private || !it.isValidELF())
                continue;

            // skip dladdr check for linker/linker64
            if (strstr(it.pathname.c_str(), "/bin/linker")) {
                retMap = it;
                break;
            }

            Dl_info info{};
            int rt = dladdr((void *) it.startAddress, &info);
            // check dli_fname and dli_fbase if NULL
            if (rt == 0 || !info.dli_fname || !info.dli_fbase ||
                it.startAddress != (uintptr_t) info.dli_fbase)
                continue;

            if (!isZippedInAPK) {
                retMap = it;
                break;
            }

            // if library is zipped inside base.apk, compare dli_fname and fix pathname
            if (KittyUtils::String::EndsWith(info.dli_fname, name)) {
                retMap = it;
                retMap.pathname = info.dli_fname;
                break;
            }
        }

        return retMap;
    }

    struct mapsCache
    {
        std::string identifier;
        ProcMap map;
    };

    static std::vector<mapsCache> __mapsCache;
    static ProcMap findMapInCache(std::string id)
    {
        ProcMap ret;
        for (int i = 0; i < __mapsCache.size(); i++)
        {
            if (__mapsCache[i].identifier.compare(id) == 0)
            {
                ret = __mapsCache[i].map;
                break;
            }
        }
        return ret;
    }

    ProcMap getLibraryMap(const char *libraryName)
    {
        ProcMap retMap;
        char line[512] = {0};

        FILE *fp = fopen("/proc/self/maps", "rt");
        if (fp != NULL)
        {
            while (fgets(line, sizeof(line), fp))
            {
                if (strstr(line, libraryName))
                {
                    char tmpPerms[5] = {0}, tmpDev[12] = {0}, tmpPathname[444] = {0};
                    // parse a line in maps file
                    // (format) startAddress-endAddress perms offset dev inode pathname
                    sscanf(line, "%llx-%llx %s %ld %s %d %s",
                           (long long unsigned *)&retMap.startAddress,
                           (long long unsigned *)&retMap.endAddress,
                           tmpPerms, &retMap.offset, tmpDev, &retMap.inode, tmpPathname);

                    retMap.length = retMap.endAddress - retMap.startAddress;
                    retMap.perms = tmpPerms;
                    retMap.dev = tmpDev;
                    retMap.pathname = tmpPathname;

                    break;
                }
            }
            fclose(fp);
        }
        return retMap;
    }

    uintptr_t getAbsoluteAddress(const char *libraryName, uintptr_t relativeAddr, bool useCache)
    {
        ProcMap libMap;

        if (useCache)
        {
            libMap = findMapInCache(libraryName);
            if (libMap.isValid())
                return (reinterpret_cast<uintptr_t>((uintptr_t)libMap.startAddress) + relativeAddr);
        }

        libMap = getLibraryMap(libraryName);
        if (!libMap.isValid())
            return 0;

        if (useCache)
        {
            mapsCache cachedMap;
            cachedMap.identifier = libraryName;
            cachedMap.map = libMap;
            __mapsCache.push_back(cachedMap);
        }

        return (reinterpret_cast<uintptr_t>((uintptr_t)libMap.startAddress) + relativeAddr);
    }
}