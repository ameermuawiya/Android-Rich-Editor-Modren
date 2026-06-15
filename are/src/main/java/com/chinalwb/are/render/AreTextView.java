package com.chinalwb.are.render;

import android.content.Context;
import androidx.annotation.Nullable;
import com.google.android.material.textview.MaterialTextView;

import android.text.Spannable;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;

import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.android.inner.Html;
import com.chinalwb.are.events.AREMovementMethod;
import com.chinalwb.are.strategies.AreClickStrategy;
import com.chinalwb.are.strategies.defaults.DefaultClickStrategy;

import java.util.HashMap;

public class AreTextView extends MaterialTextView {

    private static HashMap<String, Spanned> spannedHashMap = new HashMap<>();
    private static HashMap<String, Float> zoomHistory = new HashMap<>();

    private AreClickStrategy mClickStrategy;
    Context mContext;

    private boolean zoomEnabled = true;
    private float minZoom = 0.75f;
    private float maxZoom = 3.0f;
    private float scaleFactor = 1f;
    private float baseTextSizePx;
    private ScaleGestureDetector scaleDetector;
    private boolean isZooming = false;

    public AreTextView(Context context) {
        this(context, null);
    }

    public AreTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AreTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initGlobalValues();
        initMovementMethod();
        initZoom(context, attrs);
    }

    private void initZoom(Context context, AttributeSet attrs) {
        try {
            if (attrs != null) {
                zoomEnabled = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto", "zoomEnabled", true);
                minZoom = attrs.getAttributeFloatValue("http://schemas.android.com/apk/res-auto", "minZoom", 0.75f);
                maxZoom = attrs.getAttributeFloatValue("http://schemas.android.com/apk/res-auto", "maxZoom", 3.0f);
            }
        } catch (Exception e) {
            Log.e("AreTextView", "Error reading attributes", e);
        }

        if (zoomEnabled) {
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, Constants.DEFAULT_FONT_SIZE);
        }
        baseTextSizePx = getTextSize();

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                isZooming = true;
                ViewGroup scrollParent = getScrollableParent();
                if (scrollParent != null) {
                    scrollParent.requestDisallowInterceptTouchEvent(true);
                } else if (getParent() instanceof ViewGroup) {
                    ((ViewGroup) getParent()).requestDisallowInterceptTouchEvent(true);
                }
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isZooming = false;
                ViewGroup scrollParent = getScrollableParent();
                if (scrollParent != null) {
                    scrollParent.requestDisallowInterceptTouchEvent(false);
                } else if (getParent() instanceof ViewGroup) {
                    ((ViewGroup) getParent()).requestDisallowInterceptTouchEvent(false);
                }
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                try {
                    if (!zoomEnabled) return false;
                    if (hasTextSelection()) return false;

                    float oldScale = scaleFactor;
                    scaleFactor *= detector.getScaleFactor();
                    scaleFactor = Math.max(minZoom, Math.min(maxZoom, scaleFactor));

                    ViewGroup scrollParent = getScrollableParent();
                    int oldScrollY = scrollParent != null ? scrollParent.getScrollY() : 0;

                    setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSizePx * scaleFactor);
                    saveZoomHistory();

                    if (scrollParent != null) {
                        float ratio = scaleFactor / oldScale;
                        int newScrollY = (int) (oldScrollY * ratio);
                        int maxScroll = scrollParent.getChildAt(0).getHeight() - scrollParent.getHeight();
                        newScrollY = Math.max(0, Math.min(newScrollY, maxScroll));
                        final int finalY = newScrollY;
                        scrollParent.post(() -> scrollParent.setScrollY(finalY));
                    }
                    return true;
                } catch (Exception e) {
                    Log.e("AreTextView", "Error in onScale", e);
                    return false;
                }
            }
        });
    }

    private ViewGroup getScrollableParent() {
        ViewParent p = getParent();
        while (p != null) {
            if (p instanceof ScrollView || p instanceof NestedScrollView) {
                return (ViewGroup) p;
            }
            p = p.getParent();
        }
        return null;
    }

    private boolean hasTextSelection() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        return start >= 0 && end > start;
    }

    private String getZoomKey() {
        try {
            String contextName = mContext.getClass().getName();
            int id = getId();
            return contextName + "_" + id;
        } catch (Exception e) {
            Log.e("AreTextView", "Error generating zoom key", e);
            return null;
        }
    }

    private void saveZoomHistory() {
        try {
            String key = getZoomKey();
            if (key != null) {
                zoomHistory.put(key, scaleFactor);
            }
        } catch (Exception e) {
            Log.e("AreTextView", "Error saving zoom history", e);
        }
    }

    private void restoreZoomHistory() {
        try {
            String key = getZoomKey();
            if (key != null && zoomHistory.containsKey(key)) {
                scaleFactor = zoomHistory.get(key);
                setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSizePx * scaleFactor);
            }
        } catch (Exception e) {
            Log.e("AreTextView", "Error restoring zoom history", e);
        }
    }

    private void initGlobalValues() {
        try {
            int[] wh = Util.getScreenWidthAndHeight(mContext);
            Constants.SCREEN_WIDTH = wh[0];
            Constants.SCREEN_HEIGHT = wh[1];
        } catch (Exception e) {
            Log.e("AreTextView", "Error initializing global values", e);
        }
    }

    private void initMovementMethod() {
        try {
            if (this.mClickStrategy == null) {
                this.mClickStrategy = new DefaultClickStrategy();
            }
            this.setMovementMethod(new AREMovementMethod(this.mClickStrategy));
        } catch (Exception e) {
            Log.e("AreTextView", "Error initializing movement method", e);
        }
    }

    public void fromHtml(String html) {
        try {
            Spanned spanned = getSpanned(html);
            setText(spanned);
        } catch (Exception e) {
            Log.e("AreTextView", "Error setting html text", e);
        }
    }

    private Spanned getSpanned(String html) {
        try {
            Html.sContext = mContext;
            Html.ImageGetter imageGetter = new AreImageGetter(mContext, this);
            Html.TagHandler tagHandler = new AreTagHandler();
            Spanned spanned = Html.fromHtml(html, Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH, imageGetter, tagHandler);
            
            if (spanned instanceof Spannable) {
                Spannable spannable = (Spannable) spanned;
                android.text.style.QuoteSpan[] quoteSpans = spannable.getSpans(0, spannable.length(), android.text.style.QuoteSpan.class);
                int dynamicQuoteColor = Constants.getQuoteColor(mContext);
                for (android.text.style.QuoteSpan span : quoteSpans) {
                    int start = spannable.getSpanStart(span);
                    int end = spannable.getSpanEnd(span);
                    int flags = spannable.getSpanFlags(span);
                    spannable.removeSpan(span);
                    spannable.setSpan(new com.chinalwb.are.spans.AreQuoteSpan(dynamicQuoteColor), start, end, flags);
                }

                android.text.style.BackgroundColorSpan[] bgSpans = spannable.getSpans(0, spannable.length(), android.text.style.BackgroundColorSpan.class);
                int dynamicHighlightColor = Constants.getDefaultHighlightColor(mContext);
                for (android.text.style.BackgroundColorSpan span : bgSpans) {
                    int start = spannable.getSpanStart(span);
                    int end = spannable.getSpanEnd(span);
                    int flags = spannable.getSpanFlags(span);
                    spannable.removeSpan(span);
                    spannable.setSpan(new android.text.style.BackgroundColorSpan(dynamicHighlightColor), start, end, flags);
                }
            }
            return spanned;
        } catch (Exception e) {
            Log.e("AreTextView", "Error getting spanned text", e);
            return Spannable.Factory.getInstance().newSpannable("");
        }
    }

    public void fromHtmlWithCache(String html) {
        try {
            Spanned spanned = null;
            if (spannedHashMap.containsKey(html)) {
                spanned = spannedHashMap.get(html);
            }
            if (spanned == null) {
                spanned = getSpanned(html);
                spannedHashMap.put(html, spanned);
            }
            if (spanned != null) {
                setText(spanned);
            }
        } catch (Exception e) {
            Log.e("AreTextView", "Error setting cached html text", e);
        }
    }

    public static void clearCache() {
        try {
            spannedHashMap.clear();
        } catch (Exception e) {
            Log.e("AreTextView", "Error clearing cache", e);
        }
    }

    public void setClickStrategy(AreClickStrategy clickStrategy) {
        try {
            this.mClickStrategy = clickStrategy;
            this.setMovementMethod(new AREMovementMethod(this.mClickStrategy));
        } catch (Exception e) {
            Log.e("AreTextView", "Error setting click strategy", e);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            restoreZoomHistory();
            post(() -> {
                try {
                    requestLayout();
                    invalidate();
                } catch (Exception e) {
                    Log.e("AreTextView", "Error refreshing layout", e);
                }
            });
        } catch (Exception e) {
            Log.e("AreTextView", "Error in onAttachedToWindow", e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (zoomEnabled && (event.getPointerCount() >= 2 || isZooming)) {
                if (hasTextSelection()) {
                    return super.onTouchEvent(event);
                }
                scaleDetector.onTouchEvent(event);
                return true;
            }
            return super.onTouchEvent(event);
        } catch (Exception e) {
            Log.e("AreTextView", "Error handling touch event", e);
            return false;
        }
    }
}