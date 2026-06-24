package com.chinalwb.are.styles;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.Constants;
import com.chinalwb.are.spans.AreHrSpan;
import com.chinalwb.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;

public class ARE_Hr extends ARE_ABS_FreeStyle {

    private AREditText mEditText;
    private MaterialButton mHrButton;
    private boolean mIsChecked;

    /*
     Initializes the Horizontal Rule style and sets the click listener
     for the provided material button.
     */
    public ARE_Hr(MaterialButton button, ARE_Toolbar toolbar) {
        super(toolbar);
        this.mHrButton = button;
        setListenerForButton(this.mHrButton);
    }

    /*
     Sets the target edit text where the horizontal rule
     will be inserted.
     */
    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    /*
     Attaches a click listener to insert a horizontal rule span
     at the current cursor position.
     */
    @Override
    public void setListenerForButton(MaterialButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = mEditText.getEditableText();
                int start = mEditText.getSelectionStart();
                int end = mEditText.getSelectionEnd();

                SpannableStringBuilder ssb = new SpannableStringBuilder();
                ssb.append(Constants.CHAR_NEW_LINE);
                ssb.append(Constants.CHAR_NEW_LINE);
                ssb.append(Constants.ZERO_WIDTH_SPACE_STR);
                ssb.append(Constants.CHAR_NEW_LINE);
                ssb.setSpan(new AreHrSpan(), 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                editable.replace(start, end, ssb);
            }
        });
    }

    /*
     Applies the style formatting. Not required for HR span insertion.
     */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        // Do nothing
    }

    /*
     Returns the material button used for the horizontal rule style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mHrButton;
    }

    /*
     Updates the checked state of the horizontal rule style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    /*
     Returns the current checked state of the horizontal rule style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mIsChecked;
    }
}
