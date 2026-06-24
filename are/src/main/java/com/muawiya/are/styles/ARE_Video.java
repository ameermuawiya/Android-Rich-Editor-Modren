package com.muawiya.are.styles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.muawiya.are.AREditText;
import com.muawiya.are.Constants;
import com.muawiya.are.R;
import com.muawiya.are.Util;
import com.muawiya.are.activities.Are_VideoPlayerActivity;
import com.muawiya.are.spans.AreVideoSpan;
import com.muawiya.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class ARE_Video implements IARE_Style {

    private MaterialButton mInsertVideoButton;
    private AREditText mEditText;
    private Context mContext;
    private static int sWidth = 0;
    private boolean mIsChecked;

    public ARE_Video(MaterialButton button) {
        this.mInsertVideoButton = button;
        this.mContext = button.getContext();
        sWidth = Util.getScreenWidthAndHeight(mContext)[0];
        setListenerForButton(this.mInsertVideoButton);
    }

    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    @Override
    public void setListenerForButton(MaterialButton button) {
        button.setOnClickListener(v -> showVideoInsertOptionsDialog());
    }

    private void showVideoInsertOptionsDialog() {
        String[] options = {
            "Insert from Gallery (Direct)", "Upload from Gallery (Link)", "Insert from Internet URL"
        };

        new MaterialAlertDialogBuilder(mContext)
                .setTitle("Insert Video")
                .setItems(
                        options,
                        (dialog, which) -> {
                            if (which == 0) {
                                openVideoPicker(false);
                            } else if (which == 1) {
                                openVideoPicker(true);
                            } else if (which == 2) {
                                showUrlInputDialog();
                            }
                        })
                .show();
    }

    private void showUrlInputDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.are_dialog_url, null);
        TextInputEditText urlEditText = view.findViewById(R.id.url);

        AreVideoSpan existingSpan = null;
        if (mEditText != null) {
            Editable editable = mEditText.getEditableText();
            int selStart = mEditText.getSelectionStart();
            int selEnd = mEditText.getSelectionEnd();

            AreVideoSpan[] spans = editable.getSpans(selStart, selEnd, AreVideoSpan.class);
            if (spans != null && spans.length > 0) {
                existingSpan = spans[0];
                urlEditText.setText(existingSpan.getVideoUrl());
            }
        }

        final AreVideoSpan finalExistingSpan = existingSpan;
        String title = finalExistingSpan != null ? "Update Video URL" : "Insert Internet Video";
        String btnText = finalExistingSpan != null ? "Update" : "Insert";

        new MaterialAlertDialogBuilder(mContext)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(
                        btnText,
                        (dialog, which) -> {
                            if (urlEditText.getText() != null) {
                                String videoUrl = urlEditText.getText().toString().trim();
                                if (videoUrl.toLowerCase().startsWith("http")) {
                                    insertVideo(null, videoUrl, finalExistingSpan);
                                } else {
                                    Util.toast(mContext, "Please enter a valid URL");
                                }
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private Activity getActivityFromContext(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof android.content.ContextWrapper) {
            return getActivityFromContext(
                    ((android.content.ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    private void openVideoPicker(boolean shouldUpload) {
        Are_VideoPlayerActivity.sShouldUpload = shouldUpload;

        if (mEditText != null) {
            Are_VideoPlayerActivity.sVideoStrategy = mEditText.getVideoStrategy();
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");

        Activity activity = getActivityFromContext(mContext);
        if (activity != null) {
            activity.startActivityForResult(intent, ARE_Toolbar.REQ_VIDEO_CHOOSE);
        } else {
            Util.toast(mContext, "Cannot open gallery: Context is not an Activity.");
        }
    }

    public void insertVideo(final Uri uri, final String videoUrl) {
        insertVideo(uri, videoUrl, null);
    }

        public void insertVideo(final Uri uri, final String videoUrl, final AreVideoSpan spanToUpdate) {
        if (mEditText == null) return;
        final Editable editable = mEditText.getEditableText();
        final int spanStart, spanEnd;

        if (spanToUpdate != null) {
            spanStart = editable.getSpanStart(spanToUpdate);
            spanEnd = editable.getSpanEnd(spanToUpdate);
            if (spanStart < 0 || spanEnd < 0) return;
            editable.removeSpan(spanToUpdate);
        } else {
            int selStart = mEditText.getSelectionStart();
            int selEnd = mEditText.getSelectionEnd();
            if (selStart < 0 || selEnd < 0) {
                selStart = editable.length();
                selEnd = editable.length();
            }

            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(Constants.CHAR_NEW_LINE).append(Constants.ZERO_WIDTH_SPACE_STR)
               .append(Constants.CHAR_NEW_LINE).append(Constants.ZERO_WIDTH_SPACE_STR);

            editable.replace(selStart, selEnd, ssb);
            editable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), selStart + 1, selStart + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            editable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), selStart + 3, selStart + 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            spanStart = selStart + 1;
            spanEnd = selStart + 2;
        }

        Bitmap placeholderBmp = Util.getVideoPlaceholderBitmap(mContext);
        String localPath = uri != null ? uri.toString() : "";
        String url = videoUrl != null ? videoUrl : "";
        final AreVideoSpan placeholderSpan = new AreVideoSpan(mContext, placeholderBmp, localPath, url);

        editable.setSpan(placeholderSpan, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Object src = (url != null && !url.isEmpty()) ? url : uri;
        Util.loadVideoAsynchronously(mContext, src, new Util.VideoLoadCallback() {
            @Override
            public void onVideoReady(Bitmap bitmap) {
                AreVideoSpan realSpan = new AreVideoSpan(mContext, bitmap, localPath, url);
                Util.replaceSpanSafely(mEditText, placeholderSpan, realSpan);
            }

            @Override
            public void onError(Bitmap errorBitmap) {
                AreVideoSpan realSpan = new AreVideoSpan(mContext, errorBitmap, localPath, url);
                Util.replaceSpanSafely(mEditText, placeholderSpan, realSpan);
            }
        });
    }


    @Override
    public void applyStyle(Editable editable, int start, int end) {}

    @Override
    public MaterialButton getButton() {
        return this.mInsertVideoButton;
    }

    @Override
    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    @Override
    public boolean getIsChecked() {
        return this.mIsChecked;
    }

    @Override
    public EditText getEditText() {
        return this.mEditText;
    }
}
