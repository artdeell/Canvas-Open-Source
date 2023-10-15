package com.tgc.sky.ui.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

/* renamed from: com.tgc.sky.ui.spans.StrokeSpan */
public class StrokeSpan extends ReplacementSpan {
    public int color;
    private Paint m_backGroupPaint;
    private int[] m_offsetWidthList;
    private int m_split;
    public int width;

    public StrokeSpan(int i, int i2, int i3) {
        i = i <= 0 ? 1 : i;
        this.m_split = i;
        this.width = i2;
        this.color = i3;
        this.m_offsetWidthList = new int[i];
        Paint paint = new Paint();
        this.m_backGroupPaint = paint;
        paint.setColor(i3);
        this.m_backGroupPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.m_backGroupPaint.setStrokeWidth((float) i2);
    }

    public StrokeSpan(int i, int i2) {
        this(1, i, i2);
    }

    public int getSize(Paint paint, CharSequence charSequence, int i, int i2, Paint.FontMetricsInt fontMetricsInt) {
        int i3 = i2 - i;
        if (this.m_split > i3) {
            this.m_split = i3;
            this.m_offsetWidthList = new int[i3];
        }
        int i4 = i3 / this.m_split;
        int i5 = 0;
        int i6 = 0;
        while (i5 < this.m_split) {
            this.m_offsetWidthList[i5] = i6;
            i5++;
            i6 += (int) paint.measureText(charSequence, (i5 * i4) + i, (i5 * i4) + i);
        }
        return (int) paint.measureText(charSequence, i, i2);
    }

    public void draw(Canvas canvas, CharSequence charSequence, int i, int i2, float f, int i3, int i4, int i5, Paint paint) {
        int i6 = i4;
        this.m_backGroupPaint.setTextSize(paint.getTextSize());
        int i7 = (i2 - i) / this.m_split;
        int i8 = 0;
        while (true) {
            int i9 = this.m_split;
            if (i8 < i9) {
                canvas.drawText(charSequence, i + (i8 * i7), i8 == i9 + -1 ? i2 : ((i8 + 1) * i7) + i, f + ((float) this.m_offsetWidthList[i8]), (float) i6, this.m_backGroupPaint);
                i8++;
            } else {
                canvas.drawText(charSequence, i, i2, f, (float) i6, paint);
                return;
            }
        }
    }
}
