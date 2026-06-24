package com.muawiya.are.spans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ImageSpan;

public class AreImageSpan extends ImageSpan implements ARE_Clickable_Span {

    public enum ImageType {
        URI,
        URL,
        RES
    }

    private Context mContext;

    private Uri mUri;

    private String mUrl;

    private int mResId;

    public AreImageSpan(Context context, Bitmap bitmap, Uri uri) {
        super(context, bitmap);
        this.mContext = context;
        this.mUri = uri;
    }

    public AreImageSpan(Context context, Bitmap bitmap, String url) {
        super(context, bitmap);
        this.mContext = context;
        this.mUrl = url;
    }

    public AreImageSpan(Context context, Drawable drawable, String url) {
        super(drawable, url);
        this.mContext = context;
        this.mUrl = url;
    }

    public AreImageSpan(Context context, int resId) {
        super(context, resId);
        this.mContext = context;
        this.mResId = resId;
    }

    public AreImageSpan(Context context, Uri uri) {
        super(context, uri);
        this.mContext = context;
        this.mUri = uri;
    }

    @Override
    public String getSource() {
        if (mUri != null) {
            return mUri.toString();
        }
        if (mUrl != null) {
            return mUrl;
        }
        return String.valueOf(mResId);
    }

    public ImageType getImageType() {
        if (mUri != null) {
            return ImageType.URI;
        }
        if (mUrl != null) {
            return ImageType.URL;
        }
        return ImageType.RES;
    }

    public Uri getUri() {
        return mUri;
    }

    public String getURL() {
        return mUrl;
    }

    public int getResId() {
        return mResId;
    }
}