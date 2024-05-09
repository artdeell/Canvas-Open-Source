package com.tgc.sky;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.view.InputDeviceCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tgc.sky.io.AudioDeviceType;
import com.tgc.sky.ui.panels.BasePanel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.fmod.FMOD;
import org.json.JSONException;
import org.json.JSONObject;

import git.artdeell.skymodloader.BuildConfig;
import git.artdeell.skymodloader.DialogJNI;
import git.artdeell.skymodloader.FileSelector;
import git.artdeell.skymodloader.ImGUI;
import git.artdeell.skymodloader.R;
import git.artdeell.skymodloader.SMLApplication;
import git.artdeell.skymodloader.ImGUITextInput;
import git.artdeell.skymodloader.updater.UpdaterService;
import git.artdeell.skymodloader.updater.UpdaterServiceConnection;

public class GameActivity extends TGCNativeActivity {
    static final boolean ENABLE_DISPLAY_CUTOUT_MODE = true;
    private static final String TAG = "GameActivity";
    /* access modifiers changed from: private */
    public ArrayList<Integer> mGameControllerIds;
    private ArrayList<OnActivityIntentListener> mOnActivityIntentListeners;
    private ArrayList<OnActivityResultListener> mOnActivityResultListeners;
    private ArrayList<OnKeyboardListener> mOnKeyboardListeners;
    private PermissionCallback mPermissionCallback = null;
    private ArrayList<BasePanel> mActivePanels = new ArrayList<>();
    private ImageView logoView;
    private MediaPlayer m_mediaPlayer;
    private ImGUI imgui;
    private boolean imguiKeybaordShowing;
    private ImGUITextInput imguiInput;
    private boolean m_logoSoundReleased = false;
    /* access modifiers changed from: private */
    public Rect mSafeAreaInsets = new Rect();
    private boolean m_isKeyboardShowing = false;
    private RelativeLayout m_relativeLayout;
    SystemAccounts_android m_systemAccounts = null;
    SystemIO_android m_systemIO = null;
    SystemUI_android m_systemUI = null;
    public boolean portraitOnResume = false;

    public static class ActivityRequestCode {
        static final int ASK_PERMISSIONS = 100;
        public static final int DYNAMIC_FEATURE_DOWNLOAD_CONFIRM = 120;
        public static final int GOOGLE_SIGN_IN = 140;
        public static final int HUAWEI_SIGN_IN = 160;
        public static final int IMAGE_PICKER = 130;
        static final int SHARE_IMG = 111;
        static final int SHARE_URL = 110;
        static final int SHARE_VIDEO = 112;
        public static final int SURVEY_MONKEY_RESPONSE = 150;
    }

    public interface OnActivityIntentListener {
        boolean onNewIntent(Intent intent);
    }

    public interface OnActivityResultListener {
        void onActivityResult(int i, int i2, Intent intent);
    }

    public interface OnKeyboardListener {
        void onKeyboardChange(boolean z, int i);
    }

    public interface PermissionCallback {
        void onPermissionResult(String[] strArr, int[] iArr);
    }

    public static float AppleConvertAndroidScale(float f) {
        return f * 1.5f;
    }

    private native void onCreateNative();

    /* access modifiers changed from: private */
    public native void onSafeAreaInsetsChanged(float[] fArr);

    private static native boolean onTouchNative(int i, int i2, float f, float f2, double d);

    public native String ResolveTemplateArgsNative(String str);

    public int getAppBuildVersion() {
        return com.tgc.sky.BuildConfig.VERSION_CODE;
    }

    public String getAppVersion() {
        return com.tgc.sky.BuildConfig.SKY_VERSION;
    }

    public String getPlatformName() {
        return "android";
    }

    public native void onAudioDeviceTypeChangedNative(int i);

    public native void onBackPressedNative();

    public native void onButtonPressNative(int i, boolean z, double d);

    public native void onCommerceUpdateNative(boolean z, boolean z2, boolean z3);

    public native void onDpadEventNative(float f, float f2, double d);

    public native void onGamepadConnectedNative();

    public native void onGamepadDisconnectedNative();

    public native void onInternetReachabilityNative(boolean z, boolean z2);

    public native void onKeyboardCompleteNative(String str, boolean z, boolean z2);

    public native void onNFCTagScannedNative(String str, int i, String str2, String str3);

    public native void onOpenedWithURLNative(String str, boolean z);

    public native void onStickEventNative(float f, float f2, float f3, float f4);

    public native void onSystemScreenshotTakenNative();

    public native void onVolumeChangeNative(float f, float f2);

    public float transformHeightToProgram(float f) {
        return f;
    }

    public float transformHeightToSystem(float f) {
        return f;
    }

    public float transformWidthToProgram(float f) {
        return f;
    }

    public float transformWidthToSystem(float f) {
        return f;
    }

    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
    }

    private boolean isTextRenderingBrokenForDevice() {
        if (Build.VERSION.SDK_INT == 31 || Build.VERSION.SDK_INT == 32) {
            String[] strArr = {"OPD2102", "X21N2", "PFUM10", "TB128FU", "RMX3478", "RMX3471", "RMX3472", "2201116SC", "22101317C"};
            for (int i = 0; i < 9; i++) {
                if (Build.MODEL.compareToIgnoreCase(strArr[i]) == 0) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @SuppressLint("WrongConstant")
    private void fixTextRenderingOnProblemDevices_HACK() {
        if (isTextRenderingBrokenForDevice()) {
            Log.i(TAG, "Detected problematic text rendering on this device - applying workaround");
            for (int i = 0; i < 29; i++) {
                View view = new View(this);
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.width = 2;
                layoutParams.height = 2;
                layoutParams.flags = 1064;
                layoutParams.format = PixelFormat.RGBA_8888;
                layoutParams.gravity = Gravity.BOTTOM;
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(view, layoutParams);
            }
        }
    }

    public void runUpdater() {
        Intent updaterService = new Intent(this, UpdaterService.class);
        bindService(updaterService, new UpdaterServiceConnection(this), BIND_AUTO_CREATE);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        runUpdater();
        DialogJNI.setActivity(this);
        hideNavigationFullScreen(getWindow().getDecorView());
        getWindow().addFlags(2097280);
        tryEnablingDisplayCutoutMode();
        setContentView(R.layout.tgc_logo);
        this.m_relativeLayout = findViewById(R.id.sml_relLayout);
        ((SurfaceView)findViewById(R.id.surfaceView)).getHolder().addCallback(this);
        FileSelector.setActivity(this);
        if(imgui == null) imgui = new ImGUI();
        SurfaceView imguiView = findViewById(R.id.imguiView);
        imguiView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        imguiView.getHolder().addCallback(imgui);
        imguiView.setZOrderOnTop(true);
        ImGUI.setClipboardService((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE));
        imguiInput = findViewById(R.id.imguiInput);
        FMOD.init(this);
        new SystemCommerce_android(this);
        this.m_systemIO = new SystemIO_android(this);
        this.m_systemAccounts = new SystemAccounts_android(this);
        SystemRemoteConfig_android.getInstance().Initialize(this);
        SystemSupport_android.getInstance().Initialize(this);
        this.m_systemUI = new SystemUI_android(this);
        onCreateNative();
        initGameController();
        logoView = findViewById(R.id.imageView);
        Intent intent = getIntent();
        if (intent != null) {
            HandleNewIntent(intent);
        }
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            try {
                int max = Integer.max(windowInsets.getStableInsetTop(), windowInsets.getStableInsetBottom());
                if (Build.VERSION.SDK_INT >= 27) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            if (windowInsets.getDisplayCutout() != null) {
                                max = Integer.max(max, Integer.max(windowInsets.getDisplayCutout().getSafeInsetLeft(), windowInsets.getDisplayCutout().getSafeInsetRight()));
                            }
                        }
                    } catch (NoSuchMethodError ignored) {
                    }
                }
                GameActivity.this.mSafeAreaInsets.left = max;
                GameActivity.this.mSafeAreaInsets.top = 0;
                GameActivity.this.mSafeAreaInsets.right = max;
                GameActivity.this.mSafeAreaInsets.bottom = 0;
                float transformWidthToProgram = GameActivity.this.transformWidthToProgram(max);
                GameActivity.this.onSafeAreaInsetsChanged(new float[]{transformWidthToProgram, 0.0f, transformWidthToProgram, 0.0f});
                return view.onApplyWindowInsets(windowInsets);
            } catch (Exception | NoSuchMethodError unused2) {
                return windowInsets;
            }
        });
    }
    public String getDeviceBrand() {
        return Build.BRAND;
    }

    @Override
    public void onDestroy() {
        FMOD.close();
        FileSelector.unsetActivity();
        this.m_systemIO.onDestroy();
        super.onDestroy();
    }

    @Override // com.tgc.sky.TGCNativeActivity, android.app.Activity
    public void onResume() {
        if (this.portraitOnResume) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        this.m_systemIO.onResume();
        this.m_systemAccounts.onResume();
        super.onResume();
        if (this.portraitOnResume) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (GameActivity.this.portraitOnResume) {
                    GameActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }, 1000L);
        }
    }

    @Override
    public void onPause() {
        if (this.portraitOnResume) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        this.m_systemIO.onPause();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean z) {
        RelativeLayout relativeLayout;
        super.onWindowFocusChanged(z);
        if (z && (relativeLayout = this.m_relativeLayout) != null) {
            hideNavigationFullScreen(relativeLayout);
        }
    }

    @Override
    public void onBackPressed() {
        onBackPressedNative();
    }

    public void AddOnActivityIntentListener(OnActivityIntentListener onActivityIntentListener) {
        if (this.mOnActivityIntentListeners == null) {
            this.mOnActivityIntentListeners = new ArrayList<>();
        }
        if (!this.mOnActivityIntentListeners.contains(onActivityIntentListener)) {
            this.mOnActivityIntentListeners.add(onActivityIntentListener);
        }
    }

    public void RemoveOnActivityIntentListeners(OnActivityIntentListener onActivityIntentListener) {
        ArrayList<OnActivityIntentListener> arrayList = this.mOnActivityIntentListeners;
        if (arrayList != null) {
            arrayList.remove(onActivityIntentListener);
        }
    }

    public void onNewIntent(Intent intent) {
        ArrayList<OnActivityIntentListener> arrayList = this.mOnActivityIntentListeners;
        if (arrayList != null) {
            Iterator<OnActivityIntentListener> it = arrayList.iterator();
            while (it.hasNext()) {
                if (it.next().onNewIntent(intent)) {
                    return;
                }
            }
        }
        HandleNewIntent(intent);
    }

    /* access modifiers changed from: package-private */
    public void HandleNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            JSONObject jSONObject = new JSONObject();
            for (String str : extras.keySet()) {
                try {
                    //if (!str.startsWith(Constants.REFERRER_API_GOOGLE) && !str.equalsIgnoreCase(Constants.MessagePayloadKeys.FROM) && !str.equalsIgnoreCase(Constants.MessagePayloadKeys.COLLAPSE_KEY)) {
                        jSONObject.put(str, JSONObject.wrap(extras.get(str)));
                    //}
                } catch (JSONException ignored) {
                }
            }
            SystemIO_android.getInstance().OnAppLaunchNotificationMessage(jSONObject.toString());
        }
        String action = intent.getAction();
        Uri data = intent.getData();
        if ("android.intent.action.VIEW".equals(action) && data != null) {
            onOpenedWithURLNative(data.toString(), false);
        } else if ("android.nfc.action.NDEF_DISCOVERED".equals(action) && data != null) {
            onOpenedWithURLNative(data.toString(), true);
        }
    }

    public void AddOnActivityResultListener(OnActivityResultListener onActivityResultListener) {
        if (this.mOnActivityResultListeners == null) {
            this.mOnActivityResultListeners = new ArrayList<>();
        }
        if (!this.mOnActivityResultListeners.contains(onActivityResultListener)) {
            this.mOnActivityResultListeners.add(onActivityResultListener);
        }
    }

    public void RemoveOnActivityResultListeners(OnActivityResultListener onActivityResultListener) {
        ArrayList<OnActivityResultListener> arrayList = this.mOnActivityResultListeners;
        if (arrayList != null) {
            arrayList.remove(onActivityResultListener);
        }
    }

    /* access modifiers changed from: protected */
    @Override
    protected void onActivityResult(int i, int i2, Intent intent) {
        Log.i("Interlock","GameActivity onActivityResult");
        super.onActivityResult(i, i2, intent);
        ArrayList<OnActivityResultListener> arrayList = this.mOnActivityResultListeners;
        if (arrayList != null) {
            for (OnActivityResultListener onActivityResultListener : arrayList) {
                onActivityResultListener.onActivityResult(i, i2, intent);
            }
        }
    }

    public boolean checkSelfPermissions(String[] strArr) {
        boolean z = true;
        for (String str : strArr) {
            z &= checkSelfPermission(str) == PackageManager.PERMISSION_GRANTED;
        }
        return z;
    }

    public boolean checkResultPermissions(int[] iArr) {
        boolean z = iArr.length > 0;
        for (int i : iArr) {
            z &= i == PackageManager.PERMISSION_GRANTED;
        }
        return z;
    }

    public int[] getSelfPermissions(String[] strArr) {
        int[] iArr = new int[strArr.length];
        int length = strArr.length;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            iArr[i2] = checkSelfPermission(strArr[i]);
            i++;
            i2++;
        }
        return iArr;
    }

    public boolean shouldShowRequestPermissionsRationale(String[] strArr) {
        boolean z = false;
        for (String str : strArr) {
            z |= shouldShowRequestPermissionRationale(str);
        }
        return z;
    }

    public void requestPermissions(String[] strArr, PermissionCallback permissionCallback) {
        if (checkSelfPermissions(strArr)) {
            permissionCallback.onPermissionResult(strArr, getSelfPermissions(strArr));
        } else if (this.mPermissionCallback != null) {
            permissionCallback.onPermissionResult(new String[0], new int[0]);
        } else {
            this.mPermissionCallback = permissionCallback;
            requestPermissions(strArr, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        PermissionCallback permissionCallback;
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != 100 || (permissionCallback = this.mPermissionCallback) == null) {
            return;
        }
        permissionCallback.onPermissionResult(strArr, iArr);
        this.mPermissionCallback = null;
    }


    public void requestPermissionsThroughSettings(final String[] strArr, final PermissionCallback permissionCallback) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GameActivity.this.AddOnActivityResultListener(new OnActivityResultListener() { // from class: com.tgc.sky.GameActivity.3.1
                    @Override
                    public void onActivityResult(int i, int i2, Intent intent) {
                        if (i == 100) {
                            permissionCallback.onPermissionResult(strArr, GameActivity.this.getSelfPermissions(strArr));
                            GameActivity.this.RemoveOnActivityResultListeners(this);
                        }
                    }
                });
                GameActivity.this.startActivityForResult(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)), 100);
            }
        });
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_DOWN  || actionMasked == MotionEvent.ACTION_MOVE) {
            ImGUI.submitPositionEvent(motionEvent.getX(), motionEvent.getY());
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                ImGUI.submitButtonEvent(0, true);
            }
            if (actionMasked == MotionEvent.ACTION_UP) {
                ImGUI.submitButtonEvent(0, false);
            }

            boolean wantsKeyboard = ImGUI.wantsKeyboard();
            if (wantsKeyboard && !imguiKeybaordShowing) {
                imguiInput.setKeyboardState(true);
                imguiKeybaordShowing = wantsKeyboard;
            }
            if (!wantsKeyboard && imguiKeybaordShowing) {
                imguiInput.setKeyboardState(false);
                imguiKeybaordShowing = wantsKeyboard;
            }
            if (ImGUI.wantsMouse()) {
                return true;
            }
        }
        if (actionMasked == MotionEvent.ACTION_MOVE || actionMasked == MotionEvent.ACTION_CANCEL) {
            for (int i = 0; i < motionEvent.getPointerCount(); i++) {
                onTouchNative(motionEvent.getPointerId(i) + 1, actionMasked, motionEvent.getX(i), motionEvent.getY(i), (double) motionEvent.getEventTime());
            }
            return true;
        }
        int actionIndex = motionEvent.getActionIndex();
        return onTouchNative(motionEvent.getPointerId(actionIndex) + 1, actionMasked, motionEvent.getX(actionIndex), motionEvent.getY(actionIndex), (double) motionEvent.getEventTime());
    }

    public void onGlobalLayout() {
        Rect rect = new Rect();
        this.m_relativeLayout.getWindowVisibleDisplayFrame(rect);
        int height = this.m_relativeLayout.getHeight() - rect.height();
        LocalBroadcastManager instance = LocalBroadcastManager.getInstance(this);
        if (height <= 0) {
            if (this.m_isKeyboardShowing) {
                this.m_isKeyboardShowing = false;
                onHideKeyboard();
                instance.sendBroadcast(new Intent("KeyboardWillHide"));
            }
        } else if (!this.m_isKeyboardShowing) {
            this.m_isKeyboardShowing = true;
            onShowKeyboard(height);
            Intent intent = new Intent("KeyboardWillShow");
            intent.putExtra("KeyboardHeight", height);
            instance.sendBroadcast(intent);
        }
    }

    protected void onShowKeyboard(int i) {
        ArrayList<OnKeyboardListener> arrayList = this.mOnKeyboardListeners;
        if (arrayList == null) {
            return;
        }
        for (OnKeyboardListener onKeyboardListener : arrayList) {
            onKeyboardListener.onKeyboardChange(true, i);
        }
        getBrigeView().postDelayed(() -> GameActivity.hideNavigationFullScreen(GameActivity.this.getBrigeView()), 100L);
    }

    /* access modifiers changed from: protected */
    public void onHideKeyboard() {
        //imguiInput.setVisibility(View.GONE);
        ArrayList<OnKeyboardListener> arrayList = this.mOnKeyboardListeners;
        if (arrayList != null) {
            for (OnKeyboardListener onKeyboardListener : arrayList) {
                onKeyboardListener.onKeyboardChange(false, 0);
            }
        }
    }

    public void addOnKeyboardListener(OnKeyboardListener onKeyboardListener) {
        if (this.mOnKeyboardListeners == null) {
            this.mOnKeyboardListeners = new ArrayList<>();
        }
        if (!this.mOnKeyboardListeners.contains(onKeyboardListener)) {
            this.mOnKeyboardListeners.add(onKeyboardListener);
        }
    }

    public void RemoveOnKeyboardListener(OnKeyboardListener onKeyboardListener) {
        ArrayList<OnKeyboardListener> arrayList = this.mOnKeyboardListeners;
        if (arrayList != null) {
            arrayList.remove(onKeyboardListener);
        }
    }

    /* access modifiers changed from: private */
    public boolean isValidGameController(int i) {
        boolean z;
        InputDevice device = InputDevice.getDevice(i);
        if (device == null) {
            return false;
        }
        int sources = device.getSources();
        if ((sources & InputDeviceCompat.SOURCE_GAMEPAD) != 1025 || (sources & InputDeviceCompat.SOURCE_JOYSTICK) != 16777232) {
            return false;
        }
        boolean[] hasKeys = device.hasKeys(new int[]{96, 97, 99, 100, 103});
        int length = hasKeys.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                z = true;
                break;
            } else if (!hasKeys[i2]) {
                z = false;
                break;
            } else {
                i2++;
            }
        }
        int i3 = 0;
        for (InputDevice.MotionRange next : device.getMotionRanges()) {
            if (next.getAxis() == 0 || next.getAxis() == 1 || next.getAxis() == 11 || next.getAxis() == 14) {
                i3++;
            }
        }
        return z && i3 >= 4;
    }

    private void initGameController() {
        int[] deviceIds;
        this.mGameControllerIds = new ArrayList<>();
        for (int i : InputDevice.getDeviceIds()) {
            if (isValidGameController(i)) {
                this.mGameControllerIds.add(i);
            }
        }
        if (!this.mGameControllerIds.isEmpty()) {
            onGamepadConnectedNative();
        }
        InputManager inputManager = (InputManager) getBaseContext().getSystemService(Context.INPUT_SERVICE);
        if (inputManager != null) {
            inputManager.registerInputDeviceListener(new InputManager.InputDeviceListener() { // from class: com.tgc.sky.GameActivity.5
                @Override
                public void onInputDeviceChanged(int i2) {
                }

                @Override
                public void onInputDeviceAdded(int i2) {
                    if (GameActivity.this.isValidGameController(i2)) {
                        boolean isEmpty = GameActivity.this.mGameControllerIds.isEmpty();
                        GameActivity.this.mGameControllerIds.add(i2);
                        if (isEmpty) {
                            GameActivity.this.onGamepadConnectedNative();
                        }
                    }
                }

                @Override
                public void onInputDeviceRemoved(int i2) {
                    if (GameActivity.this.mGameControllerIds.contains(i2)) {
                        GameActivity.this.mGameControllerIds.remove(Integer.valueOf(i2));
                        if (GameActivity.this.mGameControllerIds.isEmpty()) {
                            GameActivity.this.onGamepadDisconnectedNative();
                        }
                    }
                }
            }, null);
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if ((keyEvent.getSource() & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {
            imgui.onKey(i, true);
        }
        if (this.mGameControllerIds.contains(keyEvent.getDeviceId())) {
            if ((keyEvent.getSource() & 1025) == 1025 || (keyEvent.getSource() & InputDeviceCompat.SOURCE_JOYSTICK) == 16777232) {
                if (keyEvent.getRepeatCount() == 0) {
                    onButtonPressNative(i, true, keyEvent.getEventTime());
                }
                return true;
            }
        } else if (i == 4 && keyEvent.getRepeatCount() == 0) {
            onButtonPressNative(i, true, keyEvent.getEventTime());
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if ((keyEvent.getSource() & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {
            imgui.onKey(i, false);
        }
        if (this.mGameControllerIds.contains(keyEvent.getDeviceId())) {
            if ((keyEvent.getSource() & 1025) == 1025 || (keyEvent.getSource() & InputDeviceCompat.SOURCE_JOYSTICK) == 16777232) {
                onButtonPressNative(i, false, keyEvent.getEventTime());
                return true;
            }
        } else if (i == 4) {
            onButtonPressNative(i, false, keyEvent.getEventTime());
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if (this.mGameControllerIds.contains(motionEvent.getDeviceId())) {
            int action = motionEvent.getAction();
            if ((motionEvent.getSource() & 1025) == 1025 || (motionEvent.getSource() & InputDeviceCompat.SOURCE_JOYSTICK) == 16777232) {
                if ((action & 255) == 2) {
                    onStickEventNative(motionEvent.getAxisValue(0), motionEvent.getAxisValue(1), motionEvent.getAxisValue(11), motionEvent.getAxisValue(14));
                }
                return true;
            } else if ((motionEvent.getSource() & InputDeviceCompat.SOURCE_DPAD) == 513) {
                if ((action & 255) == 2) {
                    onDpadEventNative(motionEvent.getAxisValue(0), motionEvent.getAxisValue(1), motionEvent.getEventTime());
                }
                return true;
            }
        }
        return false;
    }

    public void addActivePanel(BasePanel basePanel) {
        if (this.mActivePanels == null) {
            this.mActivePanels = new ArrayList<>();
        }
        if (!this.mActivePanels.contains(basePanel)) {
            this.mActivePanels.add(basePanel);
        }
    }

    public void removeActivePanel(BasePanel basePanel) {
        ArrayList<BasePanel> arrayList = this.mActivePanels;
        if (arrayList != null) {
            arrayList.remove(basePanel);
        }
    }

    public void dismissAllPanels() {
        ArrayList<BasePanel> arrayList = this.mActivePanels;
        if (arrayList != null) {
            for (BasePanel basePanel : arrayList) {
                basePanel.dismiss();
            }
        }
    }

    private void tryEnablingDisplayCutoutMode() {
        View decorView;
        if (Build.VERSION.SDK_INT >= 28 && (decorView = getWindow().getDecorView()) != null) {
            WindowInsets rootWindowInsets = decorView.getRootWindowInsets();
            if (rootWindowInsets == null) {
                WindowManager.LayoutParams attributes = getWindow().getAttributes();
                attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                getWindow().setAttributes(attributes);
            } else if (rootWindowInsets.getDisplayCutout() != null) {
                WindowManager.LayoutParams attributes2 = getWindow().getAttributes();
                attributes2.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                getWindow().setAttributes(attributes2);
            }
        }
    }

    public RelativeLayout getBrigeView() {
        return this.m_relativeLayout;
    }

    public Rect GetSafeAreaInsets() {
        return this.mSafeAreaInsets;
    }

    public static void hideNavigationFullScreen(View view) {
        view.setSystemUiVisibility(5894);
    }

    public static void showNavigationFullScreen(View view) {
        view.setSystemUiVisibility(1792);
    }

    public void onAudioDeviceTypeChanged(AudioDeviceType audioDeviceType) {
        onAudioDeviceTypeChangedNative(audioDeviceType.ordinal());
    }

    public void onCommerceUpdate(boolean z, boolean z2, boolean z3) {
        onCommerceUpdateNative(z, z2, z3);
    }

    public String ResolveTemplateArgs(String str) {
        return ResolveTemplateArgsNative(str);
    }

    public void transformPointToSystem(float f, float f2, RectF rectF) {
        rectF.left += f;
        rectF.right += f;
        float height = ((float) getWindow().getDecorView().getHeight()) - f2;
        rectF.top += height;
        rectF.bottom += height;
    }

    public RectF transformRectToSystem(RectF rectF) {
        return new RectF(rectF);
    }

    public RectF transformRectToProgram(RectF rectF) {
        return new RectF(rectF);
    }

    public String getAppId() {
        return getApplicationInfo().packageName;
    }

    public String getAppName() {
        return "Sky";
    }

    public String getAppProgramLibDir() {
        return getApplicationInfo().nativeLibraryDir;
    }

    public String getOpenedWithURL() {
        Intent intent = getIntent();
        if ("android.intent.action.VIEW".equals(intent.getAction())) {
            return intent.getDataString();
        }
        return null;
    }

    public String getOpenedWithNFC() {
        Intent intent = getIntent();
        if ("android.nfc.action.NDEF_DISCOVERED".equals(intent.getAction())) {
            return intent.getDataString();
        }
        return null;
    }

    public int getDisplayWidth() {
        Rect rect = new Rect();
        getWindow().getDecorView().getLocalVisibleRect(rect);
        return rect.width();
    }

    public int getDisplayHeight() {
        Rect rect = new Rect();
        getWindow().getDecorView().getLocalVisibleRect(rect);
        return rect.height();
    }

    public float getDisplaySizeInInches() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        float f = ((float) displayMetrics.heightPixels) / displayMetrics.ydpi;
        float f2 = ((float) displayMetrics.widthPixels) / displayMetrics.xdpi;
        return (float) Math.sqrt((double) ((f2 * f2) + (f * f)));
    }

    public float getDisplayXdpi() {
        return SMLApplication.skyRes.getDisplayMetrics().xdpi;
    }

    public float getDisplayYdpi() {
        return SMLApplication.skyRes.getDisplayMetrics().ydpi;
    }

    public float getDisplayDensity() {
        return SMLApplication.skyRes.getDisplayMetrics().density;
    }

    public boolean isScreenHdr() {
        return SMLApplication.skyRes.getConfiguration().isScreenHdr();
    }

    public boolean isScreenWideColorGamut() {
        return SMLApplication.skyRes.getConfiguration().isScreenWideColorGamut();
    }

    public float getDesiredMinLum() {
        return getWindowManager().getDefaultDisplay().getHdrCapabilities().getDesiredMinLuminance();
    }

    public float getDesiredMaxLum() {
        return getWindowManager().getDefaultDisplay().getHdrCapabilities().getDesiredMaxLuminance();
    }

    public int getPhysicalMemorySize() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
        return (int) (memoryInfo.totalMem / 1024);
    }

    public String getDeviceName() {
        String string = Settings.Global.getString(getContentResolver(), "device_name");
        if (string == null || string.isEmpty()) {
            string = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
        }
        return (string == null || string.isEmpty()) ? "NO_DEVICE_NAME" : string;
    }

    public String getDeviceModel() {
        String str = Build.MANUFACTURER;
        String str2 = Build.MODEL;
        if (!str.isEmpty()) {
            str = str + " ";
        }
        return str + str2;
    }

    public String getDeviceDescriptionJson(String str) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("brand", Build.MANUFACTURER);
            jSONObject.put("model", Build.MODEL);
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("build_brand", Build.BRAND);
            jSONObject2.put("build_device", Build.DEVICE);
            jSONObject2.put("build_product", Build.PRODUCT);
            jSONObject2.put("gpu", str);
            jSONObject.put("device_extra", jSONObject2);
            return jSONObject.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to generate deviceDescriptionJson", e);
            return "{}";
        }
    }

    public byte[] getDeviceUuid() {
        @SuppressLint("HardwareIds") String string = Settings.Secure.getString(getContentResolver(), "android_id");
        if (string.length() < 16) {
            string = new String(new char[(16 - string.length())]).replace('\0', '0') + string;
        }
        byte[] bArr = new byte[(string.length() / 2)];
        for (int i = 0; i < string.length(); i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character.digit(string.charAt(i + 1), 16));
        }
        return bArr;
    }

    public void playLogoSound() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if(audioManager.isMusicActive()) return;
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(SMLApplication.skyRes.openRawResourceFd(SMLApplication.skyRes.getIdentifier("tgc_logo", "raw", SMLApplication.skyPName)));
            player.prepare();
            (m_mediaPlayer = player).start();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean tryReleaseLogoSound() {
        if(m_mediaPlayer == null) {
            this.m_logoSoundReleased = true;
            return true;
        }
        if (!this.m_logoSoundReleased) {
            if (this.m_mediaPlayer.isPlaying()) {
                return false;
            }
            this.m_mediaPlayer.release();
            this.m_logoSoundReleased = true;
        }
        return true;
    }

    public void fadeoutLogos() {
        runOnUiThread(()->{
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(1000);
            alphaAnimation.setRepeatCount(0);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    logoView.setVisibility(View.GONE);
                    // TODO: Don't remove!
                    git.artdeell.skymodloader.MainActivity.lateInitUserLibs();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            logoView.startAnimation(alphaAnimation);
        });
    }

    public void pressBackButton() {
        moveTaskToBack(true);
    }

    public void finishActivity() {
        finishAndRemoveTask();
        new Timer().schedule(new TimerTask() {
            public void run() {
                System.exit(0);
            }
        }, 5000);
    }

}
