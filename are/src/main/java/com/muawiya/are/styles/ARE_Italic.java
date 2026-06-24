package com.muawiya.are.styles;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.muawiya.are.AREditText;
import com.muawiya.are.spans.AreItalicSpan;
import com.google.android.material.button.MaterialButton;

public class ARE_Italic extends ARE_ABS_Style<AreItalicSpan> {

    private MaterialButton mItalicButton;
    private boolean mItalicChecked;
    private AREditText mEditText;

    /*
     Initializes the italic style and sets the click listener
     for the provided material button.
     */
    public ARE_Italic(MaterialButton italicButton) {
        super(italicButton.getContext());
        this.mItalicButton = italicButton;
        setListenerForButton(this.mItalicButton);
    }

    /*
     Sets the target edit text where the italic style
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
     Attaches a click listener to toggle the italic status
     and apply the formatting to the selected text.
     */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mItalicChecked = !mItalicChecked;
                ARE_Helper.updateCheckStatus(ARE_Italic.this, mItalicChecked);
                if (null != mEditText) {
                    applyStyle(mEditText.getEditableText(), mEditText.getSelectionStart(), mEditText.getSelectionEnd());
                }
            }
        });
    }

    /*
     Returns the material button used for the italic style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mItalicButton;
    }

    /*
     Updates the checked state of the italic style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mItalicChecked = isChecked;
    }

    /*
     Returns the current checked state of the italic style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mItalicChecked;
    }

    /*
     Creates and returns a new span instance for italic text.
     */
    @Override
    public AreItalicSpan newSpan() {
        return new AreItalicSpan();
    }
}
