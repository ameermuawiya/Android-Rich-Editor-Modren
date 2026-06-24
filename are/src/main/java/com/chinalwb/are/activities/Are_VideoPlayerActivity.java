package com.chinalwb.are.activities;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.chinalwb.are.R;
import com.chinalwb.are.Util;
import com.chinalwb.are.strategies.VideoStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Modernized Activity to preview videos, featuring custom controls, aspect ratio tuning,
 * mute toggle, metadata inspection, and clean background operations.
 */
public class Are_VideoPlayerActivity extends AppCompatActivity {

    public static final String VIDEO_URL = "VIDEO_URL";
    public static VideoStrategy sVideoStrategy;
    public static boolean sShouldUpload = false;

    private VideoView mVideoView;
    private MaterialButton mActionButton;
    private ProgressBar mLoadingIndicator;
    private Uri mLocalUri;
    private String mRemoteUrl;
    private boolean isPreviewMode = false;

    // Custom media control variables
    private View mTopBar;
    private View mBottomControlPanel;
    private View mActionContainer;
    private ShapeableImageView mCenterPlayBtn;
    private ShapeableImageView mBottomPlayBtn;
    private ShapeableImageView mMuteBtn;
    private ShapeableImageView mAspectBtn;
    private SeekBar mSeekBar;
    private TextView mCurrentTimeTv;
    private TextView mTotalTimeTv;
    private View mTapInterceptor;

    private MediaPlayer mMediaPlayer = null;

    private boolean mControlsVisible = true;
    private boolean mIsMuted = false;
    private int mAspectMode = 0; // 0 = Fit, 1 = Stretch, 2 = Fill
    private boolean isDragging = false;

    private ProgressBar mUploadProgressBar;
    private TextView mProgressText;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private final Handler mHideHandler = new Handler(Looper.getMainLooper());
    private final Handler mSeekHandler = new Handler(Looper.getMainLooper());

    private boolean isUploadCancelled = false;

    private final Runnable mControlsHideRunnable = new Runnable() {
        @Override
        public void run() {
            setControlsVisible(false);
        }
    };

    private final Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mVideoView != null && mVideoView.isPlaying() && !isDragging) {
                int pos = mVideoView.getCurrentPosition();
                int duration = mVideoView.getDuration();
                if (duration > 0) {
                    long relativeProgress = 1000L * pos / duration;
                    mSeekBar.setProgress((int) relativeProgress);
                    mCurrentTimeTv.setText(formatTime(pos));
                    mTotalTimeTv.setText(formatTime(duration));
                }
            }
            mSeekHandler.postDelayed(this, 200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.are_activity_video_player);

        mVideoView = findViewById(R.id.are_video_view);
        mActionButton = findViewById(R.id.are_btn_action);
        mLoadingIndicator = findViewById(R.id.video_loading_indicator);

        // Custom Overlay Controls Binding
        mTopBar = findViewById(R.id.video_top_bar);
        mBottomControlPanel = findViewById(R.id.playback_control_panel);
        mActionContainer = findViewById(R.id.fullscreen_content_controls);
        mCenterPlayBtn = findViewById(R.id.btn_center_play);
        mBottomPlayBtn = findViewById(R.id.btn_bottom_play);
        mMuteBtn = findViewById(R.id.btn_volume);
        mAspectBtn = findViewById(R.id.btn_aspect_ratio);
        mSeekBar = findViewById(R.id.video_seekbar);
        mCurrentTimeTv = findViewById(R.id.tv_current_time);
        mTotalTimeTv = findViewById(R.id.tv_total_time);
        mTapInterceptor = findViewById(R.id.tap_interceptor);

        setupModernBackPressed();
        extractIntentData();
        setupMode();
        setupCustomVideoControls();
        startPlayback();
    }

    private void setupModernBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        mLocalUri = intent.getData();
        mRemoteUrl = intent.getStringExtra(VIDEO_URL);
        isPreviewMode = getCallingActivity() == null;
    }

    private void setupMode() {
        if (isPreviewMode) {
            mActionButton.setText("Save to Gallery");
            mActionButton.setIconResource(R.drawable.ic_save);
            mActionButton.setOnClickListener(v -> saveVideoToGallery());
        } else {
            mActionButton.setText("Attach Video");
            mActionButton.setIconResource(R.drawable.ic_check);
            mActionButton.setOnClickListener(v -> processVideoAttachment());
        }
    }

    private void setupCustomVideoControls() {
        mTapInterceptor.setOnClickListener(v -> toggleControls());

        View.OnClickListener playToggle = v -> togglePlayPause();
        mCenterPlayBtn.setOnClickListener(playToggle);
        mBottomPlayBtn.setOnClickListener(playToggle);

        findViewById(R.id.video_btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.video_btn_info).setOnClickListener(v -> showVideoDetailsDialog());
        findViewById(R.id.btn_video_share).setOnClickListener(v -> shareVideo());

        mMuteBtn.setOnClickListener(v -> toggleMute());
        mAspectBtn.setOnClickListener(v -> toggleAspectRatio());

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mVideoView.getDuration() > 0) {
                    int seekMs = (int) ((long) mVideoView.getDuration() * progress / 1000L);
                    mVideoView.seekTo(seekMs);
                    mCurrentTimeTv.setText(formatTime(seekMs));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isDragging = true;
                mHideHandler.removeCallbacks(mControlsHideRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isDragging = false;
                scheduleControlsHide();
            }
        });

        scheduleControlsHide();
        mSeekHandler.post(mUpdateProgressRunnable);
    }

    private void togglePlayPause() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            updatePlayPauseUI(false);
        } else {
            mVideoView.start();
            updatePlayPauseUI(true);
        }
        scheduleControlsHide();
    }

    private void updatePlayPauseUI(boolean isPlaying) {
        if (isPlaying) {
            mCenterPlayBtn.setImageResource(R.drawable.ic_pause);
            mBottomPlayBtn.setImageResource(R.drawable.ic_pause);
        } else {
            mCenterPlayBtn.setImageResource(R.drawable.ic_play);
            mBottomPlayBtn.setImageResource(R.drawable.ic_play);
        }
    }

    private void toggleMute() {
        mIsMuted = !mIsMuted;
        if (mMediaPlayer != null) {
            float vol = mIsMuted ? 0f : 1f;
            mMediaPlayer.setVolume(vol, vol);
        }
        mMuteBtn.setImageResource(mIsMuted ? R.drawable.ic_volume_off : R.drawable.ic_volume_up);
        Util.toast(this, mIsMuted ? "Volume Muted" : "Volume Restored");
        scheduleControlsHide();
    }

    private void toggleAspectRatio() {
        mAspectMode = (mAspectMode + 1) % 3;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mVideoView.getLayoutParams();

        if (mAspectMode == 0) {
            lp.width = FrameLayout.LayoutParams.MATCH_PARENT;
            lp.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            lp.gravity = android.view.Gravity.CENTER;
            Util.toast(this, "Aspect: Center Fit");
        } else if (mAspectMode == 1) {
            lp.width = FrameLayout.LayoutParams.MATCH_PARENT;
            lp.height = FrameLayout.LayoutParams.MATCH_PARENT;
            Util.toast(this, "Aspect: Stretched Screen");
        } else {
            lp.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            lp.height = FrameLayout.LayoutParams.MATCH_PARENT;
            lp.gravity = android.view.Gravity.CENTER;
            Util.toast(this, "Aspect: Fill Height");
        }

        mVideoView.setLayoutParams(lp);
        scheduleControlsHide();
    }

    private void toggleControls() {
        setControlsVisible(!mControlsVisible);
    }

    private void setControlsVisible(boolean visible) {
        mControlsVisible = visible;
        mHideHandler.removeCallbacks(mControlsHideRunnable);

        float from = visible ? 0f : 1f;
        float to = visible ? 1f : 0f;

        AlphaAnimation anim = new AlphaAnimation(from, to);
        anim.setDuration(250);
        anim.setFillAfter(true);

        mTopBar.startAnimation(anim);
        mBottomControlPanel.startAnimation(anim);
        mCenterPlayBtn.startAnimation(anim);
        if (mActionContainer != null) {
            mActionContainer.startAnimation(anim);
        }

        mTopBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBottomControlPanel.setVisibility(visible ? View.VISIBLE : View.GONE);
        mCenterPlayBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (mActionContainer != null) {
            mActionContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        if (visible) {
            scheduleControlsHide();
        }
    }

    private void scheduleControlsHide() {
        mHideHandler.removeCallbacks(mControlsHideRunnable);
        mHideHandler.postDelayed(mControlsHideRunnable, 3500);
    }

    private void startPlayback() {
        mLoadingIndicator.setVisibility(View.VISIBLE);

        if (mRemoteUrl != null && !mRemoteUrl.isEmpty()) {
            mVideoView.setVideoURI(Uri.parse(mRemoteUrl));
        } else if (mLocalUri != null) {
            mVideoView.setVideoURI(mLocalUri);
        }

        mVideoView.setOnPreparedListener(mp -> {
            mMediaPlayer = mp;
            mLoadingIndicator.setVisibility(View.GONE);
            mMediaPlayer.setVolume(mIsMuted ? 0f : 1f, mIsMuted ? 0f : 1f);
            mVideoView.start();
            updatePlayPauseUI(true);

            int duration = mVideoView.getDuration();
            mTotalTimeTv.setText(formatTime(duration));
        });

        mVideoView.setOnErrorListener((mp, what, extra) -> {
            mLoadingIndicator.setVisibility(View.GONE);
            Util.toast(this, "Cannot play this video.");
            return true;
        });

        mVideoView.setOnCompletionListener(mp -> {
            updatePlayPauseUI(false);
            setControlsVisible(true);
        });
    }

    private void showVideoDetailsDialog() {
        StringBuilder info = new StringBuilder();
        int d = mVideoView.getDuration();
        info.append("<b>Duration:</b> ").append(formatTime(d)).append("<br/>");
        info.append("<b>Aspect Tuning:</b> ").append(mAspectMode == 0 ? "Fit Aspect" : mAspectMode == 1 ? "Fullscreen Stretch" : "Fill Scale").append("<br/>");
        info.append("<b>Source Profile:</b> ").append(mRemoteUrl != null ? "Remote Cloud Server" : "Local Device Asset").append("<br/>");
        info.append("<b>Address:</b> ").append(mRemoteUrl != null ? mRemoteUrl : mLocalUri != null ? mLocalUri.toString() : "Unknown");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Video Specifications")
                .setMessage(android.text.Html.fromHtml(info.toString()))
                .setPositiveButton("Dismiss", null)
                .show();
    }

    private void shareVideo() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/*");
        if (mRemoteUrl != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this video: " + mRemoteUrl);
            startActivity(Intent.createChooser(shareIntent, "Share Video Link"));
        } else if (mLocalUri != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, mLocalUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Video File"));
        } else {
            Util.toast(this, "No video content available to share yet.");
        }
    }

    private void saveVideoToGallery() {
        if (mRemoteUrl != null && mRemoteUrl.startsWith("http")) {
            downloadRemoteVideo();
        } else if (mLocalUri != null) {
            copyLocalVideoToGallery();
        } else {
            Util.toast(this, "No valid video found to save.");
        }
    }

    private void downloadRemoteVideo() {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mRemoteUrl));
            request.setTitle("Downloading Video");
            request.setDescription("Saving to your gallery...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            String fileName = "video_" + System.currentTimeMillis() + ".mp4";
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, fileName);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Util.toast(this, "Download started. Check notifications.");
            }
        } catch (Exception e) {
            Util.toast(this, "Failed to start download.");
        }
    }

    private void copyLocalVideoToGallery() {
        executorService.execute(() -> {
            boolean success = false;
            String destPath = "";
            try {
                InputStream in = getContentResolver().openInputStream(mLocalUri);
                String fileName = "video_" + System.currentTimeMillis() + ".mp4";
                destPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + fileName;

                if (in != null) {
                    OutputStream out = Files.newOutputStream(Paths.get(destPath));
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    in.close();
                    out.close();
                    success = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final boolean isSuccess = success;
            final String finalPath = destPath;
            mainThreadHandler.post(() -> {
                if (isSuccess) {
                    MediaScannerConnection.scanFile(this, new String[]{finalPath}, new String[]{"video/mp4"}, null);
                    Util.toast(this, "Video saved to Gallery (Movies folder).");
                } else {
                    Util.toast(this, "Failed to save video.");
                }
            });
        });
    }

    private void processVideoAttachment() {
        if (!sShouldUpload) {
            Intent resultData = new Intent();
            resultData.setData(mLocalUri);
            setResult(RESULT_OK, resultData);
            finish();
            return;
        }

        if (sVideoStrategy == null) {
            Util.toast(this, "Video configuration error!");
            return;
        }

        isUploadCancelled = false;
        AlertDialog loadingDialog = showUploadProgressDialog();

        executorService.execute(() -> {
            String url = sVideoStrategy.uploadVideo(this, mLocalUri, progress -> mainThreadHandler.post(() -> {
                if (mUploadProgressBar != null && mProgressText != null && !isUploadCancelled) {
                    mUploadProgressBar.setProgress(progress);
                    if (progress == 100) {
                        mProgressText.setText("Processing...");
                    } else {
                        mProgressText.setText(progress + "%");
                    }
                }
            }));

            mainThreadHandler.post(() -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }

                if (isUploadCancelled) {
                    return;
                }

                if (url != null && url.startsWith("http")) {
                    Intent resultData = new Intent();
                    resultData.setData(mLocalUri);
                    resultData.putExtra(VIDEO_URL, url);
                    setResult(RESULT_OK, resultData);
                    finish();
                } else {
                    Util.toast(this, "Failed to upload video.");
                }
            });
        });
    }

    private AlertDialog showUploadProgressDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 60, 60, 60);
        layout.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("Uploading Video");
        title.setTextSize(18f);
        title.setPadding(0, 0, 0, 40);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        mUploadProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        mUploadProgressBar.setIndeterminate(false);
        mUploadProgressBar.setMax(100);
        mUploadProgressBar.setProgress(0);
        layout.addView(mUploadProgressBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mProgressText = new TextView(this);
        mProgressText.setText("0%");
        mProgressText.setPadding(0, 20, 0, 0);
        mProgressText.setGravity(Gravity.CENTER);
        layout.addView(mProgressText);

        return new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setView(layout)
                .setNegativeButton("Cancel", (dialogInterface, which) -> {
                    isUploadCancelled = true;
                    if (sVideoStrategy != null) {
                        sVideoStrategy.cancelUpload();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSeekHandler.removeCallbacks(mUpdateProgressRunnable);
        mHideHandler.removeCallbacks(mControlsHideRunnable);
    }

    private String formatTime(int ms) {
        if (ms <= 0) return "00:00";
        int totalSeconds = ms / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
