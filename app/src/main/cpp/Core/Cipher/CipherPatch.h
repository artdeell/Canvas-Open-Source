#pragma once

#include <string>

#include "../../Utils/artpatch/artpatch.h"
#include "CipherBase.h"

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
