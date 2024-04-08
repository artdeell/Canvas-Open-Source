package git.artdeell.skymodloader;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AlertDialog;

import com.tgc.sky.BuildConfig;
import com.tgc.sky.GameActivity;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import git.artdeell.skymodloader.elfmod.ElfRefcountLoader;
import git.artdeell.skymodloader.iconloader.IconLoader;

public class MainActivity extends Activity {
    SharedPreferences sharedPreferences;
    public static String SKY_PACKAGE_NAME;
    Map<String, Integer> skyPackages;

    public static DeviceInfo deviceInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceInfo = getDeviceInfo();
        sharedPreferences = getSharedPreferences("package_configs", Context.MODE_PRIVATE);
        SKY_PACKAGE_NAME = sharedPreferences.getString("sky_package_name", "com.tgc.sky.android");
        sharedPreferences.edit().putString("sky_package_name", SKY_PACKAGE_NAME).apply();
        skyPackages = new HashMap<>();
        skyPackages.put("com.tgc.sky.android", 0);
        skyPackages.put("com.tgc.sky.android.test.gold", 1);
        skyPackages.put("com.tgc.sky.android.huawei", 2);
        loadGame();
    }

    private void loadGame() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info;
            info = pm.getPackageInfo(SKY_PACKAGE_NAME, PackageManager.GET_SHARED_LIBRARY_FILES);
            SMLApplication.skyPName = info.packageName;
            SMLApplication.skyRes = pm.getResourcesForApplication(info.packageName);
            SMLApplication.smlRes = getResources();
            String versionName = info.versionName;
            BuildConfig.SKY_VERSION = versionName.substring(0, versionName.indexOf(' ')).trim();
            BuildConfig.VERSION_CODE = info.versionCode;
            String nativeLibraryDir = info.applicationInfo.nativeLibraryDir;
            File modsDir = new File(getFilesDir(), "mods");
            File configDir = new File(getFilesDir(), "config");
            if (!configDir.isDirectory() && !configDir.mkdirs()) throw new IOException("Failed to create mod configuration directory");
            ElfLoader loader = new ElfLoader(nativeLibraryDir + ":/system/lib64");
            loader.loadLib("libBootloader.so");
            System.loadLibrary("ciphered");

            setDeviceInfoNative(
                deviceInfo.xdpi,
                deviceInfo.ydpi,
                deviceInfo.density,
                Optional.ofNullable(deviceInfo.deviceName).orElse(""),
                Optional.ofNullable(deviceInfo.deviceManufacturer).orElse(""),
                Optional.ofNullable(deviceInfo.deviceModel).orElse("")
            );

            IconLoader.findIcons();
            MainActivity.settle(
                    BuildConfig.VERSION_CODE,
                    skyPackages.getOrDefault(SKY_PACKAGE_NAME, 0),
                    configDir.getAbsolutePath(),
                    SMLApplication.skyRes.getAssets()
            );
            new ElfRefcountLoader(nativeLibraryDir + ":/system/lib64", modsDir).load();
            if (SKY_PACKAGE_NAME.equals("com.tgc.sky.android.test.gold")) {
                SKY_PACKAGE_NAME = "com.tgc.sky.android.test.";
                BuildConfig.SKY_SERVER_HOSTNAME = "beta.radiance.thatgamecompany.com";
                BuildConfig.SKY_BRANCH_NAME = "Test";
                BuildConfig.SKY_STAGE_NAME = "Test";
                BuildConfig.VERSION_CODE = sharedPreferences.getBoolean("skip_updates", false) ? 0x99999 : info.versionCode;
            }
            BuildConfig.APPLICATION_ID = SKY_PACKAGE_NAME;
            startActivity(new Intent(this, GameActivity.class));
        } catch (PackageManager.NameNotFoundException e) {
            alertDialog(getString(R.string.sky_not_installed));
        } catch (Throwable e) {
            alertDialog(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void alertDialog(Throwable th) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        String stackTrace = sw.toString();
        pw.close();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stackTrace);
        builder.setPositiveButton(android.R.string.ok, (d, w) -> finish());
        builder.show();
    }

    public void alertDialog(String th) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(th);
        builder.setPositiveButton(android.R.string.ok, (d, w) -> finish());
        builder.show();
    }

    public DeviceInfo getDeviceInfo() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.xdpi = displayMetrics.xdpi;
        deviceInfo.ydpi = displayMetrics.ydpi;
        deviceInfo.density = displayMetrics.density;

        deviceInfo.deviceName = Settings.Global.getString(getContentResolver(), "device_name");
        if (deviceInfo.deviceName == null || deviceInfo.deviceName.isEmpty()) {
            deviceInfo.deviceName = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
        }
        deviceInfo.deviceName = (deviceInfo.deviceName == null || deviceInfo.deviceName.isEmpty()) ? "NO_DEVICE_NAME" : deviceInfo.deviceName;

        deviceInfo.deviceManufacturer = Build.MANUFACTURER;
        deviceInfo.deviceModel = Build.MODEL;
        return deviceInfo;
    }

    public static native void settle(int _gameVersion, int _gameType, String _configDir, AssetManager _gameAssets);
    public static native void setDeviceInfoNative(float _xdpi, float _ydpi, float _density, String _deviceName, String _manufacturer, String _model);
    public static native void onKeyboardCompleteNative(String message);



}