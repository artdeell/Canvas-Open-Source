package com.tgc.sky.ui.panels;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import com.tgc.sky.BuildConfig;
import com.tgc.sky.GameActivity;
//import com.tgc.sky.R;
import com.tgc.sky.SystemUI_android;
import com.tgc.sky.ui.panels.CodeScanner;
import com.tgc.sky.ui.text.Markup;
import com.tgc.sky.ui.webview.RoundCornerWebView;

import git.artdeell.skymodloader.R;

/* loaded from: classes2.dex */
public class Starboard extends BasePanel implements View.OnLayoutChangeListener, GameActivity.OnKeyboardListener {
    private final PanelButton _closeButton;
    public Handle mHandler;
    private final RelativeLayout view;

    /* loaded from: classes2.dex */
    public interface Handle {
        void run(String str, int i, boolean z);
    }

    @Override // com.tgc.sky.GameActivity.OnKeyboardListener
    public void onKeyboardChange(boolean z, int i) {
    }

    public void setPreviewOrientation() {
    }

    public Starboard(Context context, SystemUI_android systemUI_android, Markup markup, Handle handle) {
        super(context, systemUI_android, markup);
        this.mHandler = handle;
        setBackgroundDrawable(new ColorDrawable(0));
        RelativeLayout relativeLayout = new RelativeLayout(this.m_activity);
        this.view = relativeLayout;
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        setContentView(relativeLayout);
        setFocusable(true);
        setWidth(-1);
        setHeight(-1);
        RelativeLayout relativeLayout2 = new RelativeLayout(this.m_activity);
        GradientDrawable gradientDrawable = (GradientDrawable) relativeLayout2.getBackground();
        if (gradientDrawable == null) {
            gradientDrawable = new GradientDrawable();
            relativeLayout2.setBackground(gradientDrawable);
        }
        gradientDrawable.setColor(0);
        relativeLayout.addView(relativeLayout2);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.m_activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        relativeLayout2.setLayoutParams(setLayoutParams(displayMetrics, 0.5625f, 0.9f));
        ConstraintLayout constraintLayout = new ConstraintLayout(this.m_activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.gravity = 5;
        constraintLayout.setLayoutParams(layoutParams);
        PanelButton panelButton = new PanelButton(this.m_activity, new View.OnClickListener() { // from class: com.tgc.sky.ui.panels.Starboard$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Starboard.this.m419lambda$new$0$comtgcskyuipanelsStarboard(view);
            }
        });
        this._closeButton = panelButton;
        panelButton.setId(View.generateViewId());
        panelButton.setLayoutParams(new ConstraintLayout.LayoutParams(-2, -2));
        panelButton.setVisibility(View.VISIBLE);
        panelButton.setEnabled(true);
        ScaleDrawable scaleDrawable = new ScaleDrawable(ContextCompat.getDrawable(this.m_activity, R.drawable.back_button), 17, 0.5f, 0.5f);
        scaleDrawable.setLevel(100);
        panelButton.setBackground(scaleDrawable);
        RoundCornerWebView roundCornerWebView = new RoundCornerWebView(this.m_activity);
        roundCornerWebView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        roundCornerWebView.setBackgroundColor(0);
        roundCornerWebView.getSettings().setJavaScriptEnabled(true);
        roundCornerWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        StringBuilder sb = new StringBuilder(BuildConfig.SKY_SERVER_HOSTNAME);
        sb.insert(sb.indexOf(".") + 1, "starboard.");
        sb.insert(0, "https://");
        roundCornerWebView.loadUrl(sb.toString());
        relativeLayout2.addView(roundCornerWebView);
        relativeLayout2.addView(constraintLayout);
        constraintLayout.addView(panelButton);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(panelButton.getId(), 2, 0, 2);
        constraintSet.applyTo(constraintLayout);
    }

    /* renamed from: lambda$new$0$com-tgc-sky-ui-panels-Starboard */
    public /* synthetic */ void m419lambda$new$0$comtgcskyuipanelsStarboard(View view) {
        onCloseButton();
    }

    private static RelativeLayout.LayoutParams setLayoutParams(DisplayMetrics displayMetrics, float f, float f2) {
        int i = displayMetrics.widthPixels;
        int i2 = (int) (displayMetrics.heightPixels * f2);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((i2 / 9) * 16, i2);
        layoutParams.addRule(14);
        layoutParams.addRule(13);
        layoutParams.addRule(15);
        return layoutParams;
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        viewWillLayoutSubviews();
    }

    public void viewWillLayoutSubviews() {
        setPreviewOrientation();
    }

    public void onCloseButton() {
        this.view.performHapticFeedback(7);
        dismissInternal();
        this.mHandler.run(null, CodeScanner.ResultOptions.kCodeScanner_UserClosedPanel.ordinal(), true);
    }

    public void dismissInternal() {
        super.dismiss();
        viewDidDisappear();
    }

    public void viewDidDisappear() {
        this.m_activity.RemoveOnKeyboardListener(this);
    }

    @Override // com.tgc.sky.ui.panels.BasePanel, android.widget.PopupWindow
    public void dismiss() {
        if (this._closeButton.isEnabled()) {
            dismissInternal();
            this.mHandler.run(null, CodeScanner.ResultOptions.kCodeScanner_UserClosedPanel.ordinal(), true);
        }
    }
}
