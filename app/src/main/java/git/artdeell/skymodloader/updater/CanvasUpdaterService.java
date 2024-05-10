package git.artdeell.skymodloader.updater;

import org.json.JSONException;
import org.json.JSONObject;

import git.artdeell.skymodloader.BuildConfig;

public class CanvasUpdaterService extends AbstractUpdaterService {
    @Override
    protected String getCacheFileName() {
        return "update.apk";
    }

    @Override
    protected String getUpdateCheckerURL() {
        return "https://api.github.com/repos/RomanChamelo/Canvas-Open-Source/releases/latest";
    }

    @Override
    protected boolean isTargetAsset(String assetName) {
        return assetName.equals("Canvas.apk");
    }

    @Override
    protected boolean hasInstallActions() {
        return false;
    }

    @Override
    protected boolean serviceAutoStarts() {
        return true;
    }

    @Override
    protected boolean needsUpdate(JSONObject updateInfo) throws JSONException {
        String name = updateInfo.getString("name");
        if (name.startsWith("[")) {
            int versionCode = Integer.parseInt(name.substring(name.indexOf('[')+1, name.indexOf(']')));
            return versionCode > BuildConfig.VERSION_CODE;
        }
        return false;
    }

    @Override
    protected void performInstallActions() {
        // Do nothing here, as we don't have any install actions
    }
}
