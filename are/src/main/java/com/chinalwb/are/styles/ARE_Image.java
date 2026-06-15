package com.chinalwb.are.styles;

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.CustomTarget;
import com.chinalwb.are.AREditText;
import com.chinalwb.are.Constants;
import com.chinalwb.are.R;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.AreImageSpan;
import com.chinalwb.are.spans.AreImageSpan.ImageType;
import com.chinalwb.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ARE_Image implements IARE_Style, IARE_Image {

    public static int REQUEST_CODE = ARE_Toolbar.REQ_IMAGE;

    private MaterialButton mInsertImageButton;
    private AREditText mEditText;
    private Context mContext;
    private boolean mShouldUploadSelectedImage = false;

    private RequestManager mGlideRequests;
    private int mWidth = 0;

    private final List<CustomTarget<Bitmap>> mGlideTargets = new ArrayList<>();

    public ARE_Image(MaterialButton button) {
        this.mInsertImageButton = button;
        this.mContext = button.getContext();
        this.mGlideRequests = Glide.with(mContext);
        this.mWidth = Util.getScreenWidthAndHeight(mContext)[0];
        setListenerForButton(this.mInsertImageButton);
    }

    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    @Override
    public void setListenerForButton(MaterialButton button) {
        button.setOnClickListener(v -> showImageInsertOptionsDialog());
    }

    private void showImageInsertOptionsDialog() {
        String[] options = {"Insert from Gallery (Direct)", "Upload from Gallery (Link)", "Insert from Internet URL"};

        new MaterialAlertDialogBuilder(mContext)
                .setTitle("Insert Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openImagePicker(false);
                    } else if (which == 1) {
                        openImagePicker(true);
                    } else if (which == 2) {
                        showUrlInputDialog();
                    }
                })
                .show();
    }

    private void showUrlInputDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.are_dialog_url, null);
        TextInputEditText urlEditText = view.findViewById(R.id.url);

        AreImageSpan existingSpan = null;
        if (mEditText != null) {
            Editable editable = mEditText.getEditableText();
            int selStart = mEditText.getSelectionStart();
            int selEnd = mEditText.getSelectionEnd();
            
            AreImageSpan[] spans = editable.getSpans(selStart, selEnd, AreImageSpan.class);
            if (spans != null && spans.length > 0) {
                existingSpan = spans[0];
                String src = existingSpan.getSource();
                if (src != null) {
                    urlEditText.setText(src);
                }
            }
        }

        final AreImageSpan finalExistingSpan = existingSpan;
        String title = finalExistingSpan != null ? "Update Image URL" : "Insert Internet Image";
        String btnText = finalExistingSpan != null ? "Update" : "Insert";

        new MaterialAlertDialogBuilder(mContext)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(btnText, (dialog, which) -> {
                    if (urlEditText.getText() != null) {
                        String imageUrl = urlEditText.getText().toString().trim();
                        if (imageUrl.toLowerCase().startsWith("http")) {
                            insertImage(imageUrl, ImageType.URL, finalExistingSpan);
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
            return getActivityFromContext(((android.content.ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    private void openImagePicker(boolean shouldUpload) {
        this.mShouldUploadSelectedImage = shouldUpload;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        
        Activity activity = getActivityFromContext(mContext);
        if (activity != null) {
            activity.startActivityForResult(intent, REQUEST_CODE);
        } else {
            Util.toast(mContext, "Cannot open gallery: Context is not an Activity.");
        }
    }

    public boolean shouldUploadSelectedImage() {
        return mShouldUploadSelectedImage;
    }

    public void insertImage(final Object src, final ImageType type) {
        insertImage(src, type, null);
    }

        public void insertImage(final Object src, final ImageType type, final AreImageSpan spanToUpdate) {
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

        Bitmap placeholderBmp = Util.getImagePlaceholderBitmap(mContext);
        final AreImageSpan placeholderSpan = (type == ImageType.URI) ? new AreImageSpan(mContext, placeholderBmp, (Uri) src) :
                (type == ImageType.URL) ? new AreImageSpan(mContext, placeholderBmp, (String) src) : new AreImageSpan(mContext, (int) src);
        
        editable.setSpan(placeholderSpan, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Util.loadImageAsynchronously(mContext, src, new Util.ImageLoadCallback() {
            @Override
            public void onImageReady(Bitmap bitmap) {
                AreImageSpan realSpan = (type == ImageType.URI) ? new AreImageSpan(mContext, bitmap, (Uri) src) :
                        (type == ImageType.URL) ? new AreImageSpan(mContext, bitmap, (String) src) : new AreImageSpan(mContext, (int) src);
                Util.replaceSpanSafely(mEditText, placeholderSpan, realSpan);
            }

            @Override
            public void onError(Bitmap errorBitmap) {
                AreImageSpan realSpan = (type == ImageType.URI) ? new AreImageSpan(mContext, errorBitmap, (Uri) src) :
                        (type == ImageType.URL) ? new AreImageSpan(mContext, errorBitmap, (String) src) : new AreImageSpan(mContext, (int) src);
                Util.replaceSpanSafely(mEditText, placeholderSpan, realSpan);
            }
        });
    }


    @Override
    public void applyStyle(Editable editable, int start, int end) {}

    @Override
    public MaterialButton getButton() {
        return this.mInsertImageButton;
    }

    @Override
    public void setChecked(boolean isChecked) {}

    @Override
    public boolean getIsChecked() {
        return false;
    }

    @Override
    public EditText getEditText() {
        return this.mEditText;
    }
}
