package com.muawiya.are.strategies;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.muawiya.are.Util;
import com.muawiya.are.spans.AreImageSpan;
import com.muawiya.are.styles.ARE_Image;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class ImageStrategy {

    /*
     * IMPORTANT NOTE:
     * An ImgBB API key is used here to upload images. The current key is 
     * provided by the original developer for test.
     * If you are using or forking this project for your own application, 
     * it is highly recommended to create your own free account at:
     * https://api.imgbb.com/
     * * Please generate your own API key and replace the string below to 
     * ensure absolute privacy for your users' uploads and to avoid 
     * hitting any potential rate limits shared across multiple apps.
     */
    private static final String IMGBB_API_KEY = "7d349ef15111dffbe4aadce7f9aa5676";
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private Call currentCall;

    public interface ProgressListener {
        void onProgress(int percentage);
    }

    public void uploadAndInsertImage(Uri uri, ARE_Image areImage) {
        if (areImage == null || areImage.getEditText() == null || uri == null) {
            return;
        }

        Context context = areImage.getEditText().getContext();
        WeakReference<ARE_Image> areImageRef = new WeakReference<>(areImage);
        
        final ProgressBar[] progressBarHolder = new ProgressBar[1];
        final TextView[] progressTextHolder = new TextView[1];
        
        AlertDialog loadingDialog = showUploadProgressDialog(context, progressBarHolder, progressTextHolder);

        executorService.execute(() -> {
            String uploadedUrl = performNetworkUpload(context, uri, progress -> {
                mainThreadHandler.post(() -> {
                    if (progressBarHolder[0] != null && progressTextHolder[0] != null) {
                        progressBarHolder[0].setProgress(progress);
                        if (progress == 100) {
                            progressTextHolder[0].setText("Processing...");
                        } else {
                            progressTextHolder[0].setText(progress + "%");
                        }
                    }
                });
            });

            mainThreadHandler.post(() -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }

                if (currentCall != null && currentCall.isCanceled()) {
                    return; 
                }

                ARE_Image imageStyle = areImageRef.get();
                if (imageStyle != null) {
                    if (uploadedUrl != null && uploadedUrl.startsWith("http")) {
                        imageStyle.insertImage(uploadedUrl, AreImageSpan.ImageType.URL);
                    } else {
                        Util.toast(imageStyle.getEditText().getContext(), "Upload failed. Please try again.");
                    }
                }
            });
        });
    }

    private String performNetworkUpload(Context context, Uri uri, ProgressListener progressListener) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;

            final byte[] imageBytes = readBytesFromStream(inputStream);
            String fileName = "upload_" + System.currentTimeMillis() + ".jpg";

            RequestBody fileBody = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse("image/*");
                }

                @Override
                public long contentLength() {
                    return imageBytes.length;
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    long totalLength = imageBytes.length;
                    int bufferSize = 8192;
                    long uploaded = 0;

                    for (int i = 0; i < totalLength; i += bufferSize) {
                        if (currentCall != null && currentCall.isCanceled()) {
                            break;
                        }

                        int length = (int) Math.min(bufferSize, (int) (totalLength - i));
                        sink.write(imageBytes, i, length);
                        uploaded += length;

                        if (progressListener != null) {
                            int progress = (int) ((uploaded * 100) / totalLength);
                            progressListener.onProgress(progress);
                        }
                    }
                }
            };

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", fileName, fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build();

            currentCall = client.newCall(request);

            try (Response response = currentCall.execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        if (jsonObject.optBoolean("success", false)) {
                            JSONObject dataObj = jsonObject.optJSONObject("data");
                            if (dataObj != null) {
                                String fileUrl = dataObj.optString("url");
                                if (fileUrl != null && !fileUrl.isEmpty()) {
                                    return fileUrl.replace("\\/", "/");
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] readBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 8192;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private AlertDialog showUploadProgressDialog(Context context, ProgressBar[] pBarHolder, TextView[] pTextHolder) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 60, 60, 60);
        layout.setGravity(Gravity.CENTER);

        TextView title = new TextView(context);
        title.setText("Uploading Image");
        title.setTextSize(18f);
        title.setPadding(0, 0, 0, 40);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(false);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        layout.addView(progressBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        pBarHolder[0] = progressBar;

        TextView progressText = new TextView(context);
        progressText.setText("0%");
        progressText.setPadding(0, 20, 0, 0);
        progressText.setGravity(Gravity.CENTER);
        layout.addView(progressText);
        pTextHolder[0] = progressText;

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setCancelable(false)
                .setView(layout)
                .setNegativeButton("Cancel", (dialogInterface, which) -> {
                    if (currentCall != null) {
                        currentCall.cancel();
                    }
                })
                .show();

        return dialog;
    }
}
