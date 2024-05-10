package git.artdeell.skymodloader.updater;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import git.artdeell.skymodloader.elfmod.ElfModMetadata;
import git.artdeell.skymodloader.elfmod.ElfRefcountLoader;

public class ModUpdaterService extends AbstractUpdaterService {
    public static final String EXTRA_UPDATE_URL = "update_url";
    public static final String EXTRA_LIB_NAME = "lib_name";
    public static final String EXTRA_VERSION_NUMBER = "version_number";
    private String mGithubUpdaterURL;
    private File mLibraryPath;
    private VersionNumber mCurrentVersionNumber;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ModUpdaterService", "onStartCommand...");
        Bundle extras = intent.getExtras();
        if(extras == null) {
            Log.w("ModUpdater","Extras missing for ModUpdater startup");
        }else {
            mGithubUpdaterURL = extras.getString(EXTRA_UPDATE_URL);
            mCurrentVersionNumber = (VersionNumber) extras.getSerializable(EXTRA_VERSION_NUMBER);
            String libName = extras.getString(EXTRA_LIB_NAME);
            mLibraryPath = new File(getFilesDir(), "mods"+File.separator+libName);
            start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected String getCacheFileName() {
        return "libtemp.so.temp";
    }

    @Override
    protected String getUpdateCheckerURL() {
        return mGithubUpdaterURL;
    }

    @Override
    protected boolean isTargetAsset(String assetName) {
        return assetName.startsWith("lib") && assetName.endsWith(".so");
    }

    @Override
    protected boolean hasInstallActions() {
        return true;
    }

    @Override
    protected boolean serviceAutoStarts() {
        return false;
    }

    @Override
    protected boolean needsUpdate(JSONObject updateInfo) throws JSONException {
        String tag = updateInfo.getString("tag_name");
        Log.i("ModUpdaterService", "Tag: "+tag);
        VersionNumber newVersion = VersionNumber.parseVersion(tag);
        if(newVersion == null) return false;
        return mCurrentVersionNumber.compare(newVersion) < 0;
    }

    @Override
    protected void performInstallActions() throws Exception {
        File source = getDownloadTarget();
        ElfModMetadata metadata = ElfRefcountLoader.loadMetadata(source);
        // Make sure that we actually replace the mod name after an update
        // if the library name changes.
        if(!mLibraryPath.delete())
            throw new IOException("Failed to delete old mod file");
        // Pick the new name based on metadata.
        File modsFolder = mLibraryPath.getParentFile();
        assert modsFolder != null;
        mLibraryPath = new File(modsFolder, metadata.name);
        // Copy the file.
        long length = source.length();
        setProgressBarMax(length);
        try (FileInputStream inputStream = new FileInputStream(getDownloadTarget())) {
            try (FileOutputStream outputStream = new FileOutputStream(mLibraryPath)) {
                copyStream(inputStream, outputStream, length != -1);
            }
        }
    }
}
