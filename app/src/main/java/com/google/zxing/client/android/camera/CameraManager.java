package com.google.zxing.client.android.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.client.android.camera.open.OpenCamera;
import com.google.zxing.client.android.camera.open.OpenCameraInterface;
import com.tgc.sky.ui.qrcodereaderview.SimpleLog;
import java.io.IOException;

/* loaded from: classes2.dex */
public final class CameraManager {
    private static final String TAG = "CameraManager";
    private AutoFocusManager autoFocusManager;
    private final CameraConfigurationManager configManager;
    private final Context context;
    private boolean initialized;
    private OpenCamera openCamera;
    private Camera.PreviewCallback previewCallback;
    private boolean previewing;
    private int displayOrientation = 0;
    private int requestedCameraId = -1;
    private long autofocusIntervalInMs = 5000;

    public CameraManager(Context context) {
        this.context = context;
        this.configManager = new CameraConfigurationManager(context);
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
        if (isOpen()) {
            this.openCamera.getCamera().setPreviewCallback(previewCallback);
        }
    }

    public void setDisplayOrientation(int i) {
        this.displayOrientation = i;
        if (isOpen()) {
            this.openCamera.getCamera().setDisplayOrientation(i);
        }
    }

    public void setAutofocusInterval(long j) {
        this.autofocusIntervalInMs = j;
        AutoFocusManager autoFocusManager = this.autoFocusManager;
        if (autoFocusManager != null) {
            autoFocusManager.setAutofocusInterval(j);
        }
    }

    public void forceAutoFocus() {
        AutoFocusManager autoFocusManager = this.autoFocusManager;
        if (autoFocusManager != null) {
            autoFocusManager.start();
        }
    }

    public Point getPreviewSize() {
        return this.configManager.getCameraResolution();
    }

    public synchronized void openDriver(SurfaceHolder surfaceHolder, int i, int i2) throws IOException {
        openDriverImp(i, i2).setPreviewDisplay(surfaceHolder);
    }

    public synchronized void openDriver(SurfaceTexture surfaceTexture, int i, int i2) throws IOException {
        openDriverImp(i, i2).setPreviewTexture(surfaceTexture);
    }

    public synchronized Camera openDriverImp(int i, int i2) throws IOException {
        Camera camera;
        OpenCamera openCamera = this.openCamera;
        if (!isOpen()) {
            openCamera = OpenCameraInterface.open(this.requestedCameraId);
            if (openCamera == null || openCamera.getCamera() == null) {
                throw new IOException("Camera.open() failed to return object from driver");
            }
            this.openCamera = openCamera;
        }
        openCamera.getCamera().setPreviewCallback(this.previewCallback);
        openCamera.getCamera().setDisplayOrientation(this.displayOrientation);
        if (!this.initialized) {
            this.initialized = true;
            this.configManager.initFromCameraParameters(openCamera, i, i2);
        }
        camera = openCamera.getCamera();
        Camera.Parameters parameters = camera.getParameters();
        String flatten = parameters == null ? null : parameters.flatten();
        try {
            this.configManager.setDesiredCameraParameters(openCamera, false);
        } catch (RuntimeException unused) {
            String str = TAG;
            SimpleLog.w(str, "Camera rejected parameters. Setting only minimal safe-mode parameters");
            SimpleLog.i(str, "Resetting to saved camera params: " + flatten);
            if (flatten != null) {
                Camera.Parameters parameters2 = camera.getParameters();
                parameters2.unflatten(flatten);
                try {
                    camera.setParameters(parameters2);
                    this.configManager.setDesiredCameraParameters(openCamera, true);
                } catch (RuntimeException unused2) {
                    SimpleLog.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
                }
            }
        }
        return camera;
    }

    public synchronized void setPreviewCameraId(int i) {
        this.requestedCameraId = i;
    }

    public int getPreviewCameraId() {
        return this.requestedCameraId;
    }

    public synchronized void setTorchEnabled(boolean z) {
        OpenCamera openCamera = this.openCamera;
        if (openCamera != null && z != this.configManager.getTorchState(openCamera.getCamera())) {
            AutoFocusManager autoFocusManager = this.autoFocusManager;
            boolean z2 = autoFocusManager != null;
            if (z2) {
                autoFocusManager.stop();
                this.autoFocusManager = null;
            }
            this.configManager.setTorchEnabled(openCamera.getCamera(), z);
            if (z2) {
                AutoFocusManager autoFocusManager2 = new AutoFocusManager(openCamera.getCamera());
                this.autoFocusManager = autoFocusManager2;
                autoFocusManager2.start();
            }
        }
    }

    public synchronized boolean isOpen() {
        OpenCamera openCamera = this.openCamera;
        if (openCamera != null) {
            return openCamera.getCamera() != null;
        }
        return false;
    }

    public synchronized void closeDriver() {
        if (isOpen()) {
            this.openCamera.getCamera().release();
            this.openCamera = null;
        }
    }

    public synchronized void startPreview() {
        OpenCamera openCamera = this.openCamera;
        if (openCamera != null && !this.previewing) {
            openCamera.getCamera().startPreview();
            this.previewing = true;
            AutoFocusManager autoFocusManager = new AutoFocusManager(openCamera.getCamera());
            this.autoFocusManager = autoFocusManager;
            autoFocusManager.setAutofocusInterval(this.autofocusIntervalInMs);
        }
    }

    public synchronized void stopPreview() {
        AutoFocusManager autoFocusManager = this.autoFocusManager;
        if (autoFocusManager != null) {
            autoFocusManager.stop();
            this.autoFocusManager = null;
        }
        OpenCamera openCamera = this.openCamera;
        if (openCamera != null && this.previewing) {
            openCamera.getCamera().stopPreview();
            this.previewing = false;
        }
    }

    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] bArr, int i, int i2) {
        return new PlanarYUVLuminanceSource(bArr, i, i2, 0, 0, i, i2, false);
    }
}
