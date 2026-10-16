package com.tgc.sky.ui.panels;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.tgc.sky.GameActivity;
import com.tgc.sky.SystemUI_android;
import com.tgc.sky.ui.text.Markup;

import git.artdeell.skymodloader.SMLApplication;

public class FuntapAgeVerification extends BasePanel implements View.OnLayoutChangeListener, GameActivity.OnKeyboardListener {
    private static final String TAG = "FuntapAgeVerification";
    private static final String funtapOrigin = "https://cp.hhgame.vn";
    private final PanelButton _closeButton;
    private final float heightMult;
    private final float landscapeAspectRatio;
    public Handle mHandler;

    public enum DialogOptions {
        kDialogOptions_UserClosedPanel,
        kDialogOptions_FormAccepted,
        kDialogOptions_PageLoaded
    }

    public interface Handle {
        void run(String str, int i, boolean z);
    }

    @Override
    public void onKeyboardChange(boolean z, int i) {
    }

    @Override
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
    }

    public FuntapAgeVerification(Context context, SystemUI_android systemUI_android, Markup markup, Handle handle, String accountId, String token) {
        super(context, systemUI_android, markup);
        this.heightMult = 0.9f;
        this.landscapeAspectRatio = 1.5f;
        this.mHandler = handle;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.m_activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

        float width = displayMetrics.widthPixels < displayMetrics.heightPixels
                ? displayMetrics.widthPixels * 0.9f
                : 1.5f * displayMetrics.heightPixels * 0.9f;

        setHeight((int) (displayMetrics.heightPixels * 0.9f));
        setWidth((int) width);
        setBackgroundDrawable(new ColorDrawable(0));
        setFocusable(true);

        final CardView cardView = new CardView(this.m_activity);
        cardView.setCardBackgroundColor(0);
        cardView.setRadius(dp2px(16.0f));
        cardView.setPreventCornerOverlap(false);
        cardView.setCardElevation(0.0f);
        cardView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        setContentView(cardView);

        ConstraintLayout constraintLayout = new ConstraintLayout(this.m_activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.gravity = 5;
        constraintLayout.setLayoutParams(layoutParams);

        PanelButton panelButton = new PanelButton(this.m_activity, new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                FuntapAgeVerification.this.onCloseButton();
            }
        });
        this._closeButton = panelButton;
        panelButton.setId(View.generateViewId());
        panelButton.setLayoutParams(new ConstraintLayout.LayoutParams(-2, -2));
        panelButton.setVisibility(View.VISIBLE);
        panelButton.setEnabled(true);

        Drawable closeDrawable = null;
        try {
            if (SMLApplication.skyRes != null && SMLApplication.skyPName != null) {
                int resId = SMLApplication.skyRes.getIdentifier("systemui_closebutton", "drawable", SMLApplication.skyPName);
                if (resId != 0) {
                    closeDrawable = SMLApplication.skyRes.getDrawable(resId, null);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load close button", e);
        }
        if (closeDrawable != null) {
            ScaleDrawable scaleDrawable = new ScaleDrawable(closeDrawable, 17, 0.7f, 0.7f);
            scaleDrawable.setLevel(100);
            panelButton.setBackground(scaleDrawable);
        }

        final FrameLayout frameLayoutCreateLoadingScreen = createLoadingScreen();
        final WebView webView = new WebView(this.m_activity);
        webView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        webView.setBackgroundColor(0);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setDomStorageEnabled(true);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void close() {
                FuntapAgeVerification.this.m_activity.runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        FuntapAgeVerification.this.onCloseButton();
                    }
                });
            }

            @JavascriptInterface
            public void mobAppSDKexecute(String str3, String str4) {
                Log.d("Funtap", "Received payload: " + str4);
                if (str3 == null || !str3.contains("closeWindow")) {
                    return;
                }
                FuntapAgeVerification.this.m_activity.runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        FuntapAgeVerification.this.onSuccess();
                    }
                });
            }
        }, "JsHandler");

        String funtapURL = getFuntapURL(accountId, token);
        Log.i(TAG, "Loading Funtap Age Verification URL: " + funtapURL);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView2, WebResourceRequest webResourceRequest) {
                return handleUrl(webResourceRequest.getUrl());
            }

            @Override
            public void onPageFinished(WebView webView2, String str3) {
                super.onPageFinished(webView2, str3);
                frameLayoutCreateLoadingScreen.setVisibility(View.GONE);
                cardView.removeView(frameLayoutCreateLoadingScreen);
                webView.requestFocus();
                FuntapAgeVerification.this.mHandler.run(null, DialogOptions.kDialogOptions_PageLoaded.ordinal(), true);
            }

            private boolean handleUrl(Uri uri) {
                if (uri.toString().startsWith(FuntapAgeVerification.funtapOrigin)) {
                    return false;
                }
                FuntapAgeVerification.this.m_activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(funtapURL);

        cardView.addView(frameLayoutCreateLoadingScreen, new FrameLayout.LayoutParams(-1, -1));
        cardView.addView(webView);
        cardView.addView(constraintLayout);
        constraintLayout.addView(panelButton);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(panelButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.applyTo(constraintLayout);

        this.m_activity.setMotionEventsDisabled(true);
        this.m_activity.addOnKeyboardListener(this);
    }

    private static String getFuntapURL(String accountId, String token) {
        return "https://cp.hhgame.vn/v2/user/profile/" + accountId + "/update?accountId=" + accountId + "&token=" + token + "&os=android&server_id=0&server_name=Live&character_id=" + accountId + "&character_name=default";
    }

    private FrameLayout createLoadingScreen() {
        FrameLayout frameLayout = new FrameLayout(this.m_activity);
        frameLayout.setForegroundGravity(17);
        frameLayout.setVisibility(View.VISIBLE);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(dp2px(16.0f));
        gradientDrawable.setColor(Color.argb(255, 0, 0, 0));
        frameLayout.setBackground(gradientDrawable);
        ProgressBar progressBar = new ProgressBar(this.m_activity);
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(-1));
        frameLayout.addView(progressBar, new FrameLayout.LayoutParams(-2, -2, 17));
        return frameLayout;
    }

    public void onCloseButton() {
        dismissInternal();
        this.mHandler.run(null, DialogOptions.kDialogOptions_UserClosedPanel.ordinal(), true);
    }

    public void onSuccess() {
        dismissInternal();
        this.mHandler.run(null, DialogOptions.kDialogOptions_FormAccepted.ordinal(), true);
    }

    public void dismissInternal() {
        super.dismiss();
        this.m_activity.setMotionEventsDisabled(false);
        this.m_activity.RemoveOnKeyboardListener(this);
    }

    @Override
    public void dismiss() {
        if (this._closeButton.isEnabled()) {
            dismissInternal();
            this.mHandler.run(null, DialogOptions.kDialogOptions_UserClosedPanel.ordinal(), true);
        }
    }
}
