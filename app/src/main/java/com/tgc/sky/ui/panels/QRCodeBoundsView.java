package com.tgc.sky.ui.panels;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import androidx.core.internal.view.SupportMenu;

/* renamed from: com.tgc.sky.ui.panels.QRCodeBoundsView */
/* compiled from: InvitationPanel */
class QRCodeBoundsView extends View {
    private int numRects = 0;
    private Paint paint = new Paint();
    private RectF[] rects = new RectF[16];
    private boolean[] valids = new boolean[16];

    public QRCodeBoundsView(Context context) {
        super(context);
        setBackgroundColor(0);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(6.0f);
        this.paint.setAlpha(255);
    }

    public void clear() {
        this.numRects = 0;
        postInvalidate();
    }

    public void addBounds(RectF rectF, boolean z) {
        int i = this.numRects;
        RectF[] rectFArr = this.rects;
        if (i != rectFArr.length) {
            rectFArr[i] = rectF;
            this.valids[i] = z;
            this.numRects = i + 1;
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        for (int i = 0; i < this.numRects; i++) {
            if (this.rects[i] != null) {
                this.paint.setColor(this.valids[i] ? -16711936 : -65536);
                canvas.drawRect(this.rects[i], this.paint);
            }
        }
    }
}
