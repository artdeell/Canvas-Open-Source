package git.artdeell.skymodloader.elfmod;

import android.app.Activity;
import android.graphics.Bitmap;

import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.SMLApplication;

import java.io.File;

public class ElfModUIMetadata extends ElfModMetadata {

    public Activity activity;
    public File modFile;

    public String description;

    public Bitmap icon;

    public ElfUIBackbone loader;

    public int which = 0;
    public String name;


    public void remove() {
        loader.removeModSafelyAsync(which);
    }

    public String getName() {
        super.name = name;
        return ModListAdapter.getVisibleModName(activity, this);
    }

    public String getVersion() {
        if (modIsValid) {
            return activity.getString(
                    R.string.mod_version,
                    majorVersion,
                    minorVersion,
                    patchVersion
            );
        } else {
            return activity.getString(R.string.mod_invalid);
        }
    }

    public Boolean getEnabled() {
        return !new File(modFile.getPath() + "_invalid.txt").exists();
    }

    public void setEnabled(Boolean enabled) {
        if (!enabled) {
            try {
                new File(modFile.getPath() + "_invalid.txt").createNewFile();
            }catch(Exception e){}
        }
        else {
            try {
                new File(modFile.getPath() + "_invalid.txt").delete();
            }catch(Exception e){}
        }
    }
}


