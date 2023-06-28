// 
// Decompiled by Procyon v0.5.36
// 

package com.tgc.sky;

import com.tgc.sky.commerce.Receipt;
import com.tgc.sky.commerce.ProductInfo;

public class SystemCommerce_android
{
    private static volatile SystemCommerce_android sInstance;
    SystemCommerce_android(final GameActivity activity) {
        SystemCommerce_android.sInstance = this;
    }
    
    public static SystemCommerce_android getInstance() {
        return SystemCommerce_android.sInstance;
    }
    
    public boolean CanMakePayments() {
        return false;
    }
    
    public boolean FinishPurchase(String productIdToSystemProductId, final String s) {
        return false;
    }
    
    public int GetPlatformInt() {
        return 0;
    }
    
    public ProductInfo GetProductInfo(String productIdToSystemProductId) {
        return null;
    }
    
    public Receipt GetReceipt() {
        return null;
    }
    
    public boolean IsPurchasePending(String productIdToSystemProductId) {
        return false;
    }
    
    public void LoadProducts(final String[] a) {

    }
    
    public boolean MakePurchase(String productIdToSystemProductId) {
        return false;
    }
    
    public boolean RefreshReceipt() {
        return true;
    }
    
    public boolean RestorePurchases() {
        return true;
    }
    
    void onDestroy() {
    }

    void onResume() {
    }
}
