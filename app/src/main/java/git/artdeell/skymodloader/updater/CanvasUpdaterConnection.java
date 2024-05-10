package git.artdeell.skymodloader.updater;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class CanvasUpdaterConnection extends IUpdaterConnection.Stub implements ServiceConnection {
    private final Activity context;
    private IUpdater updater;

    public CanvasUpdaterConnection(Activity context) {
        this.context = context;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            updater = IUpdater.Stub.asInterface(service);
            updater.setUpdateListener(this);
        }catch (RemoteException e) {
            Log.i("CanvasUpdaterConnection", "Failed to connect self to updater service", e);
            context.unbindService(this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onStateChanged() throws RemoteException {
        switch (updater.getServiceState()) {
            // Added unbinds, because without them my phone keeps
            // restarting the service automatically after SERVICE_STATE_PROCEED
            case AbstractUpdaterService.SERVICE_STATE_UPDATE_AVAILABLE:
                context.unbindService(this);
                Intent intent = new Intent(context, CanvasUpdaterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                break;
            case AbstractUpdaterService.SERVICE_STATE_PROCEED:
                context.unbindService(this);
                shutDownService();
                break;
        }
    }

    private void shutDownService() throws RemoteException{
        try {
            updater.suicide();
        }catch (DeadObjectException e) {
            Log.i("UpdaterConnection", "Received expected DeadObjectException on service shutdown");
        }
    }

    @Override
    public void onProgressBarChanged() {

    }
}
