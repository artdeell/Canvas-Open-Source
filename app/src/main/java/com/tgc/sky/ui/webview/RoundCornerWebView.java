package com.tgc.sky.ui.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.webkit.WebView;
import com.tgc.sky.ui.Utils;


public class RoundCornerWebView extends WebView {
    private Context context;
    private int height;
    private Path path;
    private int radius;
    private int width;

    public RoundCornerWebView(Context context) {
        super(context);
        initialize(context);
    }

    public RoundCornerWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initialize(context);
    }

    private void initialize(Context context) {
        this.context = context;
        this.path = new Path();
    }

    @Override // android.webkit.WebView, android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.width = i;
        this.height = i2;
        this.radius = Utils.dp2px(16.0f);
    }

    @Override // android.webkit.WebView, android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.path.setFillType(Path.FillType.INVERSE_WINDING);
        Path path = this.path;
        @SuppressLint("DrawAllocation") RectF rectF = new RectF(0.0f, getScrollY(), this.width, getScrollY() + this.height);
        int i = this.radius;
        path.addRoundRect(rectF, i, i, Path.Direction.CW);
        canvas.drawPath(this.path, createPorterDuffClearPaint());
    }

    private Paint createPorterDuffClearPaint() {
        Paint paint = new Paint();
        paint.setColor(0);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        return paint;
    }
}
