package com.chinalwb.are.spans;

import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;

public class AreFontSizeSpan extends AbsoluteSizeSpan implements AreDynamicSpan {

    public static float sCurrentScaleFactor = 1.0f;

    public AreFontSizeSpan(int size) {
        super(size, true);
    }

    @Override
    public int getDynamicFeature() {
        return this.getSize();
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        float size = getSize();
        if (getDip()) {
            size = size * ds.density;
        }
        size = size * sCurrentScaleFactor;
        ds.setTextSize(size);
    }

    @Override
    public void updateMeasureState(TextPaint ds) {
        float size = getSize();
        if (getDip()) {
            size = size * ds.density;
        }
        size = size * sCurrentScaleFactor;
        ds.setTextSize(size);
    }
}
