package com.tgc.sky.ui.text;

/* renamed from: com.tgc.sky.ui.text.TextAttributes */
public class TextAttributes {
    public boolean adjustFontSizeToFitWidth = false;
    public float[] bgColor;
    public float bgCornerRadius = 0.0f;
    public String fontName;
    public float fontSize = 12.0f;
    public boolean hasBackground = false;
    public boolean hasShadow = false;
    public boolean ignoreMarkupOptimization = false;
    public int maxNumberOfLines = 0;
    public float scale = 1.0f;
    public float[] shadowColor;
    public float[] shadowOffset;
    public SystemHAlignment textAlignment = SystemHAlignment.ALIGN_H_CENTER;
    public float[] textColor;

    TextAttributes() {
    }

    /* access modifiers changed from: package-private */
    public void UpdateWith(TextAttributes textAttributes) {
        this.fontName = textAttributes.fontName;
        this.hasBackground = textAttributes.hasBackground;
        this.hasShadow = textAttributes.hasShadow;
        this.adjustFontSizeToFitWidth = textAttributes.adjustFontSizeToFitWidth;
        this.ignoreMarkupOptimization = textAttributes.ignoreMarkupOptimization;
        this.scale = textAttributes.scale;
        this.maxNumberOfLines = textAttributes.maxNumberOfLines;
        this.textAlignment = textAttributes.textAlignment;
        this.textColor = textAttributes.textColor;
        this.bgColor = textAttributes.bgColor;
        this.bgCornerRadius = textAttributes.bgCornerRadius;
        this.shadowColor = textAttributes.shadowColor;
        this.shadowOffset = textAttributes.shadowOffset;
    }
}
