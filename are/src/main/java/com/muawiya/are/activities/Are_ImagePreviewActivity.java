package com.muawiya.are.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.muawiya.are.R;
import com.muawiya.are.Util;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.muawiya.are.spans.AreImageSpan.ImageType;

public class Are_ImagePreviewActivity extends AppCompatActivity {

    private static final int AUTO_HIDE_DELAY_MILLIS = 3500;
    private final Handler mHideHandler = new Handler();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private ImageView mContentView;
    private View mTopBar;
    private View mBottomBar;
    private MaterialButton mSaveButton;

    private boolean mVisible = true;
    private RequestManager mGlideRequests;

    private ImageType mImageType;
    private Uri mImageUri;
    private String mImageUrl;
    private int mImageResId = 0;
    private Bitmap mCurrentBitmap = null;

    // Zoom/Pan Matrix fields
    private Matrix mMatrix = new Matrix();
    private Matrix mSavedMatrix = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mMode = NONE;

    private PointF mStartPoint = new PointF();
    private PointF mMidPoint = new PointF();
    private float mOldDist = 1f;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            setControlsVisible(false);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.are_activity_image_preview);

        mGlideRequests = Glide.with(this);

        mContentView = findViewById(R.id.default_image_preview);
        mTopBar = findViewById(R.id.top_control_bar);
        mBottomBar = findViewById(R.id.fullscreen_content_controls);
        mSaveButton = findViewById(R.id.default_button_save);

        mContentView.setScaleType(ImageView.ScaleType.MATRIX);

        setupGestures();
        setupClickListeners();
        loadImage();
        scheduleAutoHide();
    }

    private void setupGestures() {
        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureListener());
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleControls();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                resetZoom();
                return true;
            }
        });

        mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                mScaleDetector.onTouchEvent(event);

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mSavedMatrix.set(mMatrix);
                        mStartPoint.set(event.getX(), event.getY());
                        mMode = DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mOldDist = spacing(event);
                        if (mOldDist > 10f) {
                            mSavedMatrix.set(mMatrix);
                            midPoint(mMidPoint, event);
                            mMode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mMode = NONE;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mMode == DRAG) {
                            mMatrix.set(mSavedMatrix);
                            mMatrix.postTranslate(event.getX() - mStartPoint.x, event.getY() - mStartPoint.y);
                        } else if (mMode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                mMatrix.set(mSavedMatrix);
                                float scale = newDist / mOldDist;
                                mMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y);
                            }
                        }
                        break;
                }

                mContentView.setImageMatrix(mMatrix);
                scheduleAutoHide();
                return true;
            }
        });
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            mContentView.setImageMatrix(mMatrix);
            return true;
        }
    }

    private void resetZoom() {
        if (mCurrentBitmap != null) {
            centerAndFitImage(mCurrentBitmap);
        } else {
            mMatrix.reset();
            mContentView.setImageMatrix(mMatrix);
        }
        toast("Zoom levels reset!");
    }

    private void centerAndFitImage(Bitmap bitmap) {
        if (bitmap == null || mContentView == null) return;

        float viewWidth = mContentView.getWidth();
        float viewHeight = mContentView.getHeight();
        if (viewWidth == 0 || viewHeight == 0) {
            mContentView.post(() -> centerAndFitImage(bitmap));
            return;
        }

        float drawableWidth = bitmap.getWidth();
        float drawableHeight = bitmap.getHeight();

        float scale;
        if (drawableWidth * viewHeight > viewWidth * drawableHeight) {
            scale = viewWidth / drawableWidth;
        } else {
            scale = viewHeight / drawableHeight;
        }

        float dx = (viewWidth - drawableWidth * scale) * 0.5f;
        float dy = (viewHeight - drawableHeight * scale) * 0.5f;

        mMatrix.reset();
        mMatrix.setScale(scale, scale);
        mMatrix.postTranslate(dx, dy);
        mContentView.setImageMatrix(mMatrix);
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_info).setOnClickListener(v -> showImageDetailsDialog());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareImage());
        mSaveButton.setOnClickListener(v -> saveImageToGallery());
    }

    private void loadImage() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            toast("No image source found.");
            return;
        }

        mImageType = (ImageType) extras.get("imageType");
        if (mImageType == ImageType.URI) {
            mImageUri = (Uri) extras.get("uri");
            mGlideRequests.asBitmap().load(mImageUri).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    mCurrentBitmap = resource;
                    mContentView.setImageBitmap(mCurrentBitmap);
                    centerAndFitImage(mCurrentBitmap);
                }

                @Override
                public void onLoadCleared(Drawable placeholder) {}
            });
        } else if (mImageType == ImageType.URL) {
            mImageUrl = extras.getString("url");
            mGlideRequests.asBitmap().load(mImageUrl).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    mCurrentBitmap = resource;
                    mContentView.setImageBitmap(mCurrentBitmap);
                    centerAndFitImage(mCurrentBitmap);
                }

                @Override
                public void onLoadCleared(Drawable placeholder) {}
            });
        } else if (mImageType == ImageType.RES) {
            mImageResId = extras.getInt("resId");
            mContentView.setImageResource(mImageResId);
            // Extract bitmap from drawable if needed
            Drawable d = mContentView.getDrawable();
            if (d instanceof BitmapDrawable) {
                mCurrentBitmap = ((BitmapDrawable) d).getBitmap();
                centerAndFitImage(mCurrentBitmap);
            }
        }
    }

    private void toggleControls() {
        setControlsVisible(!mVisible);
    }

    private void setControlsVisible(boolean visible) {
        mVisible = visible;
        mHideHandler.removeCallbacks(mHideRunnable);

        float from = visible ? 0f : 1f;
        float to = visible ? 1f : 0f;

        AlphaAnimation anim = new AlphaAnimation(from, to);
        anim.setDuration(250);
        anim.setFillAfter(true);

        mTopBar.startAnimation(anim);
        mBottomBar.startAnimation(anim);

        mTopBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBottomBar.setVisibility(visible ? View.VISIBLE : View.GONE);

        if (visible) {
            scheduleAutoHide();
        }
    }

    private void scheduleAutoHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        if (mVisible) {
            mHideHandler.postDelayed(mHideRunnable, AUTO_HIDE_DELAY_MILLIS);
        }
    }

    private void showImageDetailsDialog() {
        if (mCurrentBitmap == null) {
            toast("Details not ready yet.");
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("<b>Resolution:</b> ").append(mCurrentBitmap.getWidth()).append(" x ").append(mCurrentBitmap.getHeight()).append(" px<br/>");
        info.append("<b>Config:</b> ").append(mCurrentBitmap.getConfig() != null ? mCurrentBitmap.getConfig().toString() : "Unknown").append("<br/>");
        info.append("<b>Memory Usage:</b> ").append(Math.round((mCurrentBitmap.getByteCount() / (1024.0 * 1024.0)) * 100) / 100.0).append(" MB<br/>");
        info.append("<b>Type:</b> ").append(mImageType.name()).append("<br/>");

        if (mImageType == ImageType.URI && mImageUri != null) {
            info.append("<b>Path:</b> ").append(mImageUri.toString());
        } else if (mImageType == ImageType.URL && mImageUrl != null) {
            info.append("<b>URL:</b> ").append(mImageUrl);
        } else if (mImageType == ImageType.RES) {
            info.append("<b>Resource ID:</b> ").append(mImageResId);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Image Specifications")
                .setMessage(android.text.Html.fromHtml(info.toString()))
                .setPositiveButton("Dismiss", null)
                .show();
    }

    private void shareImage() {
        if (mCurrentBitmap == null) {
            toast("No image ready to share!");
            return;
        }

        mExecutor.execute(() -> {
            try {
                File cachePath = new File(getCacheDir(), "shared_images");
                cachePath.mkdirs();
                File file = new File(cachePath, "image_" + System.currentTimeMillis() + ".png");
                try (OutputStream stream = new FileOutputStream(file)) {
                    mCurrentBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                }

                // Temporary cache file sharing via local content authority context
                Uri contentUri = Uri.fromFile(file); // Safe share fallback for basic URIs
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                Intent chosenIntent = Intent.createChooser(shareIntent, "Share Image via");
                new Handler(Looper.getMainLooper()).post(() -> startActivity(chosenIntent));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> toast("Failed to share image: " + e.getMessage()));
            }
        });
    }

    private void saveImageToGallery() {
        if (mCurrentBitmap == null) {
            toast("No image loaded to save!");
            return;
        }

        mExecutor.execute(() -> {
            boolean success = false;
            String savedPath = "";
            try {
                String fileName = "Img_" + System.currentTimeMillis() + ".jpg";
                File galleryDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if (!galleryDir.exists()) galleryDir.mkdirs();

                File file = new File(galleryDir, fileName);
                try (OutputStream out = new FileOutputStream(file)) {
                    mCurrentBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                }
                savedPath = file.getAbsolutePath();
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            final boolean finalSuccess = success;
            final String finalPath = savedPath;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalSuccess) {
                    MediaScannerConnection.scanFile(this, new String[]{finalPath}, new String[]{"image/jpeg"}, null);
                    toast("Image saved to Gallery: Pictures folder!");
                } else {
                    toast("Failed to save image.");
                }
            });
        });
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
