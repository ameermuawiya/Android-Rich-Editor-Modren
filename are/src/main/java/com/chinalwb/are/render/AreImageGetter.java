package com.chinalwb.are.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.TextView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.Util;
import com.chinalwb.are.android.inner.Html;

public class AreImageGetter implements Html.ImageGetter {

    private Context mContext;
    private TextView mTextView;

    public AreImageGetter(Context context, TextView textView) {
        mContext = context;
        mTextView = textView;
    }

    /*
    Returns a drawable for the given image source.
    Sets a placeholder immediately to prevent blank spaces,
    then loads the actual image asynchronously.
    */
    @Override
    public Drawable getDrawable(String source) {
        if (source == null || source.length() == 0) return null;

        final AreUrlDrawable urlDrawable = new AreUrlDrawable(mContext);
        
        Bitmap placeholder = Util.getImagePlaceholderBitmap(mContext);
        setBitmapToDrawable(placeholder, urlDrawable);

        Object mediaSource = source.startsWith("content") ? Uri.parse(source) : source;
        
        Util.loadImageAsynchronously(mContext, mediaSource, new Util.ImageLoadCallback() {
            @Override
            public void onImageReady(Bitmap bitmap) {
                updateTextView(bitmap, urlDrawable);
            }
            @Override
            public void onError(Bitmap errorBitmap) {
                updateTextView(errorBitmap, urlDrawable);
            }
        });

        return urlDrawable;
    }

    private void setBitmapToDrawable(Bitmap bitmap, AreUrlDrawable urlDrawable) {
        if (bitmap == null) return;
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        BitmapDrawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
        bitmapDrawable.setBounds(rect);
        urlDrawable.setBounds(rect);
        urlDrawable.setDrawable(bitmapDrawable);
    }

    private void updateTextView(final Bitmap bitmap, final AreUrlDrawable urlDrawable) {
        if (mTextView == null) return;
        mTextView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    setBitmapToDrawable(bitmap, urlDrawable);
                    
                    // Force layout engine recalculation to naturally adapt text bounds around the new image dimensions,
                    // preserving cursor position, selection, and scrolling position flawlessly.
                    float spacingExtra = mTextView.getLineSpacingExtra();
                    float spacingMultiplier = mTextView.getLineSpacingMultiplier();
                    mTextView.setLineSpacing(spacingExtra + 0.1f, spacingMultiplier);
                    mTextView.setLineSpacing(spacingExtra, spacingMultiplier);
                    
                    mTextView.requestLayout();
                    mTextView.invalidate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}