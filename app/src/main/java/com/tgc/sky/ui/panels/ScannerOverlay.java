package com.tgc.sky.ui.panels;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.core.view.ViewCompat;
import com.tgc.sky.ui.Utils;
import com.tgc.sky.ui.ZXingUtils;
import java.util.HashMap;

/* renamed from: com.tgc.sky.ui.panels.ScannerOverlay */
/* compiled from: InvitationPanel */
class ScannerOverlay extends RelativeLayout {
    private String mCurrentLink;
    /* access modifiers changed from: private */
    public ImageView mImageView;
    private HashMap<Object, Object> mImages;
    /* access modifiers changed from: private */
    public ProgressBar mProgressBar;
    private boolean mScanMode;
    private Paint paint = new Paint();

    public ScannerOverlay(Context context) {
        super(context);
        setWillNotDraw(false);
        initWithFrame();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        layoutSubviews();
    }

    public void initWithFrame() {
        initViews();
    }

    public void initViews() {
        this.mImageView = new ImageView(getContext());
        ProgressBar progressBar = new ProgressBar(getContext(), (AttributeSet) null, 16842873);
        this.mProgressBar = progressBar;
        progressBar.setIndeterminate(true);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Utils.dp2px(32.0f), Utils.dp2px(32.0f));
        layoutParams.addRule(13, -1);
        this.mProgressBar.setLayoutParams(layoutParams);
        this.mImages = new HashMap<>();
        this.mScanMode = false;
        addView(this.mImageView);
        addView(this.mProgressBar);
    }

    public void layoutSubviews() {
        Rect rect = new Rect(0, 0, getMeasuredWidth(), getMeasuredWidth());
        rect.inset((rect.width() - Utils.dp2px(180.0f)) / 2, (rect.height() - Utils.dp2px(180.0f)) / 2);
        if (this.mScanMode) {
            rect.inset(30, 30);
        }
        this.mImageView.layout(rect.left, rect.top, rect.right, rect.bottom);
    }

    public void setInviteLink(String str, boolean z) {
        this.mCurrentLink = str;
        this.mScanMode = z;
        int i = 0;
        if (str != null) {
            this.mImageView.setImageBitmap((Bitmap) null);
            this.mProgressBar.setVisibility(View.VISIBLE);
            setVisibility(View.VISIBLE);
            setQRCodeAsync(str, this);
        } else {
            this.mImageView.setImageBitmap((Bitmap) null);
            this.mProgressBar.setVisibility(View.GONE);
            if (!z) {
                i = View.GONE;
            }
            setVisibility(i);
        }
        invalidate();
    }

    static void setQRCodeAsync(final String str, final ScannerOverlay scannerOverlay) {
        new AsyncTask<Void, Void, Bitmap>() {
            /* access modifiers changed from: protected */
            public Bitmap doInBackground(Void... voidArr) {
                return ZXingUtils.createQRImage(str, Utils.dp2px(180.0f), Utils.dp2px(180.0f));
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                scannerOverlay.mImageView.setImageBitmap(bitmap);
                scannerOverlay.mProgressBar.setVisibility(View.GONE);
                scannerOverlay.setVisibility(View.VISIBLE);
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        Size intrinsicContentSize = intrinsicContentSize();
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(intrinsicContentSize.getWidth(), MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(intrinsicContentSize.getHeight(), MeasureSpec.EXACTLY));
    }

    public Size intrinsicContentSize() {
        return new Size(Utils.dp2px(180.0f), Utils.dp2px(180.0f));
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawRect(canvas);
    }

    public void drawRect(Canvas canvas) {
        if (this.mScanMode) {
            Path path = new Path();
            this.paint.setStrokeWidth((float) Utils.dp2px(6.0f));
            this.paint.setColor(ViewCompat.MEASURED_STATE_MASK);
            this.paint.setStyle(Paint.Style.STROKE);
            float dp2px = (float) Utils.dp2px(20.0f);
            Rect rect = new Rect(0, 0, getWidth(), getHeight());
            rect.inset((rect.width() - Utils.dp2px(170.0f)) / 2, (rect.height() - Utils.dp2px(170.0f)) / 2);
            path.moveTo(((float) rect.left) + dp2px, (float) rect.top);
            path.arcTo((float) rect.left, (float) rect.top, ((float) rect.left) + dp2px, ((float) rect.top) + dp2px, -90.0f, -90.0f, false);
            path.arcTo((float) rect.left, ((float) rect.bottom) - dp2px, ((float) rect.left) + dp2px, (float) rect.bottom, -180.0f, -90.0f, false);
            path.lineTo(((float) rect.left) + dp2px, (float) rect.bottom);
            path.moveTo(((float) rect.right) - dp2px, (float) rect.top);
            path.arcTo(((float) rect.right) - dp2px, (float) rect.top, (float) rect.right, ((float) rect.top) + dp2px, -90.0f, 90.0f, false);
            path.arcTo(((float) rect.right) - dp2px, ((float) rect.bottom) - dp2px, (float) rect.right, (float) rect.bottom, 0.0f, 90.0f, false);
            path.lineTo(((float) rect.right) - dp2px, (float) rect.bottom);
            canvas.drawPath(path, this.paint);
        }
    }
}
