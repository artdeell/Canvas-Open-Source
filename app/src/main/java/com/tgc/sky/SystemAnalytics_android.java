// 
// Decompiled by Procyon v0.5.36
// 

package com.tgc.sky;

import android.net.Uri;
import com.tgc.sky.commerce.ProductInfo;
import com.tgc.sky.commerce.ReceiptItem;
import android.content.Context;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

public class SystemAnalytics_android
{
    private static volatile SystemAnalytics_android sInstance;
    private String mUserId;
    
    public SystemAnalytics_android() {
    }
    
    static void OnApplicationCreate(final Application application) {

    }
    
    public static SystemAnalytics_android getInstance() {
        if (SystemAnalytics_android.sInstance == null) {
            synchronized (SystemIO_android.class) {
                if (SystemAnalytics_android.sInstance == null) {
                    SystemAnalytics_android.sInstance = new SystemAnalytics_android();
                }
            }
        }
        return SystemAnalytics_android.sInstance;
    }

    public String GetUserId() {
        return this.mUserId;
    }
    
    void Initialize(final GameActivity activity) {
    }
    
    public void LogCrashBreadcrumb(final String s) {
    }
    
    public void OnFinishPurchase(final ReceiptItem receiptItem, final ProductInfo productInfo) {
    }
    
    void OnOpenURL(final Uri uri, final Context context) {
    }
    
    public void OnPushNotificationToken(final String s, final Context context) {
    }
    
    public void SetCrashInfoInt(final String s, final int n) {
    }
    
    public void SetCrashInfoString(final String s, final String s2) {
    }
    
    public void SetFixedParam(final String s, final long i) {
    }
    
    public void SetFixedParam(final String s, final String s2) {
    }
    
    public void SetUserId(final String s) {
        this.mUserId = s;
    }
    
    public void SetUserProperty(final String s, final String s2) {
    }
    
    public void SubmitEvent(final String s, final String[] array, final String[] array2) {
    }
}
