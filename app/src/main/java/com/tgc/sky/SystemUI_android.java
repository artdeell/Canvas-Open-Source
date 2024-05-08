package com.tgc.sky;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.core.internal.view.SupportMenu;
//import com.google.android.gms.drive.DriveFile;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;
import com.tgc.sky.GameActivity;
//import com.tgc.sky.accounts.Facebook;
import com.tgc.sky.accounts.SystemAccountType;
import com.tgc.sky.ui.QRCameraHandler;
import com.tgc.sky.ui.TextField;
import com.tgc.sky.ui.TextFieldLimiter;
import com.tgc.sky.ui.Utils;
import com.tgc.sky.ui.dialogs.DialogResult;
import com.tgc.sky.ui.panels.CodeScanner;
import com.tgc.sky.ui.panels.Starboard;
import com.tgc.sky.ui.text.LocalizationManager;
import com.tgc.sky.ui.text.Markup;
import com.tgc.sky.ui.text.SystemHAlignment;
import com.tgc.sky.ui.text.SystemVAlignment;
import com.tgc.sky.ui.text.TextLabel;
import com.tgc.sky.ui.text.TextLabelArgs;
import com.tgc.sky.ui.text.TextLabelManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class SystemUI_android {
    public static final int kInvalidDialogId = -1;
    public static final int kInvalidLabelId = -1;
    public static final int kInvalidTextId = -1;
    private static volatile SystemUI_android sInstance;
    private GameActivity m_activity;
    private CodeScanner m_codeScanner;
    private int m_currentId;
    private float m_keyboardHeight;
    private boolean m_keyboardIsShowing;
    private LocalizationManager m_localizationManager;
    private Markup m_markup;
    private QRCameraHandler m_qrCameraHandler;
    private DialogResult m_result;
    private Starboard m_starBoard;
    private TextField m_textField;
    private TextFieldLimiter m_textFieldLimiter;
    private TextLabelManager m_textLabelManager;
    private boolean m_useSensorOrientation;
    private String pasteStr = "";
    private boolean m_usingGamepad = false;
    private boolean m_enableHaptics = true;
    private String CHANNEL_ID = "sky";
    private int notificationId = 0;

    public void PressDialogButton(int i) {
    }

    public boolean SupportsQRCamera() {
        return true;
    }

    public SystemUI_android(GameActivity gameActivity) {
        this.m_activity = gameActivity;
        this.m_localizationManager = new LocalizationManager(gameActivity);
        this.m_markup = new Markup(gameActivity);
        this.m_textLabelManager = new TextLabelManager(gameActivity, this.m_localizationManager, this.m_markup);
        TextField textField = new TextField();
        this.m_textField = textField;
        textField.initWithParams(gameActivity, this);
        this.m_textFieldLimiter = new TextFieldLimiter();
        this.m_keyboardIsShowing = false;
        this.m_keyboardHeight = 0.0f;
        this.m_result = new DialogResult();
        this.m_qrCameraHandler = new QRCameraHandler(gameActivity, (str, i, z) -> SystemUI_android.this.SetResult(str, i, z));
        this.m_currentId = 0;
        this.m_activity.addOnKeyboardListener((z, i) -> {
            SystemUI_android.this.m_keyboardIsShowing = z;
            SystemUI_android.this.m_keyboardHeight = i;
        });
        final ClipboardManager clipboardManager = (ClipboardManager) gameActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(() -> {
            CharSequence text;
            ClipData primaryClip = clipboardManager.getPrimaryClip();
            if (primaryClip == null || primaryClip.getItemCount() <= 0 || (text = primaryClip.getItemAt(0).getText()) == null) {
                return;
            }
            SystemUI_android.this.pasteStr = text.toString();
        });
        SetUseSensorOrientation(false);
        sInstance = this;
    }

    public static SystemUI_android getInstance() {
        return sInstance;
    }

    public String LocalizeString(String str) {
        return this.m_localizationManager.LocalizeString(str);
    }

    public boolean HasLocalizedString(String str) {
        return this.m_localizationManager.HasLocalizedString(str);
    }

    public SpannableStringBuilder GetMarkedUpString(String str, ArrayList<Object> arrayList, boolean z) {
        return this.m_markup.GetMarkedUpString(str, arrayList, z);
    }

    public Typeface DefaultFont() {
        return this.m_markup.DefaultFont();
    }

    public ArrayList<Object> DefaultMarkupWithBoldFontSize(float f) {
        return new ArrayList<>(Arrays.asList(this.m_markup.DefaultFont(f), new StyleSpan(1)));
    }

    public ArrayList<Object> DefaultMarkupWithFontSize(float f) {
        return new ArrayList<>(Arrays.asList(this.m_markup.DefaultFont(f)));
    }

    public SpannableStringBuilder LocalizeAndMarkUpString(String str, ArrayList<Object> arrayList, ArrayList<Object> arrayList2, boolean z) {
        return this.m_textLabelManager.ApplyTextArgs(this.m_markup.GetMarkedUpString(LocalizeString(str), arrayList2, z), arrayList);
    }

    public synchronized int TryActivate() {
        if (this.m_result.response != DialogResult.Response.kInvalid) {
            return -1;
        }
        this.m_result.response = DialogResult.Response.kWaiting;
        int i = this.m_currentId + 1;
        this.m_currentId = i;
        return i;
    }

    public synchronized void SetResult(String stringBuffer, int option, boolean isClosed) {
        this.m_result.stringBuffer = stringBuffer;
        this.m_result.option = option;
        this.m_result.response = isClosed ? DialogResult.Response.kClosed : DialogResult.Response.kResponded;
    }

    public boolean GetMainWindowAttachedSheet() {
        return !this.m_activity.getBrigeView().hasWindowFocus();
    }

    @SuppressLint("WrongConstant")
    void SetUseSensorOrientation(boolean z) {
        this.m_useSensorOrientation = z;
        final int i = z ? 4 : 6;
        if (i != this.m_activity.getRequestedOrientation()) {
            this.m_activity.runOnUiThread(new Runnable() {
                @Override
                public final void run() {
                    SystemUI_android.this.SetUseSensorOrientation(i);
                }
            });
        }
    }

    void SetUseSensorOrientation(int i) {
        if (this.m_activity.portraitOnResume) {
            return;
        }
        this.m_activity.setRequestedOrientation(i);
    }

    public boolean GetUseSensorOrientation() {
        return this.m_useSensorOrientation;
    }

    int ShowTextField(final String str, final int i, final int i2) {
        int TryActivate;
        if (this.m_textField.getState() == TextField.State.kTextFieldState_Hidden && (TryActivate = this.m_textField.TryActivate()) != -1) {
            this.m_textField.setState(TextField.State.kTextFieldState_RequestShow);
            this.m_activity.runOnUiThread(() -> {
                SystemUI_android.this.m_textField.showTextFieldWithPrompt(SystemUI_android.this.LocalizeString(str), i, i2);
                SystemUI_android.this.m_textField.setState(TextField.State.kTextFieldState_Showing);
            });
            return TryActivate;
        }
        return -1;
    }

    int ShowTextField(final String str, final String str2, final int i, final int i2, final boolean z) {
        int TryActivate;
        if (this.m_textField.getState() == TextField.State.kTextFieldState_Hidden && (TryActivate = this.m_textField.TryActivate()) != -1) {
            this.m_textField.setState(TextField.State.kTextFieldState_RequestShow);
            this.m_activity.runOnUiThread(() -> {
                SystemUI_android.this.m_textField.showTextFieldWithPrompt(SystemUI_android.this.LocalizeString(str), str2, i, i2, z);
                SystemUI_android.this.m_textField.setState(TextField.State.kTextFieldState_Showing);
            });
            return TryActivate;
        }
        return -1;
    }

    void HideTextField() {
        if (this.m_textField.getState() != TextField.State.kTextFieldState_RequestHide && this.m_textField.getState() != TextField.State.kTextFieldState_Hidden) {
            this.m_textField.setState(TextField.State.kTextFieldState_RequestHide);
            this.m_activity.runOnUiThread(() -> {
                SystemUI_android.this.m_textField.hideTextField();
                SystemUI_android.this.m_textField.setState(TextField.State.kTextFieldState_Hidden);
            });
            this.m_textField.clearId();
        }
    }

    boolean IsTextFieldIdActive(int i) {
        return this.m_textField.IsActiveId(i);
    }

    boolean IsTextFieldShowing() {
        return this.m_textField.getState() == TextField.State.kTextFieldState_Showing;
    }

    float GetTextFieldHeight() {
        return this.m_activity.transformHeightToProgram(this.m_textField.getTextFieldHeight());
    }

    boolean IsKeyboardShowing() {
        return this.m_keyboardIsShowing;
    }

    String GetTextEditBuffer() {
        return this.m_textField.getTextBuffer();
    }

    int GetTextEditCursor() {
        return this.m_textField.getCursorPos();
    }

    int GetTextEditSelect() {
        return this.m_textField.getSelectPos();
    }

    float GetKeyboardHeight() {
        return this.m_activity.transformHeightToProgram(this.m_keyboardHeight);
    }

    public int ShowTextFieldDialog(String str, String str2, String str3, String str4, int i, int i2, final String str5, String str6) throws UnsupportedEncodingException {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        final String LocalizeString = LocalizeString(str);
        final String LocalizeString2 = LocalizeString(str2);
        final String LocalizeString3 = LocalizeString(str3);
        final String LocalizeString4 = LocalizeString(str4);
        final String str7 = (str6 == null || str6.isEmpty()) ? null : str6;
        this.m_textFieldLimiter.maxCharacters = i;
        this.m_textFieldLimiter.maxByteSize = i2;
        this.m_activity.runOnUiThread(() -> {
            SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1))), false);
            AlertDialog.Builder builder = new AlertDialog.Builder(SystemUI_android.this.m_activity);
            TextView textView = new TextView(SystemUI_android.this.m_activity);
            textView.setText(GetMarkedUpString, TextView.BufferType.SPANNABLE);
            int dp2px = Utils.dp2px(20.0f);
            FrameLayout frameLayout = new FrameLayout(SystemUI_android.this.m_activity);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
            layoutParams.leftMargin = dp2px;
            layoutParams.rightMargin = dp2px;
            layoutParams.topMargin = dp2px;
            frameLayout.addView(textView, layoutParams);
            builder.setCustomTitle(frameLayout);
            final EditText editText = new EditText(SystemUI_android.this.m_activity);
            editText.setText(str5, TextView.BufferType.SPANNABLE);
            String str8 = str7;
            if (str8 == null) {
                str8 = LocalizeString2;
            }
            editText.setHint(str8);
            editText.setFilters(new InputFilter[]{SystemUI_android.this.m_textFieldLimiter});
            editText.setImeOptions(33554438);
            editText.setSingleLine();
            FrameLayout frameLayout2 = new FrameLayout(SystemUI_android.this.m_activity);
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, -2);
            layoutParams2.leftMargin = dp2px;
            layoutParams2.rightMargin = dp2px;
            layoutParams2.topMargin = dp2px;
            frameLayout2.addView(editText, layoutParams);
            builder.setView(frameLayout2);
            builder.setOnDismissListener(dialogInterface -> GameActivity.hideNavigationFullScreen(SystemUI_android.this.m_activity.getBrigeView()));
            builder.setPositiveButton(LocalizeString3, (dialogInterface, i3) -> {
                String obj = editText.getText().toString();
                if (obj.isEmpty()) {
                    SystemUI_android.this.SetResult(str7 != null ? str7 : str5, 1, true);
                } else {
                    SystemUI_android.this.SetResult(obj, 1, true);
                }
                dialogInterface.dismiss();
            });

            builder.setNegativeButton(LocalizeString4, (dialogInterface, i3) -> {
                SystemUI_android.this.SetResult(null, 0, true);
                dialogInterface.dismiss();
            });

            builder.setOnCancelListener(dialogInterface -> SystemUI_android.this.SetResult(null, 0, true));
            AlertDialog create = builder.create();
            GameActivity.hideNavigationFullScreen(Objects.requireNonNull(create.getWindow()).getDecorView());
            create.getWindow().setFlags(8, 8);
            create.setCanceledOnTouchOutside(false);
            create.show();
            create.getWindow().clearFlags(8);
        });
        return TryActivate;
    }

    public int ShowConfirmationDialog(String str, String str2, String str3, String str4, boolean z) {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        final String LocalizeString = LocalizeString(str);
        final String LocalizeString2 = LocalizeString(str2);
        final String LocalizeString3 = LocalizeString(str3);
        final String LocalizeString4 = LocalizeString(str4);
        this.m_activity.runOnUiThread(new Runnable() {
            @SuppressLint("RestrictedApi")
            @Override
            public void run() {
                SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1))), false);
                SpannableStringBuilder GetMarkedUpString2 = SystemUI_android.this.GetMarkedUpString(LocalizeString2, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false);
                AlertDialog.Builder builder = new AlertDialog.Builder(SystemUI_android.this.m_activity);
                TextView textView = new TextView(SystemUI_android.this.m_activity);
                textView.setText(GetMarkedUpString, TextView.BufferType.SPANNABLE);
                int dp2px = Utils.dp2px(20.0f);
                FrameLayout frameLayout = new FrameLayout(SystemUI_android.this.m_activity);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
                layoutParams.leftMargin = dp2px;
                layoutParams.rightMargin = dp2px;
                layoutParams.topMargin = dp2px;
                frameLayout.addView(textView, layoutParams);
                builder.setCustomTitle(frameLayout);
                TextView textView2 = new TextView(SystemUI_android.this.m_activity);
                textView2.setText(GetMarkedUpString2, TextView.BufferType.SPANNABLE);
                FrameLayout frameLayout2 = new FrameLayout(SystemUI_android.this.m_activity);
                FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, -2);
                layoutParams2.leftMargin = dp2px;
                layoutParams2.rightMargin = dp2px;
                layoutParams2.topMargin = dp2px;
                frameLayout2.addView(textView2, layoutParams2);
                builder.setView(frameLayout2);
                builder.setPositiveButton(LocalizeString3, (dialogInterface, i) -> {
                    SystemUI_android.this.SetResult(null, 1, true);
                    dialogInterface.dismiss();
                });

                builder.setNegativeButton(LocalizeString4, (dialogInterface, i) -> {
                    SystemUI_android.this.SetResult(null, 0, true);
                    dialogInterface.dismiss();
                });

                builder.setOnCancelListener(dialogInterface -> SystemUI_android.this.SetResult(null, 0, true));
                AlertDialog create = builder.create();
                GameActivity.hideNavigationFullScreen(Objects.requireNonNull(create.getWindow()).getDecorView());
                create.getWindow().setFlags(8, 8);
                create.setCanceledOnTouchOutside(false);
                create.show();
                create.getWindow().clearFlags(8);
                create.getButton(-1).setTextColor(SupportMenu.CATEGORY_MASK);
                create.getButton(-2).setTextColor(-16776961);
            }
        });
        return TryActivate;
    }

    public int ShowCountdownDialog(String str, String str2, String str3, String str4, final int i, final int i2, String str5) {
        int TryActivate;
        if (i == 0 && i2 == 0) {
            return ShowConfirmationDialog(str, str2, str3, str4, false);
        }
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        final String LocalizeString = LocalizeString(str);
        final String LocalizeString2 = LocalizeString(str2);
        final String LocalizeString3 = str5 != null ? LocalizeString(str5) : null;
        final String LocalizeString4 = LocalizeString(str3);
        final String LocalizeString5 = LocalizeString(str4);
        final int max = Integer.max(0, Integer.max(i, i2));
        this.m_activity.runOnUiThread(new Runnable() {
            @SuppressLint("RestrictedApi")
            @Override
            public void run() {
                int i3;
                SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1))), false);
                final SpannableStringBuilder GetMarkedUpString2 = SystemUI_android.this.GetMarkedUpString(LocalizeString2, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false);
                AlertDialog.Builder builder = new AlertDialog.Builder(SystemUI_android.this.m_activity);
                TextView textView = new TextView(SystemUI_android.this.m_activity);
                textView.setText(GetMarkedUpString, TextView.BufferType.SPANNABLE);
                int dp2px = Utils.dp2px(20.0f);
                FrameLayout frameLayout = new FrameLayout(SystemUI_android.this.m_activity);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
                layoutParams.leftMargin = dp2px;
                layoutParams.rightMargin = dp2px;
                layoutParams.topMargin = dp2px;
                frameLayout.addView(textView, layoutParams);
                builder.setCustomTitle(frameLayout);
                final TextView textView2 = new TextView(SystemUI_android.this.m_activity);
                textView2.setText(GetMarkedUpString2, TextView.BufferType.SPANNABLE);
                FrameLayout frameLayout2 = new FrameLayout(SystemUI_android.this.m_activity);
                FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, -2);
                layoutParams2.leftMargin = dp2px;
                layoutParams2.rightMargin = dp2px;
                layoutParams2.topMargin = dp2px;
                frameLayout2.addView(textView2, layoutParams2);
                builder.setView(frameLayout2);
                if (LocalizeString3 != null && (i3 = i) > 0) {
                    textView2.setText(SystemUI_android.this.GetMarkedUpString(LocalizeString3.replace("{{1}}", Integer.toString(i3)), new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false), TextView.BufferType.SPANNABLE);
                } else {
                    textView2.setText(GetMarkedUpString2, TextView.BufferType.SPANNABLE);
                }
                builder.setPositiveButton(LocalizeString4, (dialogInterface, i4) -> {
                    SystemUI_android.this.SetResult(null, 1, true);
                    dialogInterface.dismiss();
                });
                builder.setNegativeButton(LocalizeString5, (dialogInterface, i4) -> {
                    SystemUI_android.this.SetResult(null, 0, true);
                    dialogInterface.dismiss();
                });
                builder.setOnCancelListener(dialogInterface -> SystemUI_android.this.SetResult(null, 0, true));
                builder.setCancelable(i2 == 0);
                final AlertDialog create = builder.create();
                GameActivity.hideNavigationFullScreen(Objects.requireNonNull(create.getWindow()).getDecorView());
                create.getWindow().setFlags(8, 8);
                create.setCanceledOnTouchOutside(false);
                create.show();
                create.getWindow().clearFlags(8);
                if (i == 0) {
                    create.getButton(-1).setTextColor(-16776961);
                } else {
                    create.getButton(-1).setEnabled(false);
                }
                if (i2 == 0) {
                    create.getButton(-2).setTextColor(SupportMenu.CATEGORY_MASK);
                } else {
                    create.getButton(-2).setEnabled(false);
                }
                if (max > 0) {
                    new CountDownTimer(max * 1000L, 1000L) {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onTick(long j) {
                            int i4 = (int) (max - (j / 1000));
                            if (i >= 0 && i <= i4) {
                                create.getButton(-1).setEnabled(true);
                                create.getButton(-1).setTextColor(-16776961);
                            }
                            if (i2 >= 0 && i2 <= i4) {
                                create.getButton(-2).setEnabled(true);
                                create.getButton(-2).setTextColor(SupportMenu.CATEGORY_MASK);
                            }
                            if (LocalizeString3 == null || i < i4) {
                                return;
                            }
                            textView2.setText(SystemUI_android.this.GetMarkedUpString(LocalizeString3.replace("{{1}}", Integer.toString(i - i4)), new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false), TextView.BufferType.SPANNABLE);
                        }

                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onFinish() {
                            if (i >= 0) {
                                create.getButton(-1).setEnabled(true);
                                create.getButton(-1).setTextColor(-16776961);
                            }
                            if (i2 >= 0) {
                                create.getButton(-2).setEnabled(true);
                                create.getButton(-2).setTextColor(SupportMenu.CATEGORY_MASK);
                                create.setCancelable(true);
                            }
                            textView2.setText(GetMarkedUpString2, TextView.BufferType.SPANNABLE);
                        }
                    }.start();
                }
            }
        });
        return TryActivate;
    }

    public int ShowInfoDialog(String str, String str2) {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        final String LocalizeString = LocalizeString(str);
        final String LocalizeString2 = LocalizeString(str2);
        final String LocalizeString3 = LocalizeString("system_button_ok");
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.SystemUI_android.9
            @Override // java.lang.Runnable
            public void run() {
                SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1))), false);
                SpannableStringBuilder GetMarkedUpString2 = SystemUI_android.this.GetMarkedUpString(LocalizeString2, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false);
                AlertDialog.Builder builder = new AlertDialog.Builder(SystemUI_android.this.m_activity);
                TextView textView = new TextView(SystemUI_android.this.m_activity);
                textView.setText(GetMarkedUpString, TextView.BufferType.SPANNABLE);
                int dp2px = Utils.dp2px(20.0f);
                FrameLayout frameLayout = new FrameLayout(SystemUI_android.this.m_activity);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
                layoutParams.leftMargin = dp2px;
                layoutParams.rightMargin = dp2px;
                layoutParams.topMargin = dp2px;
                frameLayout.addView(textView, layoutParams);
                builder.setCustomTitle(frameLayout);
                TextView textView2 = new TextView(SystemUI_android.this.m_activity);
                textView2.setText(GetMarkedUpString2, TextView.BufferType.SPANNABLE);
                FrameLayout frameLayout2 = new FrameLayout(SystemUI_android.this.m_activity);
                FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, -2);
                layoutParams2.leftMargin = dp2px;
                layoutParams2.rightMargin = dp2px;
                layoutParams2.topMargin = dp2px;
                frameLayout2.addView(textView2, layoutParams2);
                builder.setView(frameLayout2);
                builder.setPositiveButton(LocalizeString3, new DialogInterface.OnClickListener() { // from class: com.tgc.sky.SystemUI_android.9.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult(null, 1, true);
                        dialogInterface.dismiss();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.tgc.sky.SystemUI_android.9.2
                    @Override // android.content.DialogInterface.OnCancelListener
                    public void onCancel(DialogInterface dialogInterface) {
                        SystemUI_android.this.SetResult(null, 1, true);
                    }
                });
                AlertDialog create = builder.create();
                GameActivity.hideNavigationFullScreen(create.getWindow().getDecorView());
                create.getWindow().setFlags(8, 8);
                create.setCanceledOnTouchOutside(false);
                create.show();
                create.getWindow().clearFlags(8);
                create.getButton(-1).setTextColor(-16776961);
            }
        });
        return TryActivate;
    }

    public int ShowDateTimeDialog(String str, String str2, final long j) {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        final String LocalizeString = LocalizeString(str);
        final String LocalizeString2 = LocalizeString(str2);
        final String LocalizeString3 = LocalizeString("system_button_ok");
        final String LocalizeString4 = LocalizeString("system_button_reset");
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.SystemUI_android.10
            @Override // java.lang.Runnable
            public void run() {
                SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1))), false);
                SystemUI_android.this.GetMarkedUpString(LocalizeString2, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false);
                AlertDialog.Builder builder = new AlertDialog.Builder(SystemUI_android.this.m_activity);
                TextView textView = new TextView(SystemUI_android.this.m_activity);
                textView.setText(GetMarkedUpString, TextView.BufferType.SPANNABLE);
                int dp2px = Utils.dp2px(20.0f);
                FrameLayout frameLayout = new FrameLayout(SystemUI_android.this.m_activity);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
                layoutParams.leftMargin = dp2px;
                layoutParams.rightMargin = dp2px;
                layoutParams.topMargin = dp2px;
                layoutParams.bottomMargin = dp2px;
                frameLayout.addView(textView, layoutParams);
                builder.setCustomTitle(frameLayout);
                LinearLayout linearLayout = new LinearLayout(SystemUI_android.this.m_activity);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setGravity(17);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(j * 1000);
                final DatePicker datePicker = new DatePicker(SystemUI_android.this.m_activity);
                datePicker.init(calendar.get(1), calendar.get(2), calendar.get(5), null);
                datePicker.setCalendarViewShown(false);
                final TimePicker timePicker = new TimePicker(SystemUI_android.this.m_activity);
                timePicker.setHour(calendar.get(11));
                timePicker.setMinute(calendar.get(12));
                linearLayout.addView(datePicker);
                linearLayout.addView(timePicker);
                builder.setView(linearLayout);
                builder.setPositiveButton(LocalizeString3, new DialogInterface.OnClickListener() { // from class: com.tgc.sky.SystemUI_android.10.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult(null, (int) (SystemUI_android.this.GetEpochTime("", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute(), 0) - (Calendar.getInstance().getTimeInMillis() / 1000)), true);
                        dialogInterface.dismiss();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.tgc.sky.SystemUI_android.10.2
                    @Override // android.content.DialogInterface.OnCancelListener
                    public void onCancel(DialogInterface dialogInterface) {
                        SystemUI_android.this.SetResult(null, 0, true);
                    }
                });
                builder.setNegativeButton(LocalizeString4, new DialogInterface.OnClickListener() { // from class: com.tgc.sky.SystemUI_android.10.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult(null, 0, true);
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog create = builder.create();
                GameActivity.hideNavigationFullScreen(create.getWindow().getDecorView());
                create.getWindow().setFlags(8, 8);
                create.setCanceledOnTouchOutside(false);
                create.show();
                create.getWindow().clearFlags(8);
                create.getButton(-1).setTextColor(-16776961);
            }
        });
        return TryActivate;
    }

    public synchronized DialogResult TryGetDialogResult(int n) {
        // monitorenter(this)
        if (n != -1) {
            try {
                if (n == this.m_currentId) {
                    final DialogResult result = this.m_result;
                    n = ResponseSwitchMap.INTS[this.m_result.response.ordinal()];
                    if (n != 3) {
                        if (n == 4) {
                            this.m_result = new DialogResult();
                        }
                    }
                    else {
                        final DialogResult result2 = new DialogResult();
                        this.m_result = result2;
                        result2.response = DialogResult.Response.kWaiting;
                    }
                    return result;
                }
            }
            finally {
            }
            // monitorexit(this)
        }
        // monitorexit(this)
        return null;
    }

    /* renamed from: com.tgc.sky.SystemUI_android$25 */
    static /* synthetic */ class ResponseSwitchMap {
        static final /* synthetic */ int[] INTS;
        static {
            INTS = new int[DialogResult.Response.values().length];
            INTS[DialogResult.Response.kInvalid.ordinal()] = 1;
            INTS[DialogResult.Response.kWaiting.ordinal()] = 2;
            INTS[DialogResult.Response.kResponded.ordinal()] = 3;
            INTS[DialogResult.Response.kClosed.ordinal()] = 4;
        }
    }

    public boolean IsAnyDialogShowing() {
        return this.m_result.response != DialogResult.Response.kInvalid;
    }

    public boolean RequestUserAppRating() {
        if (GetMainWindowAttachedSheet()) {
            return false;
        }
        final ReviewManager create = ReviewManagerFactory.create(this.m_activity);
        create.requestReviewFlow().addOnCompleteListener(new OnCompleteListener<ReviewInfo>() { // from class: com.tgc.sky.SystemUI_android.11
            @Override // com.google.android.play.core.tasks.OnCompleteListener
            public void onComplete(Task<ReviewInfo> task) {
                if (task.isSuccessful()) {
                    create.launchReviewFlow(SystemUI_android.this.m_activity, task.getResult()).addOnCompleteListener(new OnCompleteListener<Void>() { // from class: com.tgc.sky.SystemUI_android.11.1
                        @Override // com.google.android.play.core.tasks.OnCompleteListener
                        public void onComplete(Task<Void> task2) {
                        }
                    });
                }
            }
        });
        return true;
    }

    public int ShowCodeScanner(final int i) {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.SystemUI_android.12
            @Override // java.lang.Runnable
            public void run() {
                SystemUI_android systemUI_android = SystemUI_android.this;
                GameActivity gameActivity = SystemUI_android.this.m_activity;
                SystemUI_android systemUI_android2 = SystemUI_android.this;
                systemUI_android.m_codeScanner = new CodeScanner(gameActivity, systemUI_android2, systemUI_android2.m_markup, CodeScanner.Mode.values()[i], new CodeScanner.Handle() { // from class: com.tgc.sky.SystemUI_android.12.1
                    @Override // com.tgc.sky.ui.panels.CodeScanner.Handle
                    public void run(String str, int i2, boolean z) {
                        SystemUI_android.this.SetResult(str, i2, z);
                        if (z) {
                            SystemUI_android.this.m_codeScanner = null;
                        }
                    }
                });
                SystemUI_android.this.m_codeScanner.showAtLocation(SystemUI_android.this.m_activity.getWindow().getDecorView(), 19, 0, 0);
            }
        });
        return TryActivate;
    }

    public void InformCodeInputResult(String str, String str2, int i) {
        CodeScanner codeScanner = this.m_codeScanner;
        if (codeScanner != null) {
            codeScanner.informCodeInputResult(str, str2, i);
        }
    }

    public int ShowStarboardView(String str, String str2) {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }

        this.m_activity.runOnUiThread(() -> {
            SystemUI_android systemUI_android = SystemUI_android.this;
            GameActivity gameActivity = SystemUI_android.this.m_activity;
            SystemUI_android systemUI_android2 = SystemUI_android.this;
            systemUI_android.m_starBoard = new Starboard(gameActivity, systemUI_android2, systemUI_android2.m_markup, new Starboard.Handle() { // from class: com.tgc.sky.SystemUI_android.13.1
                @Override
                public void run(String str3, int i, boolean z) {
                    SystemUI_android.this.SetResult(str3, i, z);
                    if (z) {
                        SystemUI_android.this.m_starBoard = null;
                    }
                }
            });
            SystemUI_android.this.m_starBoard.showAtLocation(SystemUI_android.this.m_activity.getWindow().getDecorView(), 19, 0, 0);
        });
        return TryActivate;
    }

    public int GetQRCameraPermissionState() {
        return this.m_qrCameraHandler.getPermissionState().ordinal();
    }

    public void RequestPermissionAndStartQRCamera() {
        this.m_qrCameraHandler.requestPermissionAndStartCamera();
    }

    public void StopQRCamera() {
        this.m_qrCameraHandler.stopCamera();
    }

    public boolean IsQRCameraRunning() {
        return this.m_qrCameraHandler.isRunning();
    }

    public int GetQRCameraVideoFormat() {
        return this.m_qrCameraHandler.getFormat().ordinal();
    }

    public void LockQRCamera() {
        this.m_qrCameraHandler.lock();
    }

    public int GetQRCameraWidth() {
        return this.m_qrCameraHandler.getWidth();
    }

    public int GetQRCameraHeight() {
        return this.m_qrCameraHandler.getHeight();
    }

    public byte[] GetQRCameraImageBuffer() {
        return this.m_qrCameraHandler.getImageBuffer().array();
    }

    public void UnlockQRCamera() {
        this.m_qrCameraHandler.unlock();
    }

    public int StartListeningForQRScanEvent() {
        if (IsQRCameraRunning()) {
            int TryActivate = TryActivate();
            if (TryActivate != -1) {
                this.m_qrCameraHandler.startListeningForQRScanEvent();
            }
            return TryActivate;
        }
        return -1;
    }

    public boolean StopListeningForQRScanEvent(int i) {
        if (i == this.m_currentId) {
            this.m_qrCameraHandler.stopListeningForQRScanEvent();
            this.m_result = new DialogResult();
            return true;
        }
        return false;
    }

    public int ShowQRImagePicker() {
        int TryActivate = TryActivate();
        if (TryActivate != -1) {
            this.m_qrCameraHandler.showQRImagePicker();
        }
        return TryActivate;
    }

    boolean ShowFacebookFriendFinder() {
        //LaunchURL(String.format("https://fb.gg/me/friendfinder/%s", this.m_activity.getString(R.string.facebook_app_id)));
        return true;
    }

    int PresentFacebookFriendFinder() {
        return -1;
/*        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        ((Facebook) SystemAccounts_android.getInstance().GetSystemAccount(SystemAccountType.kSystemAccountType_Facebook)).ShowFriendFinderDialog(new Facebook.OnCallback() { // from class: com.tgc.sky.SystemUI_android.13
            @Override // com.tgc.sky.accounts.Facebook.OnCallback
            public void onCallback(boolean z, String str) {
                if (str != null) {
                    SystemUI_android.this.SetResult(str, 2, true);
                } else {
                    SystemUI_android.this.SetResult(null, z ? 1 : 0, true);
                }
            }
        });
        return TryActivate;*/
    }

    boolean HasFacebookFriendGraphPermission() {
       // return ((Facebook) SystemAccounts_android.getInstance().GetSystemAccount(SystemAccountType.kSystemAccountType_Facebook)).HasAppFriendsPermission();
        return false;
    }

    int RequestFacebookFriendGraphPermission() {
        return -1;
/*        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        if (!((Facebook) SystemAccounts_android.getInstance().GetSystemAccount(SystemAccountType.kSystemAccountType_Facebook)).GetAppFriendsPermission(new Facebook.OnCallback() { // from class: com.tgc.sky.SystemUI_android.14
            @Override // com.tgc.sky.accounts.Facebook.OnCallback
            public void onCallback(boolean z, String str) {
                if (str != null) {
                    SystemUI_android.this.SetResult(str, 2, true);
                } else {
                    SystemUI_android.this.SetResult(null, z ? 1 : 0, true);
                }
            }
        })) {
            SetResult("Another request is already pending", 2, true);
        }
        return TryActivate;*/
    }

    public int ShareURL(String str, String str2) throws UnsupportedEncodingException {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        String LocalizeString = LocalizeString(str);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.setType("text/html");
        intent.putExtra("android.intent.extra.SUBJECT", LocalizeString);
        intent.putExtra("android.intent.extra.TEXT", str2);
        this.m_activity.startActivityForResult(Intent.createChooser(intent, LocalizeString), 110);
        this.m_activity.AddOnActivityResultListener(new GameActivity.OnActivityResultListener() { // from class: com.tgc.sky.SystemUI_android.15
            @Override // com.tgc.sky.GameActivity.OnActivityResultListener
            public void onActivityResult(int i, int i2, Intent intent2) {
                if (i == 110) {
                    SystemUI_android.this.SetResult(null, i2 == -1 ? 1 : 0, true);
                    SystemUI_android.this.m_activity.RemoveOnActivityResultListeners(this);
                }
            }
        });
        return TryActivate;
    }

    public int ShareImage(String str, String str2, byte[] bArr, final boolean z) {
        final int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        String LocalizeString = LocalizeString(str);
        try {
            Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
            File file = new File(this.m_activity.getExternalCacheDir() + "/temp/ShareImageTemp.png");
            file.delete();
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            decodeByteArray.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Uri fromFile = Uri.fromFile(file);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.SEND");
            intent.setType("image/*");
            intent.putExtra("android.intent.extra.SUBJECT", LocalizeString);
            intent.putExtra("android.intent.extra.TEXT", str2);
            intent.putExtra("android.intent.extra.STREAM", fromFile);
            this.m_activity.startActivityForResult(Intent.createChooser(intent, LocalizeString), 111);
            this.m_activity.AddOnActivityResultListener(new GameActivity.OnActivityResultListener() { // from class: com.tgc.sky.SystemUI_android.16
                @Override // com.tgc.sky.GameActivity.OnActivityResultListener
                public void onActivityResult(int i, int i2, Intent intent2) {
                    if (i == 111) {
                        SystemUI_android.this.SetResult(null, i2 == -1 ? 1 : 0, true);
                        SystemUI_android.this.m_activity.RemoveOnActivityResultListeners(this);
                        if (z) {
                            SystemUI_android.this.TryGetDialogResult(TryActivate);
                        }
                    }
                }
            });
            return TryActivate;
        } catch (IOException e) {
            e.printStackTrace();
            SetResult(null, 0, true);
            return -1;
        }
    }

    public int ShareVideo(String str) {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        File file = new File((this.m_activity.getExternalCacheDir() + "/") + str);
        Context baseContext = this.m_activity.getBaseContext();
        try {
            Uri uriForFile = FileProvider.getUriForFile(baseContext, baseContext.getApplicationContext().getPackageName() + ".provider", file);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.SEND");
            intent.setType("video/*");
            intent.putExtra("android.intent.extra.STREAM", uriForFile);
            this.m_activity.startActivityForResult(Intent.createChooser(intent, ""), 112);
            this.m_activity.AddOnActivityResultListener(new GameActivity.OnActivityResultListener() { // from class: com.tgc.sky.SystemUI_android.17
                @Override // com.tgc.sky.GameActivity.OnActivityResultListener
                public void onActivityResult(int i, int i2, Intent intent2) {
                    if (i == 112) {
                        SystemUI_android.this.SetResult(null, i2 == -1 ? 1 : 0, true);
                        SystemUI_android.this.m_activity.RemoveOnActivityResultListeners(this);
                    }
                }
            });
            return TryActivate;
        } catch (Exception unused) {
            return -1;
        }
    }

    public void SaveImage(String str, byte[] bArr) {
        String[] strArr = {"android.permission.WRITE_EXTERNAL_STORAGE"};
        if (this.m_activity.checkSelfPermissions(strArr) || Build.VERSION.SDK_INT >= 29) {
            SaveImageInternal(str, bArr);
        } else {
            SaveImageAskPermissions(strArr, true, str, bArr);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void SaveImageAskPermissions(String[] strArr, boolean z, String str, byte[] bArr) {
        this.m_activity.runOnUiThread(new AnonymousClass18(LocalizeString("system_screenshot_permission_title"), LocalizeString(z ? "system_screenshot_permission_message_00" : "system_screenshot_permission_message_01"), LocalizeString(z ? "system_button_ask" : "system_button_settings"), z, strArr, str, bArr, LocalizeString("system_button_cancel")));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.tgc.sky.SystemUI_android$18  reason: invalid class name */
    /* loaded from: classes2.dex */
    public class AnonymousClass18 implements Runnable {
        final /* synthetic */ String val$_cancelButton;
        final /* synthetic */ String val$_message;
        final /* synthetic */ String val$_okButton;
        final /* synthetic */ String val$_title;
        final /* synthetic */ boolean val$canAsk;
        final /* synthetic */ byte[] val$imageData;
        final /* synthetic */ String[] val$permissions;
        final /* synthetic */ String val$title;

        AnonymousClass18(String str, String str2, String str3, boolean z, String[] strArr, String str4, byte[] bArr, String str5) {
            this.val$_title = str;
            this.val$_message = str2;
            this.val$_okButton = str3;
            this.val$canAsk = z;
            this.val$permissions = strArr;
            this.val$title = str4;
            this.val$imageData = bArr;
            this.val$_cancelButton = str5;
        }

        @SuppressLint("RestrictedApi")
        @Override // java.lang.Runnable
        public void run() {
            SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(this.val$_title, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1))), false);
            SpannableStringBuilder GetMarkedUpString2 = SystemUI_android.this.GetMarkedUpString(this.val$_message, new ArrayList<>(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false);
            AlertDialog.Builder builder = new AlertDialog.Builder(SystemUI_android.this.m_activity);
            TextView textView = new TextView(SystemUI_android.this.m_activity);
            textView.setText(GetMarkedUpString, TextView.BufferType.SPANNABLE);
            int dp2px = Utils.dp2px(20.0f);
            FrameLayout frameLayout = new FrameLayout(SystemUI_android.this.m_activity);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
            layoutParams.leftMargin = dp2px;
            layoutParams.rightMargin = dp2px;
            layoutParams.topMargin = dp2px;
            frameLayout.addView(textView, layoutParams);
            builder.setCustomTitle(frameLayout);
            TextView textView2 = new TextView(SystemUI_android.this.m_activity);
            textView2.setText(GetMarkedUpString2, TextView.BufferType.SPANNABLE);
            FrameLayout frameLayout2 = new FrameLayout(SystemUI_android.this.m_activity);
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, -2);
            layoutParams2.leftMargin = dp2px;
            layoutParams2.rightMargin = dp2px;
            layoutParams2.topMargin = dp2px;
            frameLayout2.addView(textView2, layoutParams2);
            builder.setView(frameLayout2);
            builder.setPositiveButton(this.val$_okButton, new DialogInterface.OnClickListener() { // from class: com.tgc.sky.SystemUI_android.18.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (AnonymousClass18.this.val$canAsk) {
                        SystemUI_android.this.m_activity.requestPermissions(AnonymousClass18.this.val$permissions, new GameActivity.PermissionCallback() { // from class: com.tgc.sky.SystemUI_android.18.1.1
                            @Override // com.tgc.sky.GameActivity.PermissionCallback
                            public void onPermissionResult(String[] strArr, int[] iArr) {
                                if (SystemUI_android.this.m_activity.checkResultPermissions(iArr)) {
                                    SystemUI_android.this.SaveImageInternal(AnonymousClass18.this.val$title, AnonymousClass18.this.val$imageData);
                                } else if (SystemUI_android.this.m_activity.shouldShowRequestPermissionsRationale(strArr)) {
                                } else {
                                    SystemUI_android.this.SaveImageAskPermissions(strArr, false, AnonymousClass18.this.val$title, AnonymousClass18.this.val$imageData);
                                }
                            }
                        });
                        dialogInterface.dismiss();
                        return;
                    }
                    SystemUI_android.this.m_activity.requestPermissionsThroughSettings(AnonymousClass18.this.val$permissions, new GameActivity.PermissionCallback() { // from class: com.tgc.sky.SystemUI_android.18.1.2
                        @Override // com.tgc.sky.GameActivity.PermissionCallback
                        public void onPermissionResult(String[] strArr, int[] iArr) {
                            if (SystemUI_android.this.m_activity.checkResultPermissions(iArr)) {
                                SystemUI_android.this.SaveImageInternal(AnonymousClass18.this.val$title, AnonymousClass18.this.val$imageData);
                            }
                        }
                    });
                }
            });
            builder.setNegativeButton(this.val$_cancelButton, new DialogInterface.OnClickListener() { // from class: com.tgc.sky.SystemUI_android.18.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog create = builder.create();
            GameActivity.hideNavigationFullScreen(create.getWindow().getDecorView());
            create.getWindow().setFlags(8, 8);
            create.setCanceledOnTouchOutside(false);
            create.show();
            create.getWindow().clearFlags(8);
            create.getButton(-1).setTextColor(SupportMenu.CATEGORY_MASK);
            create.getButton(-2).setTextColor(-16776961);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void SaveImageInternal(String str, byte[] bArr) {
        OutputStream fileOutputStream;
        try {
            Context baseContext = this.m_activity.getBaseContext();
            long currentTimeMillis = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();
            ContentResolver contentResolver = baseContext.getContentResolver();
            String str2 = "SKY_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
            contentValues.put("_display_name", str2 + ".jpg");
            contentValues.put("mime_type", "image/jpeg");
            contentValues.put("date_added", Long.valueOf(currentTimeMillis));
            contentValues.put("datetaken", Long.valueOf(currentTimeMillis));
            if (Build.VERSION.SDK_INT >= 29) {
                contentValues.put("relative_path", Environment.DIRECTORY_PICTURES + File.separator + "Sky");
                Uri insert = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fileOutputStream = insert != null ? contentResolver.openOutputStream(insert) : null;
            } else {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Sky").toString());
                if (!file.exists()) {
                    file.mkdirs();
                }
                File createTempFile = File.createTempFile(str2, ".jpg", file);
                contentValues.put("_data", createTempFile.getAbsolutePath());
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fileOutputStream = new FileOutputStream(createTempFile);
            }
            if (fileOutputStream != null) {
                fileOutputStream.write(bArr);
                fileOutputStream.flush();
                fileOutputStream.close();
                return;
            }
            Log.e("GameActivity", "Unable to save image");
        } catch (IOException e) {
            Log.e("GameActivity", "IO error when saving image: " + e.getMessage());
        }
    }

    public void FreeLocalizedText(int i) {
        this.m_localizationManager.FreeLocalizedText(i);
    }

    public void SetLocalizedText(int i, String str) {
        this.m_localizationManager.SetLocalizedText(i, str);
    }

    public void SetLocalizedTextWithArgs(int i, String str, float f) {
        this.m_localizationManager.SetLocalizedTextWithArgs(i, str, f);
    }

    public void SetLocalizedTextWithArgs(int i, String str, String str2) {
        this.m_localizationManager.SetLocalizedTextWithArgs(i, str, str2);
    }

    public void SetLocalizedTextWithArgs(int i, String str, float f, float f2) {
        this.m_localizationManager.SetLocalizedTextWithArgs(i, str, f, f2);
    }

    public void SetLocalizedTextWithArgs(int i, String str, float f, String str2) {
        this.m_localizationManager.SetLocalizedTextWithArgs(i, str, f, str2);
    }

    public void SetLocalizedTextWithArgs(int i, String str, String str2, float f) {
        this.m_localizationManager.SetLocalizedTextWithArgs(i, str, str2, f);
    }

    public void SetLocalizedTextWithArgs(int i, String str, String str2, String str3) {
        this.m_localizationManager.SetLocalizedTextWithArgs(i, str, str2, str3);
    }

    public void SetLocalizedTextWithArgs(int i, String str, float f, float f2, float f3) {
        this.m_localizationManager.SetLocalizedTextWithArgs(i, str, f, f2, f3);
    }

    public void SetLocalizedTextWithArgs(int i, String str, String str2, String str3, String str4) {
        this.m_localizationManager.SetLocalizedTextWithArgs(i, str, str2, str3, str4);
    }

    public void SetLocalizedTextCompounded(int i, int[] iArr) {
        this.m_localizationManager.SetLocalizedTextCompounded(i, iArr);
    }

    public void SetPreLocalizedText(int i, String str) {
        this.m_localizationManager.SetPreLocalizedText(i, str);
    }

    public String GetPreferredLanguageID() {
        return Locale.getDefault().getLanguage();
    }

    public String GetPreferredSupportedLanguageID() {
        String language = this.m_activity.getResources().getConfiguration().getLocales().get(0).getLanguage();
        String script = this.m_activity.getResources().getConfiguration().getLocales().get(0).getScript();
        if (!script.isEmpty()) {
            language = language + "-" + script;
        }
        return language.equalsIgnoreCase("in") ? "id" : language;
    }

    public boolean IsPreferredSupportedLanguageJapanese() {
        return GetPreferredSupportedLanguageID().equalsIgnoreCase("ja");
    }

    public boolean IsPreferredSupportedLanguageVietnamese() {
        return GetPreferredSupportedLanguageID().equalsIgnoreCase("vi");
    }

    public void SetGameInputConfig(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, String[] strArr, String[] strArr2) {
        this.m_localizationManager.SetGameInputConfig(z, z2, z3, z4, z5, z6, z7);
        this.m_markup.SetGamepadButtonMap(strArr, strArr2);
        this.m_usingGamepad = z4;
        this.m_enableHaptics = z7;
    }

    public void AddTextLabel(int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, float f, float f2, boolean z6, int i2, boolean z7, int i3, float[] fArr, float[] fArr2, float f3, float[] fArr3, float[] fArr4, float f4, float f5, float f6, int i4, int i5, boolean z8, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, boolean z9, boolean z10, int i6, boolean z11) {
        TextLabel GetTextLabel = this.m_textLabelManager.GetTextLabel(i);
        if (GetTextLabel == null) {
            return;
        }
        GetTextLabel.attrs.fontName = "";
        GetTextLabel.attrs.hasBackground = z;
        GetTextLabel.attrs.hasShadow = z2;
        GetTextLabel.attrs.forceBold = z3;
        GetTextLabel.attrs.adjustFontSizeToFitWidth = z4;
        GetTextLabel.attrs.ignoreMarkupOptimization = z5;
        GetTextLabel.attrs.fontSize = f;
        GetTextLabel.attrs.scale = f2;
        GetTextLabel.attrs.trilinearMinification = z6;
        GetTextLabel.attrs.maxNumberOfLines = i2;
        GetTextLabel.attrs.truncateWithEllipses = z7;
        GetTextLabel.attrs.textAlignment = SystemHAlignment.values()[i3];
        GetTextLabel.attrs.textColor = fArr;
        GetTextLabel.attrs.bgColor = fArr2;
        GetTextLabel.attrs.bgCornerRadius = f3;
        GetTextLabel.attrs.shadowColor = fArr3;
        GetTextLabel.attrs.shadowOffset = fArr4;
        GetTextLabel.pos.f1049x = f4;
        GetTextLabel.pos.f1050y = f5;
        GetTextLabel.pos.f1051z = f6;
        GetTextLabel.pos.f1047h = SystemHAlignment.values()[i4];
        GetTextLabel.pos.f1048v = SystemVAlignment.values()[i5];
        GetTextLabel.pos.shrinkBoxToText = z8;
        GetTextLabel.pos.maxWidth = f7;
        GetTextLabel.pos.maxHeight = f8;
        GetTextLabel.pos.padWidth = f9;
        GetTextLabel.pos.padHeight = f10;
        GetTextLabel.pos.clipMinX = f11;
        GetTextLabel.pos.clipMinY = f12;
        GetTextLabel.pos.clipMaxX = f13;
        GetTextLabel.pos.clipMaxY = f14;
        GetTextLabel.pos.clip = z9;
        GetTextLabel.pos.autoAnchor = z10;
        GetTextLabel.textId = i6;
        GetTextLabel.autoFreeTextId = z11;
        this.m_textLabelManager.AddTextLabel(i);
    }

    public void AddTextLabel(int i, boolean z, boolean z2, boolean z3, boolean z4, float f, float f2, int i2, boolean z5, int i3, float[] fArr, float[] fArr2, float f3, float[] fArr3, float[] fArr4, float f4, float f5, float f6, int i4, int i5, boolean z6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, boolean z7, boolean z8, int i6, boolean z9) {
        TextLabel GetTextLabel = this.m_textLabelManager.GetTextLabel(i);
        if (GetTextLabel == null) {
            return;
        }
        GetTextLabel.attrs.fontName = "";
        GetTextLabel.attrs.hasBackground = z;
        GetTextLabel.attrs.hasShadow = z2;
        GetTextLabel.attrs.adjustFontSizeToFitWidth = z3;
        GetTextLabel.attrs.ignoreMarkupOptimization = z4;
        GetTextLabel.attrs.fontSize = f;
        GetTextLabel.attrs.scale = f2;
        GetTextLabel.attrs.maxNumberOfLines = i2;
        GetTextLabel.attrs.truncateWithEllipses = z5;
        GetTextLabel.attrs.textAlignment = SystemHAlignment.values()[i3];
        GetTextLabel.attrs.textColor = fArr;
        GetTextLabel.attrs.bgColor = fArr2;
        GetTextLabel.attrs.bgCornerRadius = f3;
        GetTextLabel.attrs.shadowColor = fArr3;
        GetTextLabel.attrs.shadowOffset = fArr4;
        GetTextLabel.pos.f1049x = f4;
        GetTextLabel.pos.f1050y = f5;
        GetTextLabel.pos.f1051z = f6;
        GetTextLabel.pos.f1047h = SystemHAlignment.values()[i4];
        GetTextLabel.pos.f1048v = SystemVAlignment.values()[i5];
        GetTextLabel.pos.shrinkBoxToText = z6;
        GetTextLabel.pos.maxWidth = f7;
        GetTextLabel.pos.maxHeight = f8;
        GetTextLabel.pos.padWidth = f9;
        GetTextLabel.pos.padHeight = f10;
        GetTextLabel.pos.clipMinX = f11;
        GetTextLabel.pos.clipMinY = f12;
        GetTextLabel.pos.clipMaxX = f13;
        GetTextLabel.pos.clipMaxY = f14;
        GetTextLabel.pos.clip = z7;
        GetTextLabel.pos.autoAnchor = z8;
        GetTextLabel.textId = i6;
        GetTextLabel.autoFreeTextId = z9;
        this.m_textLabelManager.AddTextLabel(i);
    }

    public float[] GetTextLabelSize(int i) {
        return this.m_textLabelManager.GetTextLabelSize(i);
    }

    public float[] GetTextLabelUnconstrainedSize(int i) {
        return this.m_textLabelManager.GetTextLabelUnconstrainedSize(i);
    }

    public void UpdateTextLabel(int i, boolean z, boolean z2, boolean z3, boolean z4, float f, float f2, int i2, int i3, float[] fArr, float[] fArr2, float f3, float[] fArr3, float[] fArr4, float f4, float f5, float f6, int i4, int i5, boolean z5, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, boolean z6, boolean z7) {
        TextLabelArgs GetTextLabelArgs = this.m_textLabelManager.GetTextLabelArgs();
        if (GetTextLabelArgs != null) {
            GetTextLabelArgs.labelId = i;
            GetTextLabelArgs.attrs.fontName = "";
            GetTextLabelArgs.attrs.hasBackground = z;
            GetTextLabelArgs.attrs.hasShadow = z2;
            GetTextLabelArgs.attrs.adjustFontSizeToFitWidth = z3;
            GetTextLabelArgs.attrs.ignoreMarkupOptimization = z4;
            GetTextLabelArgs.attrs.fontSize = f;
            GetTextLabelArgs.attrs.scale = f2;
            GetTextLabelArgs.attrs.maxNumberOfLines = i2;
            GetTextLabelArgs.attrs.textAlignment = SystemHAlignment.values()[i3];
            GetTextLabelArgs.attrs.textColor = fArr;
            GetTextLabelArgs.attrs.bgColor = fArr2;
            GetTextLabelArgs.attrs.bgCornerRadius = f3;
            GetTextLabelArgs.attrs.shadowColor = fArr3;
            GetTextLabelArgs.attrs.shadowOffset = fArr4;
            GetTextLabelArgs.pos.f1049x = f4;
            GetTextLabelArgs.pos.f1050y = f5;
            GetTextLabelArgs.pos.f1051z = f6;
            GetTextLabelArgs.pos.f1047h = SystemHAlignment.values()[i4];
            GetTextLabelArgs.pos.f1048v = SystemVAlignment.values()[i5];
            GetTextLabelArgs.pos.shrinkBoxToText = z5;
            GetTextLabelArgs.pos.maxWidth = f7;
            GetTextLabelArgs.pos.maxHeight = f8;
            GetTextLabelArgs.pos.padWidth = f9;
            GetTextLabelArgs.pos.padHeight = f10;
            GetTextLabelArgs.pos.clipMinX = f11;
            GetTextLabelArgs.pos.clipMinY = f12;
            GetTextLabelArgs.pos.clipMaxX = f13;
            GetTextLabelArgs.pos.clipMaxY = f14;
            GetTextLabelArgs.pos.clip = z6;
            GetTextLabelArgs.pos.autoAnchor = z7;
            this.m_textLabelManager.UpdateTextLabel(GetTextLabelArgs);
        }
    }

    public void UpdateTextLabel(int i, boolean z, boolean z2, boolean z3, boolean z4, float f, float f2, int i2, boolean z5, int i3, float[] fArr, float[] fArr2, float f3, float[] fArr3, float[] fArr4, float f4, float f5, float f6, int i4, int i5, boolean z6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, boolean z7, boolean z8) {
        TextLabelArgs GetTextLabelArgs = this.m_textLabelManager.GetTextLabelArgs();
        if (GetTextLabelArgs == null) {
            return;
        }
        GetTextLabelArgs.labelId = i;
        GetTextLabelArgs.attrs.fontName = "";
        GetTextLabelArgs.attrs.hasBackground = z;
        GetTextLabelArgs.attrs.hasShadow = z2;
        GetTextLabelArgs.attrs.adjustFontSizeToFitWidth = z3;
        GetTextLabelArgs.attrs.ignoreMarkupOptimization = z4;
        GetTextLabelArgs.attrs.fontSize = f;
        GetTextLabelArgs.attrs.scale = f2;
        GetTextLabelArgs.attrs.maxNumberOfLines = i2;
        GetTextLabelArgs.attrs.truncateWithEllipses = z5;
        GetTextLabelArgs.attrs.textAlignment = SystemHAlignment.values()[i3];
        GetTextLabelArgs.attrs.textColor = fArr;
        GetTextLabelArgs.attrs.bgColor = fArr2;
        GetTextLabelArgs.attrs.bgCornerRadius = f3;
        GetTextLabelArgs.attrs.shadowColor = fArr3;
        GetTextLabelArgs.attrs.shadowOffset = fArr4;
        GetTextLabelArgs.pos.f1049x = f4;
        GetTextLabelArgs.pos.f1050y = f5;
        GetTextLabelArgs.pos.f1051z = f6;
        GetTextLabelArgs.pos.f1047h = SystemHAlignment.values()[i4];
        GetTextLabelArgs.pos.f1048v = SystemVAlignment.values()[i5];
        GetTextLabelArgs.pos.shrinkBoxToText = z6;
        GetTextLabelArgs.pos.maxWidth = f7;
        GetTextLabelArgs.pos.maxHeight = f8;
        GetTextLabelArgs.pos.padWidth = f9;
        GetTextLabelArgs.pos.padHeight = f10;
        GetTextLabelArgs.pos.clipMinX = f11;
        GetTextLabelArgs.pos.clipMinY = f12;
        GetTextLabelArgs.pos.clipMaxX = f13;
        GetTextLabelArgs.pos.clipMaxY = f14;
        GetTextLabelArgs.pos.clip = z7;
        GetTextLabelArgs.pos.autoAnchor = z8;
        this.m_textLabelManager.UpdateTextLabel(GetTextLabelArgs);
    }
    public void RemoveTextLabel(int i) {
        this.m_textLabelManager.RemoveTextLabel(i);
    }

    boolean HapticFeedbackSuccess() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(() -> SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(0));
        }
        return false;
    }

    boolean HapticFeedbackSuccessStrong() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(() -> SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(0));
        }
        return false;
    }

    boolean HapticFeedbackWarning() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.SystemUI_android.21
                @Override // java.lang.Runnable
                public void run() {
                    SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(1);
                }
            });
        }
        return false;
    }

    boolean HapticFeedbackError() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.SystemUI_android.22
                @Override // java.lang.Runnable
                public void run() {
                    SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(4);
                }
            });
        }
        return false;
    }

    boolean HapticFeedbackSelection() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(() -> SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(6));
        }
        return false;
    }

    boolean HapticFeedbackImpactLight() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(() -> SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(3));
        }
        return false;
    }

    boolean HapticFeedbackImpact() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(() -> SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(1));
        }
        return false;
    }

    boolean HapticFeedbackImpactHeavy() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(() -> SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(0));
        }
        return false;
    }

    public String GetPasteBufferString() {
        return this.pasteStr;
    }

    public void SetPasteBufferString(final String str) {
        this.m_activity.runOnUiThread(() -> ((ClipboardManager) SystemUI_android.this.m_activity.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(null, str)));
    }

    public void LaunchURL(String str) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse(str));
        this.m_activity.startActivity(intent);
    }

    public void ShowSettings() {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", this.m_activity.getPackageName(), null));
            this.m_activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ShowStatusBar(boolean z) {
        View decorView = this.m_activity.getWindow().getDecorView();
        if (z) {
            GameActivity.showNavigationFullScreen(decorView);
        } else {
            GameActivity.hideNavigationFullScreen(decorView);
        }
    }

    public void TerminateApplication() {
        Log.d("sky LOG", "kill application");
        Process.killProcess(Process.myPid());
    }

    public boolean HasNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            return this.m_activity.checkSelfPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"});
        }
        return ((NotificationManager) this.m_activity.getSystemService(Context.NOTIFICATION_SERVICE)).areNotificationsEnabled();
    }

    public void RequestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            this.m_activity.requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, new GameActivity.PermissionCallback() { // from class: com.tgc.sky.SystemUI_android.28
                @Override // com.tgc.sky.GameActivity.PermissionCallback
                public void onPermissionResult(String[] strArr, int[] iArr) {
                }
            });
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", this.m_activity.getPackageName());
        this.m_activity.startActivity(intent);
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(this.CHANNEL_ID, "Channel name", NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription("Channel description");
        ((NotificationManager) this.m_activity.getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
    }

    public void PresentNotificationNow(String str, String str2, String str3) {
        createNotificationChannel();
        //NotificationManagerCompat.from(this.m_activity).notify(this.notificationId, new NotificationCompat.Builder(this.m_activity, this.CHANNEL_ID).setSmallIcon(R.drawable.ic_notification).setContentTitle(str).setContentText(str2).setPriority(0).build());
        this.notificationId++;
    }

    public int[] GetTime(String str) {
        TimeZone timeZone;
        if (str != null && !str.isEmpty()) {
            timeZone = TimeZone.getTimeZone(str);
        } else {
            timeZone = TimeZone.getDefault();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        return new int[]{calendar.get(11), calendar.get(12), calendar.get(13)};
    }

    public int[] GetDate(String str) {
        TimeZone timeZone = (str == null || str.length() <= 0) ? TimeZone.getDefault() : TimeZone.getTimeZone(str);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        return new int[]{calendar.get(1), calendar.get(2) + 1, calendar.get(5), calendar.get(7)};
    }

    public long GetEpochTime(String str, int i, int i2, int i3, int i4, int i5, int i6) {
        TimeZone timeZone = (str == null || str.isEmpty()) ? TimeZone.getDefault() : TimeZone.getTimeZone(str);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        calendar.set(i, i2 - 1, i3, i4, i5, i6);
        return calendar.getTimeInMillis() / 1000;
    }

    String GetDateTimeString(long j) {
        return DateFormat.getDateTimeInstance(0, 2, Locale.getDefault()).format(new Date(j * 1000));
    }

    String GetDateString(long j) {
        return DateFormat.getDateInstance(0, Locale.getDefault()).format(new Date(j * 1000));
    }
}
