#include "CipherUtils.h"

#include "../../include/misc/Logger.h"
#include "../../include/misc/visibility.h"
#include <android/asset_manager.h>

#include <Canvas/Canvas.h>
#include <KittyMemory/KittyInclude.hpp>




std::uint32_t CipherUtils::get_GameVersion() {
    return Canvas::gameVersion;
}

GameType CipherUtils::get_GameType() {
    return static_cast<GameType>(Canvas::gameType);
}

const char* CipherUtils::get_libName() {
    return Canvas::libName;
}

std::uintptr_t CipherUtils::get_libBase() {
    return Canvas::libBase;
}

const char *CipherUtils::get_ConfigsPath() {
    return Canvas::configsPath;
}

void CipherUtils::patternToBytes(const std::string _pattern, char* _bytesBuffer, std::string& _maskBuffer) {
    std::string mask;
    std::vector<char> bytes;

    const size_t patternLen = _pattern.length();
    for (std::size_t i = 0; i < patternLen; i++) {
        if (_pattern[i] != ' ') {
            if (_pattern[i] == '?') {
                if (patternLen > i + 1 && _pattern[i + 1] == '?') {
                    bytes.push_back(0);
                    mask += '?';
                    ++i;
                } else {
                    bytes.push_back(0);
                    mask += '?';
                }
            } else if (patternLen > i + 1 && std::isxdigit(_pattern[i]) && std::isxdigit(_pattern[i + 1])) {
                bytes.push_back(std::stoi(_pattern.substr(i++, 2), nullptr, 16));
                mask += 'x';
            }
        }
    }

    if (!(bytes.empty() || mask.empty() || bytes.size() != mask.size())) {
        std::memcpy(_bytesBuffer, bytes.data(), bytes.size());
        _bytesBuffer[bytes.size()] = '\0';
        _maskBuffer = mask;
    }
}

bool segmentHasFlag(const KittyMemory::ProcMap& _segment, const Flags& _flag) {
    switch (_flag) {
        case Flags::ReadOnly:
            return _segment.is_ro;
        case Flags::ReadAndWrite:
            return _segment.is_rw;
        case Flags::ReadAndExecute:
            return _segment.is_rx;
        case Flags::Any:
            return _segment.is_ro || _segment.is_rw || _segment.is_rx;
        default:
            return false;
    }
}

std::uintptr_t CipherUtils::CipherScan(
        const std::uintptr_t _start,
        const std::uintptr_t _end,
        const char* _bytes,
        const char* _mask
) {
    if (_bytes == nullptr || _mask == nullptr) {
        return 0;
    }

    const std::size_t size = std::strlen(_mask);
    if (size == 0 || _start >= _end || (_start + size) > _end) {
        return 0;
    }

    const char* data = reinterpret_cast<const char*>(_start);
    for (std::size_t i = 0; i <= (_end - _start - size); ++i) {
        bool found = true;
        for (std::size_t j = 0; j < size; ++j) {
            if (_mask[j] == 'x' && data[i + j] != _bytes[j]) {
                found = false;
                break;
            }
        }
        if (found) {
            return reinterpret_cast<std::uintptr_t>(&data[i]);
        }
    }

    return 0;
}

std::uintptr_t CipherUtils::CipherScan(
        const char* _bytes,
        const char* _mask,
        const Flags& _flag,
        const std::uintptr_t& _start,
        const char* _libName
) {
    const char* libName = (_libName != nullptr && strlen(_libName) != 0) ? _libName : Canvas::libName;
    const KittyScanner::ElfScanner elfScanner = (strcmp(libName, Canvas::libName) ?
        KittyScanner::ElfScanner::createWithPath(libName) :
        Canvas::libElfScanner
    );

    if (!elfScanner.isValid()) {
        LOGE("!elfScanner.isValid()");
        return 0;
    }

    const std::vector<KittyMemory::ProcMap> elfMap = elfScanner.segments();
    for (const auto& segment : elfMap) {
        if (segmentHasFlag(segment, _flag)) {
            std::uintptr_t startAddress = segment.startAddress;
            std::uintptr_t endAddress = segment.endAddress;
            if (_start >= startAddress && _start < endAddress) {
                startAddress = _start;
            }
            if (std::uintptr_t result = CipherScan(
                    startAddress,
                    endAddress,
                    _bytes,
                    _mask
            )) {
                return result;
            }
        }
    }
    return 0;
}

std::vector<uintptr_t> CipherUtils::CipherScanAll(
        const std::uintptr_t _start,
        const std::uintptr_t _end,
        const char* _bytes,
        const char* _mask
) {
    std::vector<uintptr_t> list;
    const std::size_t size = std::strlen(_mask);
    if (_start >= _end || (_bytes == nullptr || _mask == nullptr) || size == 0) {
        return list;
    }
    std::uintptr_t curr_search_address = _start;
    do {
        if (!list.empty()) {
            curr_search_address = list.back() + size;
        }
        std::uintptr_t found = CipherScan(curr_search_address, _end, _bytes, _mask);
        if (!found) {
            break;
        }
        list.push_back(found);
    } while (true);

    return list;
}

std::vector<std::uintptr_t> CipherUtils::CipherScanAll(
        const char* _bytes,
        const char* _mask,
        const Flags& _flag,
        const std::uintptr_t& _start,
        const char* _libName
) {
    const char* libName = (_libName != nullptr  && strlen(_libName) != 0) ? _libName : Canvas::libName;
    const KittyScanner::ElfScanner elfScanner = KittyScanner::ElfScanner::createWithPath(libName);
    if (!elfScanner.isValid()) {
        return std::vector<std::uintptr_t>();
    }

    const std::vector<KittyMemory::ProcMap> elfMap = elfScanner.segments();
    for (const auto& segment : elfMap) {
        if (segmentHasFlag(segment, _flag)) {
            std::uintptr_t startAddress = segment.startAddress;
            std::uintptr_t endAddress = segment.endAddress;
            if (_start >= startAddress && _start < endAddress) {
                startAddress = _start;
            }
            return CipherScanAll(startAddress, endAddress, _bytes, _mask);
        }
    }

    return std::vector<std::uintptr_t>();
}

std::uintptr_t CipherUtils::CipherScanPattern(
        const std::uintptr_t _start,
        const std::uintptr_t _end,
        const char* _pattern
) {
    char bytes[256] = "";
    std::string mask;
    patternToBytes(_pattern, bytes, mask);
    return CipherUtils::CipherScan(_start, _end, bytes, mask.c_str());
}

std::uintptr_t CipherUtils::CipherScanPattern(
        const char* _pattern,
        const Flags& _flag,
        const uintptr_t& _start,
        const char* _libName
) {
    char bytes[256] = "";
    std::string mask;
    patternToBytes(_pattern, bytes, mask);
    return CipherUtils::CipherScan(bytes, mask.c_str(), _flag, _start, _libName);
}


std::vector<std::uintptr_t> CipherUtils::CipherScanPatternAll(
        const std::uintptr_t _start,
        const std::uintptr_t _end,
        const char* _pattern
) {
    char bytes[256] = "";
    std::string mask;
    patternToBytes(_pattern, bytes, mask);
    return CipherUtils::CipherScanAll(_start, _end, bytes, mask.c_str());
}

std::vector<std::uintptr_t> CipherUtils::CipherScanPatternAll(
        const char* _pattern,
        const Flags& _flag,
        const uintptr_t &_start,
        const char* _libName
) {
    char bytes[256] = "";
    std::string mask;
    patternToBytes(_pattern, bytes, mask);
    return CipherUtils::CipherScanAll(bytes, mask.c_str(), _flag, _start, _libName);
}

char* CipherUtils::readAsset(const char* _assetPath) {
    if (!_assetPath) return nullptr;
    AAsset* aAsset = AAssetManager_open(Canvas::aAssetManager, _assetPath, AASSET_MODE_STREAMING);

    if (aAsset == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "CanvasReadAsset", "Asset not found: %s", _assetPath);
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

void CipherUtils::addOnKeyboardCompleteListener(void (*_listener)(std::string)) {
    Canvas::onKeyboardCompleteListeners.push_back(_listener);
}

DeviceInfo CipherUtils::get_DeviceInfo() {
    DeviceInfo deviceInfo;
    std::memcpy(&deviceInfo, &Canvas::deviceInfo, sizeof(deviceInfo));
    return deviceInfo;
}

