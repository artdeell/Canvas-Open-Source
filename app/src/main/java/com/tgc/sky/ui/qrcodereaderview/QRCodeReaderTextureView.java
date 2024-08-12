package com.tgc.sky.ui.qrcodereaderview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.WindowManager;

import com.google.zxing.client.android.camera.CameraManager;
import com.tgc.sky.QrScanner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class QRCodeReaderTextureView extends TextureView implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {
    private static final String TAG = "com.tgc.sky.ui.qrcodereaderview.QRCodeReaderTextureView";
    private CameraManager mCameraManager;
    public OnQRCodeReadListener mOnQRCodeReadListener;
    private int mPreviewHeight;
    private int mPreviewWidth;
    private QrScanner mScanner;

    public interface OnQRCodeReadListener {
        void onBeginDetect();

        boolean onQRCodeRead(String str);
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    public QRCodeReaderTextureView(Context context) {
        this(context, null);
    }

    public QRCodeReaderTextureView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (isInEditMode()) {
            return;
        }
        if (checkCameraHardware()) {
            CameraManager cameraManager = new CameraManager(getContext());
            this.mCameraManager = cameraManager;
            cameraManager.setPreviewCallback(this);
            setSurfaceTextureListener(this);
            setBackCamera();
            return;
        }
        throw new RuntimeException("Error: Camera not found");
    }

    public void setOnQRCodeReadListener(OnQRCodeReadListener onQRCodeReadListener) {
        this.mOnQRCodeReadListener = onQRCodeReadListener;
    }

    public void setLoggingEnabled(boolean z) {
        SimpleLog.setLoggingEnabled(z);
    }

    public void startCamera() {
        this.mCameraManager.startPreview();
    }

    public void stopCamera() {
        this.mCameraManager.stopPreview();
    }

    public void setAutofocusInterval(long j) {
        CameraManager cameraManager = this.mCameraManager;
        if (cameraManager != null) {
            cameraManager.setAutofocusInterval(j);
        }
    }

    public void forceAutoFocus() {
        CameraManager cameraManager = this.mCameraManager;
        if (cameraManager != null) {
            cameraManager.forceAutoFocus();
        }
    }

    public void setTorchEnabled(boolean z) {
        CameraManager cameraManager = this.mCameraManager;
        if (cameraManager != null) {
            cameraManager.setTorchEnabled(z);
        }
    }

    public void setPreviewCameraId(int i) {
        this.mCameraManager.setPreviewCameraId(i);
    }

    public void setBackCamera() {
        setPreviewCameraId(0);
    }

    public void setFrontCamera() {
        setPreviewCameraId(1);
    }

    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        SimpleLog.d(TAG, "surfaceCreated");
        try {
            this.mCameraManager.openDriver(surfaceTexture, getWidth(), getHeight());
        } catch (IOException | RuntimeException e) {
            SimpleLog.w(TAG, "Can not openDriver: " + e.getMessage());
            this.mCameraManager.closeDriver();
        }
        try {
            this.mScanner = new QrScanner();
            this.mCameraManager.startPreview();
        } catch (Exception e2) {
            SimpleLog.e(TAG, "Exception: " + e2.getMessage());
            this.mCameraManager.closeDriver();
        }
        onSurfaceTextureSizeChanged(surfaceTexture, i, i2);
        Matrix matrix = new Matrix();
        float f = (i / i2) / (this.mPreviewWidth / this.mPreviewHeight);
        float f2 = 1.0f;
        if (f > 1.0f) {
            f2 = f;
            f = 1.0f;
        }
        matrix.setScale(f, f2);
        setTransform(matrix);
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        String str = TAG;
        SimpleLog.d(str, "surfaceChanged");
        if (this.mCameraManager.getPreviewSize() == null) {
            SimpleLog.e(str, "Error: preview size does not exist");
            return;
        }
        this.mPreviewWidth = this.mCameraManager.getPreviewSize().x;
        this.mPreviewHeight = this.mCameraManager.getPreviewSize().y;
        this.mCameraManager.stopPreview();
        this.mCameraManager.setPreviewCallback(this);
        this.mCameraManager.setDisplayOrientation(getCameraDisplayOrientation());
        this.mCameraManager.startPreview();
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        SimpleLog.d(TAG, "surfaceDestroyed");
        this.mCameraManager.setPreviewCallback(null);
        this.mCameraManager.stopPreview();
        this.mCameraManager.closeDriver();
        return true;
    }

    public void transformRect(RectF rectF) {
        Point previewSize = this.mCameraManager.getPreviewSize();
        rectF.left /= previewSize.x;
        rectF.left -= 0.5f;
        rectF.right /= previewSize.x;
        rectF.right -= 0.5f;
        rectF.top /= previewSize.y;
        rectF.top -= 0.5f;
        rectF.bottom /= previewSize.y;
        rectF.bottom -= 0.5f;
        Matrix matrix = new Matrix();
        matrix.setRotate(getCameraDisplayOrientation());
        matrix.mapRect(rectF);
        rectF.left += 0.5f;
        rectF.right += 0.5f;
        rectF.top += 0.5f;
        rectF.bottom += 0.5f;
        getTransform(null).mapRect(rectF);
        rectF.left *= getWidth();
        rectF.right *= getWidth();
        rectF.top *= getHeight();
        rectF.bottom *= getHeight();
    }

    @Override // android.hardware.Camera.PreviewCallback
    public void onPreviewFrame(byte[] bArr, Camera camera) {
        if (this.mScanner.isOperational()) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                int i = parameters.getPreviewSize().width;
                int i2 = parameters.getPreviewSize().height;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                new YuvImage(bArr, parameters.getPreviewFormat(), i, i2, null).compressToJpeg(new Rect(0, 0, i, i2), 90, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                Bitmap decodeByteArray = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                this.mOnQRCodeReadListener.onBeginDetect();
                this.mScanner.scan(this, decodeByteArray);
            } catch (IOException unused) {
                Log.w("QRCodeReaderTextureView", "Error creating bitmap");
            }
        }
    }

    private boolean checkCameraHardware() {
        return getContext().getPackageManager().hasSystemFeature("android.hardware.camera") || getContext().getPackageManager().hasSystemFeature("android.hardware.camera.front") || getContext().getPackageManager().hasSystemFeature("android.hardware.camera.any");
    }

    private int getCameraDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(this.mCameraManager.getPreviewCameraId(), cameraInfo);
        int rotation = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRotation();
        int i = 0;
        if (rotation != 0) {
            if (rotation == 1) {
                i = 90;
            } else if (rotation == 2) {
                i = 180;
            } else if (rotation == 3) {
                i = 270;
            }
        }
        if (cameraInfo.facing == 1) {
            return (360 - ((cameraInfo.orientation + i) % 360)) % 360;
        }
        return ((cameraInfo.orientation - i) + 360) % 360;
    }
}
