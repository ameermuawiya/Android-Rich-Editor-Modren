package com.chinalwb.are.styles;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.spans.AreUnderlineSpan;
import com.google.android.material.button.MaterialButton;

public class ARE_Underline extends ARE_ABS_Style<AreUnderlineSpan> {

    private MaterialButton mUnderlineButton;
    private boolean mUnderlineChecked;
    private AREditText mEditText;

    /*
     Initializes the underline style and sets the click listener
     for the provided material button.
     */
    public ARE_Underline(MaterialButton button) {
        super(button.getContext());
        this.mUnderlineButton = button;
        setListenerForButton(this.mUnderlineButton);
    }

    /*
     Sets the target edit text where the underline style
     will be applied.
     */
    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    /*
     Returns the currently attached edit text instance.
     */
    @Override
    public EditText getEditText() {
        return mEditText;
    }

    /*
     Attaches a click listener to toggle the underline status
     and apply the formatting to the selected text.
     */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUnderlineChecked = !mUnderlineChecked;
                ARE_Helper.updateCheckStatus(ARE_Underline.this, mUnderlineChecked);
                if (null != mEditText) {
                    applyStyle(mEditText.getEditableText(), mEditText.getSelectionStart(), mEditText.getSelectionEnd());
                }
            }
        });
    }

    /*
     Returns the material button used for the underline style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mUnderlineButton;
    }

    /*
     Updates the checked state of the underline style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mUnderlineChecked = isChecked;
    }

    /*
     Returns the current checked state of the underline style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mUnderlineChecked;
    }

    /*
     Creates and returns a new span instance for underline text.
     */
    @Override
    public AreUnderlineSpan newSpan() {
        return new AreUnderlineSpan();
    }
}
