package com.tgc.sky.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.Hashtable;
import java.util.Map;

/* renamed from: com.tgc.sky.ui.ZXingUtils */
public class ZXingUtils {
    public static Bitmap createQRImage(String str, int i, int i2) {
        if (str != null) {
            try {
                if (!"".equals(str)) {
                    if (str.length() >= 1) {
                        Hashtable hashtable = new Hashtable();
                        hashtable.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                        hashtable.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_SQUARE);
                        hashtable.put(EncodeHintType.MARGIN, 0);
                        BitMatrix encode = new QRCodeWriter().encode(str, BarcodeFormat.QR_CODE, i, i2, hashtable);
                        int[] iArr = new int[(i * i2)];
                        for (int i3 = 0; i3 < i2; i3++) {
                            for (int i4 = 0; i4 < i; i4++) {
                                if (encode.get(i4, i3)) {
                                    iArr[(i3 * i) + i4] = -16777216;
                                } else {
                                    iArr[(i3 * i) + i4] = -1;
                                }
                            }
                        }
                        Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
                        createBitmap.setPixels(iArr, 0, i, 0, 0, i, i2);
                        return createBitmap;
                    }
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Bitmap creatBarcode(Context context, String str, int i, int i2, boolean z) {
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;
        if (z) {
            return mixtureBitmap(encodeAsBitmap(str, barcodeFormat, i, i2), creatCodeBitmap(str, i + 40, i2, context), new PointF(0.0f, (float) i2));
        }
        return encodeAsBitmap(str, barcodeFormat, i, i2);
    }

    protected static Bitmap encodeAsBitmap(String str, BarcodeFormat barcodeFormat, int i, int i2) {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(str, barcodeFormat, i, i2, (Map<EncodeHintType, ?>) null);
        } catch (WriterException e) {
            e.printStackTrace();
            bitMatrix = null;
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] iArr = new int[(width * height)];
        for (int i3 = 0; i3 < height; i3++) {
            int i4 = i3 * width;
            for (int i5 = 0; i5 < width; i5++) {
                iArr[i4 + i5] = bitMatrix.get(i5, i3) ? ViewCompat.MEASURED_STATE_MASK : -1;
            }
        }
        Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        createBitmap.setPixels(iArr, 0, width, 0, 0, width, height);
        return createBitmap;
    }

    protected static Bitmap creatCodeBitmap(String str, int i, int i2, Context context) {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        textView.setText(str);
        textView.setHeight(i2);
        textView.setGravity(1);
        textView.setWidth(i);
        textView.setDrawingCacheEnabled(true);
        textView.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
        textView.buildDrawingCache();
        return textView.getDrawingCache();
    }

    protected static Bitmap mixtureBitmap(Bitmap bitmap, Bitmap bitmap2, PointF pointF) {
        if (bitmap == null || bitmap2 == null || pointF == null) {
            return null;
        }
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth() + bitmap2.getWidth() + 20, bitmap.getHeight() + bitmap2.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawBitmap(bitmap, (float) 20, 0.0f, (Paint) null);
        canvas.drawBitmap(bitmap2, pointF.x, pointF.y, (Paint) null);
        canvas.save();
        canvas.restore();
        return createBitmap;
    }

    public static Result decodeQRBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] iArr = new int[(width * height)];
        bitmap.getPixels(iArr, 0, width, 0, 0, width, height);
        try {
            return new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), iArr))));
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (ChecksumException e2) {
            e2.printStackTrace();
            return null;
        } catch (FormatException e3) {
            e3.printStackTrace();
            return null;
        }
    }
}
