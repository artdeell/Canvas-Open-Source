package com.google.zxing.client.android.camera;

import android.hardware.Camera;
import android.os.AsyncTask;
import com.tgc.sky.ui.qrcodereaderview.SimpleLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes2.dex */
public final class AutoFocusManager implements Camera.AutoFocusCallback {
    protected static final long DEFAULT_AUTO_FOCUS_INTERVAL_MS = 5000;
    private static final Collection<String> FOCUS_MODES_CALLING_AF;
    private static final String TAG = "AutoFocusManager";
    private long autofocusIntervalMs = 5000;
    private final Camera camera;
    private boolean focusing;
    private AsyncTask<?, ?, ?> outstandingTask;
    private boolean stopped;
    private final boolean useAutoFocus;

    static {
        ArrayList arrayList = new ArrayList(2);
        FOCUS_MODES_CALLING_AF = arrayList;
        arrayList.add("auto");
        arrayList.add("macro");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AutoFocusManager(Camera camera) {
        this.camera = camera;
        String focusMode = camera.getParameters().getFocusMode();
        boolean contains = FOCUS_MODES_CALLING_AF.contains(focusMode);
        this.useAutoFocus = contains;
        SimpleLog.i(TAG, "Current focus mode '" + focusMode + "'; use auto focus? " + contains);
        start();
    }

    @Override // android.hardware.Camera.AutoFocusCallback
    public synchronized void onAutoFocus(boolean z, Camera camera) {
        this.focusing = false;
        autoFocusAgainLater();
    }

    public void setAutofocusInterval(long j) {
        if (j <= 0) {
            throw new IllegalArgumentException("AutoFocusInterval must be greater than 0.");
        }
        this.autofocusIntervalMs = j;
    }

    private synchronized void autoFocusAgainLater() {
        if (!this.stopped && this.outstandingTask == null) {
            AutoFocusTask autoFocusTask = new AutoFocusTask();
            try {
                autoFocusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[0]);
                this.outstandingTask = autoFocusTask;
            } catch (RejectedExecutionException e) {
                SimpleLog.w(TAG, "Could not request auto focus", e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void start() {
        if (this.useAutoFocus) {
            this.outstandingTask = null;
            if (!this.stopped && !this.focusing) {
                try {
                    this.camera.autoFocus(this);
                    this.focusing = true;
                } catch (RuntimeException e) {
                    SimpleLog.w(TAG, "Unexpected exception while focusing", e);
                    autoFocusAgainLater();
                }
            }
        }
    }

    private synchronized void cancelOutstandingTask() {
        AsyncTask<?, ?, ?> asyncTask = this.outstandingTask;
        if (asyncTask != null) {
            if (asyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                this.outstandingTask.cancel(true);
            }
            this.outstandingTask = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void stop() {
        this.stopped = true;
        if (this.useAutoFocus) {
            cancelOutstandingTask();
            try {
                this.camera.cancelAutoFocus();
            } catch (RuntimeException e) {
                SimpleLog.w(TAG, "Unexpected exception while cancelling focusing", e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes2.dex */
    public final class AutoFocusTask extends AsyncTask<Object, Object, Object> {
        private AutoFocusTask() {
        }

        @Override // android.os.AsyncTask
        protected Object doInBackground(Object... objArr) {
            try {
                Thread.sleep(AutoFocusManager.this.autofocusIntervalMs);
            } catch (InterruptedException unused) {
            }
            AutoFocusManager.this.start();
            return null;
        }
    }
}
