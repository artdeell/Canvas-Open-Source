package com.tgc.sky.ui.text;

import android.graphics.RectF;

public class TextPositioning {
    public float x = 0.0f;
    public float y = 0.0f;
    public float z = 0.0f;
    public SystemHAlignment h = SystemHAlignment.ALIGN_H_LEFT;
    public SystemVAlignment v = SystemVAlignment.ALIGN_V_BOTTOM;
    public boolean shrinkBoxToText = false;
    public float maxWidth = 0.0f;
    public float maxHeight = 0.0f;
    public float padWidth = 16.0f;
    public float padHeight = 8.0f;
    public float clipMinX = 0.0f;
    public float clipMinY = 0.0f;
    public float clipMaxX = 8192.0f;
    public float clipMaxY = 8192.0f;
    public boolean clip = false;
    public boolean autoAnchor = true;

    public void UpdateWith(TextPositioning textPositioning) {
        this.x = textPositioning.x;
        this.y = textPositioning.y;
        this.z = textPositioning.z;
        this.h = textPositioning.h;
        this.v = textPositioning.v;
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

    public RectF ProgramRect() {
        float f = this.maxWidth;
        if (f == 0.0f) {
            f = 8192.0f;
        }
        float f2 = this.maxHeight;
        return new RectF(0.0f, 0.0f, f, f2 != 0.0f ? f2 : 8192.0f);
    }
}
