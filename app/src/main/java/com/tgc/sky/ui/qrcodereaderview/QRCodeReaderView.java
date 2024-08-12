package com.tgc.sky.ui.qrcodereaderview;

import android.content.Context;
import android.widget.FrameLayout;
import androidx.cardview.widget.CardView;
import com.tgc.sky.ui.qrcodereaderview.QRCodeReaderTextureView;


public class QRCodeReaderView extends CardView {
    private QRCodeReaderTextureView mQRCodeReaderTextureView;

    public QRCodeReaderView(Context context) {
        super(context);
        this.mQRCodeReaderTextureView = new QRCodeReaderTextureView(context);
        this.mQRCodeReaderTextureView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        addView(this.mQRCodeReaderTextureView);
    }

    public void setOnQRCodeReadListener(QRCodeReaderTextureView.OnQRCodeReadListener onQRCodeReadListener) {
        this.mQRCodeReaderTextureView.setOnQRCodeReadListener(onQRCodeReadListener);
    }

    public void startCamera() {
        this.mQRCodeReaderTextureView.startCamera();
    }

    public void stopCamera() {
        this.mQRCodeReaderTextureView.stopCamera();
    }
}
