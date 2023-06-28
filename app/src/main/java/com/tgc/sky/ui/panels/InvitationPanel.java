// 
// Decompiled by Procyon v0.5.36
// 

package com.tgc.sky.ui.panels;

import android.graphics.Typeface;
import android.provider.MediaStore;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.content.ContentResolver;
import java.io.IOException;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.Arrays;
import android.text.Spannable;
import java.io.UnsupportedEncodingException;
import android.graphics.RectF;
import android.widget.Space;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintSet;

import android.util.AttributeSet;
import android.text.InputFilter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.widget.RadioButton;
import java.util.ArrayList;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.DisplayMetrics;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import com.tgc.sky.ui.text.Markup;
import com.tgc.sky.SystemUI_android;
import android.content.Context;
import com.tgc.sky.ui.TextFieldLimiter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.content.Intent;
import java.util.HashMap;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tgc.sky.GameActivity;

public class InvitationPanel extends BasePanel implements GameActivity.OnKeyboardListener, GameActivity.OnActivityResultListener, View.OnLayoutChangeListener
{
    private PanelButton _actionButton;
    private PanelButton _closeButton;
    private RelativeLayout _container;
    private RelativeLayout _containerContentView;
    private PanelButton _deleteButton;
    private TextView _headerTextView;
    private ProgressBar mActivityIndicatorView;
    private HashMap<Object, Object> mButtonLocalizedStrings;
    private boolean mCaptureSession;
    public Handle mHandler;
    private Intent mImagePicker;
    private boolean mImagePickerActive;
    private ScannerOverlay mImageScannerView;
    private ImportState mImportState;
    public String mIncomingInvitationLink;
    public String mIncomingInvitationLinkError;
    public IncomingState mIncomingInvitationLinkState;
    public String mIncomingNickname;
    private Mode mMode;
    public String mOutgoingInvitationLink;
    public String mOutgoingInvitationLinkError;
    public OutgoingState mOutgoingInvitationLinkState;
    public String mOutgoingNickname;
    private QRCodeBoundsView mOverlay;
    private RadioGroup mSegmentedControl;
    private ShareState mShareState;
    private HashMap<Object, Object> mTextAttributedStrings;
    private EditText mTextField;
    TextFieldLimiter mTextFieldLimiter;
    private RelativeLayout mVideoPreview;
    private RelativeLayout view;

    public InvitationPanel(final Context context, final SystemUI_android systemUI_android, final Markup markup, final Mode mMode, final String mOutgoingNickname, final String mOutgoingInvitationLink, final OutgoingState mOutgoingInvitationLinkState, String s, final IncomingState mIncomingInvitationLinkState, final String mIncomingInvitationLinkError, final Handle mHandler) {
        super(context, systemUI_android, markup);
        this.mHandler = mHandler;
        this.mMode = mMode;
        this.mOutgoingNickname = mOutgoingNickname;
        this.mOutgoingInvitationLink = mOutgoingInvitationLink;
        this.mOutgoingInvitationLinkState = mOutgoingInvitationLinkState;
        this.mIncomingInvitationLink = s;
        this.mIncomingInvitationLinkState = mIncomingInvitationLinkState;
        this.mIncomingInvitationLinkError = mIncomingInvitationLinkError;
        this.mImagePickerActive = false;
        this.mShareState = ShareState.kShareState_EnterNickname;
        this.mImportState = ImportState.kImportState_CameraAskPermission;
        this.mTextAttributedStrings = new HashMap<Object, Object>();
        this.mButtonLocalizedStrings = new HashMap<Object, Object>();
        final TextFieldLimiter mTextFieldLimiter = new TextFieldLimiter();
        this.mTextFieldLimiter = mTextFieldLimiter;
        mTextFieldLimiter.maxCharacters = 16;
        this.mTextFieldLimiter.maxByteSize = 63;
        this.updateState();
        this.setBackgroundDrawable((Drawable)new ColorDrawable(0));
        (this.view = new RelativeLayout((Context)this.m_activity)).setLayoutParams((ViewGroup.LayoutParams)new RelativeLayout.LayoutParams(-1, -1));
        this.setContentView((View)this.view);
        this.setFocusable(true);
        this.setWidth(-1);
        this.setHeight(-1);
        this.view.addOnLayoutChangeListener((View.OnLayoutChangeListener)this);
        final RelativeLayout relativeLayout = new RelativeLayout((Context)this.m_activity);
        this._container = relativeLayout;
        this._containerContentView = relativeLayout;
        GradientDrawable background;
        if ((background = (GradientDrawable)relativeLayout.getBackground()) == null) {
            background = new GradientDrawable();
            this._container.setBackground((Drawable)background);
        }
        background.setColor(-3355444);
        background.setAlpha(216);
        background.setCornerRadius((float)this.dp2px(12.0f));
        this.view.addView((View)this._container);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        this.m_activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        final float n = (float)displayMetrics.widthPixels;
        final float n2 = displayMetrics.heightPixels / n;
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)(n * 0.9f), -2);
        layoutParams.addRule(14);
        layoutParams.addRule(13);
        layoutParams.addRule(15);
        this._container.setLayoutParams((ViewGroup.LayoutParams)layoutParams);
        final LinearLayout makeLayout = this.MakeLayout(1, 17);
        final LinearLayout makeLayout2 = this.MakeLayout(0, 17);
        SetMarginsV((View)makeLayout2, -1, -2, this.dp2px(16.0f), 0, 80);
        final LinearLayout makeLayout3 = this.MakeLayout(1, 0);
        SetMarginsH((View)makeLayout3, -1, -1, 0, 0);
        final ConstraintLayout constraintLayout = new ConstraintLayout((Context)this.m_activity);
        final LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.gravity = 80;
        constraintLayout.setLayoutParams((ViewGroup.LayoutParams)layoutParams2);
        final int n3 = this.mMode.getValue();
        ArrayList<String> list = null;
        boolean b = false;
        Label_0525: {
            if (n3 != 3 && n3 != 4) {
                if (n3 == 5) {
                    list = new ArrayList<String>() {
                        {
                            this.add(InvitationPanel.this.m_systemUI.LocalizeString("invite_incoming_title_external"));
                        }
                    };
                    b = false;
                    break Label_0525;
                }
                b = false;
            }
            else {
                b = true;
            }
            list = new ArrayList<String>() {
                {
                    this.add(InvitationPanel.this.m_systemUI.LocalizeString("invite_outgoing_title"));
                    this.add(InvitationPanel.this.m_systemUI.LocalizeString("invite_incoming_title"));
                }
            };
        }
        (this.mSegmentedControl = new RadioGroup((Context)this.m_activity)).setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < list.size(); ++i) {
            s = list.get(i);
            final RadioButton radioButton = new RadioButton((Context)this.m_activity);
            radioButton.setText((CharSequence)s);
            radioButton.setButtonDrawable((Drawable)null);
            this.mSegmentedControl.addView((View)radioButton);
            float appleConvertAndroidScale = GameActivity.AppleConvertAndroidScale(12.0f);
            final boolean b2 = i == 0;
            final boolean b3 = i == list.size() - 1;
            float n4;
            if (b2) {
                n4 = appleConvertAndroidScale;
            }
            else {
                n4 = 0.0f;
            }
            float n5;
            if (b3) {
                n5 = appleConvertAndroidScale;
            }
            else {
                n5 = 0.0f;
            }
            float n6;
            if (b3) {
                n6 = appleConvertAndroidScale;
            }
            else {
                n6 = 0.0f;
            }
            if (!b2) {
                appleConvertAndroidScale = 0.0f;
            }
            final GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadii(new float[] { n4, n4, n5, n5, n6, n6, appleConvertAndroidScale, appleConvertAndroidScale });
            gradientDrawable.setColor(-16777216);
            final GradientDrawable gradientDrawable2 = new GradientDrawable();
            gradientDrawable2.setCornerRadii(new float[] { n4, n4, n5, n5, n6, n6, appleConvertAndroidScale, appleConvertAndroidScale });
            gradientDrawable2.setStroke(4, -16777216);
            final StateListDrawable background2 = new StateListDrawable();
            background2.addState(new int[] { 16842912 }, (Drawable)gradientDrawable);
            background2.addState(new int[] { -16842912 }, (Drawable)gradientDrawable2);
            radioButton.setBackground((Drawable)background2);
            radioButton.setTextColor(new ColorStateList(new int[][] { { 16842912 }, { -16842912 } }, new int[] { -1, -16777216 }));
            radioButton.setChecked(i == (b ? 1 : 0));
            radioButton.setPadding(32, 8, 32, 8);
        }
        SetMarginsV((View)this.mSegmentedControl, -2, -2, 0, 0);
        this.mSegmentedControl.setOnCheckedChangeListener((RadioGroup.OnCheckedChangeListener)new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(final RadioGroup radioGroup, final int n) {
                InvitationPanel.this.onSegmentAction(radioGroup, n);
            }
        });
        (this._headerTextView = new TextView((Context)this.m_activity)).setGravity(8388611);
        this._headerTextView.setTextIsSelectable(true);
        this._headerTextView.setBackgroundColor(0);
        SetMarginsH((View)this._headerTextView, -2, -2, 0, 0, 8388659);
        this.mTextField = new EditText((Context)this.m_activity);
        final LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.topMargin = this.dp2px(16.0f);
        layoutParams3.bottomMargin = this.dp2px(16.0f);
        this.mTextField.setLayoutParams((ViewGroup.LayoutParams)layoutParams3);
        this.mTextField.setTextColor(-16777216);
        this.mTextField.setTypeface(systemUI_android.DefaultFont(), Typeface.NORMAL);
        this.mTextField.setTextSize(20.0f);
        this.mTextField.setOnEditorActionListener((TextView.OnEditorActionListener)new TextView.OnEditorActionListener() {
            public boolean onEditorAction(final TextView textView, final int n, final KeyEvent keyEvent) {
                final InvitationPanel this$0 = InvitationPanel.this;
                return this$0.textFieldShouldReturn(this$0.mTextField);
            }
        });
        this.mTextField.setHint((CharSequence)systemUI_android.LocalizeString("invite_outgoing_nickname_placeholder"));
        final GradientDrawable background3 = new GradientDrawable();
        this.mTextField.setBackground((Drawable)background3);
        background3.setCornerRadius((float)this.dp2px(8.0f));
        background3.setColor(-1);
        background3.setAlpha(76);
        this.mTextField.setInputType(16385);
        this.mTextField.setGravity(8388611);
        this.mTextField.setPadding(this.dp2px(16.0f), this.dp2px(8.0f), this.dp2px(16.0f), this.dp2px(8.0f));
        final EditText mTextField = this.mTextField;
        mTextField.setImeOptions(mTextField.getImeOptions() | 0x4);
        this.mTextField.setSingleLine();
        this.mTextField.addTextChangedListener((TextWatcher)new TextWatcher() {
            public void afterTextChanged(final Editable editable) {
                InvitationPanel.this.textFieldEditingChanged((CharSequence)editable);
            }

            public void beforeTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
            }

            public void onTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
                InvitationPanel.this.textFieldEditingChanged(charSequence);
            }
        });
        this.mTextField.setFilters(new InputFilter[] { (InputFilter)this.mTextFieldLimiter });
        (this.mActivityIndicatorView = new ProgressBar((Context)this.m_activity, (AttributeSet)null, 16842873)).setIndeterminate(true);
        final LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(this.dp2px(32.0f), this.dp2px(32.0f));
        layoutParams4.gravity = 17;
        layoutParams4.topMargin = this.dp2px(16.0f);
        layoutParams4.bottomMargin = this.dp2px(16.0f);
        this.mActivityIndicatorView.setLayoutParams((ViewGroup.LayoutParams)layoutParams4);
        SetMarginsH((View)(this.mImageScannerView = new ScannerOverlay((Context)this.m_activity)), -2, -2, 0, this.dp2px(16.0f), 80);
        (this._closeButton = new PanelButton((Context)this.m_activity, (View.OnClickListener)new View.OnClickListener() {
            public void onClick(final View view) {
                InvitationPanel.this.onCloseButton();
            }
        })).setId(View.generateViewId());
        this._closeButton.setLayoutParams((ViewGroup.LayoutParams)new ConstraintLayout.LayoutParams(-2, -2));
        (this._deleteButton = new PanelButton((Context)this.m_activity, (View.OnClickListener)new View.OnClickListener() {
            public void onClick(final View view) {
                InvitationPanel.this.onDeleteButton();
            }
        })).setId(View.generateViewId());
        this._deleteButton.setLayoutParams((ViewGroup.LayoutParams)new ConstraintLayout.LayoutParams(-2, -2));
        (this._actionButton = new PanelButton((Context)this.m_activity, (View.OnClickListener)new View.OnClickListener() {
            public void onClick(final View view) {
                InvitationPanel.this.onActionButton();
            }
        })).setId(View.generateViewId());
        this._actionButton.setLayoutParams((ViewGroup.LayoutParams)new ConstraintLayout.LayoutParams(-2, -2));
        makeLayout.addView((View)this.mSegmentedControl);
        makeLayout.addView((View)makeLayout2);
        makeLayout2.addView((View)this.mImageScannerView);
        makeLayout2.addView((View)makeLayout3);
        makeLayout3.addView((View)this._headerTextView);
        makeLayout3.addView((View)this.MakeSpace());
        makeLayout3.addView((View)this.mTextField);
        makeLayout3.addView((View)this.mActivityIndicatorView);
        makeLayout3.addView((View)this.MakeSpace());
        makeLayout3.addView((View)constraintLayout);
        constraintLayout.addView((View)this._closeButton);
        constraintLayout.addView((View)this._deleteButton);
        constraintLayout.addView((View)this._actionButton);
        final ConstraintSet set = new ConstraintSet();
        set.clone(constraintLayout);
        set.connect(this._closeButton.getId(), 1, 0, 1);
        set.connect(this._deleteButton.getId(), 1, this._closeButton.getId(), 2);
        set.connect(this._actionButton.getId(), 1, this._deleteButton.getId(), 2);
        set.connect(this._actionButton.getId(), 2, 0, 2);
        set.createHorizontalChain(0, 1, 0, 2, new int[] { this._closeButton.getId(), this._deleteButton.getId(), this._actionButton.getId() }, (float[])null, 1);
        set.applyTo(constraintLayout);
        final RelativeLayout.LayoutParams layoutParams5 = new RelativeLayout.LayoutParams(-1, -2);
        layoutParams5.addRule(9, -1);
        layoutParams5.addRule(11, -1);
        layoutParams5.leftMargin = this.dp2px(32.0f);
        layoutParams5.bottomMargin = this.dp2px(32.0f);
        layoutParams5.topMargin = this.dp2px(32.0f);
        layoutParams5.rightMargin = this.dp2px(32.0f);
        makeLayout.setLayoutParams((ViewGroup.LayoutParams)layoutParams5);
        this._containerContentView.addView((View)makeLayout);
        (this.mImagePicker = new Intent("android.intent.action.PICK")).setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        this.view.getViewTreeObserver().addOnGlobalLayoutListener((ViewTreeObserver.OnGlobalLayoutListener)new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                InvitationPanel.this.view.getViewTreeObserver().removeOnGlobalLayoutListener((ViewTreeObserver.OnGlobalLayoutListener)this);
                InvitationPanel.this.viewWillAppear();
            }
        });
        this.view.post((Runnable)new Runnable() {
            @Override
            public void run() {
                InvitationPanel.this.updateView();
            }
        });
    }

    private LinearLayout MakeLayout(final int orientation, final int gravity) {
        final LinearLayout linearLayout = new LinearLayout((Context)this.m_activity);
        linearLayout.setOrientation(orientation);
        linearLayout.setGravity(gravity);
        return linearLayout;
    }

    private Space MakeSpace() {
        final Space space = new Space((Context)this.m_activity);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 1);
        layoutParams.weight = 1.0f;
        space.setLayoutParams((ViewGroup.LayoutParams)layoutParams);
        return space;
    }

    private static void SetMarginsH(final View view, final int n, final int n2, final int n3, final int n4) {
        SetMarginsH(view, n, n2, n3, n4, 0);
    }

    private static void SetMarginsH(final View view, final int n, final int n2, final int leftMargin, final int rightMargin, final int gravity) {
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(n, n2);
        if (gravity != 0) {
            layoutParams.gravity = gravity;
        }
        if (leftMargin != 0) {
            layoutParams.leftMargin = leftMargin;
        }
        if (rightMargin != 0) {
            layoutParams.rightMargin = rightMargin;
        }
        view.setLayoutParams((ViewGroup.LayoutParams)layoutParams);
    }

    private static void SetMarginsV(final View view, final int n, final int n2, final int n3, final int n4) {
        SetMarginsV(view, n, n2, n3, n4, 1);
    }

    private static void SetMarginsV(final View view, final int n, final int n2, final int topMargin, final int bottomMargin, final int gravity) {
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(n, n2);
        layoutParams.gravity = gravity;
        if (topMargin != 0) {
            layoutParams.topMargin = topMargin;
        }
        if (bottomMargin != 0) {
            layoutParams.bottomMargin = bottomMargin;
        }
        view.setLayoutParams((ViewGroup.LayoutParams)layoutParams);
    }

    private void setIncomingInvitationLinkState(final IncomingState mIncomingInvitationLinkState) {
        this.mIncomingInvitationLinkState = mIncomingInvitationLinkState;
        this.updateState();
        this.m_activity.runOnUiThread((Runnable)new Runnable() {
            @Override
            public void run() {
                InvitationPanel.this.updateView();
            }
        });
    }

    public boolean captureOutput(final String mIncomingInvitationLink, final RectF rectF) throws UnsupportedEncodingException {
        if (rectF != null) {
            final boolean b = mIncomingInvitationLink != null && mIncomingInvitationLink.startsWith("https://sky") && mIncomingInvitationLink.indexOf(".thatg.co/?i=") >= 0;
            if (b && this.mIncomingInvitationLink == null) {
                this.mIncomingInvitationLink = mIncomingInvitationLink;
                this.mIncomingInvitationLinkState = IncomingState.kIncomingState_Parsing;
                this.mHandler.run(this.mIncomingInvitationLink, ResultOptions.kIncoming_UserImportedInvite.ordinal(), false);
            }
            this.mOverlay.addBounds(rectF, b);
            return b;
        }
        return false;
    }

    @Override
    public void dismiss() {
        if (this._closeButton.isEnabled()) {
            this.dismissInternal();
            this.mHandler.run(null, ResultOptions.kUserClosedPanel.ordinal(), true);
        }
    }

    public void dismissInternal() {
        super.dismiss();
        this.viewDidDisappear();
    }

    public Spannable getAttributedString(final String s) {
        Object getMarkedUpString;
        if ((getMarkedUpString = this.mTextAttributedStrings.get(s)) == null) {
            getMarkedUpString = this.m_systemUI.GetMarkedUpString(this.m_systemUI.LocalizeString(s), new ArrayList<Object>(Arrays.asList(this.m_markup.DefaultFontGame(14.0f))), false);
            this.mTextAttributedStrings.put(s, getMarkedUpString);
        }
        return (Spannable)getMarkedUpString;
    }

    public void imagePickerController(final int n, final int n2, final Intent intent) {
        final ContentResolver contentResolver = this.m_activity.getContentResolver();
        final Uri data = intent.getData();
        boolean scanImage = false;
        Label_0105: {
            try {
                final StringBuilder sb = new StringBuilder();
                sb.append("Opening ");
                sb.append(data.toString());
                Log.d("InvitationPanel", sb.toString());
                final Bitmap decodeStream = BitmapFactory.decodeStream(contentResolver.openInputStream(data));
                if (decodeStream != null) {
                    scanImage = false;
                    break Label_0105;
                }
                Log.d("InvitationPanel", "PickedImage was null");
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            scanImage = false;
        }
        this.mImagePickerActive = false;
        if (!scanImage) {
            this.mIncomingInvitationLinkError = this.m_systemUI.LocalizeString("invite_incoming_error_image_parsing");
            this.mIncomingInvitationLinkState = IncomingState.kIncomingState_Error;
        }
        this.updateState();
        this.updateView();
    }

    public void imagePickerControllerDidCancel() {
        this.mImagePickerActive = false;
        this.updateState();
        this.updateView();
    }

    public void keyboardDidShow(final int n) {
    }

    public void keyboardWillHide(final EditText editText) {
    }

    public void onActionButton() {
        this.view.performHapticFeedback(3);
        final int n = this.mMode.getValue();
        if (n != 1) {
            if (n == 3 || n == 4 || n == 5) {
                if (this.mIncomingInvitationLink != null) {
                    final int n2 = this.mImportState.getValue();
                    if (n2 != 6) {
                        if (n2 == 8 || n2 == 9) {
                            this.dismissInternal();
                            this.mHandler.run(null, ResultOptions.kUserClosedPanel.ordinal(), true);
                        }
                    }
                    else {
                        this.mIncomingInvitationLinkState = IncomingState.kIncomingState_Accepting;
                        this.mHandler.run(this.mIncomingNickname, ResultOptions.kIncoming_UserAcceptsInvite.ordinal(), false);
                        this.updateState();
                        this.updateView();
                    }
                }
                else if (this.mImportState.getValue() != 9) {
                    this.mImagePickerActive = true;
                    this.updateState();
                    this.updateView();
                    this.m_activity.AddOnActivityResultListener((GameActivity.OnActivityResultListener)this);
                    this.m_activity.startActivityForResult(this.mImagePicker, 130);
                }
                else {
                    this.mIncomingInvitationLinkState = IncomingState.kIncomingState_Idle;
                    this.updateState();
                    this.updateView();
                }
            }
        }
        else {
            final int n3 = this.mShareState.getValue();
            if (n3 != 1) {
                if (n3 != 3) {
                    if (n3 != 5) {
                        if (n3 == 6) {
                            OutgoingState mOutgoingInvitationLinkState;
                            if (this.mOutgoingInvitationLink != null) {
                                mOutgoingInvitationLinkState = OutgoingState.kOutgoingState_Created;
                            }
                            else {
                                mOutgoingInvitationLinkState = OutgoingState.kOutgoingState_Idle;
                            }
                            this.mOutgoingInvitationLinkState = mOutgoingInvitationLinkState;
                            this.updateState();
                            this.updateView();
                        }
                    }
                    else {
                        this.dismissInternal();
                        this.mHandler.run(null, ResultOptions.kOutgoing_UserConfirmsAccepted.ordinal(), true);
                    }
                }
                else {
                    this.dismissInternal();
                    this.mHandler.run(null, ResultOptions.kOutgoing_UserRequestsShare.ordinal(), true);
                }
            }
            else {
                this.mHandler.run(this.mOutgoingNickname, ResultOptions.kOutgoing_UserRequestsCreate.ordinal(), false);
                this.mOutgoingInvitationLinkState = OutgoingState.kOutgoingState_Creating;
                this.updateState();
                this.updateView();
            }
        }
    }

    @Override
    public void onActivityResult(final int n, final int n2, final Intent intent) {
        if (n == 130) {
            if (intent != null) {
                this.imagePickerController(n, n2, intent);
            }
            else {
                this.imagePickerControllerDidCancel();
            }
            this.m_activity.RemoveOnActivityResultListeners((GameActivity.OnActivityResultListener)this);
        }
    }

    public void onBeginDetect() {
        this.mOverlay.clear();
    }

    public void onCloseButton() {
        this.view.performHapticFeedback(7);
        this.dismissInternal();
        this.mHandler.run(null, ResultOptions.kUserClosedPanel.ordinal(), true);
    }

    public void onDeleteButton() {
        this.view.performHapticFeedback(7);
        final int n = this.mMode.getValue();
        if (n != 1) {
            if (n == 3 || n == 4 || n == 5) {
                if (this.mIncomingInvitationLink != null) {
                    this.mIncomingInvitationLink = null;
                    this.mIncomingInvitationLinkState = IncomingState.kIncomingState_Idle;
                    if (this.mMode == Mode.kBoth_Incoming) {
                        this.updateState();
                        this.updateView();
                        this.mHandler.run(null, ResultOptions.kIncoming_UserDismissesInvite.ordinal(), false);
                    }
                    else {
                        this.dismissInternal();
                        this.mHandler.run(null, ResultOptions.kIncoming_UserDismissesInvite.ordinal(), true);
                    }
                }
                else if (this.mImportState == ImportState.kImportState_Error) {
                    this.mIncomingInvitationLink = null;
                    this.mIncomingInvitationLinkState = IncomingState.kIncomingState_Idle;
                    this.updateState();
                    this.updateView();
                }
                else if (this.mImportState == ImportState.kImportState_CameraNoPermission) {
                    this.m_activity.requestPermissionsThroughSettings(new String[] { "android.permission.CAMERA" }, (GameActivity.PermissionCallback)new GameActivity.PermissionCallback() {
                        public void onPermissionResult(final String[] array, final int[] array2) {
                            InvitationPanel.this.m_activity.runOnUiThread((Runnable)new Runnable() {
                                @Override
                                public void run() {
                                    InvitationPanel.this.updateState();
                                    InvitationPanel.this.updateView();
                                }
                            });
                        }
                    });
                }
                else {
                    this.m_activity.requestPermissions(new String[] { "android.permission.CAMERA" }, (GameActivity.PermissionCallback)new GameActivity.PermissionCallback() {
                        @Override
                        public void onPermissionResult(final String[] array, final int[] array2) {
                            InvitationPanel.this.m_activity.runOnUiThread((Runnable)new Runnable() {
                                @Override
                                public void run() {
                                    if (!InvitationPanel.this.m_activity.checkResultPermissions(array2) && !InvitationPanel.this.m_activity.shouldShowRequestPermissionsRationale(array)) {
                                        InvitationPanel.this.mImportState = ImportState.kImportState_CameraNoPermission;
                                    }
                                    InvitationPanel.this.updateState();
                                    InvitationPanel.this.updateView();
                                }
                            });
                        }
                    });
                }
            }
        }
        else {
            this.mHandler.run(null, ResultOptions.kOutgoing_UserRequestsDelete.ordinal(), false);
            this.mOutgoingInvitationLinkState = OutgoingState.kOutgoingState_Deleting;
            this.updateState();
            this.updateView();
        }
    }

    @Override
    public void onKeyboardChange(final boolean b, final int n) {
        if (b) {
            this.keyboardDidShow(n);
        }
        else {
            this.keyboardWillHide(this.mTextField);
        }
    }

    public void onLayoutChange(final View view, final int n, final int n2, final int n3, final int n4, final int n5, final int n6, final int n7, final int n8) {
        this.viewWillLayoutSubviews();
    }

    public boolean onQRCodeRead(final String s, final RectF rectF) {
        try {
            this.captureOutput(s, rectF);
        }
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public void onSegmentAction(final RadioGroup radioGroup, int i) {
        while (true) {
            int n;
            for (i = 0; i < this.mSegmentedControl.getChildCount(); ++i) {
                if (((RadioButton)this.mSegmentedControl.getChildAt(i)).isChecked()) {
                    n = this.mMode.getValue();
                    if (n != 1) {
                        if (n != 2) {
                            if (n != 3) {
                                if (n == 4) {
                                    if (i == 0) {
                                        this.mMode = Mode.kIncoming_Only_Outgoing;
                                        this.updateState();
                                        this.updateView();
                                    }
                                }
                            }
                            else if (i == 0) {
                                this.mHandler.run(null, ResultOptions.kIncoming_UserSwitchedToOutgoing.ordinal(), false);
                                this.mMode = Mode.kBoth_Outgoing;
                                this.updateState();
                                this.updateView();
                            }
                        }
                        else if (i == 1) {
                            this.mMode = Mode.kIncoming_Only_Incoming;
                            this.updateState();
                            this.updateView();
                        }
                    }
                    else if (i == 1) {
                        this.mHandler.run(null, ResultOptions.kOutgoing_UserSwitchedToIncoming.ordinal(), false);
                        this.mMode = Mode.kBoth_Incoming;
                        this.updateState();
                        this.updateView();
                    }
                    return;
                }
            }
            i = -1;
            continue;
        }
    }

    public void setButton(final PanelButton panelButton, final boolean enabled, final String s, final boolean b) {
        if (s != null) {
            String localizeString;
            if ((localizeString = (String) this.mButtonLocalizedStrings.get(s)) == null) {
                localizeString = this.m_systemUI.LocalizeString(s);
                this.mButtonLocalizedStrings.put(s, localizeString);
            }
            if (b) {
                panelButton.setEnabledText(localizeString, -65536);
                panelButton.setDisabledText(localizeString);
            }
            else {
                panelButton.setText(localizeString);
            }
            panelButton.setEnabled(enabled);
            panelButton.setVisibility(View.VISIBLE);
        }
        else {
            panelButton.setVisibility(View.GONE);
            panelButton.setEnabled(false);
        }
    }

    public void setIncomingInvitationLinkState(final int n, final String mIncomingInvitationLinkError) {
        this.mIncomingInvitationLinkError = mIncomingInvitationLinkError;
        this.setIncomingInvitationLinkState(IncomingState.values()[n]);
    }

    public void setOutgoingInvitationLinkState(final int n, final String mOutgoingInvitationLink, final String mOutgoingInvitationLinkError) {
        this.mOutgoingInvitationLink = mOutgoingInvitationLink;
        this.mOutgoingInvitationLinkError = mOutgoingInvitationLinkError;
        this.setOutgoingInvitationLinkState(OutgoingState.values()[n]);
    }

    public void setOutgoingInvitationLinkState(final OutgoingState mOutgoingInvitationLinkState) {
        this.mOutgoingInvitationLinkState = mOutgoingInvitationLinkState;
        this.updateState();
        this.m_activity.runOnUiThread((Runnable)new Runnable() {
            @Override
            public void run() {
                InvitationPanel.this.updateView();
            }
        });
    }

    public void setPreviewOrientation() {
    }

    public boolean startReading() {
        return true;
    }

    public void stopReading() {
        
    }

    public void textFieldEditingChanged(final CharSequence charSequence) {
        if (this.mMode == Mode.kBoth_Outgoing) {
            this.mOutgoingNickname = charSequence.toString();
        }
        else {
            this.mIncomingNickname = charSequence.toString();
        }
        this._actionButton.setEnabled(charSequence.length() > 0);
    }

    public boolean textFieldShouldReturn(final EditText editText) {
        final String string = editText.getText().toString();
        if (string.length() == 0) {
            return true;
        }
        if (this.mMode == Mode.kBoth_Outgoing) {
            this.mOutgoingNickname = string;
            this.mHandler.run(string, ResultOptions.kOutgoing_UserRequestsCreate.ordinal(), false);
            this.mOutgoingInvitationLinkState = OutgoingState.kOutgoingState_Creating;
        }
        else {
            this.mIncomingNickname = string;
            this.mHandler.run(string, ResultOptions.kIncoming_UserAcceptsInvite.ordinal(), false);
            this.mIncomingInvitationLinkState = IncomingState.kIncomingState_Accepting;
        }
        ((InputMethodManager)this.m_activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editText.getWindowToken(), 0);
        GameActivity.hideNavigationFullScreen((View)this.m_activity.getBrigeView());
        this.updateState();
        this.updateView();
        return false;
    }

    public void updateState() {
        final int n = this.mMode.getValue();
        if (n != 1) {
            if (n == 3 || n == 4 || n == 5) {
                switch (this.mIncomingInvitationLinkState.getValue()) {
                    case 6: {
                        this.mImportState = ImportState.kImportState_Error;
                        break;
                    }
                    case 5: {
                        this.mImportState = ImportState.kImportState_AcceptedLink;
                        break;
                    }
                    case 4: {
                        this.mImportState = ImportState.kImportState_AcceptingLink;
                        break;
                    }
                    case 3: {
                        this.mImportState = ImportState.kImportState_ParsedLink;
                        break;
                    }
                    case 2: {
                        this.mImportState = ImportState.kImportState_ParsingLink;
                        break;
                    }
                    case 1: {
                        if (this.mImagePickerActive) {
                            this.mImportState = ImportState.kImportState_PickerActive;
                            break;
                        }
                        if (this.m_activity.checkSelfPermissions(new String[] { "android.permission.CAMERA" })) {
                            this.mImportState = ImportState.kImportState_CameraActive;
                            this.startReading();
                            break;
                        }
                        if (this.mImportState != ImportState.kImportState_CameraNoPermission) {
                            this.mImportState = ImportState.kImportState_CameraAskPermission;
                            break;
                        }
                        break;
                    }
                }
            }
        }
        else {
            switch (this.mOutgoingInvitationLinkState.getValue()) {
                case 6: {
                    this.mShareState = ShareState.kShareState_Error;
                    break;
                }
                case 5: {
                    this.mShareState = ShareState.kShareState_AcceptedLink;
                    break;
                }
                case 4: {
                    this.mShareState = ShareState.kShareState_DeletingLink;
                    break;
                }
                case 3: {
                    this.mShareState = ShareState.kShareState_LinkReady;
                    break;
                }
                case 2: {
                    this.mShareState = ShareState.kShareState_CreatingLink;
                    break;
                }
                case 1: {
                    this.mShareState = ShareState.kShareState_EnterNickname;
                    break;
                }
            }
        }
    }

    public void updateView() {
        final int n = this.mMode.getValue();
        final String s = null;
        String s2 = null;
        final boolean b = true;
        if (n != 1) {
            if (n != 2) {
                if (n == 3 || n == 4 || n == 5) {
                    switch (this.mImportState.getValue()) {
                        case 9: {
                            this.mSegmentedControl.setEnabled(this.mMode == Mode.kIncoming_Invite_Only);
                            this.stopReading();
                            this._headerTextView.setText((CharSequence)this.getAttributedString(this.mIncomingInvitationLinkError), TextView.BufferType.SPANNABLE);
                            this.mActivityIndicatorView.setVisibility(View.GONE);
                            this.mTextField.setVisibility(View.GONE);
                            this.mTextField.setEnabled(false);
                            this.mImageScannerView.setInviteLink(this.mIncomingInvitationLink, this.mMode != Mode.kBoth_Outgoing);
                            final boolean b2 = this.mMode == Mode.kBoth_Incoming;
                            this.setButton(this._actionButton, true, "system_button_confirm", false);
                            final PanelButton deleteButton = this._deleteButton;
                            if (b2) {
                                s2 = "system_button_dismiss";
                            }
                            this.setButton(deleteButton, b2, s2, true);
                            this.setButton(this._closeButton, true, "system_button_close", false);
                            break;
                        }
                        case 8: {
                            this.mSegmentedControl.setEnabled(this.mMode == Mode.kIncoming_Invite_Only);
                            this.stopReading();
                            this._headerTextView.setText((CharSequence)this.getAttributedString("invite_incoming_success_00"), TextView.BufferType.SPANNABLE);
                            this.mActivityIndicatorView.setVisibility(View.GONE);
                            this.mTextField.setVisibility(View.GONE);
                            this.mTextField.setEnabled(false);
                            this.mImageScannerView.setInviteLink(this.mIncomingInvitationLink, this.mMode != Mode.kBoth_Outgoing);
                            final boolean b3 = this.mMode == Mode.kBoth_Incoming;
                            this.setButton(this._actionButton, true, "system_button_confirm", false);
                            final PanelButton deleteButton2 = this._deleteButton;
                            String s3 = s;
                            if (b3) {
                                s3 = "system_button_dismiss";
                            }
                            this.setButton(deleteButton2, b3, s3, true);
                            this.setButton(this._closeButton, true, "system_button_close", false);
                            break;
                        }
                        case 7: {
                            this.mSegmentedControl.setEnabled(this.mMode == Mode.kIncoming_Invite_Only);
                            this.stopReading();
                            this._headerTextView.setText((CharSequence)this.getAttributedString("invite_incoming_accepting_00"), TextView.BufferType.SPANNABLE);
                            this.mActivityIndicatorView.postDelayed((Runnable)new Runnable() {
                                @Override
                                public void run() {
                                    InvitationPanel.this.mActivityIndicatorView.setVisibility(View.VISIBLE);
                                }
                            }, 500L);
                            this.mTextField.setVisibility(View.GONE);
                            this.mTextField.setEnabled(false);
                            this.mImageScannerView.setInviteLink(this.mIncomingInvitationLink, this.mMode != Mode.kBoth_Outgoing);
                            this.setButton(this._actionButton, false, "system_button_accept", false);
                            this.setButton(this._deleteButton, false, "system_button_dismiss", true);
                            this.setButton(this._closeButton, false, "system_button_close", false);
                            break;
                        }
                        case 6: {
                            this.mSegmentedControl.setEnabled(this.mMode == Mode.kIncoming_Invite_Only);
                            this.stopReading();
                            this._headerTextView.setText((CharSequence)this.getAttributedString("invite_incoming_accept_00"), TextView.BufferType.SPANNABLE);
                            this.mActivityIndicatorView.setVisibility(View.GONE);
                            this.mTextField.setText((CharSequence)this.mIncomingNickname);
                            this.mTextField.setVisibility(View.VISIBLE);
                            this.mTextField.setEnabled(true);
                            this.mImageScannerView.setInviteLink(this.mIncomingInvitationLink, this.mMode != Mode.kBoth_Outgoing);
                            final String mIncomingNickname = this.mIncomingNickname;
                            this.setButton(this._actionButton, mIncomingNickname != null && mIncomingNickname.length() > 0, "system_button_accept", false);
                            this.setButton(this._deleteButton, true, "system_button_dismiss", true);
                            this.setButton(this._closeButton, false, "system_button_close", false);
                            break;
                        }
                        case 5: {
                            this.mSegmentedControl.setEnabled(this.mMode == Mode.kIncoming_Invite_Only);
                            this.stopReading();
                            this._headerTextView.setText((CharSequence)this.getAttributedString("invite_incoming_verifying_00"), TextView.BufferType.SPANNABLE);
                            this.mActivityIndicatorView.postDelayed((Runnable)new Runnable() {
                                @Override
                                public void run() {
                                    InvitationPanel.this.mActivityIndicatorView.setVisibility(View.VISIBLE);
                                }
                            }, 500L);
                            this.mTextField.setVisibility(View.GONE);
                            this.mTextField.setEnabled(false);
                            this.mImageScannerView.setInviteLink(this.mIncomingInvitationLink, this.mMode != Mode.kBoth_Outgoing);
                            this.setButton(this._actionButton, false, "system_button_accept", false);
                            this.setButton(this._deleteButton, false, "system_button_dismiss", true);
                            this.setButton(this._closeButton, false, "system_button_close", false);
                            break;
                        }
                        case 4: {
                            this.mSegmentedControl.setEnabled(this.mMode == Mode.kIncoming_Invite_Only);
                            this.stopReading();
                            this._headerTextView.setText((CharSequence)this.getAttributedString("invite_incoming_picking_00"), TextView.BufferType.SPANNABLE);
                            this.mActivityIndicatorView.postDelayed((Runnable)new Runnable() {
                                @Override
                                public void run() {
                                    InvitationPanel.this.mActivityIndicatorView.setVisibility(View.VISIBLE);
                                }
                            }, 500L);
                            this.mTextField.setVisibility(View.GONE);
                            this.mTextField.setEnabled(false);
                            this.mImageScannerView.setInviteLink(null, this.mMode != Mode.kBoth_Outgoing && b);
                            this.setButton(this._actionButton, false, "system_button_import", false);
                            this.setButton(this._deleteButton, false, null, false);
                            this.setButton(this._closeButton, false, "system_button_close", false);
                            break;
                        }
                        case 3: {
                            this.mSegmentedControl.setEnabled(true);
                            this._headerTextView.setText((CharSequence)this.getAttributedString("invite_incoming_camera_scanning_00"), TextView.BufferType.SPANNABLE);
                            this.mActivityIndicatorView.setVisibility(View.GONE);
                            this.mTextField.setVisibility(View.GONE);
                            this.mTextField.setEnabled(false);
                            this.mImageScannerView.setInviteLink(null, this.mMode != Mode.kBoth_Outgoing);
                            this.setButton(this._actionButton, true, "system_button_import", false);
                            this.setButton(this._deleteButton, false, null, false);
                            this.setButton(this._closeButton, true, "system_button_close", false);
                            this.startReading();
                            break;
                        }
                        case 2: {
                            this.mSegmentedControl.setEnabled(true);
                            this.stopReading();
                            this._headerTextView.setText((CharSequence)this.getAttributedString("invite_incoming_denied_camera_permission_00"), TextView.BufferType.SPANNABLE);
                            this.mActivityIndicatorView.setVisibility(View.GONE);
                            this.mTextField.setVisibility(View.GONE);
                            this.mTextField.setEnabled(false);
                            this.mImageScannerView.setInviteLink(null, this.mMode != Mode.kBoth_Outgoing);
                            this.setButton(this._actionButton, true, "system_button_import", false);
                            this.setButton(this._deleteButton, true, "system_button_settings", false);
                            this.setButton(this._closeButton, true, "system_button_close", false);
                            break;
                        }
                        case 1: {
                            this.mSegmentedControl.setEnabled(true);
                            this.stopReading();
                            this._headerTextView.setText((CharSequence)this.getAttributedString("invite_incoming_ask_camera_permission_00"), TextView.BufferType.SPANNABLE);
                            this.mActivityIndicatorView.setVisibility(View.GONE);
                            this.mTextField.setVisibility(View.GONE);
                            this.mTextField.setEnabled(false);
                            this.mImageScannerView.setInviteLink(null, this.mMode != Mode.kBoth_Outgoing);
                            this.setButton(this._actionButton, true, "system_button_import", false);
                            this.setButton(this._deleteButton, true, "system_button_ask", false);
                            this.setButton(this._closeButton, true, "system_button_close", false);
                            break;
                        }
                    }
                }
            }
            else {
                this.stopReading();
                this.mSegmentedControl.setEnabled(true);
                this._headerTextView.setText((CharSequence)this.getAttributedString("invite_outgoing_no_free_slots"), TextView.BufferType.SPANNABLE);
                this.mActivityIndicatorView.setVisibility(View.GONE);
                this.mTextField.setVisibility(View.GONE);
                this.mTextField.setEnabled(false);
                this.mImageScannerView.setInviteLink(null, false);
                this.setButton(this._actionButton, false, "system_button_create", false);
                this.setButton(this._deleteButton, false, null, false);
                this.setButton(this._closeButton, true, "system_button_close", false);
            }
        }
        else {
            this.stopReading();
            switch (this.mShareState.getValue()) {
                case 6: {
                    this.mSegmentedControl.setEnabled(true);
                    this._headerTextView.setText((CharSequence)this.getAttributedString(this.mOutgoingInvitationLinkError), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.GONE);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(this.mOutgoingInvitationLink, this.mMode != Mode.kBoth_Outgoing);
                    this.setButton(this._actionButton, true, "system_button_confirm", false);
                    this.setButton(this._deleteButton, false, null, true);
                    this.setButton(this._closeButton, true, "system_button_close", false);
                    break;
                }
                case 5: {
                    this.mSegmentedControl.setEnabled(true);
                    this._headerTextView.setText((CharSequence)this.getAttributedString("invite_outgoing_accepted_00"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.GONE);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(null, this.mMode != Mode.kBoth_Outgoing);
                    this.setButton(this._actionButton, true, "system_button_confirm", false);
                    this.setButton(this._deleteButton, false, null, true);
                    this.setButton(this._closeButton, true, "system_button_close", false);
                    break;
                }
                case 4: {
                    this.mSegmentedControl.setEnabled(true);
                    this._headerTextView.setText((CharSequence)this.getAttributedString("invite_outgoing_deleting_00"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.VISIBLE);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(null, this.mMode != Mode.kBoth_Outgoing);
                    this.setButton(this._actionButton, false, "system_button_share", false);
                    this.setButton(this._deleteButton, false, "system_button_delete", true);
                    this.setButton(this._closeButton, true, "system_button_close", false);
                    break;
                }
                case 3: {
                    this.mSegmentedControl.setEnabled(true);
                    this._headerTextView.setText((CharSequence)this.getAttributedString("invite_outgoing_sharing_00"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.GONE);
                    this.mTextField.setVisibility(View.GONE);
                    this.mTextField.setEnabled(false);
                    this.mImageScannerView.setInviteLink(this.mOutgoingInvitationLink, this.mMode != Mode.kBoth_Outgoing);
                    this.setButton(this._actionButton, true, "system_button_share", false);
                    this.setButton(this._deleteButton, true, "system_button_delete", true);
                    this.setButton(this._closeButton, true, "system_button_close", false);
                    break;
                }
                case 2: {
                    this.mSegmentedControl.setEnabled(true);
                    this._headerTextView.setText((CharSequence)this.getAttributedString("invite_outgoing_creating_00"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.VISIBLE);
                    this.mTextField.postDelayed((Runnable)new Runnable() {
                        @Override
                        public void run() {
                            InvitationPanel.this.mTextField.setVisibility(View.GONE);
                            InvitationPanel.this.mTextField.setEnabled(false);
                        }
                    }, 500L);
                    this.mImageScannerView.setInviteLink(null, this.mMode != Mode.kBoth_Outgoing);
                    this.setButton(this._actionButton, false, "system_button_create", false);
                    this.setButton(this._deleteButton, false, null, false);
                    this.setButton(this._closeButton, true, "system_button_close", false);
                    break;
                }
                case 1: {
                    this.mSegmentedControl.setEnabled(true);
                    this._headerTextView.setText((CharSequence)this.getAttributedString("invite_outgoing_create_00"), TextView.BufferType.SPANNABLE);
                    this.mActivityIndicatorView.setVisibility(View.GONE);
                    this.mTextField.setText((CharSequence)this.mOutgoingNickname);
                    this.mTextField.postDelayed((Runnable)new Runnable() {
                        @Override
                        public void run() {
                            InvitationPanel.this.mTextField.setVisibility(View.VISIBLE);
                            InvitationPanel.this.mTextField.setEnabled(true);
                        }
                    }, 500L);
                    this.mImageScannerView.setInviteLink(null, this.mMode != Mode.kBoth_Outgoing);
                    final String mOutgoingNickname = this.mOutgoingNickname;
                    this.setButton(this._actionButton, mOutgoingNickname != null && mOutgoingNickname.length() > 0, "system_button_create", false);
                    this.setButton(this._deleteButton, false, null, false);
                    this.setButton(this._closeButton, true, "system_button_close", false);
                    break;
                }
            }
        }
    }

    public void viewDidDisappear() {
        this.m_activity.RemoveOnKeyboardListener((GameActivity.OnKeyboardListener)this);
    }

    public void viewWillAppear() {
        this.m_activity.addOnKeyboardListener((GameActivity.OnKeyboardListener)this);
    }

    public void viewWillLayoutSubviews() {
        this.setPreviewOrientation();
    }

    public void viewWillTransitionToSize() {
        this.setPreviewOrientation();
    }

    public interface Handle
    {
        void run(final String p0, final int p1, final boolean p2);
    }

    enum ImportState
    {
        kImportState_AcceptedLink(8),
        kImportState_AcceptingLink(7),
        kImportState_CameraActive(3),
        kImportState_CameraAskPermission(1),
        kImportState_CameraNoPermission(2),
        kImportState_Error(9),
        kImportState_ParsedLink(6),
        kImportState_ParsingLink(5),
        kImportState_PickerActive(4);
        final int value;

        ImportState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum IncomingState
    {
        kIncomingState_Idle(1),
        kIncomingState_Parsing(2),
        kIncomingState_Parsed(3),
        kInvitationPanelIncomingState_Nicknaming(-1),
        kIncomingState_Accepting(4),
        kIncomingState_Accepted(5),
        kIncomingState_Error(6);

        final int value;

        IncomingState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Mode
    {
        kBoth_Outgoing(1),
        kBoth_Incoming(3),
        kIncoming_Only_Outgoing(2),
        kIncoming_Only_Incoming(4),
        kIncoming_Invite_Only(5);
        final int value;

        Mode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum OutgoingState
    {
        kOutgoingState_Idle(1),
        kOutgoingState_Creating(2),
        kOutgoingState_Created(3),
        kOutgoingState_Deleting(4),
        kOutgoingState_Accepted(5),
        kOutgoingState_Error(6);
        final int value;

        OutgoingState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ResultOptions
    {
        kUserClosedPanel,
        kOutgoing_UserRequestsCreate,
        kOutgoing_UserRequestsShare,
        kOutgoing_UserRequestsDelete,
        kOutgoing_UserConfirmsAccepted,
        kOutgoing_UserSwitchedToIncoming,
        kIncoming_UserImportedInvite,
        kIncoming_UserAcceptsInvite,
        kIncoming_UserDismissesInvite,
        kIncoming_UserSwitchedToOutgoing;

    }

    enum ShareState
    {
        kShareState_EnterNickname(1),
        kShareState_CreatingLink(2),
        kShareState_LinkReady(3),
        kShareState_DeletingLink(4),
        kShareState_AcceptedLink(5),
        kShareState_Error(6);
        final int value;

        ShareState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
