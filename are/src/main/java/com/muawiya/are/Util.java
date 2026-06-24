package com.muawiya.are;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class Util {

    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void log(String s) {
        Log.d("CAKE", s);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
            return ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();
        }
        int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 1;
        int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 1;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static int getCurrentCursorLine(EditText editText) {
        int selectionStart = Selection.getSelectionStart(editText.getText());
        Layout layout = editText.getLayout();
        if (null == layout) return -1;
        if (selectionStart != -1) return layout.getLineForOffset(selectionStart);
        return -1;
    }

    public static int getThisLineStart(EditText editText, int currentLine) {
        Layout layout = editText.getLayout();
        int start = 0;
        if (currentLine > 0) {
            start = layout.getLineStart(currentLine);
            if (start > 0) {
                String text = editText.getText().toString();
                char lastChar = text.charAt(start - 1);
                while (lastChar != '\n') {
                    if (currentLine > 0) {
                        currentLine--;
                        start = layout.getLineStart(currentLine);
                        if (start > 1) {
                            start--;
                            lastChar = text.charAt(start);
                        } else break;
                    }
                }
            }
        }
        return start;
    }

    public static int getThisLineEnd(EditText editText, int currentLine) {
        Layout layout = editText.getLayout();
        if (-1 != currentLine) return layout.getLineEnd(currentLine);
        return -1;
    }

    public static int getPixelByDp(Context context, int dp) {
        if (context == null) return dp * 2;
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * dp + 0.5f);
    }

    public static int[] getScreenWidthAndHeight(Context context) {
        if (context == null) return new int[]{1080, 1920};
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return new int[]{metrics.widthPixels, metrics.heightPixels};
    }

    public static Bitmap scaleBitmapToFitWidth(Bitmap bitmap, int maxWidth) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int newHeight = maxWidth * h / w;
        if (w < maxWidth * 0.2) return bitmap;
        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
    }

    public interface ImageLoadCallback {
        void onImageReady(Bitmap bitmap);
        void onError(Bitmap errorBitmap);
    }

    public static Bitmap getImagePlaceholderBitmap(Context context) {
        int[] dimen = getScreenWidthAndHeight(context);
        int targetWidth = dimen[0] > 0 ? (dimen[0] - getPixelByDp(context, 48)) : 800;
        int targetHeight = (int) (targetWidth * 9.0f / 16.0f);
        Bitmap output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Constants.getImagePlaceholderColor(context)); 
        
        float radiusPx = getPixelByDp(context, 8);
        canvas.drawRoundRect(new RectF(0, 0, targetWidth, targetHeight), radiusPx, radiusPx, bgPaint);
        
        drawCenterIcon(context, canvas, targetWidth, targetHeight, R.drawable.ic_image, Constants.getPlaceholderIconColor(context));
        return output;
    }

    public static Bitmap getRoundedImageBitmap(Context context, Bitmap originalBitmap) {
        if (originalBitmap == null) return getImageErrorBitmap(context);
        
        Bitmap safeBitmap = originalBitmap;
        if (originalBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            Bitmap temp = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (temp != null) {
                safeBitmap = temp;
            }
        }

        int[] dimen = getScreenWidthAndHeight(context);
        int targetWidth = dimen[0] > 0 ? (dimen[0] - getPixelByDp(context, 48)) : 800;
        float ratio = (float) safeBitmap.getHeight() / safeBitmap.getWidth();
        int targetHeight = (int) (targetWidth * ratio);

        Bitmap scaled = Bitmap.createScaledBitmap(safeBitmap, targetWidth, targetHeight, true);
        Bitmap output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        float radiusPx = getPixelByDp(context, 8);
        canvas.drawRoundRect(new RectF(0, 0, targetWidth, targetHeight), radiusPx, radiusPx, paint);
        return output;
    }

    public static Bitmap getImageErrorBitmap(Context context) {
        int[] dimen = getScreenWidthAndHeight(context);
        int targetWidth = dimen[0] > 0 ? (dimen[0] - getPixelByDp(context, 48)) : 800;
        int targetHeight = (int) (targetWidth * 9.0f / 16.0f);
        Bitmap output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Constants.getErrorBackgroundColor(context)); 
        
        float radiusPx = getPixelByDp(context, 8);
        canvas.drawRoundRect(new RectF(0, 0, targetWidth, targetHeight), radiusPx, radiusPx, bgPaint);
        
        drawCenterIcon(context, canvas, targetWidth, targetHeight, R.drawable.ic_broken_image, Constants.getErrorIconColor(context));
        return output;
    }

    public static void loadImageAsynchronously(final Context context, final Object source, final ImageLoadCallback callback) {
        Glide.with(context).asBitmap().load(source).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                callback.onImageReady(getRoundedImageBitmap(context, resource));
            }
            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                callback.onError(getImageErrorBitmap(context));
            }
            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {}
        });
    }

    public interface VideoLoadCallback {
        void onVideoReady(Bitmap bitmap);
        void onError(Bitmap errorBitmap);
    }

    public static Bitmap getVideoPlaceholderBitmap(Context context) {
        int[] dimen = getScreenWidthAndHeight(context);
        int targetWidth = dimen[0] > 0 ? (dimen[0] - getPixelByDp(context, 48)) : 800;
        int targetHeight = (int) (targetWidth * 9.0f / 16.0f);
        Bitmap output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Constants.getVideoPlaceholderColor(context)); 
        
        float radiusPx = getPixelByDp(context, 8);
        canvas.drawRoundRect(new RectF(0, 0, targetWidth, targetHeight), radiusPx, radiusPx, bgPaint);
        
        drawPlayButtonIcon(context, canvas, targetWidth, targetHeight);
        return output;
    }

    public static Bitmap getRoundedVideoBitmap(Context context, Bitmap originalBitmap) {
        if (originalBitmap == null) return getVideoErrorBitmap(context);
        
        Bitmap safeBitmap = originalBitmap;
        if (originalBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            Bitmap temp = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (temp != null) {
                safeBitmap = temp;
            }
        }

        int[] dimen = getScreenWidthAndHeight(context);
        int targetWidth = dimen[0] > 0 ? (dimen[0] - getPixelByDp(context, 48)) : 800;
        float ratio = (float) safeBitmap.getHeight() / safeBitmap.getWidth();
        int targetHeight = (int) (targetWidth * ratio);

        Bitmap scaled = Bitmap.createScaledBitmap(safeBitmap, targetWidth, targetHeight, true);
        Bitmap output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        float radiusPx = getPixelByDp(context, 8);
        canvas.drawRoundRect(new RectF(0, 0, targetWidth, targetHeight), radiusPx, radiusPx, paint);
        
        drawPlayButtonIcon(context, canvas, targetWidth, targetHeight);
        return output;
    }

    public static Bitmap getVideoErrorBitmap(Context context) {
        int[] dimen = getScreenWidthAndHeight(context);
        int targetWidth = dimen[0] > 0 ? (dimen[0] - getPixelByDp(context, 48)) : 800;
        int targetHeight = (int) (targetWidth * 9.0f / 16.0f);
        Bitmap output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Constants.getErrorBackgroundColor(context)); 
        
        float radiusPx = getPixelByDp(context, 8);
        canvas.drawRoundRect(new RectF(0, 0, targetWidth, targetHeight), radiusPx, radiusPx, bgPaint);
        
        drawCenterIcon(context, canvas, targetWidth, targetHeight, R.drawable.ic_broken_image, Constants.getErrorIconColor(context));
        return output;
    }

        /*
    Fetches video thumbnail asynchronously with a powerful local caching system.
    Instantly loads previously generated thumbnails without freezing the UI.
    */
    public static void loadVideoAsynchronously(final Context context, final Object source, final VideoLoadCallback callback) {
        new Thread(() -> {
            String sourceStr = source.toString();
            String hash = String.valueOf(sourceStr.hashCode());
            java.io.File cacheFile = new java.io.File(context.getCacheDir(), "are_video_thumb_" + hash + ".png");
            Bitmap thumb = null;

            // 1. Try to load instantly from local cache
            if (cacheFile.exists()) {
                thumb = android.graphics.BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
            }

            // 2. If not cached, generate using Metadata Retriever
            if (thumb == null) {
                try {
                    android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
                    if (sourceStr.startsWith("http")) {
                        retriever.setDataSource(sourceStr, new java.util.HashMap<>());
                    } else if (source instanceof Uri) {
                        retriever.setDataSource(context, (Uri) source);
                    } else {
                        retriever.setDataSource(sourceStr);
                    }
                    thumb = retriever.getFrameAtTime(1000000, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    retriever.release();

                    // 3. Save the newly generated thumbnail to local cache
                    if (thumb != null) {
                        java.io.FileOutputStream out = new java.io.FileOutputStream(cacheFile);
                        thumb.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }

            final Bitmap finalThumb = thumb;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalThumb != null) {
                    callback.onVideoReady(getRoundedVideoBitmap(context, finalThumb));
                } else {
                    callback.onError(getVideoErrorBitmap(context));
                }
            });
        }).start();
    }


    private static void drawPlayButtonIcon(Context context, Canvas canvas, int width, int height) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        float cx = width / 2f;
        float cy = height / 2f;
        float backgroundRadius = getPixelByDp(context, 24);

        paint.setColor(Constants.getPlayButtonBgColor(context)); 
        canvas.drawCircle(cx, cy, backgroundRadius, paint);
        
        try {
            android.graphics.drawable.Drawable playIcon = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_play);
            if (playIcon != null) {
                playIcon = playIcon.mutate();
                androidx.core.graphics.drawable.DrawableCompat.setTint(playIcon, Constants.getPlayButtonIconColor(context));
                
                int iconPadding = getPixelByDp(context, 6);
                int left = (int) (cx - backgroundRadius + iconPadding);
                int top = (int) (cy - backgroundRadius + iconPadding);
                int right = (int) (cx + backgroundRadius - iconPadding);
                int bottom = (int) (cy + backgroundRadius - iconPadding);
                
                playIcon.setBounds(left, top, right, bottom);
                playIcon.draw(canvas);
            }
        } catch (Exception ignored) {}
    }

    private static void drawCenterIcon(Context context, Canvas canvas, int width, int height, int drawableRes, int tintColor) {
        try {
            Drawable icon = ContextCompat.getDrawable(context, drawableRes);
            if (icon != null) {
                icon = icon.mutate();
                DrawableCompat.setTint(icon, tintColor);

                int iconW = icon.getIntrinsicWidth();
                int iconH = icon.getIntrinsicHeight();
                int left = (width - iconW) / 2;
                int top = (height - iconH) / 2;
                icon.setBounds(left, top, left + iconW, top + iconH);
                icon.draw(canvas);
            }
        } catch (Exception ignored) {}
    }

        /*
    Safely replaces a span by substituting the underlying text to fix overlap issues.
    Strictly preserves the user's exact cursor position and selection during the layout recalculation.
    */
    public static void replaceSpanSafely(EditText editText, Object oldSpan, Object newSpan) {
        if (editText == null) return;
        Editable editable = editText.getText();
        int start = editable.getSpanStart(oldSpan);
        int end = editable.getSpanEnd(oldSpan);
        
        if (start >= 0 && end >= 0) {
            int selStart = Selection.getSelectionStart(editable);
            int selEnd = Selection.getSelectionEnd(editable);
            int flags = editable.getSpanFlags(oldSpan);
            
            editable.removeSpan(oldSpan);
            CharSequence spanText = editable.subSequence(start, end);
            
            editable.replace(start, end, spanText);
            editable.setSpan(newSpan, start, end, flags);
            
            if (selStart >= 0 && selEnd >= 0) {
                int len = editable.length();
                Selection.setSelection(editable, Math.min(selStart, len), Math.min(selEnd, len));
            }
        }
    }

}
