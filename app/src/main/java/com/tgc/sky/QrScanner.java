package com.tgc.sky;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.tgc.sky.ui.qrcodereaderview.QRCodeReaderTextureView;

/* loaded from: classes2.dex */
public class QrScanner {
    private BarcodeDetector mDetector;

    public QrScanner(Context context) {
        this.mDetector = new BarcodeDetector.Builder(context).setBarcodeFormats(256).build();
    }

    public boolean isOperational() {
        return this.mDetector.isOperational();
    }

    public void scan(QRCodeReaderTextureView qRCodeReaderTextureView, Bitmap bitmap) {
        SparseArray<Barcode> detect = this.mDetector.detect(new Frame.Builder().setBitmap(bitmap).build());
        if (detect.size() > 0) {
            for (int i = 0; i < detect.size(); i++) {
                Barcode valueAt = detect.valueAt(i);
                if (valueAt != null) {
                    RectF rectF = new RectF(valueAt.getBoundingBox());
                    qRCodeReaderTextureView.transformRect(rectF);
                    qRCodeReaderTextureView.mOnQRCodeReadListener.onQRCodeRead(valueAt.url != null ? valueAt.url.url : null, rectF);
                }
            }
        }
    }

    public static String scanImage(Context context, Bitmap bitmap) {
        Log.d("InvitationPanel", "Looking for qrcode in image");
        BarcodeDetector build = new BarcodeDetector.Builder(context).setBarcodeFormats(256).build();
        if (build.isOperational()) {
            SparseArray<Barcode> detect = build.detect(new Frame.Builder().setBitmap(bitmap).build());
            if (detect.size() > 0) {
                Log.d("QrScanner", "Found " + detect.size() + " qrcodes");
                boolean z = false;
                Barcode valueAt = detect.valueAt(0);
                if (valueAt.url != null) {
                    Log.d("QrScanner", "Qrcode URL: " + valueAt.url.url);
                    String str = valueAt.url.url;
                    if (str.startsWith("https://sky") && str.indexOf(".thatg.co/?") >= 0) {
                        z = true;
                    }
                    if (z) {
                        return valueAt.url.url;
                    }
                    return null;
                }
                Log.d("QrScanner", "QR code is not a URL!");
                return null;
            }
            Log.d("QrScanner", "No QR codes detected");
            return null;
        }
        Log.d("QrScanner", "Barcode detector not operational");
        return null;
    }
}
