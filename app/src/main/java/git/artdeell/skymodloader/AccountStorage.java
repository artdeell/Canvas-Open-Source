package git.artdeell.skymodloader;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import git.artdeell.skymodloader.server.ServerManager;

public class AccountStorage {
    private static final String PREFS_NAME = "account_storage_configs";
    private static final String PREF_LAST_SERVER = "last_synced_server";
    private static final String PRIVATE_ACCOUNTS_DIR = "PrivateAccounts";
    private static final String OFFICIAL_ACCOUNT_DIR = "OfficialAccount";
    private static final String[] ACCOUNT_FILES = {
        "AccountAuthInfo.bin",
        "device_private.key",
        "device_public.key"
    };

    public static File getPrivateAccountsDirectory(Context context) {
        File dir = new File(context.getFilesDir(), PRIVATE_ACCOUNTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getOfficialAccountDirectory(Context context) {
        File dir = new File(context.getFilesDir(), OFFICIAL_ACCOUNT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getServerStorageDirectory(Context context, String serverKey) {
        if (serverKey == null || serverKey.equals("official")) {
            return getOfficialAccountDirectory(context);
        }
        File dir = new File(getPrivateAccountsDirectory(context), sanitizeKey(serverKey));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String getCurrentServerKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("package_configs", Context.MODE_PRIVATE);
        boolean isCustomServer = prefs.getBoolean("custom_server", false);
        if (isCustomServer) {
            String host = prefs.getString("server_host", ServerManager.getDefaultHost());
            if (host == null || host.trim().isEmpty()) {
                return "official";
            }
            return "private_" + sanitizeKey(host);
        }
        return "official";
    }

    public static String sanitizeKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return "default";
        }
        String sanitized = key.trim();
        if (sanitized.startsWith("https://")) {
            sanitized = sanitized.substring(8);
        } else if (sanitized.startsWith("http://")) {
            sanitized = sanitized.substring(7);
        }
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitized.isEmpty()) {
            return "default";
        }
        return sanitized;
    }

    public static synchronized void sync(Context context) {
        if (context == null) {
            return;
        }
        String currentServerKey = getCurrentServerKey(context);
        SharedPreferences storagePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastServerKey = storagePrefs.getString(PREF_LAST_SERVER, null);
        File filesDir = context.getFilesDir();

        if (lastServerKey != null && !lastServerKey.equals(currentServerKey)) {
            File previousServerDir = getServerStorageDirectory(context, lastServerKey);
            saveServerData(filesDir, previousServerDir);
            clearActiveAccountData(filesDir);
            File currentServerDir = getServerStorageDirectory(context, currentServerKey);
            restoreServerData(currentServerDir, filesDir);
        } else if (lastServerKey == null) {
            File currentServerDir = getServerStorageDirectory(context, currentServerKey);
            if (hasActiveData(filesDir)) {
                saveServerData(filesDir, currentServerDir);
            } else if (hasServerData(currentServerDir)) {
                restoreServerData(currentServerDir, filesDir);
            }
        } else {
            File currentServerDir = getServerStorageDirectory(context, currentServerKey);
            if (hasActiveData(filesDir)) {
                saveServerData(filesDir, currentServerDir);
            } else if (hasServerData(currentServerDir)) {
                restoreServerData(currentServerDir, filesDir);
            }
        }

        storagePrefs.edit().putString(PREF_LAST_SERVER, currentServerKey).apply();
    }

    public static synchronized void saveActive(Context context) {
        if (context == null) {
            return;
        }
        String currentServerKey = getCurrentServerKey(context);
        File filesDir = context.getFilesDir();
        File currentServerDir = getServerStorageDirectory(context, currentServerKey);
        saveServerData(filesDir, currentServerDir);
        InstanceManager.saveActiveToCurrentInstance(context);
    }

    public static synchronized void restoreActive(Context context) {
        if (context == null) {
            return;
        }
        String currentServerKey = getCurrentServerKey(context);
        File filesDir = context.getFilesDir();
        File currentServerDir = getServerStorageDirectory(context, currentServerKey);
        restoreServerData(currentServerDir, filesDir);
    }

    private static void saveServerData(File filesDir, File targetDir) {
        if (filesDir == null || targetDir == null) {
            return;
        }
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        for (String fileName : ACCOUNT_FILES) {
            File sourceFile = new File(filesDir, fileName);
            if (sourceFile.exists() && sourceFile.isFile()) {
                File destFile = new File(targetDir, fileName);
                copyFile(sourceFile, destFile);
            }
        }
    }

    private static void restoreServerData(File sourceDir, File filesDir) {
        if (sourceDir == null || filesDir == null || !sourceDir.exists()) {
            return;
        }
        for (String fileName : ACCOUNT_FILES) {
            File sourceFile = new File(sourceDir, fileName);
            if (sourceFile.exists() && sourceFile.isFile()) {
                File destFile = new File(filesDir, fileName);
                copyFile(sourceFile, destFile);
            }
        }
    }

    private static void clearActiveAccountData(File filesDir) {
        if (filesDir == null || !filesDir.exists()) {
            return;
        }
        for (String fileName : ACCOUNT_FILES) {
            File file = new File(filesDir, fileName);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private static boolean hasActiveData(File filesDir) {
        if (filesDir == null || !filesDir.exists()) {
            return false;
        }
        for (String fileName : ACCOUNT_FILES) {
            File file = new File(filesDir, fileName);
            if (file.exists() && file.length() > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasServerData(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return false;
        }
        for (String fileName : ACCOUNT_FILES) {
            File file = new File(dir, fileName);
            if (file.exists() && file.length() > 0) {
                return true;
            }
        }
        return false;
    }

    private static void copyFile(File source, File destination) {
        try (FileInputStream in = new FileInputStream(source);
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException ignored) {
        }
    }
}
