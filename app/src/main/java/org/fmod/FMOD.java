package org.fmod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

public class FMOD {
    private static Context gContext;
    private static PluginBroadcastReceiver gPluginBroadcastReceiver = new PluginBroadcastReceiver();

    /* access modifiers changed from: private */
    public static native void OutputAAudioHeadphonesChanged();

    public static void init(Context context) {
        gContext = context;
    }

    public static void close() {
        gContext = null;
    }

    public static boolean checkInit() {
        return gContext != null;
    }

    public static AssetManager getAssetManager() {
        Context context = gContext;
        if (context != null) {
            return context.getAssets();
        }
        return null;
    }

    public static boolean supportsLowLatency() {
        int outputBlockSize = getOutputBlockSize();
        boolean lowLatencyFlag = lowLatencyFlag();
        boolean proAudioFlag = proAudioFlag();
        boolean z = outputBlockSize > 0 && outputBlockSize <= 1024;
        boolean isBluetoothOn = isBluetoothOn();
        Log.i("fmod", "FMOD::supportsLowLatency                 : Low latency = " + lowLatencyFlag + ", Pro Audio = " + proAudioFlag + ", Bluetooth On = " + isBluetoothOn + ", Acceptable Block Size = " + z + " (" + outputBlockSize + ")");
        if (!z || !lowLatencyFlag || isBluetoothOn) {
            return false;
        }
        return true;
    }

    public static boolean lowLatencyFlag() {
        if (gContext == null || Build.VERSION.SDK_INT < 5) {
            return false;
        }
        return gContext.getPackageManager().hasSystemFeature("android.hardware.audio.low_latency");
    }

    public static boolean proAudioFlag() {
        if (gContext == null || Build.VERSION.SDK_INT < 5) {
            return false;
        }
        return gContext.getPackageManager().hasSystemFeature("android.hardware.audio.pro");
    }

    public static boolean supportsAAudio() {
        return Build.VERSION.SDK_INT >= 27;
    }

    public static int getOutputSampleRate() {
        String property;
        if (gContext == null || Build.VERSION.SDK_INT < 17 || (property = ((AudioManager) gContext.getSystemService(Context.AUDIO_SERVICE)).getProperty("android.media.property.OUTPUT_SAMPLE_RATE")) == null) {
            return 0;
        }
        return Integer.parseInt(property);
    }

    public static int getOutputBlockSize() {
        String property;
        if (gContext == null || Build.VERSION.SDK_INT < 17 || (property = ((AudioManager) gContext.getSystemService(Context.AUDIO_SERVICE)).getProperty("android.media.property.OUTPUT_FRAMES_PER_BUFFER")) == null) {
            return 0;
        }
        return Integer.parseInt(property);
    }

    public static boolean isBluetoothOn() {
        Context context = gContext;
        if (context == null) {
            return false;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isBluetoothA2dpOn() || audioManager.isBluetoothScoOn()) {
            return true;
        }
        return false;
    }

    static class PluginBroadcastReceiver extends BroadcastReceiver {
        PluginBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.HEADSET_PLUG".equals(intent.getAction())) {
                FMOD.OutputAAudioHeadphonesChanged();
            }
        }
    }

    public static void registerHeadsetDetection() {
        if (gContext != null) {
            gContext.registerReceiver(gPluginBroadcastReceiver, new IntentFilter("android.intent.action.HEADSET_PLUG"));
        }
    }

    public static void deregisterHeadsetDetection() {
        Context context = gContext;
        if (context != null) {
            context.unregisterReceiver(gPluginBroadcastReceiver);
        }
    }
}
