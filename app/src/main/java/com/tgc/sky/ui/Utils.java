package com.tgc.sky.ui;

import android.util.TypedValue;

import git.artdeell.skymodloader.SMLApplication;

/* renamed from: com.tgc.sky.ui.Utils */
public class Utils {
    public static int dp2px(float f) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, f, SMLApplication.deez().getResources().getDisplayMetrics());
    }

    public static int sp2px(float f) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, f, SMLApplication.deez().getResources().getDisplayMetrics());
    }
}
