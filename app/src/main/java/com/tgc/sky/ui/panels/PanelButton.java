package com.tgc.sky.ui.panels;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.TransformationMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.StateSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.tgc.sky.GameActivity;
import com.tgc.sky.SystemUI_android;
import com.tgc.sky.ui.Utils;
import com.tgc.sky.ui.spans.CustomTypefaceSpan;

/* renamed from: com.tgc.sky.ui.panels.PanelButton */
public class PanelButton extends Button {
    private GameActivity mActivity;
    private Spanned mDisabledText;
    private Spanned mEnabledText;

    public PanelButton(Context context, View.OnClickListener onClickListener) {
        super(context);
        this.mActivity = (GameActivity) context;
        int dp2px = Utils.dp2px(8.0f);
        int dp2px2 = Utils.dp2px(16.0f);
        setMinHeight(0);
        setMinimumHeight(0);
        setMinWidth(0);
        setMinimumWidth(0);
        setPadding(dp2px2, dp2px, dp2px2, dp2px);
        setOnClickListener(onClickListener);
        setTransformationMethod((TransformationMethod) null);
        GradientDrawable gradientDrawable = new GradientDrawable();
        setBackground(gradientDrawable);
        gradientDrawable.setCornerRadius((float) dp2px);
        gradientDrawable.setColor(-3355444);
        gradientDrawable.setAlpha(229);
    }

    public int getDefaultColor() {
        return Color.argb(255, 10, 112, 255);
    }

    public void setText(String str) {
        setEnabledText(str);
        setDisabledText(str);
    }

    public void setEnabledText(String str) {
        setEnabledText(str, getDefaultColor());
    }

    public void setEnabledText(String str, int i) {
        this.mEnabledText = getSpanned(str, i);
        refreshDrawableState();
    }

    public void setDisabledText(String str) {
        setDisabledText(str, -7829368);
    }

    public void setDisabledText(String str, int i) {
        this.mDisabledText = getSpanned(str, i);
        refreshDrawableState();
    }

    private Spanned getSpanned(String str, int i) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
        spannableStringBuilder.setSpan(new CustomTypefaceSpan(SystemUI_android.getInstance().DefaultFont()), 0, str.length(), 17);
        spannableStringBuilder.setSpan(new AbsoluteSizeSpan(Utils.sp2px(16.0f), false), 0, str.length(), 17);
        spannableStringBuilder.setSpan(new StyleSpan(1), 0, str.length(), 17);
        spannableStringBuilder.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, str.length(), 17);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(i), 0, str.length(), 17);
        return spannableStringBuilder;
    }

    public void refreshDrawableState() {
        super.refreshDrawableState();
        if (this.mEnabledText != null) {
            if (StateSet.stateSetMatches(new int[]{16842910}, getDrawableState())) {
                setText(this.mEnabledText, TextView.BufferType.SPANNABLE);
                return;
            }
        }
        if (this.mDisabledText != null) {
            if (StateSet.stateSetMatches(new int[]{-16842910}, getDrawableState())) {
                setText(this.mDisabledText, TextView.BufferType.SPANNABLE);
            }
        }
    }
}
