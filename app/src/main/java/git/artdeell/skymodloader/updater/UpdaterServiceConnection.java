package git.artdeell.skymodloader.updater;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

public class UpdaterServiceConnection extends IUpdaterConnection.Stub implements ServiceConnection {
    private final Activity context;
    private IUpdater updater;

    public UpdaterServiceConnection(Activity context) {
        this.context = context;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            updater = IUpdater.Stub.asInterface(service);
            updater.setUpdateListener(this);
        }catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onStateChanged() {

        try {
            Log.i("ActivityManager", "onStateChanged " + updater.getServiceState());
            switch (updater.getServiceState()) {
                case UpdaterService.SERVICE_STATE_UPDATE_AVAILABLE:
                    Intent intent = new Intent(context, UpdaterActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    context.finish();
                    Process.killProcess(Process.myPid());
                    break;
                case UpdaterService.SERVICE_STATE_PROCEED:
                    updater.suicide();
                    break;

            }
        }catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressBarChanged() {

    }
}
