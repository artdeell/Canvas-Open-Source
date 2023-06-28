package com.tgc.sky;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewTreeObserver;
import java.io.File;

import git.artdeell.skymodloader.SMLApplication;

public abstract class TGCNativeActivity extends Activity implements SurfaceHolder.Callback2, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String KEY_NATIVE_SAVED_STATE = "android:native_state";
    private SurfaceHolder m_currentSurfaceHolder;
    private boolean m_destroyed = false;
    private long m_handle = 0;
    int m_lastContentHeight;
    int m_lastContentWidth;
    int m_lastContentX;
    int m_lastContentY;
    final int[] m_location = new int[2];

    private native void onConfigurationChangedNative(long j);

    private native void onContentRectChangedNative(long j, int i, int i2, int i3, int i4);

    private native long onCreateNative(String str, String str2, String str3, int i, Object obj, byte[] bArr);

    private native void onDestroyNative(long j);

    private native void onLowMemoryNative(long j);

    private native void onPauseNative(long j);

    private native void onResumeNative(long j);

    private native byte[] onSaveInstanceStateNative(long j);

    private native void onStartNative(long j);

    private native void onStopNative(long j);

    private native void onSurfaceChangedNative(long j, Surface surface, int i, int i2, int i3);

    private native void onSurfaceCreatedNative(long j, Surface surface);

    private native void onSurfaceDestroyedNative(long j, Surface surface);

    private native void onSurfaceRedrawNeededNative(long j, Surface surface);

    private native void onWindowFocusChangedNative(long j, boolean z);

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        findViewById(android.R.id.content).getRootView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        this.m_handle = onCreateNative(getAbsolutePath(getFilesDir()), getAbsolutePath(getObbDir()), getAbsolutePath(getExternalFilesDir(null)), Build.VERSION.SDK_INT, SMLApplication.skyRes.getAssets(), bundle != null ? bundle.getByteArray(KEY_NATIVE_SAVED_STATE) : null);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        this.m_destroyed = true;
        SurfaceHolder surfaceHolder = this.m_currentSurfaceHolder;
        if (surfaceHolder != null) {
            onSurfaceDestroyedNative(this.m_handle, surfaceHolder.getSurface());
            this.m_currentSurfaceHolder = null;
        }
        onDestroyNative(this.m_handle);
        super.onDestroy();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        onPauseNative(this.m_handle);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        onResumeNative(this.m_handle);
    }

    /* access modifiers changed from: protected */
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        byte[] onSaveInstanceStateNative = onSaveInstanceStateNative(this.m_handle);
        if (onSaveInstanceStateNative != null) {
            bundle.putByteArray(KEY_NATIVE_SAVED_STATE, onSaveInstanceStateNative);
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        onStartNative(this.m_handle);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        onStopNative(this.m_handle);
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (!this.m_destroyed) {
            onConfigurationChangedNative(this.m_handle);
        }
    }

    public void onLowMemory() {
        super.onLowMemory();
        if (!this.m_destroyed) {
            onLowMemoryNative(this.m_handle);
        }
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (!this.m_destroyed) {
            onWindowFocusChangedNative(this.m_handle, z);
        }
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (!this.m_destroyed) {
            this.m_currentSurfaceHolder = surfaceHolder;
            onSurfaceCreatedNative(this.m_handle, surfaceHolder.getSurface());
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (!this.m_destroyed) {
            this.m_currentSurfaceHolder = surfaceHolder;
            onSurfaceChangedNative(this.m_handle, surfaceHolder.getSurface(), i, i2, i3);
        }
    }

    public void surfaceRedrawNeeded(SurfaceHolder surfaceHolder) {
        if (!this.m_destroyed) {
            this.m_currentSurfaceHolder = surfaceHolder;
            onSurfaceRedrawNeededNative(this.m_handle, surfaceHolder.getSurface());
        }
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        this.m_currentSurfaceHolder = null;
        if (!this.m_destroyed) {
            onSurfaceDestroyedNative(this.m_handle, surfaceHolder.getSurface());
        }
    }

    public void onGlobalLayout() {

        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.getLocationInWindow(this.m_location);
        int width = rootView.getWidth();
        int height = rootView.getHeight();
        int[] iArr = this.m_location;
        if (iArr[0] != this.m_lastContentX || iArr[1] != this.m_lastContentY || width != this.m_lastContentWidth || height != this.m_lastContentHeight) {
            int[] iArr2 = this.m_location;
            int i = iArr2[0];
            this.m_lastContentX = i;
            int i2 = iArr2[1];
            this.m_lastContentY = i2;
            this.m_lastContentWidth = width;
            this.m_lastContentHeight = height;
            if (!this.m_destroyed) {
                onContentRectChangedNative(this.m_handle, i, i2, width, height);
            }
        }
    }

    private static String getAbsolutePath(File file) {
        if (file != null) {
            return file.getAbsolutePath();
        }
        return null;
    }
}
