package com.tgc.sky;

import java.util.ArrayList;

public class SystemRemoteConfig_android {
    private static final String TAG = "SystemRemoteConfig_android";
    static final double kRemoteConfigFetchCooldown = 3600.0d;
    private static volatile SystemRemoteConfig_android sInstance;

    /* access modifiers changed from: private */
    public native void SetLastActivatedTime();

    /* access modifiers changed from: private */
    public native void SetLastFetchedTime();

    private native void SetNextFetchTime(double d);

    public static SystemRemoteConfig_android getInstance() {
        if (sInstance == null) {
            synchronized (SystemIO_android.class) {
                if (sInstance == null) {
                    sInstance = new SystemRemoteConfig_android();
                }
            }
        }
        return sInstance;
    }

    /* access modifiers changed from: package-private */
    public void Initialize(GameActivity gameActivity) {
    }

    public void FetchAsync() {
        SetNextFetchTime(kRemoteConfigFetchCooldown);
    }

    public void FetchAndActivateAsync() {
        SetNextFetchTime(kRemoteConfigFetchCooldown);
    }

    public void ActivateFetched() {
    }

    public String[] GetStringValue(String str) {
        return new String[]{
                "false", ""};
    }

    public String[][] GetRemoteConfigsWithPrefix(String str) {
        ArrayList arrayList = new ArrayList();
        return (String[][]) arrayList.toArray(new String[arrayList.size()][]);
    }
}
