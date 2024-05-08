package com.google.zxing.client.android.camera.open;

import android.hardware.Camera;

/* loaded from: classes2.dex */
public final class OpenCamera {
    private final Camera camera;
    private final CameraFacing facing;
    private final int index;
    private final int orientation;

    public OpenCamera(int i, Camera camera, CameraFacing cameraFacing, int i2) {
        this.index = i;
        this.camera = camera;
        this.facing = cameraFacing;
        this.orientation = i2;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public CameraFacing getFacing() {
        return this.facing;
    }

    public int getOrientation() {
        return this.orientation;
    }

    public String toString() {
        return "Camera #" + this.index + " : " + this.facing + ',' + this.orientation;
    }
}
