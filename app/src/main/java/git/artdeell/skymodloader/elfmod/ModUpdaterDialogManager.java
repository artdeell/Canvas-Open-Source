package git.artdeell.skymodloader.elfmod;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import git.artdeell.skymodloader.DialogY;
import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.updater.AbstractUpdaterService;
import git.artdeell.skymodloader.updater.IUpdater;
import git.artdeell.skymodloader.updater.IUpdaterConnection;
import io.noties.markwon.Markwon;

public class ModUpdaterDialogManager extends IUpdaterConnection.Stub implements ServiceConnection, View.OnClickListener {
    private final Handler mUiThreadHandler = new Handler(Looper.getMainLooper());
    private final DialogY mDialog;
    private ElfUIBackbone mLoader;
    private final Markwon mMarkwon;
    private IUpdater mUpdater;
    private boolean mHasShownChangelog;
    private byte mState;
    private boolean mConnected;
    public ModUpdaterDialogManager(Activity activity) {
        mDialog = DialogY.createFromActivity(activity);
        mDialog.dialog.setCancelable(false);
        mDialog.positiveButton.setOnClickListener(this);
        mDialog.negativeButton.setOnClickListener(this);
        mMarkwon = Markwon.create(activity);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mUpdater = IUpdater.Stub.asInterface(iBinder);
        mHasShownChangelog = false;
        try {
            mUpdater.setUpdateListener(this);
            mDialog.dialog.show();
            mConnected = true;
        } catch (RemoteException e) {
            Log.i("MUDM", "Failed to set Binder callbacks", e);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mConnected = false;
    }

    public boolean isConnected() {
        return mConnected;
    }

    private String getChangelog() {
        try {
            return mUpdater.getUpdateChangelog();
        }catch (RemoteException e) {return null;}
    }
    private Exception getException() {
        try {
            return mUpdater.getException().exception;
        }catch (RemoteException e) {return e;}
    }

    private String printException(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }

    private void showChangelog() {
        if(mHasShownChangelog) return;
        String changelog = getChangelog();
        if(changelog != null) {
            mMarkwon.setMarkdown(mDialog.content, changelog);
            mDialog.content.setVisibility(View.VISIBLE);
            mHasShownChangelog = true;
        }else {
            mDialog.content.setVisibility(View.GONE);
            mHasShownChangelog = false;
        }
    }

    private void showException() {
        mHasShownChangelog = false;
        mDialog.title.setText(R.string.mod_check_updates_error);
        Exception e = getException();
        String exceptionText = "<null>";
        if(e != null) exceptionText = printException(e);
        mDialog.content.setText(exceptionText);
        mDialog.content.setVisibility(View.VISIBLE);
        setVisibilities(false, true, false);
    }

    private void updateState(byte newState) {
        mState = newState;
        if(newState != AbstractUpdaterService.SERVICE_STATE_FAILURE) {
            showChangelog();
        }else {
            showException();
            return;
        }
        switch (newState) {
            case AbstractUpdaterService.SERVICE_STATE_CHECKING:
                mDialog.title.setText(R.string.mod_check_updates_checking);
                mDialog.setProgressIndeterminate(true);
                setVisibilities(true, false, true);
                break;
            case AbstractUpdaterService.SERVICE_STATE_UPDATE_AVAILABLE:
                mDialog.title.setText(R.string.mod_check_updates_found);
                mDialog.positiveButton.setText(R.string.updater_install);
                setVisibilities(false, true, true);
                break;
            case AbstractUpdaterService.SERVICE_STATE_DOWNLOADING:
                mDialog.title.setText(R.string.mod_check_updates_downloading);
                mDialog.setProgressIndeterminate(false);
                setVisibilities(true, false, true);
                break;
            case AbstractUpdaterService.SERVICE_STATE_INSTALLING:
                mDialog.title.setText(R.string.mod_check_updates_installing);
                mDialog.setProgressIndeterminate(false);
                setVisibilities(true, false, true);
                break;
            case AbstractUpdaterService.SERVICE_STATE_INSTALL_FINISHED:
                mDialog.title.setText(R.string.mod_check_updates_complete);
                mDialog.positiveButton.setText(android.R.string.ok);
                setVisibilities(false, true, false);
                break;
            case AbstractUpdaterService.SERVICE_STATE_PROCEED:
                mDialog.title.setText(R.string.mod_check_updates_not_found);
                mDialog.positiveButton.setText(android.R.string.ok);
                setVisibilities(false, true, false);
                break;
        }
    }

    private void setVisibilities(boolean progressBar, boolean positiveButton, boolean negativeButton) {
        mDialog.positiveButton.setVisibility(positiveButton ? View.VISIBLE : View.GONE);
        mDialog.negativeButton.setVisibility(negativeButton ? View.VISIBLE : View.GONE);
        mDialog.setProgressVisibility(progressBar);
    }

    private void updateProgressBar(long curr, long max) {
        if(max == -1) {
            mDialog.setProgressIndeterminate(true);
        }else {
            mDialog.setProgressMax(max);
            mDialog.setProgress(curr);
            mDialog.setProgressIndeterminate(false);
        }
    }

    private void onClickedPositive() {
        switch(mState) {
            case AbstractUpdaterService.SERVICE_STATE_UPDATE_AVAILABLE:
                downloadUpdate();
                break;
            case AbstractUpdaterService.SERVICE_STATE_INSTALL_FINISHED:
                updateModInfo();
            default:
                // In all cases other than SERVICE_STATE_UPDATE_AVAILABLE when the positive button is shown,
                // we want to hide ourselves and kill the service.
                shutDown();
        }
    }

    private void updateModInfo() {
        File filesDir = mDialog.content.getContext().getFilesDir();
        mLoader.startLoadingAsync(new File(filesDir, "mods"));
    }

    private void downloadUpdate() {
        try {
            mUpdater.downloadUpdate();
        }catch (RemoteException e) {
            Log.e("MUDM", "Failed to send update command", e);
        }
    }

    private void shutDown() {
        mDialog.dialog.dismiss();
        try {
            mUpdater.suicide();
        }catch (DeadObjectException e) {
            Log.i("MUDM", "Got nominal exception after service suicide.");
        } catch (RemoteException e) {
            Log.w("MUDM", "Failed to shut down service", e);
        }
    }

    private void onClickedNegative() {
        shutDown();
    }

    @Override
    public void onProgressBarChanged() throws RemoteException {
        long curr = mUpdater.getDownloadCurrent();
        long max = mUpdater.getDownloadMaximum();
        mUiThreadHandler.post(()->updateProgressBar(curr, max));
    }

    @Override
    public void onStateChanged() throws RemoteException {
        byte state = mUpdater.getServiceState();
        mUiThreadHandler.post(()->updateState(state));
    }

    @Override
    public void onClick(View view) {
        if(view.equals(mDialog.positiveButton)) {
            onClickedPositive();
        }else if(view.equals(mDialog.negativeButton)){
            onClickedNegative();
        }
    }

    public void setLoader(ElfUIBackbone loader) {
        mLoader = loader;
    }
}
