//
// Created by Lukas on 24/07/2022.
//

#include "Cipher.h"
#include "CipherUtils.h"

std::uint32_t Cipher::getGameVersion() {
    return CipherUtils::get_GameVersion();
}

bool Cipher::isGameBeta() {
    return CipherUtils::get_GameType() == GameType::Beta;
}

std::uintptr_t Cipher::get_libBase() {
    return CipherUtils::get_libBase();
}

const char *Cipher::get_libName() {
    return CipherUtils::get_libName();
}

std::uintptr_t Cipher::CipherScan(const char *_bytes, const char *_mask) {
    return CipherUtils::CipherScan(_bytes, _mask, Flags::Any);
}

std::uintptr_t Cipher::CipherScan(std::uintptr_t _start, const size_t _size, const char *_bytes, const char *_mask) {
    return CipherUtils::CipherScan(_start, _start + _size, _bytes, _mask);
}

std::vector <std::uintptr_t> Cipher::CipherScanAll(const char *_bytes, const char *_mask) {
    return CipherUtils::CipherScanAll(_bytes, _mask, Flags::Any);
}

std::vector <std::uintptr_t> Cipher::CipherScanAll(std::uintptr_t _start, std::uintptr_t _end, const char *_bytes, const char *_mask) {
    return CipherUtils::CipherScanAll(_start, _end, _bytes, _mask);
}

std::uintptr_t Cipher::CipherScanSegments(const char *_bytes, const char *_mask, const Section &_section) {
    Flags flags;
    switch (_section) {
        case BOOTLOADER_ROD:
            flags = Flags::ReadOnly;
            break;
        case BOOTLOADER_RWP:
            flags = Flags::ReadAndWrite;
            break;
        case BOOTLOADER_RXP:
            flags = Flags::ReadAndExecute;
            break;
        default:
            flags = Flags::Any;
    }
    return CipherUtils::CipherScan(_bytes, _mask, flags);
}

std::uintptr_t Cipher::CipherScanIdaPattern(const std::string &_pattern) {
    return CipherUtils::CipherScanPattern(_pattern.c_str(), Flags::Any);
}

std::uintptr_t Cipher::CipherScanIdaPattern(const std::uintptr_t _start, const std::uintptr_t _end, const std::string &_pattern) {
    return CipherUtils::CipherScanPattern(_start, _end, _pattern.c_str());
}

std::vector <std::uintptr_t> Cipher::CipherScanIdaPatternAll(const std::string &_pattern) {
    return CipherUtils::CipherScanPatternAll(_pattern.c_str(), Flags::Any);
}

std::vector <std::uintptr_t> Cipher::CipherScanIdaPatternAll(const std::uintptr_t _start, const std::uintptr_t _end, const std::string &_pattern) {
    return CipherUtils::CipherScanPatternAll(_start, _end, _pattern.c_str());
}

const char *Cipher::getConfigPath() {
    return CipherUtils::get_ConfigsPath();
}

char *Cipher::read_asset(char *_assetPath) {
    return CipherUtils::readAsset(_assetPath);
}
