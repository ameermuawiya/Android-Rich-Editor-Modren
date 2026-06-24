package com.chinalwb.are.events;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.chinalwb.are.spans.ARE_Clickable_Span;
import com.chinalwb.are.spans.AreMentionSpan;
import com.chinalwb.are.spans.AreImageSpan;
import com.chinalwb.are.spans.AreUrlSpan;
import com.chinalwb.are.spans.AreVideoSpan;
import com.chinalwb.are.strategies.AreClickStrategy;

public class AREMovementMethod extends ArrowKeyMovementMethod {

    private AreClickStrategy mAreClickStrategy;

    /*
     * Initializes the movement method without a specific click strategy.
     */
    public AREMovementMethod() {
        this(null);
    }

    /*
     * Initializes the movement method with a defined click strategy.
     */
    public AREMovementMethod(AreClickStrategy areClickStrategy) {
        this.mAreClickStrategy = areClickStrategy;
    }

    /*
     * Calculates and returns the exact text offset position from the touch event.
     */
    public static int getTextOffset(TextView widget, Spannable buffer, MotionEvent event) {
        try {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            return layout.getOffsetForHorizontal(line, x);
        } catch (Exception e) {
            Log.e("AREMovementMethod", "Error getting text offset", e);
            return -1;
        }
    }

    /*
     * Processes touch events for clicking elements within the text view.
     * Prevents clicking if text is actively selected.
     */
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        try {
            int action = event.getAction();

            int selStart = widget.getSelectionStart();
            int selEnd = widget.getSelectionEnd();
            boolean isLinkSelected = false;

            if (selStart != -1 && selEnd != -1 && selStart != selEnd) {
                int off = getTextOffset(widget, buffer, event);
                if (off != -1) {
                    ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
                    if (link.length != 0) {
                        int spanStart = buffer.getSpanStart(link[0]);
                        int spanEnd = buffer.getSpanEnd(link[0]);
                        if (selStart == spanStart && selEnd == spanEnd) {
                            isLinkSelected = true;
                        }
                    }
                }
                if (!isLinkSelected) {
                    return super.onTouchEvent(widget, buffer, event);
                }
            }

            if (action == MotionEvent.ACTION_UP) {
                int off = getTextOffset(widget, buffer, event);
                if (off == -1) return super.onTouchEvent(widget, buffer, event);

                ARE_Clickable_Span[] clickableSpans = buffer.getSpans(off, off, ARE_Clickable_Span.class);
                Context context = widget.getContext();
                boolean handled = false;
                
                if (mAreClickStrategy != null && clickableSpans != null && clickableSpans.length > 0) {
                    if (clickableSpans[0] instanceof AreMentionSpan) {
                        handled = mAreClickStrategy.onClickAt(context, (AreMentionSpan) clickableSpans[0]);
                    } else if (clickableSpans[0] instanceof AreImageSpan) {
                        handled = mAreClickStrategy.onClickImage(context, (AreImageSpan) clickableSpans[0]);
                    } else if (clickableSpans[0] instanceof AreVideoSpan) {
                        handled = mAreClickStrategy.onClickVideo(context, (AreVideoSpan) clickableSpans[0]);
                    } else if (clickableSpans[0] instanceof AreUrlSpan) {
                        handled = mAreClickStrategy.onClickUrl(context, (AreUrlSpan) clickableSpans[0]);
                    }
                }
                
                if (handled) {
                    android.text.Selection.removeSelection(buffer);
                    return true;
                }

                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
                if (link.length != 0) {
                    android.text.Selection.removeSelection(buffer);
                    if (link[0] instanceof URLSpan) {
                        try {
                            String url = ((URLSpan) link[0]).getURL();
                            if (url != null && !url.isEmpty()) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                return true;
                            }
                        } catch (Exception e) {
                            Log.e("AREMovementMethod", "Error launching URLSpan onClick", e);
                        }
                    }
                    link[0].onClick(widget);
                    return true;
                }
            } else if (action == MotionEvent.ACTION_DOWN) {
                int off = getTextOffset(widget, buffer, event);
                if (off != -1) {
                    ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
                    if (link.length != 0) {
                        android.text.Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                        return true;
                    }
                }
            }
            
            return super.onTouchEvent(widget, buffer, event);
        } catch (Exception e) {
            Log.e("AREMovementMethod", "Error handling touch event", e);
            return super.onTouchEvent(widget, buffer, event);
        }
    }

    /*
     * Sets or updates the click strategy.
     */
    public void setClickStrategy(AreClickStrategy areClickStrategy) {
        this.mAreClickStrategy = areClickStrategy;
    }
}
