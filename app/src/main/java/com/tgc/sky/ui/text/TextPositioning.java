package com.tgc.sky.ui.text;

import android.graphics.RectF;

/* renamed from: com.tgc.sky.ui.text.TextPositioning */
public class TextPositioning {
    public boolean autoAnchor = true;
    public boolean clip = false;
    public float clipMaxX = 8192.0f;
    public float clipMaxY = 8192.0f;
    public float clipMinX = 0.0f;
    public float clipMinY = 0.0f;

    /* renamed from: h */
    public SystemHAlignment f1047h = SystemHAlignment.ALIGN_H_LEFT;
    public float maxHeight = 0.0f;
    public float maxWidth = 0.0f;
    public float padHeight = 8.0f;
    public float padWidth = 16.0f;
    public boolean shrinkBoxToText = false;

    /* renamed from: v */
    public SystemVAlignment f1048v = SystemVAlignment.ALIGN_V_BOTTOM;

    /* renamed from: x */
    public float f1049x = 0.0f;

    /* renamed from: y */
    public float f1050y = 0.0f;

    /* renamed from: z */
    public float f1051z = 0.0f;

    TextPositioning() {
    }

    /* access modifiers changed from: package-private */
    public void UpdateWith(TextPositioning textPositioning) {
        this.f1049x = textPositioning.f1049x;
        this.f1050y = textPositioning.f1050y;
        this.f1051z = textPositioning.f1051z;
        this.f1047h = textPositioning.f1047h;
        this.f1048v = textPositioning.f1048v;
        this.shrinkBoxToText = textPositioning.shrinkBoxToText;
        this.maxWidth = textPositioning.maxWidth;
        this.maxHeight = textPositioning.maxHeight;
        this.padWidth = textPositioning.padWidth;
        this.padHeight = textPositioning.padHeight;
        this.clipMinX = textPositioning.clipMinX;
        this.clipMinY = textPositioning.clipMinY;
        this.clipMaxX = textPositioning.clipMaxX;
        this.clipMaxY = textPositioning.clipMaxY;
        this.clip = textPositioning.clip;
        this.autoAnchor = textPositioning.autoAnchor;
    }

    /* access modifiers changed from: package-private */
    public RectF ProgramRect() {
        float f = this.maxWidth;
        float f2 = 8192.0f;
        if (f == 0.0f) {
            f = 8192.0f;
        }
        float f3 = this.maxHeight;
        if (f3 != 0.0f) {
            f2 = f3;
        }
        return new RectF(0.0f, 0.0f, f, f2);
    }
}
