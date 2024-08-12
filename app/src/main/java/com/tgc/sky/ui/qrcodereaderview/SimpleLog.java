package com.tgc.sky.ui.qrcodereaderview;

import android.util.Log;


public class SimpleLog {
    private static boolean loggingEnabled = false;

    public static void setLoggingEnabled(boolean z) {
        loggingEnabled = z;
    }

    public static void d(String str, String str2) {
        if (loggingEnabled) {
            Log.d(str, str2);
        }
    }

    public static void w(String str, String str2) {
        if (loggingEnabled) {
            Log.w(str, str2);
        }
    }

    public static void w(String str, String str2, Throwable th) {
        if (loggingEnabled) {
            Log.w(str, str2, th);
        }
    }

    public static void e(String str, String str2) {
        if (loggingEnabled) {
            Log.e(str, str2);
        }
    }

    public static void d(String str, String str2, Throwable th) {
        if (loggingEnabled) {
            Log.d(str, str2, th);
        }
    }

    public static void i(String str, String str2) {
        if (loggingEnabled) {
            Log.i(str, str2);
        }
    }
}
