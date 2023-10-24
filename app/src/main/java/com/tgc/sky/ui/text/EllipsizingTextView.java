package com.tgc.sky.ui.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/* renamed from: com.tgc.sky.ui.text.EllipsizingTextView */
public class EllipsizingTextView extends androidx.appcompat.widget.AppCompatTextView {
    private boolean isEllipsized;
    private boolean isStale;
    private final List<EllipsizeListener> mEllipsizeListeners;
    private EllipsizeStrategy mEllipsizeStrategy;
    private Pattern mEndPunctPattern;
    private CharSequence mFullText;
    private float mLineAddVertPad;
    private float mLineSpacingMult;
    private int mMaxLines;
    private boolean programmaticChange;
    private static final CharSequence ELLIPSIS = "…";
    private static final Pattern DEFAULT_END_PUNCTUATION = Pattern.compile("[\\.!?,;:…]*$", 32);

    /* renamed from: com.tgc.sky.ui.text.EllipsizingTextView$EllipsizeListener */
    public interface EllipsizeListener {
        void ellipsizeStateChanged(boolean z);
    }

    public EllipsizingTextView(Context context) {
        this(context, null);
    }

    public EllipsizingTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842884);
    }

    public EllipsizingTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mEllipsizeListeners = new ArrayList();
        this.mLineSpacingMult = 1.0f;
        this.mLineAddVertPad = 0.0f;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, new int[]{16843091}, i, 0);
        setMaxLines(obtainStyledAttributes.getInt(0, Integer.MAX_VALUE));
        obtainStyledAttributes.recycle();
        setEndPunctuationPattern(DEFAULT_END_PUNCTUATION);
    }

    public void setEndPunctuationPattern(Pattern pattern) {
        this.mEndPunctPattern = pattern;
    }

    public void addEllipsizeListener(EllipsizeListener ellipsizeListener) {
        this.mEllipsizeListeners.add(ellipsizeListener);
    }

    public void removeEllipsizeListener(EllipsizeListener ellipsizeListener) {
        this.mEllipsizeListeners.remove(ellipsizeListener);
    }

    public boolean isEllipsized() {
        return this.isEllipsized;
    }

    @Override // android.widget.TextView
    public int getMaxLines() {
        return this.mMaxLines;
    }

    @Override // android.widget.TextView
    public void setMaxLines(int i) {
        super.setMaxLines(i);
        this.mMaxLines = i;
        this.isStale = true;
    }

    public boolean ellipsizingLastFullyVisibleLine() {
        return this.mMaxLines == Integer.MAX_VALUE;
    }

    @Override // android.widget.TextView
    public void setLineSpacing(float f, float f2) {
        this.mLineAddVertPad = f;
        this.mLineSpacingMult = f2;
        super.setLineSpacing(f, f2);
    }

    @Override // android.widget.TextView
    public void setText(CharSequence charSequence, TextView.BufferType bufferType) {
        if (!this.programmaticChange) {
            this.mFullText = charSequence;
            this.isStale = true;
        }
        super.setText(charSequence, bufferType);
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (ellipsizingLastFullyVisibleLine()) {
            this.isStale = true;
        }
    }

    @Override // android.widget.TextView, android.view.View
    public void setPadding(int i, int i2, int i3, int i4) {
        super.setPadding(i, i2, i3, i4);
        if (ellipsizingLastFullyVisibleLine()) {
            this.isStale = true;
        }
    }

    @Override // android.widget.TextView, android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.isStale) {
            resetText();
        }
        super.onDraw(canvas);
    }

    private void resetText() {
        boolean z;
        int maxLines = getMaxLines();
        CharSequence charSequence = this.mFullText;
        if (maxLines != -1) {
            if (this.mEllipsizeStrategy == null) {
                setEllipsize(null);
            }
            charSequence = this.mEllipsizeStrategy.processText(this.mFullText);
            z = !this.mEllipsizeStrategy.isInLayout(this.mFullText);
        } else {
            z = false;
        }
        if (!charSequence.equals(getText())) {
            this.programmaticChange = true;
            try {
                setText(charSequence);
            } finally {
                this.programmaticChange = false;
            }
        }
        this.isStale = false;
        if (z != this.isEllipsized) {
            this.isEllipsized = z;
            for (EllipsizeListener ellipsizeListener : this.mEllipsizeListeners) {
                ellipsizeListener.ellipsizeStateChanged(z);
            }
        }
    }

    @Override // android.widget.TextView
    public void setEllipsize(TextUtils.TruncateAt truncateAt) {
        if (truncateAt == null) {
            this.mEllipsizeStrategy = new EllipsizeNoneStrategy(this, null);
            return;
        }
        int i = truncateAt.ordinal();
        if (i == 1) {
            this.mEllipsizeStrategy = new EllipsizeEndStrategy(this, null);
        } else if (i == 2) {
            this.mEllipsizeStrategy = new EllipsizeStartStrategy(this, null);
        } else if (i == 3) {
            this.mEllipsizeStrategy = new EllipsizeMiddleStrategy(this, null);
        } else {
            if (i == 4) {
                super.setEllipsize(truncateAt);
                this.isStale = false;
            }
            this.mEllipsizeStrategy = new EllipsizeNoneStrategy(this, null);
        }
    }

    /* Access modifiers changed from: package-private */
    /* renamed from: com.tgc.sky.ui.text.EllipsizingTextView$1 */
    public static /* synthetic */ class C12881 {
        static final /* synthetic */ int[] $SwitchMap$android$text$TextUtils$TruncateAt;

        static {
            int[] iArr = new int[TextUtils.TruncateAt.values().length];
            $SwitchMap$android$text$TextUtils$TruncateAt = iArr;
            try {
                iArr[TextUtils.TruncateAt.END.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$android$text$TextUtils$TruncateAt[TextUtils.TruncateAt.START.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$android$text$TextUtils$TruncateAt[TextUtils.TruncateAt.MIDDLE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$android$text$TextUtils$TruncateAt[TextUtils.TruncateAt.MARQUEE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    /*Access modifiers changed from: private */
    /* renamed from: com.tgc.sky.ui.text.EllipsizingTextView$EllipsizeStrategy */
    public abstract class EllipsizeStrategy {
        protected abstract CharSequence createEllipsizedText(CharSequence charSequence);

        private EllipsizeStrategy() {
        }

        /* synthetic */ EllipsizeStrategy(EllipsizingTextView ellipsizingTextView, C12881 c12881) {
            this();
        }

        public CharSequence processText(CharSequence charSequence) {
            return !isInLayout(charSequence) ? createEllipsizedText(charSequence) : charSequence;
        }

        public boolean isInLayout(CharSequence charSequence) {
            return createWorkingLayout(charSequence).getLineCount() <= getLinesCount();
        }

        protected Layout createWorkingLayout(CharSequence charSequence) {
            return new StaticLayout(charSequence, EllipsizingTextView.this.getPaint(), (EllipsizingTextView.this.getMeasuredWidth() - EllipsizingTextView.this.getPaddingLeft()) - EllipsizingTextView.this.getPaddingRight(), Layout.Alignment.ALIGN_NORMAL, EllipsizingTextView.this.mLineSpacingMult, EllipsizingTextView.this.mLineAddVertPad, false);
        }

        protected int getLinesCount() {
            if (!EllipsizingTextView.this.ellipsizingLastFullyVisibleLine()) {
                return EllipsizingTextView.this.mMaxLines;
            }
            int fullyVisibleLinesCount = getFullyVisibleLinesCount();
            if (fullyVisibleLinesCount == -1) {
                return 1;
            }
            return fullyVisibleLinesCount;
        }

        protected int getFullyVisibleLinesCount() {
            return ((EllipsizingTextView.this.getHeight() - EllipsizingTextView.this.getCompoundPaddingTop()) - EllipsizingTextView.this.getCompoundPaddingBottom()) / createWorkingLayout("").getLineBottom(0);
        }
    }

    /* Access modifiers changed from: private */
    /* renamed from: com.tgc.sky.ui.text.EllipsizingTextView$EllipsizeNoneStrategy */
    public class EllipsizeNoneStrategy extends EllipsizeStrategy {
        @Override // com.tgc.sky.p013ui.text.EllipsizingTextView.EllipsizeStrategy
        protected CharSequence createEllipsizedText(CharSequence charSequence) {
            return charSequence;
        }

        private EllipsizeNoneStrategy() {
            super(EllipsizingTextView.this, null);
        }

        /* synthetic */ EllipsizeNoneStrategy(EllipsizingTextView ellipsizingTextView, C12881 c12881) {
            this();
        }
    }

    /*Access modifiers changed from: private */
    /* renamed from: com.tgc.sky.ui.text.EllipsizingTextView$EllipsizeEndStrategy */
    public class EllipsizeEndStrategy extends EllipsizeStrategy {
        private EllipsizeEndStrategy() {
            super(EllipsizingTextView.this, null);
        }

        /* synthetic */ EllipsizeEndStrategy(EllipsizingTextView ellipsizingTextView, C12881 c12881) {
            this();
        }

        @Override // com.tgc.sky.p013ui.text.EllipsizingTextView.EllipsizeStrategy
        protected CharSequence createEllipsizedText(CharSequence charSequence) {
            int lastIndexOf;
            int lineEnd = createWorkingLayout(charSequence).getLineEnd(EllipsizingTextView.this.mMaxLines - 1);
            int length = charSequence.length();
            int i = length - lineEnd;
            if (i < EllipsizingTextView.ELLIPSIS.length()) {
                i = EllipsizingTextView.ELLIPSIS.length();
            }
            String trim = TextUtils.substring(charSequence, 0, length - i).trim();
            String stripEndPunctuation = stripEndPunctuation(trim);
            while (!isInLayout(stripEndPunctuation + ((Object) EllipsizingTextView.ELLIPSIS)) && (lastIndexOf = trim.lastIndexOf(32)) != -1) {
                trim = trim.substring(0, lastIndexOf).trim();
                stripEndPunctuation = stripEndPunctuation(trim);
            }
            String str = stripEndPunctuation + ((Object) EllipsizingTextView.ELLIPSIS);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
            if (charSequence instanceof Spanned) {
                TextUtils.copySpansFrom((Spanned) charSequence, 0, str.length(), null, spannableStringBuilder, 0);
            }
            return spannableStringBuilder;
        }

        public String stripEndPunctuation(CharSequence charSequence) {
            return EllipsizingTextView.this.mEndPunctPattern.matcher(charSequence).replaceFirst("");
        }
    }

    /*Access modifiers changed from: private */
    /* renamed from: com.tgc.sky.ui.text.EllipsizingTextView$EllipsizeStartStrategy */
    public class EllipsizeStartStrategy extends EllipsizeStrategy {
        private EllipsizeStartStrategy() {
            super(EllipsizingTextView.this, null);
        }

        /* synthetic */ EllipsizeStartStrategy(EllipsizingTextView ellipsizingTextView, C12881 c12881) {
            this();
        }

        @Override // com.tgc.sky.p013ui.text.EllipsizingTextView.EllipsizeStrategy
        protected CharSequence createEllipsizedText(CharSequence charSequence) {
            int indexOf;
            int lineEnd = createWorkingLayout(charSequence).getLineEnd(EllipsizingTextView.this.mMaxLines - 1);
            int length = charSequence.length();
            int i = length - lineEnd;
            if (i < EllipsizingTextView.ELLIPSIS.length()) {
                i = EllipsizingTextView.ELLIPSIS.length();
            }
            String trim = TextUtils.substring(charSequence, i, length).trim();
            while (!isInLayout(((Object) EllipsizingTextView.ELLIPSIS) + trim) && (indexOf = trim.indexOf(32)) != -1) {
                trim = trim.substring(indexOf, trim.length()).trim();
            }
            String str = ((Object) EllipsizingTextView.ELLIPSIS) + trim;
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
            if (charSequence instanceof Spanned) {
                TextUtils.copySpansFrom((Spanned) charSequence, length - str.length(), length, null, spannableStringBuilder, 0);
            }
            return spannableStringBuilder;
        }
    }

    /* Access modifiers changed from: private */
    /* renamed from: com.tgc.sky.ui.text.EllipsizingTextView$EllipsizeMiddleStrategy */

    public class EllipsizeMiddleStrategy extends EllipsizeStrategy {
        private EllipsizeMiddleStrategy() {
            super(EllipsizingTextView.this, null);
        }

        /* synthetic */ EllipsizeMiddleStrategy(EllipsizingTextView ellipsizingTextView, C12881 c12881) {
            this();
        }

        @Override // com.tgc.sky.p013ui.text.EllipsizingTextView.EllipsizeStrategy
        protected CharSequence createEllipsizedText(CharSequence charSequence) {
            SpannableStringBuilder spannableStringBuilder;
            int lineEnd = createWorkingLayout(charSequence).getLineEnd(EllipsizingTextView.this.mMaxLines - 1);
            int length = charSequence.length();
            int i = length - lineEnd;
            if (i < EllipsizingTextView.ELLIPSIS.length()) {
                i = EllipsizingTextView.ELLIPSIS.length();
            }
            int i2 = i + (lineEnd % 2);
            int i3 = length / 2;
            int i4 = i2 / 2;
            String trim = TextUtils.substring(charSequence, 0, i3 - i4).trim();
            String trim2 = TextUtils.substring(charSequence, i3 + i4, length).trim();
            while (!isInLayout(trim + ((Object) EllipsizingTextView.ELLIPSIS) + trim2)) {
                int lastIndexOf = trim.lastIndexOf(32);
                int indexOf = trim2.indexOf(32);
                if (lastIndexOf == -1 || indexOf == -1) {
                    break;
                }
                trim = trim.substring(0, lastIndexOf).trim();
                trim2 = trim2.substring(indexOf, trim2.length()).trim();
            }
            SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder(trim);
            SpannableStringBuilder spannableStringBuilder3 = new SpannableStringBuilder(trim2);
            if (charSequence instanceof Spanned) {
                Spanned spanned = (Spanned) charSequence;
                TextUtils.copySpansFrom(spanned, 0, trim.length(), null, spannableStringBuilder2, 0);
                spannableStringBuilder = spannableStringBuilder3;
                TextUtils.copySpansFrom(spanned, length - trim2.length(), length, null, spannableStringBuilder3, 0);
            } else {
                spannableStringBuilder = spannableStringBuilder3;
            }
            return TextUtils.concat(spannableStringBuilder2, EllipsizingTextView.ELLIPSIS, spannableStringBuilder);
        }
    }
}
