package com.tgc.sky.ui.text;

/* loaded from: classes2.dex */
public class TextAttributes {
    public float[] bgColor;
    public String fontName;
    public float[] shadowColor;
    public float[] shadowOffset;
    public float[] textColor;
    public boolean hasBackground = false;
    public boolean hasShadow = false;
    public boolean adjustFontSizeToFitWidth = false;
    public boolean ignoreMarkupOptimization = false;
    public float fontSize = 12.0f;
    public float scale = 1.0f;
    public int maxNumberOfLines = 0;
    public boolean truncateWithEllipses = false;
    public SystemHAlignment textAlignment = SystemHAlignment.ALIGN_H_CENTER;
    public float bgCornerRadius = 0.0f;

    /* JADX INFO: Access modifiers changed from: package-private */
    public void UpdateWith(TextAttributes textAttributes) {
        this.fontName = textAttributes.fontName;
        this.hasBackground = textAttributes.hasBackground;
        this.hasShadow = textAttributes.hasShadow;
        this.adjustFontSizeToFitWidth = textAttributes.adjustFontSizeToFitWidth;
        this.ignoreMarkupOptimization = textAttributes.ignoreMarkupOptimization;
        this.scale = textAttributes.scale;
        this.maxNumberOfLines = textAttributes.maxNumberOfLines;
        this.truncateWithEllipses = textAttributes.truncateWithEllipses;
        this.textAlignment = textAttributes.textAlignment;
        this.textColor = textAttributes.textColor;
        this.bgColor = textAttributes.bgColor;
        this.bgCornerRadius = textAttributes.bgCornerRadius;
        this.shadowColor = textAttributes.shadowColor;
        this.shadowOffset = textAttributes.shadowOffset;
    }
}
