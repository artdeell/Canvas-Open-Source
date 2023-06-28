package git.artdeell.skymodloader.updater;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import git.artdeell.skymodloader.BuildConfig;

public class UpdaterService extends Service {
    public static final byte SERVICE_STATE_CHECKING = 0;
    public static final byte SERVICE_STATE_UPDATE_AVAILABLE = 1;
    public static final byte SERVICE_STATE_DOWNLOADING = 2;
    public static final byte SERVICE_STATE_FAILURE = 3;
    public static final byte SERVICE_STATE_PROCEED = 4;
    public static final byte SERVICE_STATE_DOWNLOAD_FINISHED = 5;
    private final AtomicLong currentDownload = new AtomicLong(0);
    private IUpdaterConnection updaterListener = new IUpdaterConnection.Default();
    private Exception exception;
    private String updateChangelog;
    private String updateURL;
    private File downloadTarget;
    private byte serviceState = SERVICE_STATE_CHECKING;
    volatile private long maximumDownload = -1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IUpdater.Stub() {
            @Override
            public void setUpdateListener(IUpdaterConnection listener) {
                updaterListener = listener;
                Log.i("ActivityManager", "Set listener:"+listener);
                try {
                    listener.onStateChanged();
                    listener.onProgressBarChanged();
                }catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            public byte getServiceState() {
                return serviceState;
            }
            public String getUpdateChangelog() {
                return updateChangelog;
            }
            public void downloadUpdate() {
                new Thread(UpdaterService.this::downloadUpdate0).start();
            }
            public long getDownloadMaximum() {
                return maximumDownload;
            }
            public long getDownloadCurrent() {
                return currentDownload.get();
            }
            public ParcelableException getException() {
                return new ParcelableException(exception);
            }
            public void suicide() {
                Log.i("ActivityManager", "Suicide! Yay!");
                UpdaterService.this.stopSelf();
                Process.killProcess(Process.myPid());
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        downloadTarget = new File(getCacheDir(), "update.apk");
        new Thread(UpdaterService.this::findUpdates).start();
    }

    public void onDestroy() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void downloadUpdate0() {
        serviceState = SERVICE_STATE_DOWNLOADING;
        try {updaterListener.onStateChanged(); }catch (RemoteException _e) {_e.printStackTrace();}
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(updateURL).openConnection();
            conn.connect();
            maximumDownload = conn.getContentLength();
            InputStream is = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(downloadTarget);
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
                if (maximumDownload != -1) {
                    currentDownload.addAndGet(len);
                    updaterListener.onProgressBarChanged();
                }
            }
            serviceState = SERVICE_STATE_DOWNLOAD_FINISHED;
            updaterListener.onStateChanged();
        } catch (IOException e) {
            serviceState = SERVICE_STATE_FAILURE;
            exception = e;
            try {updaterListener.onStateChanged(); }catch (RemoteException _e) {_e.printStackTrace();}
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private JSONObject pullUpdateInfo() throws IOException, JSONException {
        InputStream inputStream = new URL("https://api.github.com/repos/lukas0x1/SML-filehost/releases/latest").openStream();
        return new JSONObject(new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n")));
    }
    private String getAssetURL(JSONObject release) throws JSONException {
        if(!release.has("assets")) return null;
        JSONArray assets = release.getJSONArray("assets");
        for(int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.getJSONObject(i);
            if(asset.getString("name").equals("Canvas.apk")) return asset.getString("browser_download_url");
        }
        return null;
    }

    private void escapeUpdater() {
        serviceState = SERVICE_STATE_PROCEED;
        maximumDownload = 0;
        try {
            updaterListener.onStateChanged();
            updaterListener.onProgressBarChanged();
        }catch (RemoteException e) {e.printStackTrace();}
    }

    public void findUpdates() {
        if(downloadTarget.exists()) downloadTarget.delete();
        try {
            JSONObject updateInfo = pullUpdateInfo();
            String name;
            if ((name = updateInfo.getString("name")).startsWith("[") && (updateURL = getAssetURL(updateInfo)) != null) {
                int versionCode = Integer.parseInt(name.substring(name.indexOf('[')+1, name.indexOf(']')));
                if (versionCode > BuildConfig.VERSION_CODE) {
                    updateChangelog = updateInfo.getString("body");
                    serviceState = SERVICE_STATE_UPDATE_AVAILABLE;
                    maximumDownload = 0;
                    updaterListener.onStateChanged();
                    updaterListener.onProgressBarChanged();
                    return;
                }
            }
            escapeUpdater();
        }catch (Exception e) {
            e.printStackTrace();
            serviceState = SERVICE_STATE_FAILURE;
            exception = e;
            maximumDownload = 0;
            try {
                updaterListener.onStateChanged();
                updaterListener.onProgressBarChanged();
            }catch (RemoteException _e) { _e.printStackTrace(); }
        }
    }
}
