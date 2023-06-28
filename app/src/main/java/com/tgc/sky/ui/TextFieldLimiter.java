package com.tgc.sky.ui;

import android.text.InputFilter;
import android.text.Spanned;
import java.io.UnsupportedEncodingException;

/* renamed from: com.tgc.sky.ui.TextFieldLimiter */
public class TextFieldLimiter implements InputFilter {
    public int maxByteSize;
    public int maxCharacters;

    public CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
        try {
            String charSequence2 = spanned.subSequence(0, i3).toString();
            int max = Integer.max(0, (charSequence2.getBytes("UTF-32").length / 4) - 1);
            int length = charSequence2.getBytes().length;
            String charSequence3 = spanned.subSequence(i4, spanned.length()).toString();
            int length2 = length + charSequence3.getBytes().length;
            CharSequence subSequence = charSequence.subSequence(i, i2);
            int max2 = Integer.max(0, (subSequence.toString().getBytes("UTF-32").length / 4) - 1);
            int length3 = subSequence.toString().getBytes().length;
            int max3 = max + Integer.max(0, (charSequence3.getBytes("UTF-32").length / 4) - 1) + max2;
            if (max3 <= this.maxCharacters && length2 + length3 <= this.maxByteSize) {
                return null;
            }
            if (max3 > this.maxCharacters) {
                return spanned.subSequence(i3, i4);
            }
            if (length2 + length3 > this.maxByteSize) {
                return spanned.subSequence(i3, i4);
            }
            return "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
