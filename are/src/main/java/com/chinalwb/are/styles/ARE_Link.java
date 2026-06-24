package com.chinalwb.are.styles;

import android.content.Context;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.R;
import com.chinalwb.are.spans.AreUrlSpan;
import com.chinalwb.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class ARE_Link extends ARE_ABS_FreeStyle {

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private MaterialButton mLinkButton;
    private AREditText mEditText;
    private Context mContext;
    private boolean mIsChecked;

    /*
     Initializes the link style and sets the click listener
     for the provided material button.
     */
    public ARE_Link(MaterialButton button, ARE_Toolbar toolbar) {
        super(toolbar);
        this.mLinkButton = button;
        this.mContext = button.getContext();
        setListenerForButton(button);
    }

    /*
     Sets the target edit text where the link style
     will be applied.
     */
    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    /*
     Attaches a click listener to the link button in the toolbar.
     */
    @Override
    public void setListenerForButton(MaterialButton button) {
        button.setOnClickListener(v -> openLinkDialog());
    }

    /*
     Opens a dialog to input or update a URL. Pre-fills the URL
     if the cursor is currently on an existing link.
     */
    private void openLinkDialog() {
        if (mEditText == null) return;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        builder.setTitle(R.string.are_dialog_link_title);

        View view = LayoutInflater.from(mContext).inflate(R.layout.are_dialog_url, null);
        TextInputEditText urlEditText = view.findViewById(R.id.url);

        Editable editable = mEditText.getEditableText();
        int selectionStart = mEditText.getSelectionStart();
        int selectionEnd = mEditText.getSelectionEnd();
        
        AreUrlSpan[] existingSpans = editable.getSpans(selectionStart, selectionEnd, AreUrlSpan.class);
        AreUrlSpan targetSpan = null;
        
        if (existingSpans != null && existingSpans.length > 0) {
            targetSpan = existingSpans[0];
            urlEditText.setText(targetSpan.getURL());
            urlEditText.setSelection(urlEditText.getText().length());
        }

        final AreUrlSpan finalTargetSpan = targetSpan;

        builder.setView(view)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    String url = urlEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(url)) {
                        insertOrUpdateLink(url, finalTargetSpan);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
    }

    /*
     Updates an existing link span or inserts a new one.
     */
    private void insertOrUpdateLink(String url, AreUrlSpan existingSpan) {
        if (mEditText == null) return;

        if (!url.startsWith(HTTP) && !url.startsWith(HTTPS)) {
            url = HTTP + url;
        }

        Editable editable = mEditText.getEditableText();
        int start;
        int end;

        if (existingSpan != null) {
            start = editable.getSpanStart(existingSpan);
            end = editable.getSpanEnd(existingSpan);
            editable.removeSpan(existingSpan);
        } else {
            start = mEditText.getSelectionStart();
            end = mEditText.getSelectionEnd();

            if (start == end) {
                editable.insert(start, url);
                end = start + url.length();
            }
        }

        if (start >= 0 && end >= start) {
            editable.setSpan(new AreUrlSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /*
     Applies the style formatting.
     */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        // No implementation needed for link style application
    }

    /*
     Returns the material button used for the link style.
     */
    @Override
    public MaterialButton getButton() {
        return mLinkButton;
    }

    /*
     Updates the checked state of the link style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    /*
     Returns the current checked state of the link style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mIsChecked;
    }
}
