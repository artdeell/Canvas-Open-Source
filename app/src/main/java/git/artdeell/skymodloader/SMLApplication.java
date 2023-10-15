package git.artdeell.skymodloader;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;

public class SMLApplication extends Application {
    private static SMLApplication smlApplication;
    public static String skyPName;
    public static Resources skyRes;
    public static Resources smlRes;
    public static SMLApplication deez() {
        return smlApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        smlApplication = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        smlApplication = null;
    }
}
