package com.tgc.sky.ui;

import android.content.Context;
import android.view.View;

/* renamed from: com.tgc.sky.ui.DisplayUtil */
public class DisplayUtil {
    public static float AppleConvertAndroidScale() {
        return 1.5f;
    }

    public static int px2dip(Context context, float f) {
        return (int) ((f / context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int dip2px(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int pt2px(Context context, float f) {
        return (int) ((f * ((float) (context.getResources().getDisplayMetrics().densityDpi / 72))) + 0.5f);
    }

    public static int px2sp(Context context, float f) {
        return (int) ((f / context.getResources().getDisplayMetrics().scaledDensity) + 0.5f);
    }

    public static int sp2px(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().scaledDensity) + 0.5f);
    }

    public static void hideNavigationFullScreen(View view) {
        view.setSystemUiVisibility(5894);
    }

    public static void showNavigationFullScreen(View view) {
        view.setSystemUiVisibility(1792);
    }
}
