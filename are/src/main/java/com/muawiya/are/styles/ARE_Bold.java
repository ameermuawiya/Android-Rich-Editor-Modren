package com.muawiya.are.styles;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.muawiya.are.AREditText;
import com.muawiya.are.spans.AreBoldSpan;
import com.google.android.material.button.MaterialButton;

public class ARE_Bold extends ARE_ABS_Style<AreBoldSpan> {

    private MaterialButton mBoldButton;
    private boolean mBoldChecked;
    private AREditText mEditText;

    /*
     Initializes the bold style and sets the click listener
     for the provided material button.
     */
    public ARE_Bold(MaterialButton button) {
        super(button.getContext());
        this.mBoldButton = button;
        setListenerForButton(this.mBoldButton);
    }

    /*
     Sets the target edit text where the bold style
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
     Attaches a click listener to toggle the bold status
     and apply the formatting to the selected text.
     */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBoldChecked = !mBoldChecked;
                ARE_Helper.updateCheckStatus(ARE_Bold.this, mBoldChecked);
                if (null != mEditText) {
                    applyStyle(mEditText.getEditableText(),
                            mEditText.getSelectionStart(),
                            mEditText.getSelectionEnd());
                }
            }
        });
    }

    /*
     Returns the material button used for the bold style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mBoldButton;
    }

    /*
     Updates the checked state of the bold style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mBoldChecked = isChecked;
    }

    /*
     Returns the current checked state of the bold style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mBoldChecked;
    }

    /*
     Creates and returns a new span instance for bold text.
     */
    @Override
    public AreBoldSpan newSpan() {
        return new AreBoldSpan();
    }
}
