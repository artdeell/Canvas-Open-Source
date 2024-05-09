package com.tgc.sky;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.Result;
import com.tgc.sky.ui.ZXingUtils;
import com.tgc.sky.ui.qrcodereaderview.QRCodeReaderTextureView;

/* loaded from: classes2.dex */
public class QrScanner {

    public QrScanner() {}

    public boolean isOperational() {
        return true;
    }

    public void scan(QRCodeReaderTextureView qRCodeReaderTextureView, Bitmap bitmap) {
        Result zxingResult = ZXingUtils.decodeQRBitmap(bitmap);
        if(zxingResult == null) return;
        String result = zxingResult.getText();
        qRCodeReaderTextureView.mOnQRCodeReadListener.onQRCodeRead(result);
    }

    public static String scanImage(Bitmap bitmap) {
        Log.d("InvitationPanel", "Looking for qrcode in image");
        Result zxingResult = ZXingUtils.decodeQRBitmap(bitmap);
        if (zxingResult == null) return null;
        String result = zxingResult.getText();
        if (result == null) return null;
        if (result.startsWith("https://sky") && result.indexOf(".thatg.co/?") >= 0) return result;
        return null;
    }
}
