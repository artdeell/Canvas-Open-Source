package com.tgc.sky.ui.text;

/* renamed from: com.tgc.sky.ui.text.SystemHAlignment */
public enum SystemHAlignment {
    ALIGN_H_LEFT(1),
    ALIGN_H_CENTER(2),
    ALIGN_H_RIGHT(3);
    final int value;

    SystemHAlignment(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
