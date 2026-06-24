package com.chinalwb.are.styles;

import android.view.View;
import android.widget.EditText;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.spans.AreSuperscriptSpan;
import com.google.android.material.button.MaterialButton;

public class ARE_Superscript extends ARE_ABS_Style<AreSuperscriptSpan> {

    private MaterialButton mSuperscriptButton;
    private boolean mSuperscriptChecked;
    private AREditText mEditText;

    /*
     Initializes the superscript style and sets the click listener
     for the provided material button.
     */
    public ARE_Superscript(MaterialButton button) {
        super(button.getContext());
        this.mSuperscriptButton = button;
        setListenerForButton(this.mSuperscriptButton);
    }

    /*
     Sets the target edit text where the superscript style
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
     Attaches a click listener to toggle the superscript status
     and apply the formatting to the selected text.
     */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSuperscriptChecked = !mSuperscriptChecked;
                ARE_Helper.updateCheckStatus(ARE_Superscript.this, mSuperscriptChecked);
                if (null != mEditText) {
                    applyStyle(mEditText.getEditableText(),
                            mEditText.getSelectionStart(),
                            mEditText.getSelectionEnd());
                }
            }
        });
    }

    /*
     Returns the material button used for the superscript style.
     */
    @Override
    public MaterialButton getButton() {
        return mSuperscriptButton;
    }

    /*
     Updates the checked state of the superscript style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mSuperscriptChecked = isChecked;
    }

    /*
     Returns the current checked state of the superscript style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mSuperscriptChecked;
    }

    /*
     Creates and returns a new span instance for superscript text.
     */
    @Override
    public AreSuperscriptSpan newSpan() {
        return new AreSuperscriptSpan();
    }
}
