package com.tgc.sky.ui.text;

import android.text.SpannableStringBuilder;
import android.widget.TextView;

/* renamed from: com.tgc.sky.ui.text.TextLabel */
public class TextLabel {
    float actualHeight = 0.0f;
    float actualWidth = 0.0f;
    public TextAttributes attrs = new TextAttributes();
    public boolean autoFreeTextId = true;
    SpannableStringBuilder finalAttributedString = null;
    int lastTextIdChange = 0;
    public TextPositioning pos = new TextPositioning();
    public int textId = -1;
    float unconstrainedHeight = 0.0f;
    float unconstrainedWidth = 0.0f;
    TextView view = null;

    public void Clear() {
        this.textId = -1;
        this.autoFreeTextId = true;
        this.view = null;
        this.lastTextIdChange = 0;
        this.finalAttributedString = null;
        this.actualWidth = 0.0f;
        this.actualHeight = 0.0f;
        this.unconstrainedWidth = 0.0f;
        this.unconstrainedHeight = 0.0f;
    }
}
