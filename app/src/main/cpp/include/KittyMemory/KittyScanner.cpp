#include "KittyScanner.h"

#include "KittyMemory.h"

using KittyMemory::ProcMap;

// refs 
// https://github.com/learn-more/findpattern-bench

namespace KittyScanner
{

    uintptr_t find(const uintptr_t pstart, const size_t size, const char *pattern, const char *mask)
    {
        size_t howMuch = strlen(mask);
        char* start = (char*)pstart;
        if(size < howMuch) return 0;
        for(size_t i = 0; i < size; i++) {
            for(size_t j = 0; j < howMuch; j++) {
                if(i+j >= size) return 0;
                if(start[i+j] != pattern[j] && mask[j] == 'x') break;
                if(j == howMuch - 1) return (uintptr_t)&start[i];
            }
        }
        return 0;
    }

    uintptr_t find_from_lib(const char *name, const char *pattern, const char *mask)
    {
        if (!name || !pattern || !mask)
            return 0;

        ProcMap libMap = KittyMemory::getLibraryMap(name);
        if(!libMap.isValid()) return 0;

        return find((uintptr_t)libMap.startAddr, (size_t)libMap.length, pattern, mask);
    }

}