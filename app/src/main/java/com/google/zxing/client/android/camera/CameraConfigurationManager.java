package com.google.zxing.client.android.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.WindowManager;
import com.google.zxing.client.android.camera.open.CameraFacing;
import com.google.zxing.client.android.camera.open.OpenCamera;
import com.tgc.sky.ui.qrcodereaderview.SimpleLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* loaded from: classes2.dex */
final class CameraConfigurationManager {
    private static final float MAX_EXPOSURE_COMPENSATION = 1.5f;
    private static final int MAX_PREVIEW_PIXELS = 921600;
    private static final float MIN_EXPOSURE_COMPENSATION = 0.0f;
    private static final int MIN_PREVIEW_PIXELS = 150400;
    private static final String TAG = "CameraConfiguration";
    private Point bestPreviewSize;
    private Point cameraResolution;
    private final Context context;
    private int cwNeededRotation;
    private int cwRotationFromDisplayToCamera;
    private Point previewSizeOnScreen;
    private Point resolution;

    /* JADX INFO: Access modifiers changed from: package-private */
    public CameraConfigurationManager(Context context) {
        this.context = context;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void initFromCameraParameters(OpenCamera openCamera, int i, int i2) {
        int i3;
        Camera.Parameters parameters = openCamera.getCamera().getParameters();
        int rotation = ((WindowManager) this.context.getSystemService("window")).getDefaultDisplay().getRotation();
        if (rotation == 0) {
            i3 = 0;
        } else if (rotation == 1) {
            i3 = 90;
        } else if (rotation == 2) {
            i3 = 180;
        } else if (rotation == 3) {
            i3 = 270;
        } else if (rotation % 90 == 0) {
            i3 = (rotation + 360) % 360;
        } else {
            throw new IllegalArgumentException("Bad rotation: " + rotation);
        }
        SimpleLog.i(TAG, "Display at: " + i3);
        int orientation = openCamera.getOrientation();
        SimpleLog.i(TAG, "Camera at: " + orientation);
        if (openCamera.getFacing() == CameraFacing.FRONT) {
            orientation = (360 - orientation) % 360;
            SimpleLog.i(TAG, "Front camera overriden to: " + orientation);
        }
        this.cwRotationFromDisplayToCamera = ((orientation + 360) - i3) % 360;
        SimpleLog.i(TAG, "Final display orientation: " + this.cwRotationFromDisplayToCamera);
        if (openCamera.getFacing() == CameraFacing.FRONT) {
            SimpleLog.i(TAG, "Compensating rotation for front camera");
            this.cwNeededRotation = (360 - this.cwRotationFromDisplayToCamera) % 360;
        } else {
            this.cwNeededRotation = this.cwRotationFromDisplayToCamera;
        }
        SimpleLog.i(TAG, "Clockwise rotation from display to camera: " + this.cwNeededRotation);
        this.resolution = new Point(i, i2);
        SimpleLog.i(TAG, "Screen resolution in current orientation: " + this.resolution);
        this.cameraResolution = findBestPreviewSizeValue(parameters, this.resolution);
        SimpleLog.i(TAG, "Camera resolution: " + this.cameraResolution);
        this.bestPreviewSize = findBestPreviewSizeValue(parameters, this.resolution);
        SimpleLog.i(TAG, "Best available preview size: " + this.bestPreviewSize);
        if ((this.resolution.x < this.resolution.y) == (this.bestPreviewSize.x < this.bestPreviewSize.y)) {
            this.previewSizeOnScreen = this.bestPreviewSize;
        } else {
            this.previewSizeOnScreen = new Point(this.bestPreviewSize.y, this.bestPreviewSize.x);
        }
        SimpleLog.i(TAG, "Preview size on screen: " + this.previewSizeOnScreen);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setDesiredCameraParameters(OpenCamera openCamera, boolean z) {
        Camera camera = openCamera.getCamera();
        Camera.Parameters parameters = camera.getParameters();
        if (parameters == null) {
            SimpleLog.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }
        SimpleLog.i(TAG, "Initial camera parameters: " + parameters.flatten());
        if (z) {
            SimpleLog.w(TAG, "In camera config safe mode -- most settings will not be honored");
        }
        String findSettableValue = !z ? findSettableValue("focus mode", parameters.getSupportedFocusModes(), "auto") : null;
        if (findSettableValue != null) {
            parameters.setFocusMode(findSettableValue);
        }
        parameters.setPreviewSize(this.bestPreviewSize.x, this.bestPreviewSize.y);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(this.cwRotationFromDisplayToCamera);
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        if (previewSize != null) {
            if (this.bestPreviewSize.x == previewSize.width && this.bestPreviewSize.y == previewSize.height) {
                return;
            }
            SimpleLog.w(TAG, "Camera said it supported preview size " + this.bestPreviewSize.x + 'x' + this.bestPreviewSize.y + ", but after setting it, preview size is " + previewSize.width + 'x' + previewSize.height);
            this.bestPreviewSize.x = previewSize.width;
            this.bestPreviewSize.y = previewSize.height;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Point getCameraResolution() {
        return this.cameraResolution;
    }

    Point getScreenResolution() {
        return this.resolution;
    }

    public Point findBestPreviewSizeValue(Camera.Parameters parameters, Point point) {
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        if (supportedPreviewSizes == null) {
            SimpleLog.w(TAG, "Device returned no supported preview sizes; using default");
            Camera.Size previewSize = parameters.getPreviewSize();
            return new Point(previewSize.width, previewSize.height);
        }
        ArrayList<Camera.Size> arrayList = new ArrayList(supportedPreviewSizes);
        Collections.sort(arrayList, new Comparator<Camera.Size>() { // from class: com.google.zxing.client.android.camera.CameraConfigurationManager.1
            @Override // java.util.Comparator
            public int compare(Camera.Size size, Camera.Size size2) {
                int i = size.height * size.width;
                int i2 = size2.height * size2.width;
                if (i2 < i) {
                    return -1;
                }
                return i2 > i ? 1 : 0;
            }
        });
        if (Log.isLoggable(TAG, 4)) {
            StringBuilder sb = new StringBuilder();
            for (Camera.Size size : arrayList) {
                sb.append(size.width).append('x').append(size.height).append(' ');
            }
            SimpleLog.i(TAG, "Supported preview sizes: " + ((Object) sb));
        }
        float f = point.x / point.y;
        Point point2 = null;
        float f2 = Float.POSITIVE_INFINITY;
        for (Camera.Size size2 : arrayList) {
            int i = size2.width;
            int i2 = size2.height;
            int i3 = i * i2;
            if (i3 >= MIN_PREVIEW_PIXELS && i3 <= MAX_PREVIEW_PIXELS) {
                if (i == point.x && i2 == point.y) {
                    Point point3 = new Point(i, i2);
                    SimpleLog.i(TAG, "Found preview size exactly matching screen size: " + point3);
                    return point3;
                }
                float abs = Math.abs((i / i2) - f);
                if (abs < f2) {
                    point2 = new Point(i, i2);
                    f2 = abs;
                }
            }
        }
        if (point2 == null) {
            Camera.Size previewSize2 = parameters.getPreviewSize();
            point2 = new Point(previewSize2.width, previewSize2.height);
            SimpleLog.i(TAG, "No suitable preview sizes, using default: " + point2);
        }
        SimpleLog.i(TAG, "Found best approximate preview size: " + point2);
        return point2;
    }

    private static String findSettableValue(String str, Collection<String> collection, String... strArr) {
        SimpleLog.i(TAG, "Requesting " + str + " value from among: " + Arrays.toString(strArr));
        SimpleLog.i(TAG, "Supported " + str + " values: " + collection);
        if (collection != null) {
            for (String str2 : strArr) {
                if (collection.contains(str2)) {
                    SimpleLog.i(TAG, "Can set " + str + " to: " + str2);
                    return str2;
                }
            }
        }
        SimpleLog.i(TAG, "No supported values match");
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean getTorchState(Camera camera) {
        String flashMode;
        if (camera == null || camera.getParameters() == null || (flashMode = camera.getParameters().getFlashMode()) == null) {
            return false;
        }
        return "on".equals(flashMode) || "torch".equals(flashMode);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setTorchEnabled(Camera camera, boolean z) {
        Camera.Parameters parameters = camera.getParameters();
        setTorchEnabled(parameters, z, false);
        camera.setParameters(parameters);
    }

    void setTorchEnabled(Camera.Parameters parameters, boolean z, boolean z2) {
        setTorchEnabled(parameters, z);
        if (z2) {
            return;
        }
        setBestExposure(parameters, z);
    }

    public static void setTorchEnabled(Camera.Parameters parameters, boolean z) {
        String findSettableValue;
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (z) {
            findSettableValue = findSettableValue("flash mode", supportedFlashModes, "torch", "on");
        } else {
            findSettableValue = findSettableValue("flash mode", supportedFlashModes, "off");
        }
        if (findSettableValue != null) {
            if (findSettableValue.equals(parameters.getFlashMode())) {
                SimpleLog.i(TAG, "Flash mode already set to " + findSettableValue);
                return;
            }
            SimpleLog.i(TAG, "Setting flash mode to " + findSettableValue);
            parameters.setFlashMode(findSettableValue);
        }
    }

    public static void setBestExposure(Camera.Parameters parameters, boolean z) {
        int minExposureCompensation = parameters.getMinExposureCompensation();
        int maxExposureCompensation = parameters.getMaxExposureCompensation();
        float exposureCompensationStep = parameters.getExposureCompensationStep();
        if (minExposureCompensation != 0 || maxExposureCompensation != 0) {
            if (exposureCompensationStep > 0.0f) {
                int round = Math.round((z ? 0.0f : MAX_EXPOSURE_COMPENSATION) / exposureCompensationStep);
                float f = exposureCompensationStep * round;
                int max = Math.max(Math.min(round, maxExposureCompensation), minExposureCompensation);
                if (parameters.getExposureCompensation() == max) {
                    SimpleLog.i(TAG, "Exposure compensation already set to " + max + " / " + f);
                    return;
                }
                SimpleLog.i(TAG, "Setting exposure compensation to " + max + " / " + f);
                parameters.setExposureCompensation(max);
                return;
            }
        }
        SimpleLog.i(TAG, "Camera does not support exposure compensation");
    }
}
