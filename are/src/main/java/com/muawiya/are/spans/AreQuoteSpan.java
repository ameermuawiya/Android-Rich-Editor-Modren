package com.muawiya.are.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.QuoteSpan;

public class AreQuoteSpan extends QuoteSpan {

    private int mQuoteColor;

    /*
    Default constructor fallback.
    Sets a standard gray color in case it is instantiated without a specific context.
    */
    public AreQuoteSpan() {
        super();
        this.mQuoteColor = 0Xffcccccc; 
    }

    /*
    Constructor that accepts a dynamic theme color.
    This color is applied to the vertical stripe of the blockquote.
    */
    public AreQuoteSpan(int color) {
        super(color);
        this.mQuoteColor = color;
    }

    /*
    Returns the leading margin width for the blockquote.
    */
    @Override
    public int getLeadingMargin(boolean first) {
        return 45; 
    }

    /*
    Draws the vertical quote stripe using the dynamically assigned theme color.
    Ensures the original paint style is restored after drawing.
    */
    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        final int INDENT = 30;
        c.translate(INDENT, 0);
        Paint.Style style = p.getStyle();
        int originalColor = p.getColor();

        p.setStyle(Paint.Style.FILL);
        
        // Use the dynamically injected color from Constants
        p.setColor(this.mQuoteColor);

        c.drawRect(x, top, x + dir * 2 + 5, bottom, p);

        p.setStyle(style);
        p.setColor(originalColor);
        c.translate(-INDENT, 0);
    }
}
