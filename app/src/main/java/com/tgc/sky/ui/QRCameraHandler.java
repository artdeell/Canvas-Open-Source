package com.tgc.sky.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
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
import android.util.SparseArray;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.tgc.sky.GameActivity;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

/* loaded from: classes2.dex */
public class QRCameraHandler implements ImageReader.OnImageAvailableListener, GameActivity.OnActivityResultListener {
    private static final int s_imageFormat = 256;
    private final GameActivity m_activity;
    private Handler m_backgroundHandler;
    private HandlerThread m_backgroundThread;
    private CameraDevice m_cameraDevice;
    private final CameraManager m_cameraManager;
    private CameraCaptureSession m_captureSession;
    private final Context m_context;
    private final BarcodeDetector m_detector;
    private CameraVideoFormat m_format;
    public HandlerCb m_handler;
    private int m_height;
    private volatile ByteBuffer m_imageBuffer;
    private final ReentrantLock m_imageLock;
    private final Intent m_imagePicker;
    private ImageReader m_imageReader;
    private volatile Mode m_mode;
    private volatile boolean m_permissionDenied;
    private volatile boolean m_requestingPermission;
    private volatile State m_state;
    private int m_width;

    public enum CameraVideoFormat {
        kCameraVideoFormat_Unknown,
        kCameraVideoFormat_ARGB_8888,
        kCameraVideoFormat_BGRA_8888,
        kCameraVideoFormat_RGBA_8888
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
        this.m_detector = new BarcodeDetector.Builder(context).setBarcodeFormats(256).build();
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
                QRCameraHandler.this.m289lambda$requestCameraPermission$0$comtgcskyuiQRCameraHandler(runnable, strArr, iArr);
            }
        };
        if (this.m_permissionDenied) {
            this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.QRCameraHandler$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    QRCameraHandler.this.m290lambda$requestCameraPermission$1$comtgcskyuiQRCameraHandler(permissionCallback);
                }
            });
        } else {
            this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.QRCameraHandler$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    QRCameraHandler.this.m291lambda$requestCameraPermission$2$comtgcskyuiQRCameraHandler(permissionCallback);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$requestCameraPermission$0$com-tgc-sky-ui-QRCameraHandler  reason: not valid java name */
    public /* synthetic */ void m289lambda$requestCameraPermission$0$comtgcskyuiQRCameraHandler(Runnable runnable, String[] strArr, int[] iArr) {
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

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$requestCameraPermission$1$com-tgc-sky-ui-QRCameraHandler  reason: not valid java name */
    public /* synthetic */ void m290lambda$requestCameraPermission$1$comtgcskyuiQRCameraHandler(GameActivity.PermissionCallback permissionCallback) {
        this.m_activity.requestPermissionsThroughSettings(new String[]{"android.permission.CAMERA"}, permissionCallback);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$requestCameraPermission$2$com-tgc-sky-ui-QRCameraHandler  reason: not valid java name */
    public /* synthetic */ void m291lambda$requestCameraPermission$2$comtgcskyuiQRCameraHandler(GameActivity.PermissionCallback permissionCallback) {
        this.m_activity.requestPermissions(new String[]{"android.permission.CAMERA"}, permissionCallback);
    }

    public void requestPermissionAndStartCamera() {
        if (hasCameraPermission()) {
            m292x86a76924();
        } else {
            requestCameraPermission(new Runnable() { // from class: com.tgc.sky.ui.QRCameraHandler$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    QRCameraHandler.this.m292x86a76924();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: setupCamera */
    public void m292x86a76924() {
        if (this.m_state != State.kState_Idle) {
            return;
        }
        this.m_state = State.kState_Starting;
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.QRCameraHandler$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                QRCameraHandler.this.m293lambda$setupCamera$4$comtgcskyuiQRCameraHandler();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$setupCamera$4$com-tgc-sky-ui-QRCameraHandler  reason: not valid java name */
    public /* synthetic */ void m293lambda$setupCamera$4$comtgcskyuiQRCameraHandler() {
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
                if (num != null && num == 1) {
                    break;
                }
                i++;
            }
            if (str != null && this.m_context.checkSelfPermission("android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED) {
                this.m_format = CameraVideoFormat.kCameraVideoFormat_ARGB_8888;
                this.m_width = 640;
                this.m_height = 480;
                this.m_cameraManager.openCamera(str, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice cameraDevice) {
                        try {
                            QRCameraHandler.this.m_cameraDevice = cameraDevice;
                            QRCameraHandler qRCameraHandler = QRCameraHandler.this;
                            qRCameraHandler.m_imageReader = ImageReader.newInstance(qRCameraHandler.m_width, QRCameraHandler.this.m_height, ImageFormat.JPEG, 2);
                            QRCameraHandler.this.m_imageReader.setOnImageAvailableListener(QRCameraHandler.this, null);
                            Surface surface = QRCameraHandler.this.m_imageReader.getSurface();
                            final CaptureRequest.Builder createCaptureRequest = QRCameraHandler.this.m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            createCaptureRequest.addTarget(surface);
                            QRCameraHandler.this.m_cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() { // from class: com.tgc.sky.ui.QRCameraHandler.1.1
                                @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
                                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                    if (QRCameraHandler.this.m_cameraDevice != null) {
                                        QRCameraHandler.this.m_captureSession = cameraCaptureSession;
                                        try {
                                            QRCameraHandler.this.startBackgroundThread();
                                            QRCameraHandler.this.m_captureSession.setRepeatingRequest(createCaptureRequest.build(), null, QRCameraHandler.this.m_backgroundHandler);
                                        } catch (CameraAccessException e) {
                                            e.printStackTrace();
                                        }
                                        QRCameraHandler.this.m_state = State.kState_Running;
                                        return;
                                    }
                                    QRCameraHandler.this.stopCamera();
                                }

                                @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
                                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                                    QRCameraHandler.this.stopCamera();
                                }
                            }, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override // android.hardware.camera2.CameraDevice.StateCallback
                    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
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
        this.m_state = State.kState_Stopping;
        this.m_activity.runOnUiThread(QRCameraHandler.this::m294lambda$stopCamera$5$comtgcskyuiQRCameraHandler);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$stopCamera$5$com-tgc-sky-ui-QRCameraHandler  reason: not valid java name */
    public /* synthetic */ void m294lambda$stopCamera$5$comtgcskyuiQRCameraHandler() {
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
        this.m_imageLock.lock();
        this.m_imageBuffer = null;
        this.m_imageLock.unlock();
        this.m_state = State.kState_Idle;
        synchronized (this) {
            if (this.m_mode == Mode.kMode_QRScanner) {
                this.m_handler.run(null, 0, true);
                this.m_mode = Mode.kMode_Idle;
            }
        }
    }

    public boolean isRunning() {
        return this.m_state == State.kState_Running;
    }

    public CameraVideoFormat getFormat() {
        return this.m_format;
    }

    public int getWidth() {
        return this.m_width;
    }

    public int getHeight() {
        return this.m_height;
    }

    public ByteBuffer lockImageBuffer() {
        this.m_imageLock.lock();
        return this.m_imageBuffer;
    }

    public void unlockImageBuffer() {
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
            this.m_activity.startActivityForResult(this.m_imagePicker, GameActivity.ActivityRequestCode.IMAGE_PICKER);
            return true;
        }
    }

    @Override // android.media.ImageReader.OnImageAvailableListener
    public void onImageAvailable(ImageReader imageReader) {
        Image acquireLatestImage;
        String scanBitmap;
        if (this.m_state == State.kState_Running && (acquireLatestImage = imageReader.acquireLatestImage()) != null) {
            if (acquireLatestImage.getFormat() != 256) {
                acquireLatestImage.close();
                return;
            }
            ByteBuffer buffer = acquireLatestImage.getPlanes()[0].getBuffer();
            buffer.rewind();
            int capacity = buffer.capacity();
            byte[] bArr = new byte[capacity];
            buffer.get(bArr);
            acquireLatestImage.close();
            Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, capacity, null);
            if (decodeByteArray.getConfig() == Bitmap.Config.ARGB_8888) {
                ByteBuffer allocate = ByteBuffer.allocate(decodeByteArray.getByteCount());
                decodeByteArray.copyPixelsToBuffer(allocate);
                allocate.flip();
                this.m_imageLock.lock();
                this.m_imageBuffer = allocate;
                this.m_imageLock.unlock();
            }
            synchronized (this) {
                if (this.m_mode == Mode.kMode_QRScanner && (scanBitmap = scanBitmap(decodeByteArray)) != null) {
                    this.m_handler.run(scanBitmap, 0, true);
                    this.m_mode = Mode.kMode_Idle;
                }
            }
            decodeByteArray.recycle();
        }
    }

    private boolean isSupportedToken(String str) {
        return str.startsWith("https://sky") && str.contains(".thatg.co/?");
    }

    private String scanBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        SparseArray<Barcode> detect = this.m_detector.detect(new Frame.Builder().setBitmap(bitmap).build());
        for (int i = 0; i < detect.size(); i++) {
            Barcode valueAt = detect.valueAt(i);
            if (valueAt != null && valueAt.url != null && valueAt.url.url != null) {
                return valueAt.url.url;
            }
        }
        return null;
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
