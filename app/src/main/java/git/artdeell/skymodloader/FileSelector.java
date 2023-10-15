package git.artdeell.skymodloader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import com.tgc.sky.GameActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class FileSelector implements GameActivity.OnActivityResultListener {
    private static final int SELECTING_FILE = 1214;
    private static final FileSelector selector = new FileSelector();
    private GameActivity gameActivity;
    private long callbackFunction = 0;
    private boolean isSaveMode;
    public static void setActivity(GameActivity activity) {
        activity.AddOnActivityResultListener(selector);
        selector.gameActivity = activity;
    }
    public static void unsetActivity() {
        selector.gameActivity = null;
    }

    public static boolean nselectFile(String mimeType, long callbackFunction, boolean save) {
        return selector.selectFile(mimeType, callbackFunction, save);
    }
    public boolean selectFile(String mimeType, long callbackFunction, boolean save) {
        if(this.callbackFunction != 0) return false;
        gameActivity.runOnUiThread(()->{
            this.callbackFunction = callbackFunction;
            this.isSaveMode = save;
            Intent i = new Intent( isSaveMode ? Intent.ACTION_CREATE_DOCUMENT : Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType(mimeType);
            gameActivity.startActivityForResult(i, SELECTING_FILE);
        });
        return true;
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        if(i == SELECTING_FILE) {
            int fd = -1;
            if(i2 == Activity.RESULT_OK) {
                Uri selectedFile = intent.getData();
                try {
                    ParcelFileDescriptor fileDescriptor = gameActivity.getContentResolver().openFileDescriptor(selectedFile, isSaveMode ? "w" : "r");
                    fd = fileDescriptor.detachFd();
                    fileDescriptor.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            final long fCallbackFunction = callbackFunction;
            final int fFd = fd;
            new Thread(()->{
                callbackFunctionCall(fCallbackFunction, fFd);
            }).start();
            callbackFunction = 0;
        }
    }
    public static native void callbackFunctionCall(long cb, int fd);
}
