package com.tgc.sky.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.Surface;
import com.google.zxing.Result;
import com.tgc.sky.GameActivity;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;


public class QRCameraHandler implements ImageReader.OnImageAvailableListener, GameActivity.OnActivityResultListener {
    private static final int kImageFormat = 256;
    private static final int kTargetHeight = 480;
    private static final int kTargetWidth = 640;
    private GameActivity m_activity;
    private Handler m_backgroundHandler;
    private HandlerThread m_backgroundThread;
    private CameraDevice m_cameraDevice;
    private CameraManager m_cameraManager;
    private CameraCaptureSession m_captureSession;
    private Context m_context;
    private CameraVideoFormat m_format;
    public HandlerCb m_handler;
    private int m_height;
    private volatile boolean m_imageAvailable;
    private volatile ByteBuffer m_imageBuffer;
    private ReentrantLock m_imageLock;
    private Intent m_imagePicker;
    private ImageReader m_imageReader;
    private volatile Mode m_mode;
    private volatile boolean m_permissionDenied;
    private volatile boolean m_requestingPermission;
    private volatile State m_state;
    private int m_width;

    /* loaded from: classes2.dex */
    public enum CameraVideoFormat {
        kCameraVideoFormat_Unknown,
        kCameraVideoFormat_BGRA_8888,
        kCameraVideoFormat_ARGB_8888
    }

    public interface HandlerCb {
        void run(String str, int i, boolean z);
    }

    public enum Mode {
        kMode_Idle,
        kMode_QRScanner,
        kMode_ImagePicker
    }

    public enum PermissionState {
        kPermissionState_None,
        kPermissionState_Requesting,
        kPermissionState_Granted,
        kPermissionState_Denied
    }

    public enum State {
        kState_Idle,
        kState_Starting,
        kState_Running,
        kState_Stopping
    }

    public boolean startBackgroundThread() {
        if (this.m_backgroundThread != null) {
            return false;
        }
        HandlerThread handlerThread = new HandlerThread("CameraBackgroundThread");
        this.m_backgroundThread = handlerThread;
        handlerThread.start();
        this.m_backgroundHandler = new Handler(this.m_backgroundThread.getLooper());
        return true;
    }

    private void stopBackgroundThread() {
        HandlerThread handlerThread = this.m_backgroundThread;
        if (handlerThread == null) {
            return;
        }
        handlerThread.quitSafely();
        try {
            this.m_backgroundThread.join();
            this.m_backgroundThread = null;
            this.m_backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public QRCameraHandler(Context context, HandlerCb handlerCb) {
        this.m_context = context;
        this.m_activity = (GameActivity) context;
        this.m_handler = handlerCb;
        Intent intent = new Intent("android.intent.action.PICK");
        this.m_imagePicker = intent;
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        this.m_cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.m_imageLock = new ReentrantLock();
        this.m_format = CameraVideoFormat.kCameraVideoFormat_Unknown;
        this.m_width = 0;
        this.m_height = 0;
        this.m_state = State.kState_Idle;
        this.m_mode = Mode.kMode_Idle;
    }

    public PermissionState getPermissionState() {
        if (this.m_requestingPermission) {
            return PermissionState.kPermissionState_Requesting;
        }
        int checkSelfPermission = this.m_context.checkSelfPermission("android.permission.CAMERA");
        if (checkSelfPermission != -1) {
            if (checkSelfPermission == 0) {
                return PermissionState.kPermissionState_Granted;
            }
            return PermissionState.kPermissionState_None;
        } else if (this.m_permissionDenied) {
            return PermissionState.kPermissionState_Denied;
        } else {
            return PermissionState.kPermissionState_None;
        }
    }

    private boolean hasCameraPermission() {
        return this.m_context.checkSelfPermission("android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission(final Runnable runnable) {
        this.m_requestingPermission = true;
        final GameActivity.PermissionCallback permissionCallback = new GameActivity.PermissionCallback() { // from class: com.tgc.sky.ui.QRCameraHandler$$ExternalSyntheticLambda2
            @Override // com.tgc.sky.GameActivity.PermissionCallback
            public final void onPermissionResult(String[] strArr, int[] iArr) {
                QRCameraHandler.this.m413lambda$requestCameraPermission$0$comtgcskyuiQRCameraHandler(runnable, strArr, iArr);
            }
        };
        if (this.m_permissionDenied) {
            this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.QRCameraHandler$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    QRCameraHandler.this.m414lambda$requestCameraPermission$1$comtgcskyuiQRCameraHandler(permissionCallback);
                }
            });
        } else {
            this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.QRCameraHandler$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    QRCameraHandler.this.m415lambda$requestCameraPermission$2$comtgcskyuiQRCameraHandler(permissionCallback);
                }
            });
        }
    }

    public /* synthetic */ void m413lambda$requestCameraPermission$0$comtgcskyuiQRCameraHandler(Runnable runnable, String[] strArr, int[] iArr) {
        this.m_requestingPermission = false;
        if (this.m_activity.checkResultPermissions(iArr)) {
            if (runnable != null) {
                runnable.run();
            }
        } else if (this.m_activity.shouldShowRequestPermissionsRationale(strArr)) {
        } else {
            this.m_permissionDenied = true;
        }
    }

    public /* synthetic */ void m414lambda$requestCameraPermission$1$comtgcskyuiQRCameraHandler(GameActivity.PermissionCallback permissionCallback) {
        this.m_activity.requestPermissionsThroughSettings(new String[]{"android.permission.CAMERA"}, permissionCallback);
    }

    public /* synthetic */ void m415lambda$requestCameraPermission$2$comtgcskyuiQRCameraHandler(GameActivity.PermissionCallback permissionCallback) {
        this.m_activity.requestPermissions(new String[]{"android.permission.CAMERA"}, permissionCallback);
    }

    public void requestPermissionAndStartCamera() {
        if (hasCameraPermission()) {
            m416x86a76924();
        } else {
            requestCameraPermission(new Runnable() { // from class: com.tgc.sky.ui.QRCameraHandler$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    QRCameraHandler.this.m416x86a76924();
                }
            });
        }
    }

    public void setState(State state) {
        synchronized (this) {
            this.m_state = state;
        }
    }

    public void m416x86a76924() {
        synchronized (this) {
            if (this.m_state != State.kState_Idle) {
                return;
            }
            setState(State.kState_Starting);
            this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.QRCameraHandler$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    QRCameraHandler.this.m417lambda$setupCamera$4$comtgcskyuiQRCameraHandler();
                }
            });
        }
    }

    public /* synthetic */ void m417lambda$setupCamera$4$comtgcskyuiQRCameraHandler() {
        String str;
        try {
            String[] cameraIdList = this.m_cameraManager.getCameraIdList();
            int length = cameraIdList.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    str = null;
                    break;
                }
                str = cameraIdList[i];
                Integer num = (Integer) this.m_cameraManager.getCameraCharacteristics(str).get(CameraCharacteristics.LENS_FACING);
                if (num != null && num.intValue() == 1) {
                    break;
                }
                i++;
            }
            if (str != null && this.m_context.checkSelfPermission("android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED) {
                this.m_format = CameraVideoFormat.kCameraVideoFormat_ARGB_8888;
                this.m_cameraManager.openCamera(str, new CameraDevice.StateCallback() { // from class: com.tgc.sky.ui.QRCameraHandler.1
                    @Override // android.hardware.camera2.CameraDevice.StateCallback
                    public void onOpened(CameraDevice cameraDevice) {
                        try {
                            QRCameraHandler.this.m_cameraDevice = cameraDevice;
                            QRCameraHandler.this.m_imageReader = ImageReader.newInstance(QRCameraHandler.kTargetWidth, QRCameraHandler.kTargetHeight, ImageFormat.JPEG, 2);
                            QRCameraHandler.this.m_imageReader.setOnImageAvailableListener(QRCameraHandler.this, null);
                            Surface surface = QRCameraHandler.this.m_imageReader.getSurface();
                            final CaptureRequest.Builder createCaptureRequest = QRCameraHandler.this.m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            createCaptureRequest.addTarget(surface);
                            QRCameraHandler.this.m_cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() { // from class: com.tgc.sky.ui.QRCameraHandler.1.1
                                @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
                                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                                    if (QRCameraHandler.this.m_cameraDevice != null) {
                                        QRCameraHandler.this.m_captureSession = cameraCaptureSession;
                                        try {
                                            QRCameraHandler.this.startBackgroundThread();
                                            QRCameraHandler.this.m_captureSession.setRepeatingRequest(createCaptureRequest.build(), null, QRCameraHandler.this.m_backgroundHandler);
                                        } catch (CameraAccessException e) {
                                            e.printStackTrace();
                                        }
                                        QRCameraHandler.this.setState(State.kState_Running);
                                        return;
                                    }
                                    QRCameraHandler.this.stopCamera();
                                }

                                @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
                                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                                    QRCameraHandler.this.stopCamera();
                                }
                            }, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override // android.hardware.camera2.CameraDevice.StateCallback
                    public void onDisconnected(CameraDevice cameraDevice) {
                        QRCameraHandler.this.stopCamera();
                    }

                    @Override // android.hardware.camera2.CameraDevice.StateCallback
                    public void onError(CameraDevice cameraDevice, int i2) {
                        QRCameraHandler.this.stopCamera();
                    }
                }, (Handler) null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopCamera() {
        setState(State.kState_Stopping);
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.QRCameraHandler$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                QRCameraHandler.this.m418lambda$stopCamera$5$comtgcskyuiQRCameraHandler();
            }
        });
    }

    public /* synthetic */ void m418lambda$stopCamera$5$comtgcskyuiQRCameraHandler() {
        stopBackgroundThread();
        CameraCaptureSession cameraCaptureSession = this.m_captureSession;
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            this.m_captureSession = null;
        }
        ImageReader imageReader = this.m_imageReader;
        if (imageReader != null) {
            imageReader.close();
            this.m_imageReader = null;
        }
        CameraDevice cameraDevice = this.m_cameraDevice;
        if (cameraDevice != null) {
            cameraDevice.close();
            this.m_cameraDevice = null;
        }
        setImageData(null, 0, 0);
        setState(State.kState_Idle);
        synchronized (this) {
            this.m_imageAvailable = false;
            if (this.m_mode == Mode.kMode_QRScanner) {
                this.m_handler.run(null, 0, true);
                this.m_mode = Mode.kMode_Idle;
            }
        }
    }

    public boolean isRunning() {
        boolean z;
        synchronized (this) {
            z = this.m_state == State.kState_Running && this.m_imageAvailable;
        }
        return z;
    }

    public CameraVideoFormat getFormat() {
        return this.m_format;
    }

    public void lock() {
        this.m_imageLock.lock();
    }

    public int getWidth() {
        return this.m_width;
    }

    public int getHeight() {
        return this.m_height;
    }

    public ByteBuffer getImageBuffer() {
        return this.m_imageBuffer;
    }

    public void unlock() {
        this.m_imageLock.unlock();
    }

    private void setImageData(ByteBuffer byteBuffer, int i, int i2) {
        this.m_imageLock.lock();
        this.m_width = i;
        this.m_height = i2;
        this.m_imageBuffer = byteBuffer;
        this.m_imageLock.unlock();
    }

    public boolean startListeningForQRScanEvent() {
        synchronized (this) {
            if (this.m_mode != Mode.kMode_Idle) {
                return false;
            }
            this.m_mode = Mode.kMode_QRScanner;
            return true;
        }
    }

    public void stopListeningForQRScanEvent() {
        synchronized (this) {
            if (this.m_mode != Mode.kMode_QRScanner) {
                return;
            }
            this.m_mode = Mode.kMode_Idle;
        }
    }

    public boolean showQRImagePicker() {
        synchronized (this) {
            if (this.m_mode != Mode.kMode_Idle) {
                return false;
            }
            this.m_mode = Mode.kMode_ImagePicker;
            this.m_activity.AddOnActivityResultListener(this);
            this.m_activity.startActivityForResult(this.m_imagePicker, 130);
            return true;
        }
    }

    int getDeviceRotationAngle() {
        int rotation = this.m_activity.getWindowManager().getDefaultDisplay().getRotation();
        if (rotation != 1) {
            if (rotation != 2) {
                return rotation != 3 ? 0 : 90;
            }
            return 180;
        }
        return -90;
    }

    Bitmap decodeByteArrayToDimension(byte[] bArr, int i, int i2, int i3) {
        int deviceRotationAngle = getDeviceRotationAngle();
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, i, null);
        Matrix matrix = new Matrix();
        matrix.setRotate(deviceRotationAngle + 90);
        Bitmap createBitmap = Bitmap.createBitmap(decodeByteArray, 0, 0, i2, i3, matrix, false);
        decodeByteArray.recycle();
        return createBitmap;
    }

    @Override // android.media.ImageReader.OnImageAvailableListener
    public void onImageAvailable(ImageReader imageReader) {
        String scanBitmap;
        synchronized (this) {
            if (this.m_state != State.kState_Running) {
                return;
            }
            Image acquireLatestImage = imageReader.acquireLatestImage();
            if (acquireLatestImage == null) {
                return;
            }
            if (acquireLatestImage.getFormat() != 256) {
                acquireLatestImage.close();
                return;
            }
            int width = acquireLatestImage.getWidth();
            int height = acquireLatestImage.getHeight();
            ByteBuffer buffer = acquireLatestImage.getPlanes()[0].getBuffer();
            buffer.rewind();
            int capacity = buffer.capacity();
            byte[] bArr = new byte[capacity];
            buffer.get(bArr);
            acquireLatestImage.close();
            Bitmap decodeByteArrayToDimension = decodeByteArrayToDimension(bArr, capacity, width, height);
            if (decodeByteArrayToDimension.getConfig() == Bitmap.Config.ARGB_8888) {
                ByteBuffer allocate = ByteBuffer.allocate(decodeByteArrayToDimension.getByteCount());
                decodeByteArrayToDimension.copyPixelsToBuffer(allocate);
                allocate.flip();
                setImageData(allocate, decodeByteArrayToDimension.getWidth(), decodeByteArrayToDimension.getHeight());
                synchronized (this) {
                    this.m_imageAvailable = true;
                }
            }
            synchronized (this) {
                if (this.m_mode == Mode.kMode_QRScanner && (scanBitmap = scanBitmap(decodeByteArrayToDimension)) != null) {
                    this.m_handler.run(scanBitmap, 0, true);
                    this.m_mode = Mode.kMode_Idle;
                }
            }
            decodeByteArrayToDimension.recycle();
        }
    }

    private boolean isSupportedToken(String str) {
        return str.startsWith("https://sky") && str.indexOf(".thatg.co/?") >= 0;
    }

    private String scanBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Result zxingResult = ZXingUtils.decodeQRBitmap(bitmap);
        if(zxingResult == null) return null;
        if(zxingResult.getText() == null) return null;
        return zxingResult.getText();
    }

    @Override // com.tgc.sky.GameActivity.OnActivityResultListener
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 130) {
            scanBitmapFromIntent(i, i2, intent);
            this.m_activity.RemoveOnActivityResultListeners(this);
        }
    }

    public void scanBitmapFromIntent(int i, int i2, Intent intent) {
        String str = null;
        if (intent != null) {
            try {
                try {
                    ContentResolver contentResolver = this.m_activity.getContentResolver();
                    Uri data = intent.getData();
                    if (data != null) {
                        str = scanBitmap(BitmapFactory.decodeStream(contentResolver.openInputStream(data)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    synchronized (this) {
                        this.m_handler.run(null, 0, true);
                        this.m_mode = Mode.kMode_Idle;
                        return;
                    }
                }
            } catch (Throwable th) {
                synchronized (this) {
                    this.m_handler.run(null, 0, true);
                    this.m_mode = Mode.kMode_Idle;
                    throw th;
                }
            }
        }
        synchronized (this) {
            this.m_handler.run(str, 0, true);
            this.m_mode = Mode.kMode_Idle;
        }
    }
}
