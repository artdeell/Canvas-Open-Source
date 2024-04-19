#pragma once

#include <vector>
#include <string>
#include "../../Utils/artpatch/artpatch.h"

/**
 * @brief Struct representing device information.
 */
struct DeviceInfo {
    float xdpi;                         /**< X-axis dots per inch (dpi) of the device screen. */
    float ydpi;                         /**< Y-axis dots per inch (dpi) of the device screen. */
    float density;                      /**< Pixel density of the device screen. */
    std::string deviceName;             /**< Name of the device. */
    std::string deviceManufacturer;     /**< Manufacturer of the device. */
    std::string deviceModel;            /**< Model of the device. */
};

/**
 * @brief Specifies the type of the game.
 */
enum class GameType : int {
    Live = 0,   /**< Represents the live version of the game. */
    Beta,       /**< Represents the beta version of the game. */
    Huawei      /**< Represents the Huawei version of the game. */
};

/**
 * @brief Specifies memory segment permissions.
 */
enum class Flags : int {
    ReadOnly = 10,      /**< Read-only segment (rodata). */
    ReadAndWrite,       /**< Read-write segment (bss). */
    ReadAndExecute,     /**< Read-execute segment (text). */
    Any                 /**< Any segment. */
};

/**
 * @brief Provides functionalities for interacting with game memory, scanning patterns, and accessing game information.
 */
class Cipher {
public:
    /**
     * @brief Retrieves the game version.
     * @return The game version as a 32-bit unsigned integer.
     */
    static std::uint32_t get_GameVersion();

    /**
     * @brief Retrieves the game type.
     * @return The game type.
     *
     * Possible return values:
     * - GameType::Live: Represents the live version of the game.
     * - GameType::Beta: Represents the beta version of the game.
     * - GameType::Huawei: Represents the Huawei version of the game.
     */
    static GameType get_GameType();

    /**
     * @brief Retrieves the name of the library.
     * @return The name of the library.
     */
    static const char *get_libName();

    /**
     * @brief Retrieves the base address of the library.
     * @return The base address of the library.
     */
    static std::uintptr_t get_libBase();

    /**
     * @brief Retrieves the path for mod configurations.
     * @return The path for mod configurations.
     */
    static const char *get_ConfigsPath();

    /**
     * @brief Reads an asset from the given asset path.
     * @param _assetPath The path of the asset to read.
     * @return A pointer to the buffer containing the asset data.
     */
    static char *readAsset(const char *_assetPath);

    /**
     * @brief Adds a listener for keyboard events.
     * @param _listener The listener function to be added.
     */
    static void addOnKeyboardCompleteListener(void (*_listener)(std::string _message));

    /**
    * @brief Adds a message to the user's chat.
    * @param _message The string to be added.
    */
    static DeviceInfo get_DeviceInfo();

public:
    /**
     * @brief Converts a pattern string into bytes and a mask for memory scanning.
     * @param _pattern The pattern string to convert.
     * @param _bytesBuffer The buffer to store the converted bytes.
     * @param _maskBuffer The buffer to store the mask.
     */
    static void patternToBytes(const std::string _pattern, char *_bytesBuffer, std::string &_maskBuffer);

    /**
     * @brief Scans memory for a specific pattern within a given range.
     * @param _start The start address of the memory range to scan.
     * @param _end The end address of the memory range to scan.
     * @param _bytes The pattern bytes to search for.
     * @param _mask The mask corresponding to the pattern bytes.
     * @return The address of the found pattern, or 0 if not found.
     */
    static std::uintptr_t CipherScan(const std::uintptr_t _start, const std::uintptr_t _end, const char *_bytes, const char *_mask);

    /**
     * @brief Scans memory for a specific pattern within a specific memory segment in the lib range.
     * @param _bytes The pattern bytes to search for.
     * @param _mask The mask corresponding to the pattern bytes.
     * @param _flag The flag indicating the memory segment type to search in.
     * @param _start The start address of the scan in the specified memory segment.
     * @param _libName The name of the library to scan.
     * @return The address of the found pattern, or 0 if not found.
     */
    static std::uintptr_t CipherScan(const char *_bytes, const char *_mask, const Flags &_flag = Flags::Any, const std::uintptr_t &_start = 0x0, const char *_libName = nullptr);

    /**
     * @brief Scans memory for all occurrences of a specific pattern within a given range.
     * @param _start The start address of the memory range to scan.
     * @param _end The end address of the memory range to scan.
     * @param _bytes The pattern bytes to search for.
     * @param _mask The mask corresponding to the pattern bytes.
     * @return A vector containing the addresses of all found patterns.
     */
    static std::vector<std::uintptr_t> CipherScanAll(const std::uintptr_t _start, const std::uintptr_t _end, const char *_bytes, const char *_mask
    );

    /**
     * @brief Scans memory for all occurrences of a specific pattern within a specific memory segment.
     * @param _bytes The pattern bytes to search for.
     * @param _mask The mask corresponding to the pattern bytes.
     * @param _flag The flag indicating the memory segment type to search in.
     * @param _start The start address of the memory segment to scan.
     * @param _libName The name of the library to scan.
     * @return A vector containing the addresses of all found patterns.
     */
    static std::vector<std::uintptr_t> CipherScanAll(const char *_bytes, const char *_mask, const Flags &_flag = Flags::Any, const std::uintptr_t &_start = 0x0, const char *_libName = nullptr);

    /**
     * @brief Scans memory for a pattern defined as a string within a given range or within a specific memory segment.
     * @param _start The start address of the memory range to scan.
     * @param _end The end address of the memory range to scan.
     * @param _pattern The pattern string to search for.
     * @return The address of the found pattern, or 0 if not found.
     */
    static std::uintptr_t CipherScanPattern(const std::uintptr_t _start, const std::uintptr_t _end, const char *_pattern);

    /**
     * @brief Scans memory for a pattern defined as a string within a specific memory segment.
     * @param _pattern The pattern string to search for.
     * @param _flag The flag indicating the memory segment type to search in.
     * @param _start The start address of the memory segment to scan.
     * @param _libName The name of the library to scan.
     * @return The address of the found pattern, or 0 if not found.
     */
    static std::uintptr_t CipherScanPattern(const char *_pattern, const Flags &_flag = Flags::Any, const std::uintptr_t &_start = 0x0, const char *_libName = nullptr);

    /**
     * @brief Scans memory for all occurrences of a pattern defined as a string within a given range or within a specific memory segment.
     * @param _start The start address of the memory range to scan.
     * @param _end The end address of the memory range to scan.
     * @param _pattern The pattern string to search for.
     * @return A vector containing the addresses of all found patterns.
     */
    static std::vector<std::uintptr_t> CipherScanPatternAll(const std::uintptr_t _start, const std::uintptr_t _end, const char *_pattern);

    /**
     * @brief Scans memory for all occurrences of a pattern defined as a string within a specific memory segment.
     * @param _pattern The pattern string to search for.
     * @param _flag The flag indicating the memory segment type to search in.
     * @param _start The start address of the memory segment to scan.
     * @param _libName The name of the library to scan.
     * @return A vector containing the addresses of all found patterns.
     */
    static std::vector<std::uintptr_t> CipherScanPatternAll(const char *_pattern, const Flags &_flag = Flags::Any, const std::uintptr_t &_start = 0x0, const char *_libName = nullptr);
};

enum class Types : int {
    e_patch = 0,
    e_hook
};
/**
 * @brief Base class for cipher implementations.
 */
class CipherBase {
private:
    bool m_isLocked = false; /**< Flag indicating if the cipher is locked. */
    const char *m_libName; /**< Name of the library associated with the cipher. */
    std::uintptr_t p_Address; /**< Address pointer for the cipher. */

protected:
    Types m_type; /**< Type of the cipher. */
    /**
     * @brief Retrieve the name of the associated library.
     * @return Name of the library.
     */
    const char* get_libName();
    /**
     * @brief Check if the cipher is locked.
     * @return True if the cipher is locked, false otherwise.
     */
    bool get_Lock();
    /**
     * @brief Get the address of the cipher.
     * @return Address of the cipher.
     */
    std::uintptr_t get_address();
    static std::vector<CipherBase *> s_InstanceVec; /**< Vector of cipher instances. */
    std::uintptr_t p_Backup; /**< Backup pointer for the cipher. */

public:
    /**
     * @brief Constructor for CipherBase class.
     */
    CipherBase();
    /**
     * @brief Destructor for CipherBase class (pure virtual).
     */
    virtual ~CipherBase() = 0;

    /**
     * @brief Set the name of the associated library.
     * @param _libName Name of the library.
     * @return Pointer to the current CipherBase instance.
     */
    CipherBase* set_libName(const char* _libName);
    /**
     * @brief Set the address of the cipher using a symbol name.
     * @param _symbol Symbol name to set the address.
     * @return Pointer to the current CipherBase instance.
     */
    CipherBase* set_Address(const char* _symbol);
    /**
     * @brief Set the address of the cipher.
     * @param _address Address to set.
     * @param _isLocal Flag indicating if the address is local to the library.
     * @return Pointer to the current CipherBase instance.
     */
    CipherBase* set_Address(std::uintptr_t _address, bool _isLocal = true);
    /**
     * @brief Set the address of the cipher using bytes and a mask.
     * @param _bytes Bytes representing the address.
     * @param _mask Mask to apply to the bytes.
     * @return Pointer to the current CipherBase instance.
     */
    CipherBase* set_Address(const char* _bytes, const char* _mask);

    /**
     * @brief Set the lock status of the cipher.
     * @param _isLocked Lock status to set.
     * @return Pointer to the current CipherBase instance.
     */
    CipherBase* set_Lock(bool _isLocked);
    /**
     * @brief Trigger the cipher.
     * @return Pointer to the triggered CipherBase instance.
     */
    virtual CipherBase* Fire() = 0;
    /**
     * @brief Restore the cipher.
     */
    virtual void Restore() = 0;
};



/**
 * @brief Represents a hook applied to the game's memory.
 * Inherits from CipherBase.
 */
class CipherHook : public CipherBase {
private:
    std::uintptr_t p_Callback; /**< Address of the callback function. */
    std::uintptr_t p_Hook; /**< Address of the hook function. */

    /**
     * @brief Restores the hook.
     */
    void m_Restore();

public:
    /**
     * @brief Constructs a new CipherHook object.
     */
    CipherHook();

    /**
     * @brief Destroys the CipherHook object and restores the hook.
     */
    ~CipherHook() override;

    /**
     * @brief Sets the address of the hook function.
     * @param _hook The address of the hook function.
     * @return A pointer to the current CipherHook object.
     */
    CipherHook* set_Hook(std::uintptr_t _hook);

    /**
     * @brief Sets the address of the callback function.
     * @param _callback The address of the callback function.
     * @return A pointer to the current CipherHook object.
     */
    CipherHook* set_Callback(std::uintptr_t _callback);

    /**
     * @brief Applies the hook to the game's memory.
     * @return A pointer to the current CipherHook object.
     */
    CipherHook* Fire() override;

    /**
     * @brief Restores the hook.
     */
    void Restore() override;
};

/**
 * @brief Represents a patch applied to the game's memory.
 * Inherits from CipherBase.
 */
class CipherPatch : public CipherBase {
    bool m_fired = false; /**< Flag indicating whether the patch has been applied. */
    patch_t patch; /**< Internal patch structure. */

public:
    /**
     * @brief Constructs a new CipherPatch object.
     */
    CipherPatch();

    /**
     * @brief Destroys the CipherPatch object and restores the patch.
     */
    ~CipherPatch() override;

    /**
     * @brief Applies the patch to the game's memory.
     * @return A pointer to the current CipherPatch object.
     */
    CipherPatch* Fire() override;

    /**
     * @brief Sets the opcode for the patch.
     * @param _hex The opcode in hexadecimal format.
     * @return A pointer to the current CipherPatch object.
     */
    CipherPatch* set_Opcode(std::string _hex);

    /**
     * @brief Restores the patch.
     */
    void Restore() override;
};
