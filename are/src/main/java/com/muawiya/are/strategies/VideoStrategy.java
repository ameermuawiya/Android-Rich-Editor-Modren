package com.muawiya.are.strategies;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Strategy for uploading local videos to a server with progress tracking and cancellation.
 */
public class VideoStrategy {

    /*
     * IMPORTANT NOTE FOR OPEN SOURCE USERS:
     * This strategy uses Videy.co's anonymous upload endpoint. 
     * It extracts the video ID from the response and constructs a direct 
     * CDN URL (https://cdn.videy.co/{id}.mp4) for seamless playback.
     * No API key or login is required, and there are no strict limits.
     */
    private static final String UPLOAD_URL = "https://videy.co/api/upload";
    private Call currentCall;

    public interface ProgressListener {
        void onProgress(int percentage);
    }

    // Method to safely cancel the ongoing upload
    public void cancelUpload() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }

    /**
     * Uploads the video synchronously, providing live progress feedback, and returns the URL.
     */
    public String uploadVideo(Context context, Uri uri, ProgressListener progressListener) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;

            final byte[] videoBytes = readBytesFromStream(inputStream);
            // If cancelled during file read
            if (currentCall != null && currentCall.isCanceled()) return null;

            String fileName = "upload_" + System.currentTimeMillis() + ".mp4";

            RequestBody fileBody = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse("video/mp4");
                }

                @Override
                public long contentLength() {
                    return videoBytes.length;
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    long totalLength = videoBytes.length;
                    int bufferSize = 8192; // Fast buffer size
                    long uploaded = 0;

                    for (int i = 0; i < totalLength; i += bufferSize) {
                        if (currentCall != null && currentCall.isCanceled()) {
                            break; // Safe cancellation
                        }
                        
                        int length = (int) Math.min(bufferSize, (int) (totalLength - i));
                        sink.write(videoBytes, i, length);
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
                    .addFormDataPart("file", fileName, fileBody)
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
                        String id = jsonObject.optString("id");
                        if (id != null && !id.isEmpty()) {
                            // Magic trick: construct the direct CDN link
                            return "https://cdn.videy.co/" + id + ".mp4";
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
            if (currentCall != null && currentCall.isCanceled()) break;
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
