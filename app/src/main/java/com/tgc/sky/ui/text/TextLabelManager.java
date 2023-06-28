package com.tgc.sky.ui.text;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.widget.TextViewCompat;
import com.tgc.sky.GameActivity;
import com.tgc.sky.ui.Utils;
import com.tgc.sky.ui.spans.CustomTypefaceSpan;
import com.tgc.sky.ui.spans.ShadowSpan;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/* renamed from: com.tgc.sky.ui.text.TextLabelManager */
public class TextLabelManager {
    private static int kMaxArgs = (128 * 8);
    private static int kMaxLabels = 128;
    /* access modifiers changed from: private */
    public GameActivity m_activity;
    /* access modifiers changed from: private */
    public ConcurrentLinkedQueue<TextLabelArgs> m_argsPool;
    /* access modifiers changed from: private */
    public List<TextLabel> m_labels;
    /* access modifiers changed from: private */
    public LocalizationManager m_localizationManager;
    private ReentrantLock m_lock = new ReentrantLock();
    private Markup m_markup;

    public native boolean FreeLabelId(int i);

    public TextLabelManager(GameActivity gameActivity, LocalizationManager localizationManager, Markup markup) {
        this.m_activity = gameActivity;
        this.m_localizationManager = localizationManager;
        this.m_markup = markup;
        this.m_labels = Collections.synchronizedList(new ArrayList(kMaxLabels));
        for (int i = 0; i < kMaxLabels; i++) {
            this.m_labels.add(new TextLabel());
        }
        this.m_argsPool = new ConcurrentLinkedQueue<>();
        for (int i2 = 0; i2 < kMaxArgs; i2++) {
            this.m_argsPool.add(new TextLabelArgs());
        }
    }

    public final TextLabel GetTextLabel(int i) {
        int GetAllocatedLabelIdx = GetAllocatedLabelIdx(i);
        if (GetAllocatedLabelIdx >= kMaxLabels) {
            return null;
        }
        return this.m_labels.get(GetAllocatedLabelIdx);
    }

    public final TextLabelArgs GetTextLabelArgs() {
        return this.m_argsPool.poll();
    }

    public void AddTextLabel(final int i) {
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                TextLabel textLabel = (TextLabel) TextLabelManager.this.m_labels.get(TextLabelManager.this.GetAllocatedLabelIdx(i));
                TextAttributes textAttributes = textLabel.attrs;
                TextPositioning textPositioning = textLabel.pos;
                LocalizedStringArgs GetLocalizedStringArgs = TextLabelManager.this.m_localizationManager.GetLocalizedStringArgs(textLabel.textId);
                textLabel.finalAttributedString = TextLabelManager.this.ProcessLabelText(GetLocalizedStringArgs, textAttributes);
                textLabel.lastTextIdChange = GetLocalizedStringArgs.lastChangeCounter;
                TextView textView = new TextView(TextLabelManager.this.m_activity);
                textView.setText(textLabel.finalAttributedString, TextView.BufferType.SPANNABLE);
                if (textAttributes.maxNumberOfLines > 0) {
                    textView.setMaxLines(textAttributes.maxNumberOfLines);
                }
                if (textAttributes.adjustFontSizeToFitWidth) {
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(textView, 1);
                }
                RectF transformRectToSystem = TextLabelManager.this.m_activity.transformRectToSystem(textPositioning.ProgramRect());
                TextLabelManager.AdjustTextRect(textPositioning, textView, transformRectToSystem);
                float transformWidthToSystem = TextLabelManager.this.m_activity.transformWidthToSystem(textPositioning.padWidth);
                float transformHeightToSystem = TextLabelManager.this.m_activity.transformHeightToSystem(textPositioning.padHeight);
                transformRectToSystem.right += transformWidthToSystem;
                transformRectToSystem.bottom += transformHeightToSystem;
                TextLabelManager.ApplyTextPositioningAnchorPoint(textPositioning, transformRectToSystem);
                TextLabelManager.this.m_activity.transformPointToSystem(textPositioning.f1049x, textPositioning.f1050y, transformRectToSystem);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) transformRectToSystem.width(), (int) transformRectToSystem.height());
                layoutParams.leftMargin = (int) transformRectToSystem.left;
                layoutParams.topMargin = (int) transformRectToSystem.top;
                int width = TextLabelManager.this.m_activity.getBrigeView().getWidth();
                if (transformRectToSystem.right > ((float) width)) {
                    layoutParams.rightMargin = width - ((int) transformRectToSystem.right);
                }
                textView.setLayoutParams(layoutParams);
                int i = (int) (transformWidthToSystem * 0.5f);
                int i2 = (int) (transformHeightToSystem * 0.5f);
                textView.setPadding(i, i2, i, i2);
                TextLabelManager.this.UpdateRemainingAttrsAndPos(textView, (TextAttributes) null, textAttributes, (TextPositioning) null, textPositioning);
                TextLabelManager.this.DoClipping(textView, transformRectToSystem, textPositioning);
                textLabel.view = textView;
                TextLabelManager.this.m_activity.getBrigeView().addView(textView);
                TextLabelManager textLabelManager = TextLabelManager.this;
                textLabelManager.UpdateCachedSize(textLabel, textLabelManager.m_activity.transformRectToProgram(transformRectToSystem));
            }
        });
    }

    public float[] GetTextLabelSize(int i) {
        this.m_lock.lock();
        try {
            int GetAllocatedLabelIdx = GetAllocatedLabelIdx(i);
            return (GetAllocatedLabelIdx >= kMaxLabels || this.m_labels.get(GetAllocatedLabelIdx).view == null) ? null : new float[]{this.m_labels.get(GetAllocatedLabelIdx).actualWidth, this.m_labels.get(GetAllocatedLabelIdx).actualHeight};
        } finally {
            this.m_lock.unlock();
        }
    }

    public float[] GetTextLabelUnconstrainedSize(int i) {
        this.m_lock.lock();
        try {
            int GetAllocatedLabelIdx = GetAllocatedLabelIdx(i);
            return (GetAllocatedLabelIdx >= kMaxLabels || this.m_labels.get(GetAllocatedLabelIdx).view == null) ? null : new float[]{this.m_labels.get(GetAllocatedLabelIdx).unconstrainedWidth, this.m_labels.get(GetAllocatedLabelIdx).unconstrainedHeight};
        } finally {
            this.m_lock.unlock();
        }
    }

    /* access modifiers changed from: private */
    public void UpdateCachedSize(TextLabel textLabel, RectF rectF) {
        try {
            int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            textLabel.view.measure(makeMeasureSpec, makeMeasureSpec);
            this.m_lock.lock();
            textLabel.actualWidth = rectF.width();
            textLabel.actualHeight = rectF.height();
            textLabel.unconstrainedWidth = this.m_activity.transformWidthToProgram((float) textLabel.view.getMeasuredWidth());
            textLabel.unconstrainedHeight = this.m_activity.transformHeightToProgram((float) textLabel.view.getMeasuredHeight());
        } finally {
            this.m_lock.unlock();
        }
    }

    public void UpdateTextLabel(final TextLabelArgs textLabelArgs) {
        this.m_activity.runOnUiThread((Runnable)new Runnable() {
            @Override
            public void run() {
                final TextLabel textLabel = TextLabelManager.this.m_labels.get(TextLabelManager.this.GetAllocatedLabelIdx(textLabelArgs.labelId));
                final TextView view = textLabel.view;
                if (view == null) {
                    TextLabelManager.this.m_argsPool.add(textLabelArgs);
                    return;
                }
                final TextAttributes attrs = textLabelArgs.attrs;
                final TextPositioning pos = textLabelArgs.pos;
                final LocalizedStringArgs getLocalizedStringArgs = TextLabelManager.this.m_localizationManager.GetLocalizedStringArgs(textLabel.textId);
                final SpannableStringBuilder finalAttributedString = textLabel.finalAttributedString;
                final float[] textColor = attrs.textColor;
                final int n = 0;
                final boolean b = textColor[0] != textLabel.attrs.textColor[0] || attrs.textColor[1] != textLabel.attrs.textColor[1] || attrs.textColor[2] != textLabel.attrs.textColor[2];
                boolean b2 = false;
                Label_0270: {
                    if (getLocalizedStringArgs.lastChangeCounter != textLabel.lastTextIdChange || (getLocalizedStringArgs.refresh != null && new Date().compareTo(getLocalizedStringArgs.refresh) > 0) || b) {
                        final SpannableStringBuilder access$300 = TextLabelManager.this.ProcessLabelText(getLocalizedStringArgs, attrs);
                        textLabel.lastTextIdChange = getLocalizedStringArgs.lastChangeCounter;
                        if (!finalAttributedString.equals((Object)access$300)) {
                            view.setText((CharSequence)(textLabel.finalAttributedString = access$300), TextView.BufferType.SPANNABLE);
                            b2 = true;
                            break Label_0270;
                        }
                    }
                    b2 = false;
                }
                int n2 = 0;
                Label_0418: {
                    if (!b2 && textLabel.attrs.adjustFontSizeToFitWidth == attrs.adjustFontSizeToFitWidth && textLabel.attrs.ignoreMarkupOptimization == attrs.ignoreMarkupOptimization && textLabel.attrs.maxNumberOfLines == attrs.maxNumberOfLines && textLabel.pos.shrinkBoxToText == pos.shrinkBoxToText && textLabel.pos.maxWidth == pos.maxWidth && textLabel.pos.maxHeight == pos.maxHeight && textLabel.pos.padWidth == pos.padWidth && textLabel.pos.padHeight == pos.padHeight) {
                        n2 = n;
                        if (textLabel.pos.autoAnchor == pos.autoAnchor) {
                            break Label_0418;
                        }
                    }
                    n2 = 1;
                }
                RectF transformRectToSystem;
                if (n2 != 0) {
                    if (attrs.maxNumberOfLines == 0) {
                        view.setMaxLines(Integer.MAX_VALUE);
                    }
                    else {
                        view.setMaxLines(attrs.maxNumberOfLines);
                    }
                    if (attrs.adjustFontSizeToFitWidth) {
                        TextViewCompat.setAutoSizeTextTypeWithDefaults(view, 1);
                    }
                    transformRectToSystem = TextLabelManager.this.m_activity.transformRectToSystem(pos.ProgramRect());
                    AdjustTextRect(pos, view, transformRectToSystem);
                    final float transformWidthToSystem = TextLabelManager.this.m_activity.transformWidthToSystem(pos.padWidth);
                    final float transformHeightToSystem = TextLabelManager.this.m_activity.transformHeightToSystem(pos.padHeight);
                    transformRectToSystem.right += transformWidthToSystem;
                    transformRectToSystem.bottom += transformHeightToSystem;
                    ApplyTextPositioningAnchorPoint(pos, transformRectToSystem);
                    TextLabelManager.this.m_activity.transformPointToSystem(pos.f1049x, pos.f1050y, transformRectToSystem);
                    final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)transformRectToSystem.width(), (int)transformRectToSystem.height());
                    layoutParams.leftMargin = (int)transformRectToSystem.left;
                    layoutParams.topMargin = (int)transformRectToSystem.top;
                    final int width = TextLabelManager.this.m_activity.getBrigeView().getWidth();
                    if (transformRectToSystem.right > width) {
                        layoutParams.rightMargin = width - (int)transformRectToSystem.right;
                    }
                    view.setLayoutParams((ViewGroup.LayoutParams)layoutParams);
                    final int n3 = (int)(transformWidthToSystem * 0.5f);
                    final int n4 = (int)(transformHeightToSystem * 0.5f);
                    view.setPadding(n3, n4, n3, n4);
                    TextLabelManager.this.UpdateCachedSize(textLabel, TextLabelManager.this.m_activity.transformRectToProgram(transformRectToSystem));
                }
                else {
                    final RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams)view.getLayoutParams();
                    transformRectToSystem = new RectF(0.0f, 0.0f, (float)layoutParams2.width, (float)layoutParams2.height);
                    ApplyTextPositioningAnchorPoint(pos, transformRectToSystem);
                    TextLabelManager.this.m_activity.transformPointToSystem(pos.f1049x, pos.f1050y, transformRectToSystem);
                    layoutParams2.leftMargin = (int)transformRectToSystem.left;
                    layoutParams2.topMargin = (int)transformRectToSystem.top;
                    final int width2 = TextLabelManager.this.m_activity.getBrigeView().getWidth();
                    if (transformRectToSystem.right > width2) {
                        layoutParams2.rightMargin = width2 - (int)transformRectToSystem.right;
                    }
                    view.setLayoutParams((ViewGroup.LayoutParams)layoutParams2);
                    final TextLabelManager this$0 = TextLabelManager.this;
                    this$0.UpdateCachedSize(textLabel, this$0.m_activity.transformRectToProgram(transformRectToSystem));
                }
                TextLabelManager.this.UpdateRemainingAttrsAndPos(view, textLabel.attrs, attrs, textLabel.pos, pos);
                TextLabelManager.this.DoClipping(view, transformRectToSystem, textLabel.pos);
                textLabel.attrs.UpdateWith(attrs);
                textLabel.pos.UpdateWith(pos);
                TextLabelManager.this.m_argsPool.add(textLabelArgs);
            }
        });
    }

    public void RemoveTextLabel(final int i) {
        this.m_lock.lock();
        try {
            final int GetAllocatedLabelIdx = GetAllocatedLabelIdx(i);
            if (GetAllocatedLabelIdx < kMaxLabels) {
                this.m_activity.runOnUiThread(new Runnable() {
                    public void run() {
                        TextView textView = ((TextLabel) TextLabelManager.this.m_labels.get(GetAllocatedLabelIdx)).view;
                        if (textView != null) {
                            ((RelativeLayout) textView.getParent()).removeView(textView);
                        }
                        if (((TextLabel) TextLabelManager.this.m_labels.get(GetAllocatedLabelIdx)).autoFreeTextId) {
                            TextLabelManager.this.m_localizationManager.FreeLocalizedText(((TextLabel) TextLabelManager.this.m_labels.get(GetAllocatedLabelIdx)).textId);
                        }
                        ((TextLabel) TextLabelManager.this.m_labels.get(GetAllocatedLabelIdx)).Clear();
                        TextLabelManager.this.FreeLabelId(i);
                    }
                });
            }
        } finally {
            this.m_lock.unlock();
        }
    }

    /* access modifiers changed from: private */
    public int GetAllocatedLabelIdx(int i) {
        return i != -1 ? i & 255 : kMaxLabels;
    }

    private static int GetColor(float f, float f2, float f3, float f4) {
        return Color.argb((int) (f4 * 255.0f), (int) (f * 255.0f), (int) (f2 * 255.0f), (int) (f3 * 255.0f));
    }

    public SpannableStringBuilder ApplyTextArgs(SpannableStringBuilder spannableStringBuilder, ArrayList<Object> arrayList) {
        if (arrayList != null && arrayList.size() > 0) {
            String str = (String) arrayList.get(0);
            while (true) {
                int indexOf = spannableStringBuilder.toString().indexOf("{{1}}");
                if (indexOf == -1) {
                    break;
                }
                spannableStringBuilder.replace(indexOf, indexOf + 5, str);
            }
            if (arrayList.size() > 1) {
                String str2 = (String) arrayList.get(1);
                while (true) {
                    int indexOf2 = spannableStringBuilder.toString().indexOf("{{2}}");
                    if (indexOf2 == -1) {
                        break;
                    }
                    spannableStringBuilder.replace(indexOf2, indexOf2 + 5, str2);
                }
                if (arrayList.size() > 2) {
                    String str3 = (String) arrayList.get(2);
                    while (true) {
                        int indexOf3 = spannableStringBuilder.toString().indexOf("{{3}}");
                        if (indexOf3 == -1) {
                            break;
                        }
                        spannableStringBuilder.replace(indexOf3, indexOf3 + 5, str3);
                    }
                }
            }
        }
        return spannableStringBuilder;
    }

    /* access modifiers changed from: private */
    public SpannableStringBuilder ProcessLabelText(LocalizedStringArgs localizedStringArgs, TextAttributes textAttributes) {
        if (localizedStringArgs.compounded == null) {
            SpannableStringBuilder ProcessLabelText = ProcessLabelText(localizedStringArgs.localizedString, textAttributes);
            if (localizedStringArgs.localizedString != null && localizedStringArgs.localizedString.contains("<time/>")) {
                localizedStringArgs.refresh = new Date(System.currentTimeMillis() + 1000);
            }
            return ApplyTextArgs(ProcessLabelText, localizedStringArgs.args);
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        for (int valueOf : localizedStringArgs.compounded) {
            LocalizedStringArgs GetLocalizedStringArgs = this.m_localizationManager.GetLocalizedStringArgs(Integer.valueOf(valueOf).intValue());
            SpannableStringBuilder ProcessLabelText2 = ProcessLabelText(GetLocalizedStringArgs, textAttributes);
            if (GetLocalizedStringArgs.refresh != null) {
                localizedStringArgs.refresh = (localizedStringArgs.refresh == null || !localizedStringArgs.refresh.before(GetLocalizedStringArgs.refresh)) ? GetLocalizedStringArgs.refresh : localizedStringArgs.refresh;
            }
            spannableStringBuilder.append(ProcessLabelText2);
        }
        return spannableStringBuilder;
    }

    private SpannableStringBuilder ProcessLabelText(String str, TextAttributes textAttributes) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new ForegroundColorSpan(GetColor(Math.min(textAttributes.textColor[0], 1.0f), Math.min(textAttributes.textColor[1], 1.0f), Math.min(textAttributes.textColor[2], 1.0f), 1.0f)));
        arrayList.add(new CustomTypefaceSpan(this.m_markup.DefaultFont()));
        arrayList.add(new AbsoluteSizeSpan(Utils.dp2px((textAttributes.fontSize * 22.0f) / 12.0f), false));
        if (textAttributes.hasShadow) {
            arrayList.add(new ShadowSpan(GetColor(textAttributes.shadowColor[0], textAttributes.shadowColor[1], textAttributes.shadowColor[2], textAttributes.shadowColor[3]), new PointF(textAttributes.shadowOffset[0], textAttributes.shadowOffset[1])));
        }
        return this.m_markup.GetMarkedUpString(str, arrayList, textAttributes.ignoreMarkupOptimization);
    }

    /* access modifiers changed from: private */
    public static void AdjustTextRect(TextPositioning textPositioning, TextView textView, RectF rectF) {
        if (textPositioning.maxWidth == 0.0f || textPositioning.maxHeight == 0.0f || textPositioning.shrinkBoxToText) {
            textView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            textView.setPadding(0, 0, 0, 0);
            textView.measure(View.MeasureSpec.makeMeasureSpec((int) rectF.width(), View.MeasureSpec.AT_MOST), 0);
            if (textPositioning.maxWidth == 0.0f || textPositioning.shrinkBoxToText) {
                rectF.right = rectF.left + ((float) textView.getMeasuredWidth()) + 1.0f;
            }
            if (textPositioning.maxHeight == 0.0f || textPositioning.shrinkBoxToText) {
                rectF.bottom = rectF.top + ((float) textView.getMeasuredHeight());
            }
        }
    }

    /* access modifiers changed from: private */
    public static void ApplyTextPositioningAnchorPoint(TextPositioning textPositioning, RectF rectF) {
        float width = rectF.width();
        float height = rectF.height();
        if (!textPositioning.autoAnchor) {
            rectF.left = -(width / 2.0f);
            rectF.top = -(height / 2.0f);
            rectF.right = rectF.left + width;
            rectF.bottom = rectF.top + height;
            return;
        }
        int i = textPositioning.f1047h.getValue();
        if (i == 1) {
            rectF.left = 0.0f;
        } else if (i == 2) {
            rectF.left = -(width / 2.0f);
        } else if (i == 3) {
            rectF.left = -width;
        }
        rectF.right = rectF.left + width;
        int i2 = textPositioning.f1048v.getValue();
        if (i2 == 1) {
            rectF.top = 0.0f;
        } else if (i2 == 2) {
            rectF.top = -(height / 2.0f);
        } else if (i2 == 3) {
            rectF.top = -height;
        }
        rectF.bottom = rectF.top + height;
    }

    /* access modifiers changed from: private */
    public void UpdateRemainingAttrsAndPos(TextView textView, TextAttributes textAttributes, TextAttributes textAttributes2, TextPositioning textPositioning, TextPositioning textPositioning2) {
        if (textAttributes == null || textAttributes.textAlignment != textAttributes2.textAlignment) {
            int i = textAttributes2.textAlignment.getValue();
            if (i == 1) {
                textView.setGravity(3);
            } else if (i == 2) {
                textView.setGravity(17);
            } else if (i == 3) {
                textView.setGravity(5);
            }
        }
        if (textAttributes == null || textAttributes.scale != textAttributes2.scale) {
            textView.setScaleX(textAttributes2.scale);
            textView.setScaleY(textAttributes2.scale);
        }
        if (textAttributes2.hasBackground) {
            GradientDrawable gradientDrawable = (GradientDrawable) textView.getBackground();
            if (gradientDrawable == null) {
                gradientDrawable = new GradientDrawable();
                textView.setBackground(gradientDrawable);
            }
            if (!(textAttributes != null && textAttributes.bgColor[0] == textAttributes2.bgColor[0] && textAttributes.bgColor[1] == textAttributes2.bgColor[1] && textAttributes.bgColor[2] == textAttributes2.bgColor[2] && textAttributes.bgColor[3] == textAttributes2.bgColor[3])) {
                gradientDrawable.setColor(GetColor(textAttributes2.bgColor[0], textAttributes2.bgColor[1], textAttributes2.bgColor[2], textAttributes2.bgColor[3]));
            }
            if (textAttributes == null || textAttributes.bgCornerRadius != textAttributes2.bgCornerRadius) {
                gradientDrawable.setCornerRadius((float) Utils.dp2px(textAttributes2.bgCornerRadius));
            }
        }
        if (textAttributes == null || textAttributes.textColor[3] != textAttributes2.textColor[3]) {
            textView.setAlpha(textAttributes2.textColor[3]);
        }
        if (textPositioning == null || textPositioning.f1051z != textPositioning2.f1051z) {
            textView.setZ(textPositioning2.f1051z);
        }
    }

    /* access modifiers changed from: private */
    public void DoClipping(TextView textView, RectF rectF, TextPositioning textPositioning) {
        if (textPositioning.clip) {
            RectF rectF2 = new RectF(0.0f, 0.0f, textPositioning.clipMaxX - textPositioning.clipMinX, textPositioning.clipMaxY - textPositioning.clipMinY);
            this.m_activity.transformPointToSystem(textPositioning.clipMinX, textPositioning.clipMaxY, rectF2);
            float scaleX = (rectF.right - rectF.left) * 0.5f * textView.getScaleX();
            float scaleY = (rectF.bottom - rectF.top) * 0.5f * textView.getScaleY();
            float centerX = rectF.centerX();
            float centerY = rectF.centerY();
            RectF rectF3 = new RectF(centerX - scaleX, centerY - scaleY, centerX + scaleX, centerY + scaleY);
            float max = Math.max(rectF3.left, rectF2.left);
            float min = Math.min(rectF3.right, rectF2.right);
            float max2 = Math.max(rectF3.top, rectF2.top);
            float min2 = Math.min(rectF3.bottom, rectF2.bottom);
            float max3 = Math.max(max, min);
            float max4 = Math.max(max2, min2);
            Rect clipBounds = textView.getClipBounds();
            Rect rect = new Rect();
            new RectF(max - rectF.left, max2 - rectF.top, max3 - rectF.left, max4 - rectF.top).round(rect);
            if (clipBounds != rect) {
                textView.setClipBounds(rect);
            }
        }
    }
}
