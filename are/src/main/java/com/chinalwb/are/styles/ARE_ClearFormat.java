package com.chinalwb.are.styles;

import android.content.Context;
import android.text.Editable;
import android.text.style.CharacterStyle;
import android.text.style.ParagraphStyle;
import android.view.View;

import com.chinalwb.are.AREditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ARE_ClearFormat {

    private MaterialButton mButton;
    private AREditText mEditText;
    private Context mContext;
    private boolean mIsChecked;

    public ARE_ClearFormat(MaterialButton button, Context context) {
        this.mButton = button;
        this.mContext = context;
        setListener();
    }

    /*
     Sets the AREditText instance to manage formatting.
     */
    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    /*
     Attaches click listener to the clear format button.
     */
    private void setListener() {
        if (mButton != null) {
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showWarningDialog();
                    
                    // Future Logic: Uncomment the below lines to enable the checkable state
                    // mIsChecked = !mIsChecked;
                    // if (mButton != null) {
                    //     mButton.setChecked(mIsChecked);
                    // }
                }
            });
        }
    }

    /*
     Shows a warning dialog before clearing all text formatting.
     */
    private void showWarningDialog() {
        if (mContext == null || mEditText == null) return;

        try {
            new MaterialAlertDialogBuilder(mContext)
                    .setTitle("Clear Format")
                    .setMessage("Are you sure you want to clear all text formatting? This action cannot be undone.")
                    .setPositiveButton("Clear", (dialog, which) -> clearFormat())
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     Removes all CharacterStyle and ParagraphStyle spans from the text.
     */
    private void clearFormat() {
        try {
            Editable editable = mEditText.getEditableText();
            if (editable == null) return;

            int start = 0;
            int end = editable.length();

            CharacterStyle[] charSpans = editable.getSpans(start, end, CharacterStyle.class);
            for (CharacterStyle span : charSpans) {
                editable.removeSpan(span);
            }

            ParagraphStyle[] paraSpans = editable.getSpans(start, end, ParagraphStyle.class);
            for (ParagraphStyle span : paraSpans) {
                editable.removeSpan(span);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
     Returns the current checked state of the clear format style.
     */
    public boolean getIsChecked() {
        return this.mIsChecked;
    }
}
