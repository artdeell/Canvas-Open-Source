package com.tgc.sky;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Parcelable;
import android.os.Process;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import git.artdeell.skymodloader.SMLApplication;
import git.artdeell.skymodloader.auth.Facebook;
import com.tgc.sky.accounts.SystemAccountType;
import com.tgc.sky.ui.TextField;
import com.tgc.sky.ui.TextFieldLimiter;
import com.tgc.sky.ui.Utils;
import com.tgc.sky.ui.dialogs.DialogResult;
import com.tgc.sky.ui.panels.InvitationPanel;
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
import java.util.TimeZone;

import git.artdeell.skymodloader.R;

public class SystemUI_android {
    public static final int kInvalidDialogId = -1;
    public static final int kInvalidLabelId = -1;
    public static final int kInvalidTextId = -1;
    private static volatile SystemUI_android sInstance;
    private String CHANNEL_ID = "sky";
    /* access modifiers changed from: private */
    public GameActivity m_activity;
    private int m_currentId;
    private boolean m_enableHaptics = true;
    /* access modifiers changed from: private */
    public InvitationPanel m_invitationPanel;
    /* access modifiers changed from: private */
    public float m_keyboardHeight;
    /* access modifiers changed from: private */
    public boolean m_keyboardIsShowing;
    private LocalizationManager m_localizationManager;
    /* access modifiers changed from: private */
    public Markup m_markup;
    private DialogResult m_result;
    /* access modifiers changed from: private */
    public TextField m_textField;
    /* access modifiers changed from: private */
    public boolean m_textFieldIsShowing;
    /* access modifiers changed from: private */
    public TextFieldLimiter m_textFieldLimiter;
    private TextLabelManager m_textLabelManager;
    private boolean m_usingGamepad = false;
    private int notificationId = 0;
    /* access modifiers changed from: private */
    public String pasteStr = "";

    public void PressDialogButton(int i) {
    }

    SystemUI_android(GameActivity gameActivity) {
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
        this.m_textFieldIsShowing = false;
        this.m_result = new DialogResult();
        this.m_currentId = 0;
        this.m_activity.addOnKeyboardListener(new GameActivity.OnKeyboardListener() {
            public void onKeyboardChange(boolean z, int i) {
                boolean unused = SystemUI_android.this.m_keyboardIsShowing = z;
                float unused2 = SystemUI_android.this.m_keyboardHeight = (float) i;
            }
        });
        final ClipboardManager clipboardManager = (ClipboardManager) gameActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                CharSequence text;
                ClipData primaryClip = clipboardManager.getPrimaryClip();
                if (primaryClip != null && primaryClip.getItemCount() > 0 && (text = primaryClip.getItemAt(0).getText()) != null) {
                    String unused = SystemUI_android.this.pasteStr = text.toString();
                }
            }
        });
        sInstance = this;
    }

    public static SystemUI_android getInstance() {
        return sInstance;
    }

    public String LocalizeString(String str) {
        return this.m_localizationManager.LocalizeString(str);
    }

    public SpannableStringBuilder GetMarkedUpString(String str, ArrayList<Object> arrayList, boolean z) {
        return this.m_markup.GetMarkedUpString(str, arrayList, z);
    }

    public Typeface DefaultFont() {
        return this.m_markup.DefaultFont();
    }

    public ArrayList<Object> DefaultMarkupWithBoldFontSize(float f) {
        return new ArrayList<>(Arrays.asList(new Object[]{this.m_markup.DefaultFont(f), new StyleSpan(1)}));
    }

    public ArrayList<Object> DefaultMarkupWithFontSize(float f) {
        return new ArrayList<>(Arrays.asList(this.m_markup.DefaultFont(f)));
    }

    public SpannableStringBuilder LocalizeAndMarkUpString(String str, ArrayList<Object> arrayList, ArrayList<Object> arrayList2, boolean z) {
        return this.m_textLabelManager.ApplyTextArgs(this.m_markup.GetMarkedUpString(LocalizeString(str), arrayList2, z), arrayList);
    }

    /* access modifiers changed from: package-private */
    public synchronized int TryActivate() {
        if (this.m_result.response != DialogResult.Response.kInvalid) {
            return -1;
        }
        this.m_result.response = DialogResult.Response.kWaiting;
        int i = this.m_currentId + 1;
        this.m_currentId = i;
        return i;
    }

    /* access modifiers changed from: package-private */
    public synchronized void SetResult(String stringBuffer, int option, boolean isClosed) {
        this.m_result.stringBuffer = stringBuffer;
        this.m_result.option = option;
        this.m_result.response = isClosed ? DialogResult.Response.kClosed : DialogResult.Response.kResponded;
    }

    /* access modifiers changed from: package-private */
    public boolean GetMainWindowAttachedSheet() {
        return !this.m_activity.getBrigeView().hasWindowFocus();
    }

    /* access modifiers changed from: package-private */
    public void ShowTextField(final String str, final int i, final int i2) {
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                SystemUI_android.this.m_textField.showTextFieldWithPrompt(SystemUI_android.this.LocalizeString(str), i, i2);
                boolean unused = SystemUI_android.this.m_textFieldIsShowing = true;
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void HideTextField() {
        if(m_textFieldIsShowing)
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                SystemUI_android.this.m_textField.hideTextField();
            }
        });
        this.m_textFieldIsShowing = false;
    }

    /* access modifiers changed from: package-private */
    public boolean IsTextFieldShowing() {
        return this.m_textFieldIsShowing;
    }

    /* access modifiers changed from: package-private */
    public float GetTextFieldHeight() {
        return this.m_activity.transformHeightToProgram(this.m_textField.getTextFieldHeight());
    }

    /* access modifiers changed from: package-private */
    public boolean IsKeyboardShowing() {
        return this.m_keyboardIsShowing;
    }

    /* access modifiers changed from: package-private */
    public float GetKeyboardHeight() {
        return this.m_activity.transformHeightToProgram(this.m_keyboardHeight);
    }

    public int ShowTextFieldDialog(String str, String str2, String str3, String str4, int i, int i2, String str5, String str6) throws UnsupportedEncodingException {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        final String LocalizeString = LocalizeString(str);
        String str7 = str2;
        final String LocalizeString2 = LocalizeString(str2);
        String str8 = str3;
        final String LocalizeString3 = LocalizeString(str3);
        final String LocalizeString4 = LocalizeString(str4);
        final String str9 = (str6 == null || str6.length() <= 0) ? null : str6;
        this.m_textFieldLimiter.maxCharacters = i;
        this.m_textFieldLimiter.maxByteSize = i2;
        final String str10 = str5;
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList(Arrays.asList(new Object[]{SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1)})), false);
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
                editText.setText(str10, TextView.BufferType.SPANNABLE);
                String str = str9;
                if (str == null) {
                    str = LocalizeString2;
                }
                editText.setHint(str);
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
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialogInterface) {
                        GameActivity.hideNavigationFullScreen(SystemUI_android.this.m_activity.getBrigeView());
                    }
                });
                builder.setPositiveButton(LocalizeString3, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String obj = editText.getText().toString();
                        if (obj.length() == 0) {
                            SystemUI_android.this.SetResult(str9 != null ? str9 : str10, 1, true);
                        } else {
                            SystemUI_android.this.SetResult(obj, 1, true);
                        }
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton(LocalizeString4, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult((String) null, 0, true);
                        dialogInterface.dismiss();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialogInterface) {
                        SystemUI_android.this.SetResult((String) null, 0, true);
                    }
                });
                AlertDialog create = builder.create();
                GameActivity.hideNavigationFullScreen(create.getWindow().getDecorView());
                create.getWindow().setFlags(8, 8);
                create.setCanceledOnTouchOutside(false);
                create.show();
                create.getWindow().clearFlags(8);
            }
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
            public void run() {
                SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList(Arrays.asList(new Object[]{SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1)})), false);
                SpannableStringBuilder GetMarkedUpString2 = SystemUI_android.this.GetMarkedUpString(LocalizeString2, new ArrayList(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false);
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
                builder.setPositiveButton(LocalizeString3, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult((String) null, 1, true);
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton(LocalizeString4, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult((String) null, 0, true);
                        dialogInterface.dismiss();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialogInterface) {
                        SystemUI_android.this.SetResult((String) null, 0, true);
                    }
                });
                AlertDialog create = builder.create();
                GameActivity.hideNavigationFullScreen(create.getWindow().getDecorView());
                create.getWindow().setFlags(8, 8);
                create.setCanceledOnTouchOutside(false);
                create.show();
                create.getWindow().clearFlags(8);
                create.getButton(-1).setTextColor(-65536);
                create.getButton(-2).setTextColor(-16776961);
            }
        });
        return TryActivate;
    }

    public int ShowCountdownDialog(String str, String str2, String str3, String str4, int i, int i2, String str5) {
        int TryActivate;
        String str6 = str5;
        if (i == 0 && i2 == 0) {
            return ShowConfirmationDialog(str, str2, str3, str4, false);
        }
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        final String LocalizeString = LocalizeString(str);
        final String LocalizeString2 = LocalizeString(str2);
        final String LocalizeString3 = str6 != null ? LocalizeString(str6) : null;
        final String LocalizeString4 = LocalizeString(str3);
        final String LocalizeString5 = LocalizeString(str4);
        final int max = Integer.max(0, Integer.max(i, i2));
        final int i3 = i;
        final int i4 = i2;
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                int i;
                boolean z = true;
                SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList(Arrays.asList(new Object[]{SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1)})), false);
                final SpannableStringBuilder GetMarkedUpString2 = SystemUI_android.this.GetMarkedUpString(LocalizeString2, new ArrayList(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false);
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
                String str = LocalizeString3;
                if (str == null || (i = i3) <= 0) {
                    textView2.setText(GetMarkedUpString2, TextView.BufferType.SPANNABLE);
                } else {
                    textView2.setText(SystemUI_android.this.GetMarkedUpString(str.replace("{{1}}", Integer.toString(i)), new ArrayList(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false), TextView.BufferType.SPANNABLE);
                }
                builder.setPositiveButton(LocalizeString4, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult((String) null, 1, true);
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton(LocalizeString5, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult((String) null, 0, true);
                        dialogInterface.dismiss();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialogInterface) {
                        SystemUI_android.this.SetResult((String) null, 0, true);
                    }
                });
                if (i4 != 0) {
                    z = false;
                }
                builder.setCancelable(z);
                final AlertDialog create = builder.create();
                GameActivity.hideNavigationFullScreen(create.getWindow().getDecorView());
                create.getWindow().setFlags(8, 8);
                create.setCanceledOnTouchOutside(false);
                create.show();
                create.getWindow().clearFlags(8);
                if (i3 == 0) {
                    create.getButton(-1).setTextColor(-16776961);
                } else {
                    create.getButton(-1).setEnabled(false);
                }
                if (i4 == 0) {
                    create.getButton(-2).setTextColor(-65536);
                } else {
                    create.getButton(-2).setEnabled(false);
                }
                if (max > 0) {
                    final TextView textView3 = textView2;
                    new CountDownTimer((long) (max * 1000), 1000) {
                        public void onTick(long j) {
                            int i = (int) (((long) max) - (j / 1000));
                            if (i3 >= 0 && i3 <= i) {
                                create.getButton(-1).setEnabled(true);
                                create.getButton(-1).setTextColor(-16776961);
                            }
                            if (i4 >= 0 && i4 <= i) {
                                create.getButton(-2).setEnabled(true);
                                create.getButton(-2).setTextColor(-65536);
                            }
                            if (LocalizeString3 != null && i3 >= i) {
                                textView3.setText(SystemUI_android.this.GetMarkedUpString(LocalizeString3.replace("{{1}}", Integer.toString(i3 - i)), new ArrayList(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false), TextView.BufferType.SPANNABLE);
                            }
                        }

                        public void onFinish() {
                            if (i3 >= 0) {
                                create.getButton(-1).setEnabled(true);
                                create.getButton(-1).setTextColor(-16776961);
                            }
                            if (i4 >= 0) {
                                create.getButton(-2).setEnabled(true);
                                create.getButton(-2).setTextColor(-65536);
                                create.setCancelable(true);
                            }
                            textView3.setText(GetMarkedUpString2, TextView.BufferType.SPANNABLE);
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
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList(Arrays.asList(new Object[]{SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1)})), false);
                SpannableStringBuilder GetMarkedUpString2 = SystemUI_android.this.GetMarkedUpString(LocalizeString2, new ArrayList(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false);
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
                builder.setPositiveButton(LocalizeString3, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult((String) null, 1, true);
                        dialogInterface.dismiss();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialogInterface) {
                        SystemUI_android.this.SetResult((String) null, 1, true);
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

    public int ShowDateTimeDialog(String str, String str2, long j) {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        final String LocalizeString = LocalizeString(str);
        final String LocalizeString2 = LocalizeString(str2);
        final String LocalizeString3 = LocalizeString("system_button_ok");
        final String LocalizeString4 = LocalizeString("system_button_reset");
        final long j2 = j;
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList(Arrays.asList(new Object[]{SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1)})), false);
                SystemUI_android.this.GetMarkedUpString(LocalizeString2, new ArrayList(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false);
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
                Calendar instance = Calendar.getInstance();
                instance.setTimeInMillis(j2 * 1000);
                final DatePicker datePicker = new DatePicker(SystemUI_android.this.m_activity);
                datePicker.init(instance.get(1), instance.get(2), instance.get(5), (DatePicker.OnDateChangedListener) null);
                datePicker.setCalendarViewShown(false);
                final TimePicker timePicker = new TimePicker(SystemUI_android.this.m_activity);
                timePicker.setHour(instance.get(11));
                timePicker.setMinute(instance.get(12));
                linearLayout.addView(datePicker);
                linearLayout.addView(timePicker);
                builder.setView(linearLayout);
                builder.setPositiveButton(LocalizeString3, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult((String) null, (int) (SystemUI_android.this.GetEpochTime("", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute(), 0) - (Calendar.getInstance().getTimeInMillis() / 1000)), true);
                        dialogInterface.dismiss();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialogInterface) {
                        SystemUI_android.this.SetResult((String) null, 0, true);
                    }
                });
                builder.setNegativeButton(LocalizeString4, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SystemUI_android.this.SetResult((String) null, 0, true);
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

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0032, code lost:
        return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        return true;
    }

    public int ShowInvitationPanel(String outgoingNickname, String outgoingInvite, int p_outgoingState, String incomingInvite, int p_incomingState, String str4, int p_mode) {
        int activationResult;
        if (GetMainWindowAttachedSheet() || (activationResult = TryActivate()) == -1) {
            return -1;
        }
        this.m_activity.runOnUiThread(() -> {
            InvitationPanel.Mode mode = InvitationPanel.Mode.values()[p_mode];
            InvitationPanel.OutgoingState outgoingState = InvitationPanel.OutgoingState.values()[p_outgoingState];
            InvitationPanel.IncomingState incomingState = InvitationPanel.IncomingState.values()[p_incomingState];
            m_invitationPanel = new InvitationPanel(m_activity, this, m_markup, mode, outgoingNickname, outgoingInvite, outgoingState, incomingInvite, incomingState, str4 != null ? SystemUI_android.this.LocalizeString(str4) : null, new InvitationPanel.Handle() {
                public void run(String str1, int i, boolean z) {
                    SetResult(str1, i, z);
                    if (z) {
                        m_invitationPanel = null;
                    }
                }
            });
            m_invitationPanel.showAtLocation(m_activity.getWindow().getDecorView(), 19, 0, 0);
        });
        return activationResult;
    }

    public void SetOutgoingInvitationLinkState(int i, String str, String str2) {
        InvitationPanel invitationPanel = this.m_invitationPanel;
        if (invitationPanel != null) {
            invitationPanel.setOutgoingInvitationLinkState(i, str, str2);
        }
    }

    public void SetIncomingInvitationLinkState(int i, String str) {
        InvitationPanel invitationPanel = this.m_invitationPanel;
        if (invitationPanel != null) {
            invitationPanel.setIncomingInvitationLinkState(i, str);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean ShowFacebookFriendFinder() {
        //LaunchURL(String.format("https://fb.gg/me/friendfinder/%s", new Object[]{this.m_activity.getString(R.string.facebook_app_id)}));
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean HasFacebookFriendGraphPermission() {
        return ((Facebook) SystemAccounts_android.getInstance().GetSystemAccount(SystemAccountType.kSystemAccountType_Facebook)).HasAppFriendsPermission();
    }

    /* access modifiers changed from: package-private */
    public int RequestFacebookFriendGraphPermission() {
        int TryActivate;
        if (GetMainWindowAttachedSheet() || (TryActivate = TryActivate()) == -1) {
            return -1;
        }
        if (!((Facebook) SystemAccounts_android.getInstance().GetSystemAccount(SystemAccountType.kSystemAccountType_Facebook)).GetAppFriendsPermission(new Facebook.OnPermissionCallback() {
            public void onCallback(boolean z, String str) {
                if (str != null) {
                    SystemUI_android.this.SetResult(str, 2, true);
                } else {
                    SystemUI_android.this.SetResult((String) null, z ? 1 : 0, true);
                }
            }
        })) {
            SetResult("Another request is already pending", 2, true);
        }
        return TryActivate;
    }

    public int ShareURL(String str, String str2) throws UnsupportedEncodingException {
        Log.i("SML", "ShareURL: "+str + " "+str2);

        int TryActivate;
        if ((TryActivate = TryActivate()) == -1) {
            return -1;
        }
        String LocalizeString = LocalizeString(str);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.setType("text/html");
        intent.putExtra("android.intent.extra.SUBJECT", LocalizeString);
        intent.putExtra("android.intent.extra.TEXT", str2);
        this.m_activity.startActivityForResult(Intent.createChooser(intent, LocalizeString), 110);
        this.m_activity.AddOnActivityResultListener(new GameActivity.OnActivityResultListener() {
            public void onActivityResult(int i, int i2, Intent intent) {
                if (i == 110) {
                    SystemUI_android.this.SetResult((String) null, i2 == -1 ? 1 : 0, true);
                    SystemUI_android.this.m_activity.RemoveOnActivityResultListeners(this);
                }
            }
        });
        return TryActivate;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0008, code lost:
        r0 = TryActivate();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int ShareImage(String localizeString, final String s, final byte[] array, final boolean b) {
        final int tryActivate = this.TryActivate();
        if (tryActivate == -1) {
            return -1;
        }
        localizeString = this.LocalizeString(localizeString);
        try {
            final Bitmap decodeByteArray = BitmapFactory.decodeByteArray(array, 0, array.length);
            final StringBuilder sb = new StringBuilder();
            sb.append(this.m_activity.getExternalCacheDir());
            sb.append("/temp/ShareImageTemp.png");
            final File file = new File(sb.toString());
            file.delete();
            file.getParentFile().mkdirs();
            file.createNewFile();
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            decodeByteArray.compress(Bitmap.CompressFormat.PNG, 100, (OutputStream)fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            final Uri fromFile = Uri.fromFile(file);
            final Intent intent = new Intent();
            intent.setAction("android.intent.action.SEND");
            intent.setType("image/*");
            intent.putExtra("android.intent.extra.SUBJECT", localizeString);
            intent.putExtra("android.intent.extra.TEXT", s);
            intent.putExtra("android.intent.extra.STREAM", (Parcelable)fromFile);
            this.m_activity.startActivityForResult(Intent.createChooser(intent, (CharSequence)localizeString), 111);
            this.m_activity.AddOnActivityResultListener((GameActivity.OnActivityResultListener)new GameActivity.OnActivityResultListener() {
                @Override
                public void onActivityResult(int n, final int n2, final Intent intent) {
                    if (n == 111) {
                        if (n2 == -1) {
                            n = 1;
                        }
                        else {
                            n = 0;
                        }
                        SystemUI_android.this.SetResult(null, n, true);
                        SystemUI_android.this.m_activity.RemoveOnActivityResultListeners((GameActivity.OnActivityResultListener)this);
                        if (b) {
                            SystemUI_android.this.TryGetDialogResult(tryActivate);
                        }
                    }
                }
            });
            return tryActivate;
        }
        catch (IOException ex) {
            ex.printStackTrace();
            this.SetResult(null, 0, true);
            return -1;
        }
    }

    public int ShareVideo(String str) {
        int TryActivate;
        if ((TryActivate = TryActivate()) == -1) {
            return -1;
        }
        Uri fromFile = Uri.fromFile(new File((this.m_activity.getExternalCacheDir() + "/") + str));
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.setType("video/*");
        intent.putExtra("android.intent.extra.STREAM", fromFile);
        this.m_activity.startActivityForResult(Intent.createChooser(intent, ""), 112);
        this.m_activity.AddOnActivityResultListener(new GameActivity.OnActivityResultListener() {
            public void onActivityResult(int i, int i2, Intent intent) {
                if (i == 112) {
                    SystemUI_android.this.SetResult((String) null, i2 == -1 ? 1 : 0, true);
                    SystemUI_android.this.m_activity.RemoveOnActivityResultListeners(this);
                }
            }
        });
        return TryActivate;
    }

    public void SaveImage(String str, byte[] bArr) {
        String[] strArr = {"android.permission.WRITE_EXTERNAL_STORAGE"};
        if (this.m_activity.checkSelfPermissions(strArr) || Build.VERSION.SDK_INT >= 29) {
            SaveImageInternal(str, bArr);
        } else {
            SaveImageAskPermissions(strArr, true, str, bArr);
        }
    }

    /* access modifiers changed from: private */
    public void SaveImageAskPermissions(String[] strArr, boolean z, String str, byte[] bArr) {
        final String LocalizeString = LocalizeString("system_screenshot_permission_title");
        final String LocalizeString2 = LocalizeString(z ? "system_screenshot_permission_message_00" : "system_screenshot_permission_message_01");
        final String LocalizeString3 = LocalizeString(z ? "system_button_ask" : "system_button_settings");
        final String LocalizeString4 = LocalizeString("system_button_cancel");
        final boolean z2 = z;
        final String[] strArr2 = strArr;
        final String str2 = str;
        final byte[] bArr2 = bArr;
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                SpannableStringBuilder GetMarkedUpString = SystemUI_android.this.GetMarkedUpString(LocalizeString, new ArrayList(Arrays.asList(new Object[]{SystemUI_android.this.m_markup.DefaultFontGame(17.0f), new StyleSpan(1)})), false);
                SpannableStringBuilder GetMarkedUpString2 = SystemUI_android.this.GetMarkedUpString(LocalizeString2, new ArrayList(Arrays.asList(SystemUI_android.this.m_markup.DefaultFontGame(13.0f))), false);
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
                builder.setPositiveButton(LocalizeString3, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (z2) {
                            SystemUI_android.this.m_activity.requestPermissions(strArr2, new GameActivity.PermissionCallback() {
                                public void onPermissionResult(String[] strArr, int[] iArr) {
                                    if (SystemUI_android.this.m_activity.checkResultPermissions(iArr)) {
                                        SystemUI_android.this.SaveImageInternal(str2, bArr2);
                                    } else if (!SystemUI_android.this.m_activity.shouldShowRequestPermissionsRationale(strArr)) {
                                        SystemUI_android.this.SaveImageAskPermissions(strArr, false, str2, bArr2);
                                    }
                                }
                            });
                            dialogInterface.dismiss();
                            return;
                        }
                        SystemUI_android.this.m_activity.requestPermissionsThroughSettings(strArr2, new GameActivity.PermissionCallback() {
                            public void onPermissionResult(String[] strArr, int[] iArr) {
                                if (SystemUI_android.this.m_activity.checkResultPermissions(iArr)) {
                                    SystemUI_android.this.SaveImageInternal(str2, bArr2);
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton(LocalizeString4, new DialogInterface.OnClickListener() {
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
                create.getButton(-1).setTextColor(-65536);
                create.getButton(-2).setTextColor(-16776961);
            }
        });
    }

    /* access modifiers changed from: private */
    public void SaveImageInternal(String str, byte[] bArr) {
        OutputStream outputStream = null;
        try {
            Context baseContext = this.m_activity.getBaseContext();
            long currentTimeMillis = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();
            ContentResolver contentResolver = baseContext.getContentResolver();
            @SuppressLint("SimpleDateFormat") String str2 = "SKY_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
            contentValues.put("_display_name", str2 + ".jpg");
            contentValues.put("mime_type", "image/jpeg");
            contentValues.put("date_added", currentTimeMillis);
            contentValues.put("datetaken", currentTimeMillis);
            if (Build.VERSION.SDK_INT >= 29) {
                contentValues.put("relative_path", Environment.DIRECTORY_PICTURES + File.separator + "Sky");
                Uri insert = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (insert != null) {
                    outputStream = contentResolver.openOutputStream(insert);
                }
            } else {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Sky").toString());
                if (!file.exists()) {
                    file.mkdirs();
                }
                File createTempFile = File.createTempFile(str2, ".jpg", file);
                contentValues.put("_data", createTempFile.getAbsolutePath());
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                outputStream = new FileOutputStream(createTempFile);
            }
            if (outputStream != null) {
                outputStream.write(bArr);
                outputStream.flush();
                outputStream.close();
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

    public void AddTextLabel(int i, boolean z, boolean z2, boolean z3, boolean z4, float f, float f2, int i2, int i3, float[] fArr, float[] fArr2, float f3, float[] fArr3, float[] fArr4, float f4, float f5, float f6, int i4, int i5, boolean z5, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, boolean z6, boolean z7, int i6, boolean z8) {
        int i7 = i;
        TextLabel GetTextLabel = this.m_textLabelManager.GetTextLabel(i);
        if (GetTextLabel != null) {
            GetTextLabel.attrs.fontName = "";
            GetTextLabel.attrs.hasBackground = z;
            GetTextLabel.attrs.hasShadow = z2;
            GetTextLabel.attrs.adjustFontSizeToFitWidth = z3;
            GetTextLabel.attrs.ignoreMarkupOptimization = z4;
            GetTextLabel.attrs.fontSize = f;
            GetTextLabel.attrs.scale = f2;
            GetTextLabel.attrs.maxNumberOfLines = i2;
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
            GetTextLabel.pos.shrinkBoxToText = z5;
            GetTextLabel.pos.maxWidth = f7;
            GetTextLabel.pos.maxHeight = f8;
            GetTextLabel.pos.padWidth = f9;
            GetTextLabel.pos.padHeight = f10;
            GetTextLabel.pos.clipMinX = f11;
            GetTextLabel.pos.clipMinY = f12;
            GetTextLabel.pos.clipMaxX = f13;
            GetTextLabel.pos.clipMaxY = f14;
            GetTextLabel.pos.clip = z6;
            GetTextLabel.pos.autoAnchor = z7;
            GetTextLabel.textId = i6;
            GetTextLabel.autoFreeTextId = z8;
            this.m_textLabelManager.AddTextLabel(i);
        }
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

    public void RemoveTextLabel(int i) {
        this.m_textLabelManager.RemoveTextLabel(i);
    }

    /* access modifiers changed from: package-private */
    public boolean HapticFeedbackSuccess() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            });
        }
        return false;
    }

    public boolean HapticFeedbackSuccessStrong() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            });
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean HapticFeedbackWarning() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
            });
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean HapticFeedbackError() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                }
            });
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean HapticFeedbackSelection() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                }
            });
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean HapticFeedbackImpactLight() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
                }
            });
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean HapticFeedbackImpact() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
            });
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean HapticFeedbackImpactHeavy() {
        if (!this.m_usingGamepad && this.m_enableHaptics) {
            this.m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    SystemUI_android.this.m_activity.getBrigeView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            });
        }
        return false;
    }

    public String GetPasteBufferString() {
        return this.pasteStr;
    }

    public void SetPasteBufferString(final String str) {
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                ((ClipboardManager) SystemUI_android.this.m_activity.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText((CharSequence) null, str));
            }
        });
    }

    public void LaunchURL(String str) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse(str));
        this.m_activity.startActivity(intent);
    }

    public void ShowSettings() {
        try {
            Intent intent = new Intent();
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", this.m_activity.getPackageName(), (String) null));
            this.m_activity.startActivity(intent);
        } catch (Exception unused) {
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
        if (Build.VERSION.SDK_INT >= 26 && ((NotificationManager) this.m_activity.getSystemService(Context.NOTIFICATION_SERVICE)).getImportance() == NotificationManager.IMPORTANCE_NONE) {
            return false;
        }
        AppOpsManager appOpsManager = (AppOpsManager) this.m_activity.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo applicationInfo = this.m_activity.getApplicationInfo();
        String packageName = this.m_activity.getApplicationContext().getPackageName();
        int i = applicationInfo.uid;
        try {
            Class<?> cls = Class.forName(AppOpsManager.class.getName());
            if (((Integer) cls.getMethod("checkOpNoThrow", new Class[]{Integer.TYPE, Integer.TYPE, String.class}).invoke(appOpsManager, new Object[]{Integer.valueOf(((Integer) cls.getDeclaredField("OP_POST_NOTIFICATION").get(Integer.class)).intValue()), Integer.valueOf(i), packageName})).intValue() == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void RequestNotificationPermissions() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 26) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", this.m_activity.getPackageName());
        } else {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", this.m_activity.getPackageName());
            intent.putExtra("app_uid", this.m_activity.getApplicationInfo().uid);
        }
        this.m_activity.startActivity(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(this.CHANNEL_ID, "Channel name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Channel description");
            ((NotificationManager) this.m_activity.getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
        }
    }

    public void PresentNotificationNow(String str, String str2, String str3) {
        createNotificationChannel();
        NotificationManagerCompat.from(this.m_activity).notify(this.notificationId, new NotificationCompat.Builder(this.m_activity, this.CHANNEL_ID).setSmallIcon(R.drawable.icon_fg).setContentTitle(str).setContentText(str2).setPriority(0).build());
        this.notificationId++;
    }

    public int[] GetTime(String str) {
        TimeZone timeZone;
        if (str == null || str.length() <= 0) {
            timeZone = TimeZone.getDefault();
        } else {
            timeZone = TimeZone.getTimeZone(str);
        }
        Calendar instance = Calendar.getInstance();
        instance.setTimeZone(timeZone);
        return new int[]{instance.get(11), instance.get(12), instance.get(13)};
    }

    public int[] GetDate(String str) {
        TimeZone timeZone = (str == null || str.length() <= 0) ? TimeZone.getDefault() : TimeZone.getTimeZone(str);
        Calendar instance = Calendar.getInstance();
        instance.setTimeZone(timeZone);
        return new int[]{instance.get(1), instance.get(2) + 1, instance.get(5), instance.get(7)};
    }

    public long GetEpochTime(String str, int i, int i2, int i3, int i4, int i5, int i6) {
        TimeZone timeZone = (str == null || str.length() <= 0) ? TimeZone.getDefault() : TimeZone.getTimeZone(str);
        Calendar instance = Calendar.getInstance();
        instance.setTimeZone(timeZone);
        instance.set(i, i2 - 1, i3, i4, i5, i6);
        return instance.getTimeInMillis() / 1000;
    }

    /* access modifiers changed from: package-private */
    public String GetDateTimeString(long j) {
        return DateFormat.getDateTimeInstance(0, 2, Locale.getDefault()).format(new Date(j * 1000));
    }

    /* access modifiers changed from: package-private */
    public String GetDateString(long j) {
        return DateFormat.getDateInstance(0, Locale.getDefault()).format(new Date(j * 1000));
    }

    public boolean HasLocalizedString(String string) {
        return SMLApplication.skyRes.getIdentifier(string, "string", SMLApplication.skyPName) != 0;
    }
}
