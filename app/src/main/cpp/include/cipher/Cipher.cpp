//
// Created by Lukas on 24/07/2022.
//

#include "Cipher.h"
#include "dlfcn.h"
#include "../And64InlineHook/And64InlineHook.hpp"
#include "../artpatch/artpatch.h"
#include "../KittyMemory/MemoryBackup.h"
#include "../misc/Logger.h"
#include "../misc/visibility.h"
#include "../canvas/Canvas.h"
#include "../KittyMemory/KittyScanner.h"
#include "CipherArm64.h"

PRIVATE_API std::vector<CipherBase *> CipherBase::m_InstanceVec;

uintptr_t Cipher::get_libBase() {
    return Canvas::get_libBase();
}

uint32_t Cipher::getGameVersion() {
    return Canvas::gameVersion;
}
bool Cipher::isGameBeta() {
    return Canvas::isBeta;
}


const char* Cipher::get_libName() {
    return Canvas::get_libName();
}

uintptr_t Cipher::CipherScan(const char *pattern, const char *mask) {
    char line[512] = {0};
    FILE *fp = fopen("/proc/self/maps", "r");
    if (!fp) return 0;
    while (fgets(line, sizeof(line), fp)) {
        if (strstr(line, Canvas::get_libName())) {
            unsigned long long start, end; uintptr_t result; size_t length;
            sscanf(line, "%llx-%llx", &start, &end);
            length = end - start;
            result = KittyScanner::find(start, length, pattern, mask);
            if (result) {
                fclose(fp);
                return result;
            }
        }
    }
    fclose(fp);
    return 0;
}

uintptr_t Cipher::CipherScan(uintptr_t start, const size_t size, const char *pattern, const char *mask) {
    return KittyScanner::find(start, size, pattern, mask);
}

uintptr_t Cipher::CipherScanSegments(const char *pattern, const char *mask, const Section& section) {
    uintptr_t result = 0;
    if(section == BOOTLOADER_ROD) {
        result = KittyScanner::find(Canvas::get_libBase(), Canvas::get_libSize(), pattern, mask);
    }
    if(section == BOOTLOADER_RXP) {
        result = KittyScanner::find(Canvas::get_libExecStart(), Canvas::get_libExecSize(), pattern, mask);
    }
    if(section == BOOTLOADER_RWP) {
        result = KittyScanner::find(Canvas::get_libDataStart(), Canvas::get_libDataSize(), pattern, mask);
    }
    return result;
}

std::vector<uintptr_t> Cipher::CipherScanAll(const char *pattern, const char *mask){
    char line[512] = {0}; std::vector<uintptr_t> result{}; std::vector<uintptr_t> resultGroups{};
    if(!pattern || ! mask) return result;
    FILE *fp = fopen("/proc/self/maps", "r");
    if (!fp) return result;
    while (fgets(line, sizeof(line), fp)) {
        if (strstr(line, Canvas::get_libName())) {
            unsigned long long start, end;
            sscanf(line, "%llx-%llx", &start, &end);
            result = KittyScanner::findBytesAll(start, end, pattern, mask);
            resultGroups.reserve(resultGroups.size() + result.size());
            resultGroups.insert(resultGroups.end(), result.begin(), result.end());
        }
    }
    fclose(fp);
    return resultGroups;
}

std::vector<uintptr_t> Cipher::CipherScanAll(uintptr_t start, uintptr_t end, const char *pattern, const char *mask) {
    return KittyScanner::findBytesAll(start, end, pattern, mask);
}

uintptr_t Cipher::CipherScanIdaPattern(const std::string &pattern){
    char line[512] = {0};
    FILE *fp = fopen("/proc/self/maps", "r");
    if (!fp) return 0;
    while (fgets(line, sizeof(line), fp)) {
        if (strstr(line, Canvas::get_libName())) {
            unsigned long long start, end; uintptr_t result;
            sscanf(line, "%llx-%llx", &start, &end);
            result = KittyScanner::findIdaPatternFirst(start, end, pattern);
            if (result) {
                fclose(fp);
                return result;
            }
        }
    }
    fclose(fp);
    return 0;
}

std::vector<uintptr_t> Cipher::CipherScanIdaPatternAll(const std::string &pattern){
    char line[512] = {0}; std::vector<uintptr_t> result{}, resultGroups{};
    if(pattern.empty()) return resultGroups;
    FILE *fp = fopen("/proc/self/maps", "r");
    if (!fp) return result;
    while (fgets(line, sizeof(line), fp)) {
        if (strstr(line, Canvas::get_libName())) {
            unsigned long long start, end;
            sscanf(line, "%llx-%llx", &start, &end);
            result = KittyScanner::findIdaPatternAll(start, end, pattern);
            resultGroups.reserve(resultGroups.size() + result.size());
            resultGroups.insert(resultGroups.end(), result.begin(), result.end());
        }
    }
    fclose(fp);
    return resultGroups;
}

uintptr_t Cipher::CipherScanIdaPattern(const uintptr_t start, const uintptr_t end, const std::string &pattern){
    return KittyScanner::findIdaPatternFirst(start, end, pattern);
}

std::vector<uintptr_t> Cipher::CipherScanIdaPatternAll(const uintptr_t start, const uintptr_t end, const std::string &pattern){
    return KittyScanner::findIdaPatternAll(start, end, pattern);
}

const char *Cipher::getConfigPath() {
    return Canvas::configDirPath;
}

CipherBase *CipherBase::set_libName(const char *libName) {
    this->m_libName = libName;
    return this;
}

CipherBase *CipherBase::set_Address(uintptr_t Address, bool isLocal) {
    if(!isLocal){
        p_Address = Address;
        return this;
    }
    if(this->m_libName == Canvas::get_libName()) this->p_Address = (Canvas::get_libBase() + Address);
    else {
        LOGD("cipher else");
        if (!Canvas::isLibLoaded(this->m_libName)) return this;
        uintptr_t libBase = Canvas::findLib(this->m_libName);
        this->p_Address = (libBase + Address);
    }
    return this;
}

uintptr_t CipherBase::get_address() {
    return this->p_Address;
}

const char *CipherBase::get_libName() {
    return this->m_libName;
}

bool CipherBase::get_Lock() {
    return this->m_isLocked;
}

CipherBase *CipherBase::set_Address(const char *Symbol) { //sets address via symbol
    if(this->get_libName() == Canvas::get_libName()) this->p_Address = (uintptr_t)dlsym(dlopen(this->get_libName(), RTLD_LOCAL), Symbol);
    if(!Canvas::isLibLoaded(this->get_libName())) return this;
    this->p_Address = (uintptr_t)dlsym(dlopen(this->get_libName(), RTLD_LOCAL), Symbol);
    return this;
}

CipherBase *CipherBase::set_Address(const char *pattern, const char *mask) {
    this->p_Address = Cipher::CipherScan(pattern, mask);
    return this;
}

CipherBase *CipherBase::set_Lock(bool isLocked) { //sets if multiple functions can hook
    this->m_isLocked = isLocked;
    return this;
}

CipherPatch *CipherPatch::set_Opcode(std::string hex) {
    artpatch_set_hex(this->patch, hex.c_str());
    return this;
}

CipherPatch *CipherPatch::Fire(){
    if(m_fired){
        artpatch_apply(this->patch);
        return this;
    }
    if(this->get_address() == NULL) return this;
    if(!this->get_Lock()){
        for(int i = 0; i < m_InstanceVec.size(); i++){
            CipherPatch *instance = (CipherPatch *)m_InstanceVec[i];
            if(instance->get_address() == this->get_address()){
                if(instance->get_Lock()) return this;
            }
        }
        //this->p_Backup = (uintptr_t)(new MemoryBackup(this->get_address(), 8)); //backs up original bytes
        artpatch_set_addr(this->patch, (void*)this->get_address());
        artpatch_apply(this->patch);
        this->m_InstanceVec.push_back((CipherBase *)this);
        this->m_fired = true;
        return this;
    }
}


void CipherPatch::Restore() {
    artpatch_restore(this->patch);
}

CipherPatch::CipherPatch() {
    this->patch = artpatch_new();
    this->m_type = TYPES::e_patch;
}


CipherBase::CipherBase() :p_Address(NULL), p_Backup(NULL), m_libName(Canvas::get_libName()), m_isLocked(false) {

}

CipherHook::CipherHook() :p_Hook(NULL), p_Callback(NULL){ //preinitialize variables
    this->m_type = TYPES::e_hook;
    this->set_libName(Canvas::get_libName());
}



CipherHook *CipherHook::set_Hook(uintptr_t Hook){ //sets detour function
    this->p_Hook = Hook;
    return this;
}

CipherHook *CipherHook::set_Callback(uintptr_t Callback) { //sets callback to original function
    this->p_Callback = Callback;
    return this;
}


CipherHook *CipherHook::Fire(){
    if(this->get_address() == NULL or this->p_Hook == NULL or (!this->get_Lock() and this->p_Callback == NULL)) return this; //check if fields are set
    if(!this->get_Lock()){
        for(int i = 0; i < m_InstanceVec.size(); i++){ //checks if function is already hooked
            CipherHook *instance = (CipherHook *)m_InstanceVec[i];
            if(instance->get_Lock()) return this;
            if(instance->get_address() == this->get_address() && instance->m_type == TYPES::e_hook) this->set_Address(((CipherHook *)m_InstanceVec[i])->p_Hook, false); //hooks the hooked function instead
        }
    }
    this->p_Backup = (uintptr_t)(new MemoryBackup(this->get_address(), 8)); //backs up original bytes
    LOGD("address: %p detour: %p callback: %p", this->get_address(), this->p_Hook, this->p_Callback);
    A64HookFunction((void *)this->get_address(), (void *)this->p_Hook, (void **)this->p_Callback); //hooks
    this->m_InstanceVec.push_back((CipherBase *)this);
    return this;
}

void CipherHook::Restore(){
    ((MemoryBackup *)this->p_Backup)->Restore();
    delete ((MemoryBackup *)this->p_Backup);
    this->m_InstanceVec.erase(std::find(this->m_InstanceVec.begin(), this->m_InstanceVec.end(), (CipherBase *)this));
    for(int i = 0; i < m_InstanceVec.size(); i++){ //fix hook queue
        CipherHook *instance = (CipherHook *)m_InstanceVec[i];
        if(instance->get_address() == this->p_Hook){
            if(instance->m_type == TYPES::e_patch){
                instance->Restore();
                return;
            }
            instance->set_Address(this->get_address(), false);
            ((MemoryBackup *)instance->p_Backup)->Restore();
            this->m_InstanceVec.erase(std::find(this->m_InstanceVec.begin(), this->m_InstanceVec.end(), (CipherBase *)instance));
            instance->Fire();

        }
    }

}

uintptr_t* get_adr_val(const char *pattern, const char *mask, uintptr_t *address, uint32_t relOffset, uint32_t patternOffset){
    uintptr_t scan_address = Cipher::CipherScan(pattern, mask);
    if(address != NULL) *address = scan_address;
    uintptr_t buf = 0;
    memcpy(&buf, (void *)scan_address, 4);
    long addr{0};
    CipherArm64::decode_adr_imm(buf, &addr);
    memcpy(&buf, (void *)(scan_address + relOffset), 4);
    int32_t rel = 0;
    CipherArm64::decode_ldrstr_uimm(buf, &rel);
    return (uintptr_t *)(((scan_address >> 12) << 12) + addr + rel);
}

CipherBase::~CipherBase(){

}

CipherPatch::~CipherPatch() {
    this->Restore();
    this->m_InstanceVec.erase(std::find(this->m_InstanceVec.begin(), this->m_InstanceVec.end(), (CipherBase *)this));
    artpatch_die(this->patch);
}

CipherHook::~CipherHook() {
    this->Restore();
    return;
}

char* Cipher::read_asset(char* asset_path) {
    if (!asset_path) return nullptr;
    AAsset* aAsset = AAssetManager_open(Canvas::aAssetManager, asset_path, AASSET_MODE_STREAMING);

    if (aAsset == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "CanvasReadAsset", "Asset not found: %s", asset_path);
        return nullptr;
    }

    size_t asset_size = AAsset_getLength64(aAsset);
    void* asset_buffer = malloc(asset_size);
    if (asset_buffer == nullptr) return nullptr; // no mem?

    if (AAsset_read(aAsset, asset_buffer, asset_size) != asset_size) {
        free(asset_buffer);
        return nullptr;
    }

    AAsset_close(aAsset);
    return (char*)asset_buffer;
}