package git.artdeell.skymodloader;

import android.content.Context;
import android.content.SharedPreferences;

import com.tgc.sky.io.DeviceKey;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstanceManager {
    private static final String PREFS_NAME = "instance_configs";
    private static final String KEY_ACTIVE_ID = "active_instance_id";
    private static final String KEY_INSTANCES_JSON = "instances_json";
    private static final String INSTANCES_DIR = "Instances";

    private static final String[] INSTANCE_FILES = {
        "AccountAuthInfo.bin",
        "device_private.key",
        "device_public.key"
    };

    private static final String[] INSTANCE_DIRS = {
        "OfficialAccount",
        "PrivateAccounts"
    };

    public static class InstanceInfo {
        public String id;
        public String name;
        public boolean isCurrent;
        public String maskedKey;
        public String fullKey;

        public InstanceInfo(String id, String name, boolean isCurrent, String maskedKey, String fullKey) {
            this.id = id;
            this.name = name;
            this.isCurrent = isCurrent;
            this.maskedKey = maskedKey;
            this.fullKey = fullKey;
        }
    }

    public static File getInstancesRoot(Context context) {
        File dir = new File(context.getFilesDir(), INSTANCES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getInstanceDirectory(Context context, String instanceId) {
        File dir = new File(getInstancesRoot(context), instanceId);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static synchronized void init(Context context) {
        if (context == null) return;
        DeviceKey.setContext(context);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String activeId = prefs.getString(KEY_ACTIVE_ID, null);
        String jsonStr = prefs.getString(KEY_INSTANCES_JSON, null);

        if (activeId == null || jsonStr == null) {
            activeId = "slot_1";
            JSONArray arr = new JSONArray();
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", "slot_1");
                obj.put("name", "Slot 1");
                arr.put(obj);
            } catch (Exception ignored) {}

            prefs.edit()
                .putString(KEY_ACTIVE_ID, activeId)
                .putString(KEY_INSTANCES_JSON, arr.toString())
                .apply();

            File slotDir = getInstanceDirectory(context, activeId);
            File filesDir = context.getFilesDir();
            File pubKey = new File(filesDir, "device_public.key");
            File offPubKey = new File(new File(filesDir, "OfficialAccount"), "device_public.key");
            File authFile = new File(filesDir, "AccountAuthInfo.bin");
            if (pubKey.exists() || offPubKey.exists() || authFile.exists()) {
                if (!pubKey.exists() && offPubKey.exists()) {
                    AccountStorage.restoreActive(context);
                }
                saveActiveToInstance(filesDir, slotDir);
            } else {
                DeviceKey.CreateKeyPairInDir(slotDir);
                restoreInstanceToActive(slotDir, filesDir);
            }
        } else {
            File activeDir = getInstanceDirectory(context, activeId);
            File pubKey = new File(context.getFilesDir(), "device_public.key");
            if (!pubKey.exists()) {
                File slotPubKey = new File(activeDir, "device_public.key");
                if (slotPubKey.exists()) {
                    restoreInstanceToActive(activeDir, context.getFilesDir());
                } else {
                    DeviceKey.CreateKeyPairInDir(activeDir);
                    restoreInstanceToActive(activeDir, context.getFilesDir());
                }
            }
        }
    }

    public static synchronized String getActiveInstanceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ACTIVE_ID, "slot_1");
    }

    public static synchronized String getActiveInstanceName(Context context) {
        String activeId = getActiveInstanceId(context);
        List<InstanceInfo> list = getInstances(context);
        for (InstanceInfo info : list) {
            if (info.id.equals(activeId)) {
                return info.name;
            }
        }
        return "Slot 1";
    }

    public static synchronized String getActiveMaskedKey(Context context) {
        File filesDir = context.getFilesDir();
        String key = DeviceKey.GetPublicKeyFromDir(filesDir);
        if (key == null) {
            String activeId = getActiveInstanceId(context);
            File instanceDir = getInstanceDirectory(context, activeId);
            key = DeviceKey.GetPublicKeyFromDir(instanceDir);
            if (key == null) {
                DeviceKey.CreateKeyPairInDir(instanceDir);
                restoreInstanceToActive(instanceDir, filesDir);
                key = DeviceKey.GetPublicKeyFromDir(filesDir);
            }
        }
        return DeviceKey.GetMaskedKey(key);
    }

    public static synchronized List<InstanceInfo> getInstances(Context context) {
        List<InstanceInfo> result = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String activeId = prefs.getString(KEY_ACTIVE_ID, "slot_1");
        String jsonStr = prefs.getString(KEY_INSTANCES_JSON, "[]");

        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String id = obj.getString("id");
                String name = obj.getString("name");
                boolean isCurrent = id.equals(activeId);

                File instanceDir = getInstanceDirectory(context, id);
                String fullKey = isCurrent
                    ? DeviceKey.GetPublicKeyFromDir(context.getFilesDir())
                    : DeviceKey.GetPublicKeyFromDir(instanceDir);

                if (fullKey == null) {
                    fullKey = DeviceKey.GetPublicKeyFromDir(instanceDir);
                }
                if (fullKey == null) {
                    DeviceKey.CreateKeyPairInDir(instanceDir);
                    fullKey = DeviceKey.GetPublicKeyFromDir(instanceDir);
                }

                String masked = DeviceKey.GetMaskedKey(fullKey);
                result.add(new InstanceInfo(id, name, isCurrent, masked, fullKey));
            }
        } catch (Exception ignored) {}

        if (result.isEmpty()) {
            result.add(new InstanceInfo("slot_1", "Slot 1", true, "N/A", null));
        }

        return result;
    }

    public static synchronized boolean switchInstance(Context context, String targetInstanceId) {
        if (context == null || targetInstanceId == null) return false;
        String currentActive = getActiveInstanceId(context);
        if (currentActive.equals(targetInstanceId)) return true;

        File filesDir = context.getFilesDir();
        File currentDir = getInstanceDirectory(context, currentActive);
        saveActiveToInstance(filesDir, currentDir);

        clearActiveData(filesDir);

        File targetDir = getInstanceDirectory(context, targetInstanceId);
        restoreInstanceToActive(targetDir, filesDir);

        File targetPubKey = new File(filesDir, "device_public.key");
        if (!targetPubKey.exists()) {
            DeviceKey.CreateKeyPairInDir(filesDir);
            saveActiveToInstance(filesDir, targetDir);
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACTIVE_ID, targetInstanceId)
            .apply();

        DeviceKey.setContext(context);
        return true;
    }

    public static synchronized InstanceInfo createInstance(Context context, String name) {
        if (context == null) return null;
        String id = "slot_" + System.currentTimeMillis();
        String finalName = (name != null && !name.trim().isEmpty()) ? name.trim() : "Slot " + (getInstances(context).size() + 1);

        File instanceDir = getInstanceDirectory(context, id);
        DeviceKey.CreateKeyPairInDir(instanceDir);
        String fullKey = DeviceKey.GetPublicKeyFromDir(instanceDir);
        String masked = DeviceKey.GetMaskedKey(fullKey);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_INSTANCES_JSON, "[]");
        try {
            JSONArray arr = new JSONArray(jsonStr);
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("name", finalName);
            arr.put(obj);
            prefs.edit().putString(KEY_INSTANCES_JSON, arr.toString()).apply();
        } catch (Exception ignored) {}

        return new InstanceInfo(id, finalName, false, masked, fullKey);
    }

    public static synchronized boolean renameInstance(Context context, String instanceId, String newName) {
        if (context == null || instanceId == null || newName == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_INSTANCES_JSON, "[]");
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.getString("id").equals(instanceId)) {
                    obj.put("name", newName.trim());
                    prefs.edit().putString(KEY_INSTANCES_JSON, arr.toString()).apply();
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static synchronized boolean deleteInstance(Context context, String instanceId) {
        if (context == null || instanceId == null) return false;
        String activeId = getActiveInstanceId(context);
        if (activeId.equals(instanceId)) return false;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_INSTANCES_JSON, "[]");
        try {
            JSONArray arr = new JSONArray(jsonStr);
            if (arr.length() <= 1) return false;

            JSONArray updated = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (!obj.getString("id").equals(instanceId)) {
                    updated.put(obj);
                }
            }
            prefs.edit().putString(KEY_INSTANCES_JSON, updated.toString()).apply();

            File instanceDir = new File(getInstancesRoot(context), instanceId);
            deleteRecursive(instanceDir);
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    public static synchronized String regenerateKeyForInstance(Context context, String instanceId) {
        if (context == null || instanceId == null) return null;
        String activeId = getActiveInstanceId(context);
        File instanceDir = getInstanceDirectory(context, instanceId);

        File privFile = new File(instanceDir, "device_private.key");
        File pubFile = new File(instanceDir, "device_public.key");
        if (privFile.exists()) privFile.delete();
        if (pubFile.exists()) pubFile.delete();

        DeviceKey.CreateKeyPairInDir(instanceDir);
        String newKey = DeviceKey.GetPublicKeyFromDir(instanceDir);

        if (activeId.equals(instanceId)) {
            File activePriv = new File(context.getFilesDir(), "device_private.key");
            File activePub = new File(context.getFilesDir(), "device_public.key");
            if (activePriv.exists()) activePriv.delete();
            if (activePub.exists()) activePub.delete();
            restoreInstanceToActive(instanceDir, context.getFilesDir());
            DeviceKey.setContext(context);
        }

        return DeviceKey.GetMaskedKey(newKey);
    }

    public static synchronized void saveActiveToCurrentInstance(Context context) {
        if (context == null) return;
        String activeId = getActiveInstanceId(context);
        File currentDir = getInstanceDirectory(context, activeId);
        saveActiveToInstance(context.getFilesDir(), currentDir);
    }

    private static void saveActiveToInstance(File filesDir, File targetDir) {
        if (filesDir == null || targetDir == null) return;
        if (!targetDir.exists()) targetDir.mkdirs();

        for (String fileName : INSTANCE_FILES) {
            File src = new File(filesDir, fileName);
            if (src.exists() && src.isFile()) {
                copyFile(src, new File(targetDir, fileName));
            }
        }
        for (String dirName : INSTANCE_DIRS) {
            File srcDir = new File(filesDir, dirName);
            if (srcDir.exists() && srcDir.isDirectory()) {
                copyDirectory(srcDir, new File(targetDir, dirName));
            }
        }
    }

    private static void restoreInstanceToActive(File sourceDir, File filesDir) {
        if (sourceDir == null || filesDir == null || !sourceDir.exists()) return;

        for (String fileName : INSTANCE_FILES) {
            File src = new File(sourceDir, fileName);
            if (src.exists() && src.isFile()) {
                copyFile(src, new File(filesDir, fileName));
            }
        }
        for (String dirName : INSTANCE_DIRS) {
            File srcDir = new File(sourceDir, dirName);
            if (srcDir.exists() && srcDir.isDirectory()) {
                copyDirectory(srcDir, new File(filesDir, dirName));
            }
        }
    }

    private static void clearActiveData(File filesDir) {
        if (filesDir == null || !filesDir.exists()) return;
        for (String fileName : INSTANCE_FILES) {
            File f = new File(filesDir, fileName);
            if (f.exists()) f.delete();
        }
        for (String dirName : INSTANCE_DIRS) {
            File d = new File(filesDir, dirName);
            if (d.exists() && d.isDirectory()) {
                deleteRecursive(d);
            }
        }
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
        } catch (IOException ignored) {}
    }

    private static void copyDirectory(File source, File destination) {
        if (source == null || !source.exists()) return;
        if (!destination.exists()) destination.mkdirs();
        File[] files = source.listFiles();
        if (files == null) return;
        for (File f : files) {
            File destChild = new File(destination, f.getName());
            if (f.isDirectory()) {
                copyDirectory(f, destChild);
            } else {
                copyFile(f, destChild);
            }
        }
    }

    private static void deleteRecursive(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }
}
