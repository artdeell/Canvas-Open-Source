package git.artdeell.skymodloader.updater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import git.artdeell.skymodloader.R;
import io.noties.markwon.Markwon;

public class UpdaterActivity extends Activity implements ServiceConnection {
    private Intent serviceStartIntent;
    private IUpdater service;
    private View updateControlButtons;
    private View skipButton;
    private View installButton;
    private ProgressBar loadingBar;
    private TextView updaterMessage;
    private TextView changelog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updater);
        updateControlButtons = findViewById(R.id.updater_update_ctl);
        skipButton = findViewById(R.id.updater_skip);
        installButton = findViewById(R.id.updater_install);
        loadingBar = findViewById(R.id.updater_progress);
        updaterMessage = findViewById(R.id.updater_messageView);
        changelog = findViewById(R.id.updater_changelog);
        serviceStartIntent = new Intent(this, UpdaterService.class);
        bindService(serviceStartIntent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        try {
            service.setUpdateListener(new IUpdaterConnection.Default());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(this);
        super.onDestroy();
    }

    public void onClickUpdate(View v) {
        try {
            service.downloadUpdate();
        }catch (RemoteException e) {
            remoteException(e);
        }
    }
    public void onClickSkipCancel(View v) {
        exit();
    }
    public void onClickInstall(View v) {
        tryInstall();
    }

    private void updateStateId() {
        try {
            switch (service.getServiceState()) {
                case UpdaterService.SERVICE_STATE_CHECKING:
                    updateControlButtons.setVisibility(View.GONE);
                    skipButton.setVisibility(View.VISIBLE);
                    updaterMessage.setText(R.string.updater_checking);
                    installButton.setVisibility(View.GONE);
                    break;
                case UpdaterService.SERVICE_STATE_UPDATE_AVAILABLE:
                    updateControlButtons.setVisibility(View.VISIBLE);
                    skipButton.setVisibility(View.GONE);
                    installButton.setVisibility(View.GONE);
                    updaterMessage.setText(R.string.updater_update_available);
                    Markwon markwon = Markwon.create(this);
                    markwon.setMarkdown(changelog, service.getUpdateChangelog());
                    break;
                case UpdaterService.SERVICE_STATE_DOWNLOADING:
                    skipButton.setVisibility(View.GONE);
                    updateControlButtons.setVisibility(View.GONE);
                    installButton.setVisibility(View.GONE);
                    updaterMessage.setText(R.string.updater_update_downloading);
                    break;
                case UpdaterService.SERVICE_STATE_DOWNLOAD_FINISHED:
                    skipButton.setVisibility(View.GONE);
                    updateControlButtons.setVisibility(View.GONE);
                    installButton.setVisibility(View.VISIBLE);
                    updaterMessage.setText(R.string.updater_update_downloaded);
                    tryInstall();
                    break;
                case UpdaterService.SERVICE_STATE_PROCEED:
                    finish();
                    service.suicide();
                    break;
                case UpdaterService.SERVICE_STATE_FAILURE:
                    skipButton.setVisibility(View.GONE);
                    updateControlButtons.setVisibility(View.GONE);
                    installButton.setVisibility(View.GONE);
                    Exception e = this.service.getException().exception;
                    stopService(serviceStartIntent);
                    AlertDialog.Builder bldr = new AlertDialog.Builder(this);
                    bldr.setTitle(R.string.updater_failed);
                    if (e instanceof IOException) {
                        bldr.setMessage(R.string.mod_ioe);
                    } else if (e instanceof JSONException) {
                        bldr.setMessage(R.string.updater_input_unreadable);
                    } else {
                        bldr.setMessage(R.string.mod_gf);
                    }
                    bldr.setPositiveButton(R.string.updater_retry, (d, v) -> recreate());
                    bldr.setNegativeButton(android.R.string.cancel, (d, v) -> exit());
                    bldr.setCancelable(false);
                    bldr.show();
                    break;
            }
        }catch (RemoteException e) {
            remoteException(e);
        }
    }

    private void exit() {
        finish();
        try {
            service.suicide();
        }catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("deprecation") // we still need to check if we have unknown install sources enabled
    private void tryInstall() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!getPackageManager().canRequestPackageInstalls()) startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))), 124);
            else tryInstallPackage();
        }else{
            try {
                if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1) startActivityForResult(new Intent(Settings.ACTION_SECURITY_SETTINGS), 124);
                else tryInstallPackage();
            }catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void tryInstallPackage() {
        Intent androidInstallerIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        androidInstallerIntent.setData(FileProvider.getUriForFile(this, "git.artdeell.skymodloader.updater", new File(getCacheDir(), "update.apk")));
        androidInstallerIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(androidInstallerIntent);
    }

    private void updateLoadingBar() {
        try {
            long maxdl = service.getDownloadMaximum();
            loadingBar.setIndeterminate(maxdl == -1);
            if(maxdl != -1) {
                loadingBar.setProgress((int) service.getDownloadCurrent());
                loadingBar.setMax((int) service.getDownloadMaximum());
            }
        }catch (RemoteException e) {
            remoteException(e);
        }
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.service = IUpdater.Stub.asInterface(service);
        try {
            this.service.setUpdateListener(new Connection());
        }catch (RemoteException e) {
            remoteException(e);
        }
        updateLoadingBar();
        updateStateId();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private class Connection extends IUpdaterConnection.Stub {

        @Override
        public void onStateChanged() {
            runOnUiThread(UpdaterActivity.this::updateStateId);
        }

        @Override
        public void onProgressBarChanged() {
            runOnUiThread(UpdaterActivity.this::updateLoadingBar);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 124) {
            tryInstallPackage();
        }
    }
    private void remoteException(RemoteException e) {
        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setMessage(e.toString());
        bldr.setPositiveButton(android.R.string.ok, (d,w)->finish());
        bldr.setCancelable(false);
        bldr.show();
    }
}
