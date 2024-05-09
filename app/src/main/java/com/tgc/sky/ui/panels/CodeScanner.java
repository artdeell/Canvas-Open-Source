package com.tgc.sky.ui.panels;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import com.tgc.sky.GameActivity;
import com.tgc.sky.QrScanner;
import com.tgc.sky.SystemUI_android;
import com.tgc.sky.ui.TextFieldLimiter;
import com.tgc.sky.ui.qrcodereaderview.QRCodeReaderTextureView;
import com.tgc.sky.ui.qrcodereaderview.QRCodeReaderView;
import com.tgc.sky.ui.text.Markup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class CodeScanner extends BasePanel implements GameActivity.OnKeyboardListener, GameActivity.OnActivityResultListener, View.OnLayoutChangeListener, TextView.OnEditorActionListener, QRCodeReaderTextureView.OnQRCodeReadListener {
    private PanelButton _actionButton;
    private PanelButton _closeButton;
    private RelativeLayout _container;
    private RelativeLayout _containerContentView;
    private PanelButton _deleteButton;
    private TextView _headerTextView;
    private ProgressBar mActivityIndicatorView;
    private HashMap<Object, Object> mButtonLocalizedStrings;
    private boolean mCaptureSession;
    public String mCodeInput;
    public String mCodeInputError;
    CodeScannerState mCodeScannerState;
    EnterState mEnterState;
    public Handle mHandler;
    private Intent mImagePicker;
    private boolean mImagePickerActive;
    private ScannerOverlay mImageScannerView;
    ImportState mImportState;
    private Mode mMode;
    private RadioGroup mSegmentedControl;
    private HashMap<Object, Object> mTextAttributedStrings;
    private EditText mTextField;
    TextFieldLimiter mTextFieldLimiter;
    private RelativeLayout mVideoPreview;
    private QRCodeReaderView mVideoPreviewLayer;
    private RelativeLayout view;

    public enum CodeScannerState {
        kCodeScannerState_Idle,
        kCodeScannerState_Parsing,
        kCodeScannerState_Parsed,
        kCodeScannerState_Throttled,
        kCodeScannerState_Error
    }

    public enum EnterState {
        kEnterState_WaitingForUser,
        kEnterState_CheckingCode,
        kEnterState_CheckedCode,
        kEnterState_RateLimited,
        kEnterState_Error
    }

    public interface Handle {
        void run(String str, int i, boolean z);
    }

    public enum ImportState {
        kImportState_CameraAskPermission,
        kImportState_CameraNoPermission,
        kImportState_CameraActive,
        kImportState_PickerActive,
        kImportState_ParsingLink,
        kImportState_ParsedLink,
        kImportState_Error
    }

    public enum Mode {
        kCodeScannerMode_Scan,
        kCodeScannerMode_Type
    }

    public enum ResultOptions {
        kCodeScanner_UserClosedPanel,
        kCodeScanner_ValidateShortCode,
        kCodeScanner_ValidateUrl,
        kCodeScanner_FinishedWithUrl
    }

    public void keyboardDidShow(int i) {
    }

    public void keyboardWillHide(EditText editText) {
    }

    public void setPreviewOrientation() {
    }

    public void informCodeInputResult(final String str, String str2, int i) {
        if (str2 != null) {
            if (str2.equals("throttled")) {
                setCodeScannerState(CodeScannerState.kCodeScannerState_Throttled, str2);
                return;
            } else {
                setCodeScannerState(CodeScannerState.kCodeScannerState_Error, str2);
                return;
            }
        }
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.1
            @Override // java.lang.Runnable
            public void run() {
                CodeScanner.this.dismissInternal();
                CodeScanner.this.mHandler.run(str, ResultOptions.kCodeScanner_FinishedWithUrl.ordinal(), true);
            }
        });
    }

    public void setCodeScannerState(CodeScannerState codeScannerState, String str) {
        this.mCodeScannerState = codeScannerState;
        if (codeScannerState == CodeScannerState.kCodeScannerState_Idle) {
            this.mCodeInput = str;
        } else if (codeScannerState != CodeScannerState.kCodeScannerState_Error && str != null) {
            this.mCodeInput = str;
        }
        if (codeScannerState == CodeScannerState.kCodeScannerState_Error && str != null) {
            this.mCodeInputError = str;
        }
        updateState();
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.2
            @Override // java.lang.Runnable
            public void run() {
                CodeScanner.this.updateView();
            }
        });
    }

    public void updateState() {
        int i = AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$Mode[this.mMode.ordinal()];
        if (i == 1) {
            int i2 = AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$CodeScannerState[this.mCodeScannerState.ordinal()];
            if (i2 == 1) {
                this.mEnterState = EnterState.kEnterState_WaitingForUser;
            } else if (i2 == 2) {
                this.mEnterState = EnterState.kEnterState_CheckingCode;
            } else if (i2 == 3) {
                this.mEnterState = EnterState.kEnterState_CheckedCode;
            } else if (i2 == 4) {
                this.mEnterState = EnterState.kEnterState_RateLimited;
            } else if (i2 != 5) {
            } else {
                this.mEnterState = EnterState.kEnterState_Error;
            }
        } else if (i != 2) {
        } else {
            int i3 = AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$CodeScannerState[this.mCodeScannerState.ordinal()];
            if (i3 != 1) {
                if (i3 == 2) {
                    this.mImportState = ImportState.kImportState_ParsingLink;
                } else if (i3 == 3) {
                    this.mImportState = ImportState.kImportState_ParsedLink;
                } else if (i3 == 4 || i3 == 5) {
                    this.mImportState = ImportState.kImportState_Error;
                }
            } else if (this.mImagePickerActive) {
                this.mImportState = ImportState.kImportState_PickerActive;
            } else if (this.m_activity.checkSelfPermissions(new String[]{"android.permission.CAMERA"})) {
                this.mImportState = ImportState.kImportState_CameraActive;
                startReading();
            } else if (this.mImportState != ImportState.kImportState_CameraNoPermission) {
                this.mImportState = ImportState.kImportState_CameraAskPermission;
            }
        }
    }

    private static void SetMarginsH(View view, int i, int i2, int i3, int i4, int i5) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i, i2);
        if (i5 != 0) {
            layoutParams.gravity = i5;
        }
        if (i3 != 0) {
            layoutParams.leftMargin = i3;
        }
        if (i4 != 0) {
            layoutParams.rightMargin = i4;
        }
        view.setLayoutParams(layoutParams);
    }

    private static void SetMarginsH(View view, int i, int i2, int i3, int i4) {
        SetMarginsH(view, i, i2, i3, i4, 0);
    }

    private static void SetMarginsV(View view, int i, int i2, int i3, int i4, int i5) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i, i2);
        layoutParams.gravity = i5;
        if (i3 != 0) {
            layoutParams.topMargin = i3;
        }
        if (i4 != 0) {
            layoutParams.bottomMargin = i4;
        }
        view.setLayoutParams(layoutParams);
    }

    private static void SetMarginsV(View view, int i, int i2, int i3, int i4) {
        SetMarginsV(view, i, i2, i3, i4, 1);
    }

    private LinearLayout MakeLayout(int i, int i2) {
        LinearLayout linearLayout = new LinearLayout(this.m_activity);
        linearLayout.setOrientation(i);
        linearLayout.setGravity(i2);
        return linearLayout;
    }

    private Space MakeSpace() {
        Space space = new Space(this.m_activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 1);
        layoutParams.weight = 1.0f;
        space.setLayoutParams(layoutParams);
        return space;
    }

    public CodeScanner(Context context, SystemUI_android systemUI_android, Markup markup, Mode mode, Handle handle) {
        super(context, systemUI_android, markup);
        this.mHandler = handle;
        this.mMode = mode;
        int i = 0;
        this.mImagePickerActive = false;
        this.mCodeScannerState = CodeScannerState.kCodeScannerState_Idle;
        this.mImportState = ImportState.kImportState_CameraAskPermission;
        this.mTextAttributedStrings = new HashMap<>();
        this.mButtonLocalizedStrings = new HashMap<>();
        TextFieldLimiter textFieldLimiter = new TextFieldLimiter();
        this.mTextFieldLimiter = textFieldLimiter;
        textFieldLimiter.maxCharacters = 14;
        this.mTextFieldLimiter.maxByteSize = 14;
        updateState();
        setBackgroundDrawable(new ColorDrawable(0));
        RelativeLayout relativeLayout = new RelativeLayout(this.m_activity);
        this.view = relativeLayout;
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        setContentView(this.view);
        int i2 = 1;
        setFocusable(true);
        setWidth(-1);
        setHeight(-1);
        this.view.addOnLayoutChangeListener(this);
        RelativeLayout relativeLayout2 = new RelativeLayout(this.m_activity);
        this._container = relativeLayout2;
        this._containerContentView = relativeLayout2;
        GradientDrawable gradientDrawable = (GradientDrawable) relativeLayout2.getBackground();
        if (gradientDrawable == null) {
            gradientDrawable = new GradientDrawable();
            this._container.setBackground(gradientDrawable);
        }
        gradientDrawable.setColor(-3355444);
        gradientDrawable.setAlpha(216);
        float f = 12.0f;
        gradientDrawable.setCornerRadius(dp2px(12.0f));
        this.view.addView(this._container);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.m_activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int i3 = displayMetrics.heightPixels;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) (displayMetrics.widthPixels * 0.9f), -2);
        layoutParams.addRule(14);
        layoutParams.addRule(13);
        layoutParams.addRule(15);
        this._container.setLayoutParams(layoutParams);
        LinearLayout MakeLayout = MakeLayout(1, 17);
        LinearLayout MakeLayout2 = MakeLayout(0, 17);
        SetMarginsV(MakeLayout2, -1, -2, dp2px(16.0f), 0, 80);
        LinearLayout MakeLayout3 = MakeLayout(1, 0);
        SetMarginsH(MakeLayout3, -1, -1, 0, 0);
        ConstraintLayout constraintLayout = new ConstraintLayout(this.m_activity);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.gravity = 80;
        constraintLayout.setLayoutParams(layoutParams2);
        ArrayList<String> arrayList = new ArrayList<String>() { // from class: com.tgc.sky.ui.panels.CodeScanner.3
            {
                add(CodeScanner.this.m_systemUI.LocalizeString("code_scanner_scan_mode_title"));
                add(CodeScanner.this.m_systemUI.LocalizeString("code_scanner_type_mode_title"));
            }
        };
        int i4 = this.mMode == Mode.kCodeScannerMode_Type ? 1 : 0;
        RadioGroup radioGroup = new RadioGroup(this.m_activity);
        this.mSegmentedControl = radioGroup;
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        int i5 = 0;
        while (i5 < arrayList.size()) {
            RadioButton radioButton = new RadioButton(this.m_activity);
            radioButton.setText(arrayList.get(i5));
            radioButton.setButtonDrawable((Drawable) null);
            this.mSegmentedControl.addView(radioButton);
            float AppleConvertAndroidScale = GameActivity.AppleConvertAndroidScale(f);
            int i6 = i5 == 0 ? i2 : i;
            int i7 = i5 == arrayList.size() - i2 ? i2 : i;
            float f2 = i6 != 0 ? AppleConvertAndroidScale : 0.0f;
            float f3 = i7 != 0 ? AppleConvertAndroidScale : 0.0f;
            float f4 = i7 != 0 ? AppleConvertAndroidScale : 0.0f;
            AppleConvertAndroidScale = i6 == 0 ? 0.0f : AppleConvertAndroidScale;
            GradientDrawable gradientDrawable2 = new GradientDrawable();
            float[] fArr = new float[8];
            fArr[i] = f2;
            fArr[i2] = f2;
            fArr[2] = f3;
            fArr[3] = f3;
            fArr[4] = f4;
            fArr[5] = f4;
            fArr[6] = AppleConvertAndroidScale;
            fArr[7] = AppleConvertAndroidScale;
            gradientDrawable2.setCornerRadii(fArr);
            gradientDrawable2.setColor(ViewCompat.MEASURED_STATE_MASK);
            GradientDrawable gradientDrawable3 = new GradientDrawable();
            float[] fArr2 = new float[8];
            fArr2[i] = f2;
            fArr2[1] = f2;
            fArr2[2] = f3;
            fArr2[3] = f3;
            fArr2[4] = f4;
            fArr2[5] = f4;
            fArr2[6] = AppleConvertAndroidScale;
            fArr2[7] = AppleConvertAndroidScale;
            gradientDrawable3.setCornerRadii(fArr2);
            gradientDrawable3.setStroke(4, ViewCompat.MEASURED_STATE_MASK);
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{16842912}, gradientDrawable2);
            stateListDrawable.addState(new int[]{-16842912}, gradientDrawable3);
            radioButton.setBackground(stateListDrawable);
            radioButton.setTextColor(new ColorStateList(new int[][]{new int[]{16842912}, new int[]{-16842912}}, new int[]{-1, ViewCompat.MEASURED_STATE_MASK}));
            radioButton.setChecked(i5 == i4);
            radioButton.setPadding(32, 8, 32, 8);
            i5++;
            i = 0;
            i2 = 1;
            f = 12.0f;
        }
        SetMarginsV(this.mSegmentedControl, -2, -2, 0, 0);
        this.mSegmentedControl.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { // from class: com.tgc.sky.ui.panels.CodeScanner.4
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public void onCheckedChanged(RadioGroup radioGroup2, int i8) {
                CodeScanner.this.onSegmentAction(radioGroup2, i8);
            }
        });
        TextView textView = new TextView(this.m_activity);
        this._headerTextView = textView;
        textView.setGravity(GravityCompat.START);
        this._headerTextView.setTextIsSelectable(true);
        this._headerTextView.setBackgroundColor(0);
        SetMarginsH(this._headerTextView, -2, -2, 0, 0, 8388659);
        this.mTextField = new EditText(this.m_activity);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.topMargin = dp2px(16.0f);
        layoutParams3.bottomMargin = dp2px(16.0f);
        this.mTextField.setLayoutParams(layoutParams3);
        this.mTextField.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.mTextField.setTypeface(systemUI_android.DefaultFont(), Typeface.NORMAL);
        this.mTextField.setTextSize(20.0f);
        this.mTextField.setOnEditorActionListener(this);
        this.mTextField.setHint(systemUI_android.LocalizeString("code_scanner_enter_code_hint"));
        GradientDrawable gradientDrawable4 = new GradientDrawable();
        this.mTextField.setBackground(gradientDrawable4);
        gradientDrawable4.setCornerRadius(dp2px(8.0f));
        gradientDrawable4.setColor(-1);
        gradientDrawable4.setAlpha(76);
        this.mTextField.setInputType(524289);
        this.mTextField.setGravity(GravityCompat.START);
        this.mTextField.setPadding(dp2px(16.0f), dp2px(8.0f), dp2px(16.0f), dp2px(8.0f));
        EditText editText = this.mTextField;
        editText.setImeOptions(editText.getImeOptions() | 6);
        this.mTextField.setSingleLine();
        this.mTextField.addTextChangedListener(new TextWatcher() { // from class: com.tgc.sky.ui.panels.CodeScanner.5
            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i8, int i9, int i10) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i8, int i9, int i10) {
                CodeScanner.this.textFieldEditingChanged(charSequence);
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                CodeScanner.this.textFieldEditingChanged(editable);
            }
        });
        this.mTextField.setFilters(new InputFilter[]{this.mTextFieldLimiter});
        ProgressBar progressBar = new ProgressBar(this.m_activity, null, 16842873);
        this.mActivityIndicatorView = progressBar;
        progressBar.setIndeterminate(true);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(dp2px(32.0f), dp2px(32.0f));
        layoutParams4.gravity = 17;
        layoutParams4.topMargin = dp2px(16.0f);
        layoutParams4.bottomMargin = dp2px(16.0f);
        this.mActivityIndicatorView.setLayoutParams(layoutParams4);
        ScannerOverlay scannerOverlay = new ScannerOverlay(this.m_activity);
        this.mImageScannerView = scannerOverlay;
        SetMarginsH(scannerOverlay, -2, -2, 0, dp2px(16.0f), 80);
        PanelButton panelButton = new PanelButton(this.m_activity, new View.OnClickListener() { // from class: com.tgc.sky.ui.panels.CodeScanner.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                CodeScanner.this.onCloseButton();
            }
        });
        this._closeButton = panelButton;
        panelButton.setId(View.generateViewId());
        this._closeButton.setLayoutParams(new ConstraintLayout.LayoutParams(-2, -2));
        PanelButton panelButton2 = new PanelButton(this.m_activity, new View.OnClickListener() { // from class: com.tgc.sky.ui.panels.CodeScanner.7
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                CodeScanner.this.onDeleteButton();
            }
        });
        this._deleteButton = panelButton2;
        panelButton2.setId(View.generateViewId());
        this._deleteButton.setLayoutParams(new ConstraintLayout.LayoutParams(-2, -2));
        PanelButton panelButton3 = new PanelButton(this.m_activity, new View.OnClickListener() { // from class: com.tgc.sky.ui.panels.CodeScanner.8
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                CodeScanner.this.onActionButton();
            }
        });
        this._actionButton = panelButton3;
        panelButton3.setId(View.generateViewId());
        this._actionButton.setLayoutParams(new ConstraintLayout.LayoutParams(-2, -2));
        MakeLayout.addView(this.mSegmentedControl);
        MakeLayout.addView(MakeLayout2);
        MakeLayout2.addView(this.mImageScannerView);
        MakeLayout2.addView(MakeLayout3);
        MakeLayout3.addView(this._headerTextView);
        MakeLayout3.addView(MakeSpace());
        MakeLayout3.addView(this.mTextField);
        MakeLayout3.addView(this.mActivityIndicatorView);
        MakeLayout3.addView(MakeSpace());
        MakeLayout3.addView(constraintLayout);
        constraintLayout.addView(this._closeButton);
        constraintLayout.addView(this._deleteButton);
        constraintLayout.addView(this._actionButton);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(this._closeButton.getId(), 1, 0, 1);
        constraintSet.connect(this._deleteButton.getId(), 1, this._closeButton.getId(), 2);
        constraintSet.connect(this._actionButton.getId(), 1, this._deleteButton.getId(), 2);
        constraintSet.connect(this._actionButton.getId(), 2, 0, 2);
        constraintSet.createHorizontalChain(0, 1, 0, 2, new int[]{this._closeButton.getId(), this._deleteButton.getId(), this._actionButton.getId()}, null, 1);
        constraintSet.applyTo(constraintLayout);
        RelativeLayout.LayoutParams layoutParams5 = new RelativeLayout.LayoutParams(-1, -2);
        layoutParams5.addRule(9, -1);
        layoutParams5.addRule(11, -1);
        layoutParams5.leftMargin = dp2px(32.0f);
        layoutParams5.bottomMargin = dp2px(32.0f);
        layoutParams5.topMargin = dp2px(32.0f);
        layoutParams5.rightMargin = dp2px(32.0f);
        MakeLayout.setLayoutParams(layoutParams5);
        this._containerContentView.addView(MakeLayout);
        Intent intent = new Intent("android.intent.action.PICK");
        this.mImagePicker = intent;
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        this.view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.tgc.sky.ui.panels.CodeScanner.9
            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                CodeScanner.this.view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                CodeScanner.this.viewWillAppear();
            }
        });
        this.view.post(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.10
            @Override // java.lang.Runnable
            public void run() {
                CodeScanner.this.updateView();
            }
        });
    }

    public void viewWillAppear() {
        this.m_activity.addOnKeyboardListener(this);
    }

    @Override // com.tgc.sky.GameActivity.OnKeyboardListener
    public void onKeyboardChange(boolean z, int i) {
        if (z) {
            keyboardDidShow(i);
        } else {
            keyboardWillHide(this.mTextField);
        }
    }

    @Override // com.tgc.sky.ui.panels.BasePanel, android.widget.PopupWindow
    public void dismiss() {
        if (this._closeButton.isEnabled()) {
            dismissInternal();
            this.mHandler.run(null, ResultOptions.kCodeScanner_UserClosedPanel.ordinal(), true);
        }
    }

    public void dismissInternal() {
        super.dismiss();
        viewDidDisappear();
    }

    public void viewDidDisappear() {
        this.m_activity.RemoveOnKeyboardListener(this);
    }

    public void updateView() {
        int i = AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$Mode[this.mMode.ordinal()];
        if (i == 1) {
            stopReading();
            int i2 = AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$EnterState[this.mEnterState.ordinal()];
            if (i2 == 1) {
                this.mSegmentedControl.setEnabled(true);
                this._headerTextView.setText(getAttributedString("code_scanner_enter_code_manually"), TextView.BufferType.SPANNABLE);
                this.mActivityIndicatorView.setVisibility(View.GONE);
                this.mTextField.postDelayed(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.11
                    @Override // java.lang.Runnable
                    public void run() {
                        CodeScanner.this.mTextField.setVisibility(View.VISIBLE);
                        CodeScanner.this.mTextField.setEnabled(true);
                    }
                }, 500L);
                this.mImageScannerView.setInviteLink(null, false);
                String str = this.mCodeInput;
                setButton(this._actionButton, str != null && str.trim().length() > 2, "system_button_check", false);
                setButton(this._deleteButton, false, null, false);
                setButton(this._closeButton, true, "system_button_close", false);
            } else if (i2 == 2) {
                this.mSegmentedControl.setEnabled(false);
                this._headerTextView.setText(getAttributedString("code_scanner_checking_code"), TextView.BufferType.SPANNABLE);
                this.mActivityIndicatorView.setVisibility(View.VISIBLE);
                this.mTextField.postDelayed(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.12
                    @Override // java.lang.Runnable
                    public void run() {
                        CodeScanner.this.mTextField.setVisibility(View.VISIBLE);
                        CodeScanner.this.mTextField.setEnabled(false);
                    }
                }, 500L);
                this.mImageScannerView.setInviteLink(null, false);
                setButton(this._actionButton, false, "system_button_check", false);
                setButton(this._deleteButton, false, null, false);
                setButton(this._closeButton, true, "system_button_close", false);
            } else if (i2 == 3) {
                this.mSegmentedControl.setEnabled(true);
                this._headerTextView.setText(getAttributedString("code_scanner_code_confirm_shortcode"), TextView.BufferType.SPANNABLE);
                this.mActivityIndicatorView.setVisibility(View.GONE);
                this.mTextField.setVisibility(View.VISIBLE);
                this.mTextField.setEnabled(true);
                this.mImageScannerView.setInviteLink(null, false);
                setButton(this._actionButton, true, "system_button_confirm", false);
                setButton(this._deleteButton, false, null, true);
                setButton(this._closeButton, true, "system_button_close", false);
            } else if (i2 == 4) {
                this.mSegmentedControl.setEnabled(true);
                this._headerTextView.setText(getAttributedString("error_server_ratelimit"), TextView.BufferType.SPANNABLE);
                this.mActivityIndicatorView.setVisibility(View.GONE);
                this.mTextField.setVisibility(View.VISIBLE);
                this.mTextField.setEnabled(true);
                this.mImageScannerView.setInviteLink(null, false);
                setButton(this._actionButton, false, "system_button_check", false);
                setButton(this._deleteButton, false, null, true);
                setButton(this._closeButton, true, "system_button_close", false);
            } else if (i2 != 5) {
            } else {
                this.mSegmentedControl.setEnabled(true);
                this._headerTextView.setText(getAttributedString(this.mCodeInputError), TextView.BufferType.SPANNABLE);
                this.mActivityIndicatorView.setVisibility(View.GONE);
                this.mTextField.postDelayed(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.13
                    @Override // java.lang.Runnable
                    public void run() {
                        CodeScanner.this.mTextField.setVisibility(View.VISIBLE);
                        CodeScanner.this.mTextField.setEnabled(true);
                    }
                }, 500L);
                this.mImageScannerView.setInviteLink(null, false);
                setButton(this._actionButton, false, "system_button_check", false);
                setButton(this._deleteButton, false, null, true);
                setButton(this._closeButton, true, "system_button_close", false);
            }
        } else if (i != 2) {
        } else {
            switch (AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState[this.mImportState.ordinal()]) {
                case 1:
                    this.mSegmentedControl.setEnabled(true);
                    stopReading();
                    this._headerTextView.setText(getAttributedString("code_scanner_ask_camera_permission"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.GONE);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(null, true);
                    setButton(this._actionButton, true, "system_button_import", false);
                    setButton(this._deleteButton, true, "system_button_ask", false);
                    setButton(this._closeButton, true, "system_button_close", false);
                    return;
                case 2:
                    this.mSegmentedControl.setEnabled(true);
                    stopReading();
                    this._headerTextView.setText(getAttributedString("code_scanner_denied_camera_permission"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.GONE);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(null, true);
                    setButton(this._actionButton, true, "system_button_import", false);
                    setButton(this._deleteButton, true, "system_button_settings", false);
                    setButton(this._closeButton, true, "system_button_close", false);
                    return;
                case 3:
                    this.mSegmentedControl.setEnabled(true);
                    this._headerTextView.setText(getAttributedString("code_scanner_camera_scanning"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.GONE);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(null, true);
                    setButton(this._actionButton, true, "system_button_import", false);
                    setButton(this._deleteButton, false, null, false);
                    setButton(this._closeButton, true, "system_button_close", false);
                    startReading();
                    return;
                case 4:
                    this.mSegmentedControl.setEnabled(true);
                    stopReading();
                    this._headerTextView.setText(getAttributedString("invite_incoming_picking_00"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.postDelayed(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.14
                        @Override // java.lang.Runnable
                        public void run() {
                            CodeScanner.this.mActivityIndicatorView.setVisibility(View.VISIBLE);
                        }
                    }, 500L);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(null, true);
                    setButton(this._actionButton, false, "system_button_import", false);
                    setButton(this._deleteButton, false, null, false);
                    setButton(this._closeButton, false, "system_button_close", false);
                    return;
                case 5:
                    this.mSegmentedControl.setEnabled(true);
                    stopReading();
                    this._headerTextView.setText(getAttributedString("code_scanner_verifying_code"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.postDelayed(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.15
                        @Override // java.lang.Runnable
                        public void run() {
                            CodeScanner.this.mActivityIndicatorView.setVisibility(View.VISIBLE);
                        }
                    }, 500L);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(this.mCodeInput, true);
                    setButton(this._actionButton, false, "system_button_accept", false);
                    setButton(this._deleteButton, false, "system_button_dismiss", true);
                    setButton(this._closeButton, false, "system_button_close", false);
                    return;
                case 6:
                    this.mSegmentedControl.setEnabled(true);
                    stopReading();
                    this._headerTextView.setText(getAttributedString("code_scanner_code_confirm_qr"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.GONE);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(this.mCodeInput, true);
                    setButton(this._actionButton, true, "system_button_check", false);
                    setButton(this._deleteButton, false, null, true);
                    setButton(this._closeButton, true, "system_button_close", false);
                    return;
                case 7:
                    this.mSegmentedControl.setEnabled(true);
                    stopReading();
                    this._headerTextView.setText(getAttributedString(this.mCodeInputError), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.GONE);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(this.mCodeInput, true);
                    setButton(this._actionButton, true, "system_button_confirm", false);
                    setButton(this._deleteButton, false, null, true);
                    setButton(this._closeButton, true, "system_button_close", false);
                    return;
                default:
                    return;
            }
        }
    }

    public static /* synthetic */ class AnonymousClass20 {
        static final /* synthetic */ int[] $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$CodeScannerState;
        static final /* synthetic */ int[] $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$EnterState;
        static final /* synthetic */ int[] $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState;
        static final /* synthetic */ int[] $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$Mode;

        static {
            int[] iArr = new int[ImportState.values().length];
            $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState = iArr;
            try {
                iArr[ImportState.kImportState_CameraAskPermission.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState[ImportState.kImportState_CameraNoPermission.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState[ImportState.kImportState_CameraActive.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState[ImportState.kImportState_PickerActive.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState[ImportState.kImportState_ParsingLink.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState[ImportState.kImportState_ParsedLink.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState[ImportState.kImportState_Error.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            int[] iArr2 = new int[EnterState.values().length];
            $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$EnterState = iArr2;
            try {
                iArr2[EnterState.kEnterState_WaitingForUser.ordinal()] = 1;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$EnterState[EnterState.kEnterState_CheckingCode.ordinal()] = 2;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$EnterState[EnterState.kEnterState_CheckedCode.ordinal()] = 3;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$EnterState[EnterState.kEnterState_RateLimited.ordinal()] = 4;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$EnterState[EnterState.kEnterState_Error.ordinal()] = 5;
            } catch (NoSuchFieldError unused12) {
            }
            int[] iArr3 = new int[Mode.values().length];
            $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$Mode = iArr3;
            try {
                iArr3[Mode.kCodeScannerMode_Type.ordinal()] = 1;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$Mode[Mode.kCodeScannerMode_Scan.ordinal()] = 2;
            } catch (NoSuchFieldError unused14) {
            }
            int[] iArr4 = new int[CodeScannerState.values().length];
            $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$CodeScannerState = iArr4;
            try {
                iArr4[CodeScannerState.kCodeScannerState_Idle.ordinal()] = 1;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$CodeScannerState[CodeScannerState.kCodeScannerState_Parsing.ordinal()] = 2;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$CodeScannerState[CodeScannerState.kCodeScannerState_Parsed.ordinal()] = 3;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$CodeScannerState[CodeScannerState.kCodeScannerState_Throttled.ordinal()] = 4;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$com$tgc$sky$ui$panels$CodeScanner$CodeScannerState[CodeScannerState.kCodeScannerState_Error.ordinal()] = 5;
            } catch (NoSuchFieldError unused19) {
            }
        }
    }

    @SuppressLint("RestrictedApi")
    public void setButton(PanelButton panelButton, boolean z, String str, boolean z2) {
        if (str != null) {
            String str2 = (String) this.mButtonLocalizedStrings.get(str);
            if (str2 == null) {
                str2 = this.m_systemUI.LocalizeString(str);
                this.mButtonLocalizedStrings.put(str, str2);
            }
            if (z2) {
                panelButton.setEnabledText(str2, SupportMenu. CATEGORY_MASK);
                panelButton.setDisabledText(str2);
            } else {
                panelButton.setText(str2);
            }
            panelButton.setEnabled(z);
            panelButton.setVisibility(View.VISIBLE);
            return;
        }
        panelButton.setVisibility(View.GONE);
        panelButton.setEnabled(false);
    }

    public Spannable getAttributedString(String str) {
        Spannable spannable = (Spannable) this.mTextAttributedStrings.get(str);
        if (spannable == null) {
            SpannableStringBuilder GetMarkedUpString = this.m_systemUI.GetMarkedUpString(this.m_systemUI.LocalizeString(str), new ArrayList<>(Arrays.asList(this.m_markup.DefaultFontGame(14.0f))), false);
            this.mTextAttributedStrings.put(str, GetMarkedUpString);
            return GetMarkedUpString;
        }
        return spannable;
    }

    public void onSegmentAction(RadioGroup radioGroup, int i) {
        this.view.performHapticFeedback(3);
        int i2 = 0;
        while (true) {
            if (i2 >= this.mSegmentedControl.getChildCount()) {
                i2 = -1;
                break;
            } else if (((RadioButton) this.mSegmentedControl.getChildAt(i2)).isChecked()) {
                break;
            } else {
                i2++;
            }
        }
        int i3 = AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$Mode[this.mMode.ordinal()];
        if (i3 == 1) {
            if (i2 == 0) {
                this.mMode = Mode.kCodeScannerMode_Scan;
                setCodeScannerState(CodeScannerState.kCodeScannerState_Idle, null);
            }
        } else if (i3 == 2 && i2 == 1) {
            this.mMode = Mode.kCodeScannerMode_Type;
            setCodeScannerState(CodeScannerState.kCodeScannerState_Idle, null);
        }
    }

    public void onActionButton() {
        this.view.performHapticFeedback(3);
        int i = AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$Mode[this.mMode.ordinal()];
        if (i == 1) {
            if (this.mCodeInput != null) {
                int i2 = AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$EnterState[this.mEnterState.ordinal()];
                if (i2 == 1) {
                    setCodeScannerState(CodeScannerState.kCodeScannerState_Parsing, null);
                    this.mHandler.run(this.mCodeInput, ResultOptions.kCodeScanner_ValidateShortCode.ordinal(), false);
                } else if (i2 != 3) {
                } else {
                    dismissInternal();
                    this.mHandler.run(this.mCodeInput, ResultOptions.kCodeScanner_FinishedWithUrl.ordinal(), true);
                }
            }
        } else if (i != 2) {
        } else {
            if (this.mCodeInput != null) {
                int i3 = AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState[this.mImportState.ordinal()];
                if (i3 == 6) {
                    dismissInternal();
                    this.mHandler.run(this.mCodeInput, ResultOptions.kCodeScanner_FinishedWithUrl.ordinal(), true);
                } else if (i3 != 7) {
                } else {
                    dismissInternal();
                    this.mHandler.run(null, ResultOptions.kCodeScanner_UserClosedPanel.ordinal(), true);
                }
            } else if (AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$ImportState[this.mImportState.ordinal()] == 7) {
                setCodeScannerState(CodeScannerState.kCodeScannerState_Idle, null);
            } else {
                this.mImagePickerActive = true;
                updateState();
                updateView();
                this.m_activity.AddOnActivityResultListener(this);
                this.m_activity.startActivityForResult(this.mImagePicker, GameActivity.ActivityRequestCode.IMAGE_PICKER);
            }
        }
    }

    @Override // com.tgc.sky.GameActivity.OnActivityResultListener
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 130) {
            if (intent != null) {
                imagePickerController(i, i2, intent);
            } else {
                imagePickerControllerDidCancel();
            }
            this.m_activity.RemoveOnActivityResultListeners(this);
        }
    }

    public void onDeleteButton() {
        this.view.performHapticFeedback(7);
        int i = AnonymousClass20.$SwitchMap$com$tgc$sky$ui$panels$CodeScanner$Mode[this.mMode.ordinal()];
        if (i == 1 || i == 2) {
            if (this.mCodeInput != null) {
                dismissInternal();
                this.mHandler.run(null, ResultOptions.kCodeScanner_UserClosedPanel.ordinal(), true);
            } else if (this.mImportState == ImportState.kImportState_Error) {
                setCodeScannerState(CodeScannerState.kCodeScannerState_Idle, null);
            } else if (this.mImportState == ImportState.kImportState_CameraNoPermission) {
                this.m_activity.requestPermissionsThroughSettings(new String[]{"android.permission.CAMERA"}, new GameActivity.PermissionCallback() { // from class: com.tgc.sky.ui.panels.CodeScanner.16
                    @Override // com.tgc.sky.GameActivity.PermissionCallback
                    public void onPermissionResult(String[] strArr, int[] iArr) {
                        CodeScanner.this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.16.1
                            @Override // java.lang.Runnable
                            public void run() {
                                CodeScanner.this.updateState();
                                CodeScanner.this.updateView();
                            }
                        });
                    }
                });
            } else {
                this.m_activity.requestPermissions(new String[]{"android.permission.CAMERA"}, new GameActivity.PermissionCallback() { // from class: com.tgc.sky.ui.panels.CodeScanner.17
                    @Override // com.tgc.sky.GameActivity.PermissionCallback
                    public void onPermissionResult(final String[] strArr, final int[] iArr) {
                        CodeScanner.this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.17.1
                            @Override // java.lang.Runnable
                            public void run() {
                                if (!CodeScanner.this.m_activity.checkResultPermissions(iArr) && !CodeScanner.this.m_activity.shouldShowRequestPermissionsRationale(strArr)) {
                                    CodeScanner.this.mImportState = ImportState.kImportState_CameraNoPermission;
                                }
                                CodeScanner.this.updateState();
                                CodeScanner.this.updateView();
                            }
                        });
                    }
                });
            }
        }
    }

    public void onCloseButton() {
        this.view.performHapticFeedback(7);
        dismissInternal();
        this.mHandler.run(null, ResultOptions.kCodeScanner_UserClosedPanel.ordinal(), true);
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == 6 && this.mMode == Mode.kCodeScannerMode_Type && this._actionButton.isEnabled()) {
            setCodeScannerState(CodeScannerState.kCodeScannerState_Parsing, textView.getText().toString());
            this.mHandler.run(this.mCodeInput, ResultOptions.kCodeScanner_ValidateShortCode.ordinal(), false);
        }
        ((InputMethodManager) this.m_activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(textView.getWindowToken(), 0);
        GameActivity.hideNavigationFullScreen(this.m_activity.getBrigeView());
        updateState();
        updateView();
        return false;
    }

    public void textFieldEditingChanged(CharSequence charSequence) {
        setCodeScannerState(CodeScannerState.kCodeScannerState_Idle, charSequence.toString());
    }

    public void viewWillTransitionToSize() {
        setPreviewOrientation();
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        viewWillLayoutSubviews();
    }

    public void viewWillLayoutSubviews() {
        setPreviewOrientation();
    }

    public void imagePickerController(int r4, int r5, Intent intent) {
        boolean z;
        ContentResolver contentResolver = this.m_activity.getContentResolver();
        Uri data = intent.getData();
        if (data != null) {
            Log.d("InvitationPanel", "Opening " + data.toString());
            Bitmap decodeStream = null;
            try {
                decodeStream = BitmapFactory.decodeStream(contentResolver.openInputStream(data));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (decodeStream != null) {
                String scanImage = QrScanner.scanImage(decodeStream);
                if (scanImage != null) {
                    setCodeScannerState(CodeScannerState.kCodeScannerState_Parsing, scanImage);
                    this.mHandler.run(this.mCodeInput, ResultOptions.kCodeScanner_ValidateUrl.ordinal(), false);
                    z = true;
                    this.mImagePickerActive = false;
                    if (!z) {
                        setCodeScannerState(CodeScannerState.kCodeScannerState_Error, "code_scanner_error_image_parsing");
                    }
                    updateState();
                    updateView();
                }
            } else {
                Log.d("InvitationPanel", "PickedImage was null");
            }
        } else {
            Log.d("InvitationPanel", "Uri was null");
        }
        z = false;
        this.mImagePickerActive = false;
        if (!z) {
        }
        updateState();
        updateView();
    }

    public void imagePickerControllerDidCancel() {
        this.mImagePickerActive = false;
        updateState();
        updateView();
    }

    public boolean startReading() {
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.18
            @Override // java.lang.Runnable
            public void run() {
                if (CodeScanner.this.mCaptureSession || CodeScanner.this._containerContentView == null) {
                    return;
                }
                CodeScanner codeScanner = CodeScanner.this;
                codeScanner.mVideoPreview = codeScanner._containerContentView;
                CodeScanner.this.mCaptureSession = true;
                CodeScanner.this._containerContentView.post(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.18.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (CodeScanner.this.mCaptureSession) {
                            CodeScanner.this.mVideoPreviewLayer = new QRCodeReaderView(CodeScanner.this.m_activity);
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(CodeScanner.this.mVideoPreview.getWidth(), CodeScanner.this.mVideoPreview.getHeight());
                            layoutParams.addRule(14);
                            layoutParams.addRule(13);
                            layoutParams.addRule(15);
                            CodeScanner.this.mVideoPreviewLayer.setLayoutParams(layoutParams);
                            CodeScanner.this.mVideoPreviewLayer.setForegroundGravity(17);
                            CodeScanner.this.mVideoPreviewLayer.setZ(-2.0f);
                            CodeScanner.this.mVideoPreviewLayer.setAlpha(0.2f);
                            CodeScanner.this.mVideoPreviewLayer.setRadius(((GradientDrawable) CodeScanner.this.mVideoPreview.getBackground()).getCornerRadius());
                            CodeScanner.this.mVideoPreview.addView(CodeScanner.this.mVideoPreviewLayer);
                            CodeScanner.this.mVideoPreviewLayer.setOnQRCodeReadListener(CodeScanner.this);
                            CodeScanner.this.view.invalidate();
                        }
                    }
                });
            }
        });
        return true;
    }

    public void stopReading() {
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.ui.panels.CodeScanner.19
            @Override // java.lang.Runnable
            public void run() {
                if (CodeScanner.this.mCaptureSession) {
                    if (CodeScanner.this.mVideoPreviewLayer != null) {
                        CodeScanner.this.mVideoPreviewLayer.stopCamera();
                        ((RelativeLayout) CodeScanner.this.mVideoPreviewLayer.getParent()).removeView(CodeScanner.this.mVideoPreviewLayer);
                    }
                    CodeScanner.this.mCaptureSession = false;
                    CodeScanner.this.mVideoPreviewLayer = null;
                }
            }
        });
    }

    @Override // com.tgc.sky.ui.qrcodereaderview.QRCodeReaderTextureView.OnQRCodeReadListener
    public void onBeginDetect() {}

    @Override // com.tgc.sky.ui.qrcodereaderview.QRCodeReaderTextureView.OnQRCodeReadListener
    public boolean onQRCodeRead(String str) {
        try {
            captureOutput(str);
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean captureOutput(String str) throws UnsupportedEncodingException {
        boolean z = str != null && str.startsWith("https://sky") && str.indexOf(".thatg.co/?") >= 0;
        if (z && this.mCodeInput == null) {
            setCodeScannerState(CodeScannerState.kCodeScannerState_Parsing, str);
            this.mHandler.run(this.mCodeInput, ResultOptions.kCodeScanner_ValidateUrl.ordinal(), false);
        }
        return z;
    }
}
