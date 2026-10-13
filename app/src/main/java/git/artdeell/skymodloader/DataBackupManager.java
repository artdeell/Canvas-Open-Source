package git.artdeell.skymodloader;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DataBackupManager {
    private static final String[] BACKUP_DIRS = {
        "mods",
        "Accounts",
        "PrivateAccounts",
        "OfficialAccount",
        "config",
        "configs",
        "Instances"
    };

    private static final String[] BACKUP_FILES = {
        "AccountAuthInfo.bin",
        "device_private.key",
        "device_public.key"
    };

    public static boolean exportBackup(Context context, Uri destinationUri) {
        try (OutputStream out = context.getContentResolver().openOutputStream(destinationUri)) {
            if (out == null) return false;
            return exportBackupStream(context, out);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean exportBackupStream(Context context, OutputStream out) {
        File filesDir = context.getFilesDir();
        if (filesDir == null || !filesDir.exists()) return false;

        try {
            InstanceManager.saveActiveToCurrentInstance(context);
        } catch (Throwable ignored) {
        }

        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(out))) {
            for (String dirName : BACKUP_DIRS) {
                File dir = new File(filesDir, dirName);
                if (dir.exists() && dir.isDirectory()) {
                    addDirectoryToZip(dir, dirName, zos);
                }
            }
            for (String fileName : BACKUP_FILES) {
                File file = new File(filesDir, fileName);
                if (file.exists() && file.isFile()) {
                    addFileToZip(file, fileName, zos);
                }
            }
            File sharedPrefsDir = new File(filesDir.getParentFile(), "shared_prefs");
            if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory()) {
                File[] prefs = sharedPrefsDir.listFiles();
                if (prefs != null) {
                    for (File pref : prefs) {
                        if (pref.isFile() && pref.getName().endsWith(".xml")) {
                            addFileToZip(pref, "shared_prefs/" + pref.getName(), zos);
                        }
                    }
                }
            }
            zos.finish();
            zos.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean importBackup(Context context, Uri sourceUri) {
        try (InputStream in = context.getContentResolver().openInputStream(sourceUri)) {
            if (in == null) return false;
            return importBackupStream(context, in);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean importBackupStream(Context context, InputStream in) {
        File filesDir = context.getFilesDir();
        if (filesDir == null) return false;
        if (!filesDir.exists()) filesDir.mkdirs();

        File appRoot = filesDir.getParentFile();

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.contains("..")) {
                    zis.closeEntry();
                    continue;
                }

                File targetFile;
                if (entryName.startsWith("shared_prefs/")) {
                    File sharedPrefsDir = new File(appRoot, "shared_prefs");
                    if (!sharedPrefsDir.exists()) sharedPrefsDir.mkdirs();
                    targetFile = new File(appRoot, entryName);
                } else {
                    targetFile = new File(filesDir, entryName);
                }

                if (entry.isDirectory()) {
                    if (!targetFile.exists()) targetFile.mkdirs();
                } else {
                    File parent = targetFile.getParentFile();
                    if (parent != null && !parent.exists()) parent.mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, count);
                        }
                        fos.flush();
                    }
                }
                zis.closeEntry();
            }
            try {
                InstanceManager.init(context);
            } catch (Throwable ignored) {
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void addDirectoryToZip(File dir, String baseName, ZipOutputStream zos) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            String entryPath = baseName + "/" + file.getName();
            if (file.isDirectory()) {
                addDirectoryToZip(file, entryPath, zos);
            } else {
                addFileToZip(file, entryPath, zos);
            }
        }
    }

    private static void addFileToZip(File file, String entryPath, ZipOutputStream zos) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryPath);
        zos.putNextEntry(zipEntry);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, count);
            }
        }
        zos.closeEntry();
    }
}
