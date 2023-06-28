package git.artdeell.skymodloader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.tgc.sky.BuildConfig;
import com.tgc.sky.GameActivity;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import git.artdeell.skymodloader.elfmod.ElfRefcountLoader;
import git.artdeell.skymodloader.iconloader.IconLoader;

public class MainActivity extends Activity {
    public static final String[] SKY_PACKAGE_NAMES = new String[] {null, "com.tgc.sky.android", "com.tgc.sky.android.huawei"};
    public static String SKY_PACKAGE_NAME = null;
    PackageManager packageManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSharedPreferences("beta_enabler",Context.MODE_PRIVATE).getBoolean("enable_beta", false))
            SKY_PACKAGE_NAMES[0] = "com.tgc.sky.android.test.gold";
        findAndLoad();
    }

    private void findAndLoad() {
        packageManager = getPackageManager();
        for(String pName : SKY_PACKAGE_NAMES) {
            if(pName == null) continue;
            try {
                packageManager.getPackageInfo(pName, PackageManager.GET_SHARED_LIBRARY_FILES);
                SKY_PACKAGE_NAME = pName;
                break;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(SKY_PACKAGE_NAME != null) {
            loadGame();
        }else{
            alertDialog(getString(R.string.sky_not_installed));
        }
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
            File modsDir = new File(getFilesDir(),"mods");
            File configDir = new File(getFilesDir(), "config");
            if(!configDir.isDirectory() && !configDir.mkdirs()) throw new IOException("Failed to create mod configuration directory");
            ElfLoader loader = new ElfLoader(nativeLibraryDir+":/system/lib64");
            loader.loadLib("libBootloader.so");
            System.loadLibrary("ciphered");
            IconLoader.findIcons();
            MainActivity.settle(BuildConfig.VERSION_CODE, SKY_PACKAGE_NAME.startsWith("com.tgc.sky.android.test"), configDir.getAbsolutePath(), SMLApplication.skyRes.getAssets());
            new ElfRefcountLoader(nativeLibraryDir+":/system/lib64",modsDir).load();
            if(SKY_PACKAGE_NAME.equals("com.tgc.sky.android.test.gold")) {
                SKY_PACKAGE_NAME = "com.tgc.sky.android.test.";
                BuildConfig.SKY_SERVER_HOSTNAME = "beta.radiance.thatgamecompany.com";
                BuildConfig.SKY_BRANCH_NAME = "Test";
                BuildConfig.SKY_STAGE_NAME = "Test";
            }
            BuildConfig.APPLICATION_ID = SKY_PACKAGE_NAME;
            startActivity(new Intent(this, GameActivity.class));
        }catch(Throwable e) {
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
        builder.setPositiveButton(android.R.string.ok, (d,w)-> finish());
        builder.show();
    }

    public void alertDialog(String th) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(th);
        builder.setPositiveButton(android.R.string.ok, (d,w)-> finish());
        builder.show();
    }
    private static native void settle(int gameVersion, boolean isBeta, String configDir, AssetManager gameAssets);
}