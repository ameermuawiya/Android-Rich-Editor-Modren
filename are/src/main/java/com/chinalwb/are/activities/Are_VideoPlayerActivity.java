package com.chinalwb.are.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.chinalwb.are.R;
import com.chinalwb.are.Util;
import com.chinalwb.are.strategies.VideoStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Modernized Activity to preview videos, handle network uploads, and download/save to gallery.
 * Completely free of deprecated methods.
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

    private ProgressBar mUploadProgressBar;
    private TextView mProgressText;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private boolean isUploadCancelled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.are_activity_video_player);

        mVideoView = findViewById(R.id.are_video_view);
        mActionButton = findViewById(R.id.are_btn_action);
        mLoadingIndicator = findViewById(R.id.video_loading_indicator);

        setupModernBackPressed();

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);
        mVideoView.setMediaController(mediaController);

        extractIntentData();
        setupMode();
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
            mActionButton.setOnClickListener(v -> saveVideoToGallery());
        } else {
            mActionButton.setText("Attach Video");
            mActionButton.setOnClickListener(v -> processVideoAttachment());
        }
    }

    private void startPlayback() {
        mLoadingIndicator.setVisibility(View.VISIBLE);

        if (mRemoteUrl != null && !mRemoteUrl.isEmpty()) {
            mVideoView.setVideoURI(Uri.parse(mRemoteUrl));
        } else if (mLocalUri != null) {
            mVideoView.setVideoURI(mLocalUri);
        }
        
        mVideoView.setOnPreparedListener(mp -> {
            mLoadingIndicator.setVisibility(View.GONE);
            mVideoView.start();
        });

        mVideoView.setOnErrorListener((mp, what, extra) -> {
            mLoadingIndicator.setVisibility(View.GONE);
            Util.toast(this, "Cannot play this video.");
            return true;
        });
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
                    return; // If user cancelled, don't show any error or result
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
}
