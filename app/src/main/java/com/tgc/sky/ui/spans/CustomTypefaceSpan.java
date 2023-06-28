package com.tgc.sky.ui.spans;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/* renamed from: com.tgc.sky.ui.spans.CustomTypefaceSpan */
public class CustomTypefaceSpan extends MetricAffectingSpan {
    private final Typeface typeface;

    public CustomTypefaceSpan(Typeface typeface2) {
        this.typeface = typeface2;
    }

    public void updateDrawState(TextPaint textPaint) {
        applyCustomTypeFace(textPaint, this.typeface);
    }

    public void updateMeasureState(TextPaint textPaint) {
        applyCustomTypeFace(textPaint, this.typeface);
    }

    private static void applyCustomTypeFace(Paint paint, Typeface typeface2) {
        paint.setTypeface(typeface2);
    }
}
