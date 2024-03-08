package com.tgc.sky.ui;

import static git.artdeell.skymodloader.MainActivity.SKY_PACKAGE_NAME;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatEditText;

import androidx.core.view.ViewCompat;
import com.tgc.sky.GameActivity;
import com.tgc.sky.SystemUI_android;

import java.nio.charset.StandardCharsets;
import java.util.Locale;


public class TextField {
    public static final int kInvalidTextFieldId = -1;

    public GameActivity m_activity;

    public boolean m_isCallbackTextfield;

    public boolean m_onKeyboardCompleteAlreadyCalled;
    private SystemUI_android m_systemUI;
    private EditText m_textField;
    private TextFieldLimiter m_textFieldLimiter;

    public String m_userText;
    private int m_id = -1;
    private int m_counter = -1;
    private String m_textBuffer = "";
    private int m_cursorPos = -1;
    private int m_selectPos = -1;
    private boolean m_init = false;


    public void initWithParams(GameActivity gameActivity, SystemUI_android systemUI_android) {
        this.m_activity = gameActivity;
        this.m_systemUI = systemUI_android;
        AppCompatEditText r4 = new AppCompatEditText(this.m_activity) {
            public void onSelectionChanged(int i, int i2) {
                super.onSelectionChanged(i, i2);
                if (TextField.this.m_init && TextField.this.m_textField.getVisibility() != View.INVISIBLE) {
                    TextField.this.updateCursorPos(i, i2, getText().toString());
                }
            }
        };
        this.m_textField = r4;
        r4.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
            }

            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (TextField.this.m_init && TextField.this.m_textField.getVisibility() != View.INVISIBLE) {
                    TextField.this.updateBuffer(TextField.this.m_textField.getText().toString());
                }
            }
        });
        this.m_textField.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.m_textField.setTextSize(18.0f);
        this.m_textField.setFitsSystemWindows(true);
        this.m_textFieldLimiter = new TextFieldLimiter();
        resizeTextField(true, 0);
        GradientDrawable gradientDrawable = new GradientDrawable();
        this.m_textField.setBackground(gradientDrawable);
        gradientDrawable.setCornerRadius((float) Utils.dp2px(8.0f));
        gradientDrawable.setColor(Color.argb(0.5f, 1.0f, 1.0f, 1.0f));
        gradientDrawable.setStroke(1, Color.argb(0.8f, 1.0f, 1.0f, 1.0f));
        this.m_textField.setImeOptions(33554432);
        this.m_textField.setHintTextColor(-12303292);
        this.m_textField.setGravity(8388627);
        this.m_textField.setInputType(16385);
        this.m_textField.setVisibility(View.INVISIBLE);
        this.m_textField.setFocusable(true);
        this.m_textField.setFocusableInTouchMode(true);
        this.m_textField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != 4 && i != 0) {
                    return false;
                }
                TextField.this.m_activity.onKeyboardCompleteNative(textView.getText().toString(), TextField.this.m_isCallbackTextfield, TextField.this.m_isCallbackTextfield);
                textView.setText("");
                boolean unused = TextField.this.m_onKeyboardCompleteAlreadyCalled = true;
                return true;
            }
        });
        this.m_textField.setFilters(new InputFilter[]{this.m_textFieldLimiter});
        this.m_activity.getBrigeView().addView(this.m_textField);
        this.m_activity.addOnKeyboardListener(new GameActivity.OnKeyboardListener() {
            public void onKeyboardChange(boolean z, int i) {
                if (z) {
                    TextField.this.resizeTextField(false, i);
                } else if (!TextField.this.m_onKeyboardCompleteAlreadyCalled) {
                    TextField.this.m_activity.onKeyboardCompleteNative("", TextField.this.m_isCallbackTextfield, true);
                }
            }
        });
        this.m_init = true;
    }

    public void showTextFieldWithPrompt(String str, int i, int i2) {
        updateBuffer((String) null);
                this.m_userText = null;
                this.m_isCallbackTextfield = false;
                this.m_onKeyboardCompleteAlreadyCalled = false;
                this.m_textFieldLimiter.maxByteSize = i2;
                this.m_textFieldLimiter.maxCharacters = i;
                this.m_textField.setText("");
                this.m_textField.setHint(str);
                EditText editText = this.m_textField;
                editText.setImeOptions(editText.getImeOptions() | 4);
                this.m_textField.setAlpha(0.0f);
                this.m_textField.setVisibility(View.VISIBLE);
                this.m_textField.requestFocus();
                ((InputMethodManager) this.m_activity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this.m_textField, 0);

    }

    public void showTextFieldWithPrompt(String str, String str2, int i, int i2) {
        this.m_userText = str2;
        this.m_isCallbackTextfield = true;
        this.m_onKeyboardCompleteAlreadyCalled = false;
        this.m_textFieldLimiter.maxByteSize = i2;
        this.m_textFieldLimiter.maxCharacters = i;
        this.m_textField.setText(str2);
        EditText editText = this.m_textField;
        editText.setSelection(editText.length());
        this.m_textField.setHint(str);
        EditText editText2 = this.m_textField;
        editText2.setImeOptions(editText2.getImeOptions() | 4);
        this.m_textField.setAlpha(0.0f);
        this.m_textField.setVisibility(View.VISIBLE);
        this.m_textField.requestFocus();
        ((InputMethodManager) this.m_activity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this.m_textField, 0);
    }

    public void showTextFieldWithPrompt(String str, String str2, int i, int i2, boolean z) {
        updateBuffer(str2);
        this.m_isCallbackTextfield = z;
        this.m_onKeyboardCompleteAlreadyCalled = false;
        this.m_textFieldLimiter.maxByteSize = i2;
        this.m_textFieldLimiter.maxCharacters = i;
        this.m_textField.setText(str2);
        EditText editText = this.m_textField;
        editText.setSelection(editText.length());
        this.m_textField.setHint(str);
        EditText editText2 = this.m_textField;
        editText2.setImeOptions(editText2.getImeOptions() | 4);
        this.m_textField.setAlpha(0.0f);
        this.m_textField.setVisibility(View.VISIBLE);
        this.m_textField.requestFocus();
        ((InputMethodManager) this.m_activity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this.m_textField, 0);
    }


    public void hideTextField() {
        ((InputMethodManager) this.m_activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.m_textField.getWindowToken(), 0);
        GameActivity.hideNavigationFullScreen(this.m_activity.getBrigeView());
        this.m_textField.setVisibility(View.INVISIBLE);
    }


    public void resizeTextField(boolean z, int i) {
        int dp2px = Utils.dp2px(8.0f);
        Rect GetSafeAreaInsets = this.m_activity.GetSafeAreaInsets();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -2);
        if (z) {
            layoutParams.addRule(10, -1);
            layoutParams.topMargin = i + dp2px;
        } else {
            layoutParams.addRule(12, -1);
            layoutParams.bottomMargin = i + dp2px;
        }
        layoutParams.leftMargin = GetSafeAreaInsets.left + dp2px;
        layoutParams.rightMargin = GetSafeAreaInsets.right + dp2px;
        this.m_textField.setLayoutParams(layoutParams);
        this.m_textField.setPadding(dp2px, dp2px, dp2px, dp2px);
        this.m_textField.setFitsSystemWindows(true);
        this.m_textField.setAlpha(1.0f);
    }

    public float getTextFieldHeight() {
        return (float) this.m_textField.getHeight();
    }

    public EditText getEditText() {
        return this.m_textField;
    }

    //-----------
    private int updateCursorPosUTF8(int i, String str) {
        return str.substring(0, i).getBytes(StandardCharsets.UTF_8).length;
    }

    public void updateCursorPos(int i, int i2, String str) {
        this.m_cursorPos = updateCursorPosUTF8(i, str);
        this.m_selectPos = i != i2 ? updateCursorPosUTF8(i2, str) : -1;
    }

    public void updateBuffer(String str) {
        this.m_userText = str;
        this.m_textBuffer = str;
    }

    public String getTextBuffer() {
        return this.m_textBuffer;
    }

    public int getCursorPos() {
        return this.m_cursorPos;
    }

    public int getSelectPos() {
        return this.m_selectPos;
    }

    public int TryActivate() {
        if (this.m_id != -1) {
            return -1;
        }
        int i = this.m_counter + 1;
        this.m_counter = i;
        this.m_id = i;
        return i;
    }

    public boolean IsActiveId(int i) {
        return i != -1 && i == this.m_id;
    }

    public void ClearId() {
        this.m_id = -1;
    }
}

