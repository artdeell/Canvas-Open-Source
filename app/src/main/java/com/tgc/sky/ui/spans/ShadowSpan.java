package com.tgc.sky.ui.spans;

import android.graphics.PointF;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

/* renamed from: com.tgc.sky.ui.spans.ShadowSpan */
public class ShadowSpan extends CharacterStyle {
    public int color;
    public PointF offset;

    public ShadowSpan(int i, PointF pointF) {
        this.color = i;
        this.offset = pointF;
    }

    public void updateDrawState(TextPaint textPaint) {
        textPaint.setShadowLayer(5.0f, this.offset.x, this.offset.y, this.color);
    }
}
