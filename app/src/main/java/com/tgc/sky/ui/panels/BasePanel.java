package com.tgc.sky.ui.panels;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.PopupWindow;
import com.tgc.sky.GameActivity;
import com.tgc.sky.SystemUI_android;
import com.tgc.sky.ui.text.Markup;

/* renamed from: com.tgc.sky.ui.panels.BasePanel */
public class BasePanel extends PopupWindow {
    GameActivity m_activity;
    Markup m_markup;
    private DisplayMetrics m_metrics;
    SystemUI_android m_systemUI;

    BasePanel(Context context, SystemUI_android systemUI_android, Markup markup) {
        super(context);
        GameActivity gameActivity = (GameActivity) context;
        this.m_activity = gameActivity;
        this.m_metrics = gameActivity.getResources().getDisplayMetrics();
        this.m_systemUI = systemUI_android;
        this.m_markup = markup;
        if (Build.VERSION.SDK_INT >= 29) {
            super.setIsClippedToScreen(true);
        }
    }

    public void showAtLocation(View view, int i, int i2, int i3) {
        GameActivity.hideNavigationFullScreen(getContentView());
        super.showAtLocation(view, i, i2, i3);
        this.m_activity.addActivePanel(this);
    }

    public void dismiss() {
        super.dismiss();
        this.m_activity.removeActivePanel(this);
    }

    /* access modifiers changed from: package-private */
    public int dp2px(float f) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, f, this.m_metrics);
    }
}
