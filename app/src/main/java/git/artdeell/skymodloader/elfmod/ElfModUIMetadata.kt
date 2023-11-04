package git.artdeell.skymodloader.elfmod

import android.app.Activity
import android.graphics.Bitmap
import git.artdeell.skymodloader.R
import git.artdeell.skymodloader.SMLApplication
import java.io.File

class ElfModUIMetadata : ElfModMetadata() {
    @JvmField
    var activity : Activity? = null
    @JvmField
    var modFile: File? = null
    @JvmField
    var description: String? = null
    @JvmField
    var icon: Bitmap? = null
    @JvmField
    var loader: ElfUIBackbone? = null
    @JvmField
    var which = 0
    var name: String
        get() = ModListAdapter.getVisibleModName(activity, this)
        set(name) {
            super.name = name
        }
    val version: String
        get() = if (modIsValid) {
            activity!!.getString(
                R.string.mod_version,
                majorVersion,
                minorVersion,
                patchVersion
            )
        } else {
            activity!!.getString(R.string.mod_invalid)
        }
    var enabled: Boolean
        get() = !File(modFile!!.path + "_invalid.txt").exists()
        set(value) {
            if (!value)
                File(modFile!!.path + "_invalid.txt").writeText("ok")
            else
                File(modFile!!.path + "_invalid.txt").deleteRecursively()
        }

    fun remove() {
        loader!!.removeModSafelyAsync(which)
    }
}
