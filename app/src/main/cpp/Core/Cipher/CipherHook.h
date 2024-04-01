#pragma once

#include "CipherBase.h"

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