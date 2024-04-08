package git.artdeell.skymodloader.elfmod;

import android.app.Activity;
import android.graphics.Bitmap;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import git.artdeell.skymodloader.R;

public class ElfModUIMetadata extends ElfModMetadata {

    public Activity activity;

    public Bitmap icon;

    public ElfUIBackbone loader;

    public int which = 0;


    public void remove() {
        loader.removeModSafelyAsync(which);
    }

    public String getName() {
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
            } catch (Exception e) {
            }
        } else {
            try {
                new File(modFile.getPath() + "_invalid.txt").delete();
            } catch (Exception e) {
            }
        }
    }
}


