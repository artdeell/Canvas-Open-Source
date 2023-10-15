package com.tgc.sky.ui.text;

/* renamed from: com.tgc.sky.ui.text.SystemVAlignment */
public enum SystemVAlignment {
    ALIGN_V_BOTTOM(3),
    ALIGN_V_CENTER(2),
    ALIGN_V_TOP(1);
    final int value;

    SystemVAlignment(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
