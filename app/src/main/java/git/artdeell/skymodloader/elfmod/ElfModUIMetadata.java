package git.artdeell.skymodloader.elfmod;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;

import java.io.File;

import git.artdeell.skymodloader.R;
import me.tatarka.bindingcollectionadapter2.BR;

public class ElfModUIMetadata extends ElfModMetadata implements Observable {
    private transient PropertyChangeRegistry mCallbacks;

    public Activity activity;

    public Bitmap bitmapIcon;
    public ImageView icon;

    public ElfUIBackbone loader;

    public int which = 0;


    public void remove() {
        loader.removeModSafelyAsync(which);
        notifyPropertyChanged(BR._all);
    }

    @Override
    public String getName() {
        return ModListAdapter.getVisibleModName(this);
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

    @Override
    public void addOnPropertyChangedCallback(@NonNull Observable.OnPropertyChangedCallback callback) {
        synchronized(this) {
            if (this.mCallbacks == null) {
                this.mCallbacks = new PropertyChangeRegistry();
            }
        }

        this.mCallbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(@NonNull Observable.OnPropertyChangedCallback callback) {
        synchronized(this) {
            if (this.mCallbacks == null) {
                return;
            }
        }

        this.mCallbacks.remove(callback);
    }

    public void notifyChange() {
        synchronized(this) {
            if (this.mCallbacks == null) {
                return;
            }
        }

        this.mCallbacks.notifyCallbacks(this, 0, (Void) null);
    }

    public void notifyPropertyChanged(int fieldId) {
        synchronized(this) {
            if (this.mCallbacks == null) {
                return;
            }
        }

        this.mCallbacks.notifyCallbacks(this, fieldId, (Void) null);
    }
}


