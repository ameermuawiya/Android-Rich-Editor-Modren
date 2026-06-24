package com.chinalwb.are.strategies.defaults;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.style.URLSpan;
import android.util.Log;

import com.chinalwb.are.activities.Are_VideoPlayerActivity;
import com.chinalwb.are.activities.Are_ImagePreviewActivity;
import com.chinalwb.are.spans.AreImageSpan;
import com.chinalwb.are.spans.AreMentionSpan;
import com.chinalwb.are.spans.AreVideoSpan;
import com.chinalwb.are.strategies.AreClickStrategy;

import static com.chinalwb.are.spans.AreImageSpan.ImageType;

public class DefaultClickStrategy implements AreClickStrategy {

    public interface AtClickListener {
        boolean onAtClick(Context context, AreMentionSpan atSpan);
    }

    public static AtClickListener sAtClickListener;

    /*
     * Handles click events on mention spans.
     */
    @Override
    public boolean onClickAt(Context context, AreMentionSpan span) {
        try {
            return com.chinalwb.are.mentions.AreMentionManager.getInstance().performClick(context, span);
        } catch (Exception e) {
            Log.e("DefaultClickStrategy", "Error processing at click", e);
            return false;
        }
    }

    /*
     * Handles click events on image spans to open the preview activity.
     */
    @Override
    public boolean onClickImage(Context context, AreImageSpan imageSpan) {
        try {
            Intent intent = new Intent();
            ImageType imageType = imageSpan.getImageType();
            intent.putExtra("imageType", imageType);
            if (imageType == ImageType.URI) {
                intent.putExtra("uri", imageSpan.getUri());
            } else if (imageType == ImageType.URL) {
                intent.putExtra("url", imageSpan.getURL());
            } else {
                intent.putExtra("resId", imageSpan.getResId());
            }
            intent.setClass(context, Are_ImagePreviewActivity.class);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e("DefaultClickStrategy", "Error processing image click", e);
            return false;
        }
    }

    /*
     * Handles click events on video spans to open the video player.
     */
    @Override
    public boolean onClickVideo(Context context, AreVideoSpan videoSpan) {
        try {
            Intent intent = new Intent(context, Are_VideoPlayerActivity.class);
            String url = videoSpan.getVideoUrl();
            String path = videoSpan.getVideoPath();

            if (url != null && url.startsWith("http")) {
                intent.putExtra(Are_VideoPlayerActivity.VIDEO_URL, url);
            } else if (path != null) {
                intent.setData(Uri.parse(path));
            }

            Are_VideoPlayerActivity.sShouldUpload = false;
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e("DefaultClickStrategy", "Error processing video click", e);
            return false;
        }
    }

    /*
     * Handles click events on standard URL spans by opening them in a browser.
     */
    @Override
    public boolean onClickUrl(Context context, URLSpan urlSpan) {
        try {
            if (urlSpan != null) {
                String url = urlSpan.getURL();
                if (url != null && !url.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("DefaultClickStrategy", "Error in onClickUrl", e);
        }
        return false;
    }
}
