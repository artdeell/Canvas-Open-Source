package com.tgc.sky.ui.spans;

import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;

/* renamed from: com.tgc.sky.ui.spans.EmbeddedImageSpan */
public class EmbeddedImageSpan extends ImageSpan {
    private boolean largeImage;
    private boolean overrideColors;

    public EmbeddedImageSpan(Drawable drawable, boolean z, boolean z2) {
        super(drawable);
        this.overrideColors = true;
        this.largeImage = z2;
    }

    public int getSize(Paint paint, CharSequence charSequence, int i, int i2, Paint.FontMetricsInt fontMetricsInt) {
        int textSize = (int) (paint.getTextSize() * (this.largeImage ? 2.5f : 1.3f));
        int i3 = (int) (((float) textSize) * 0.0f);
        getDrawable().setBounds(0, i3, textSize + 0, textSize + i3);
        if (this.overrideColors && (charSequence instanceof SpannableString)) {
            ForegroundColorSpan[] foregroundColorSpanArr = (ForegroundColorSpan[]) ((SpannableString) charSequence).getSpans(i, i2, ForegroundColorSpan.class);
            if (foregroundColorSpanArr.length > 0) {
                getDrawable().setColorFilter(foregroundColorSpanArr[foregroundColorSpanArr.length - 1].getForegroundColor(), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return super.getSize(paint, charSequence, i, i2, fontMetricsInt);
    }
}
