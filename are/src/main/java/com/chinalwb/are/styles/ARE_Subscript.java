package com.chinalwb.are.styles;

import android.view.View;
import android.widget.EditText;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.spans.AreSubscriptSpan;
import com.google.android.material.button.MaterialButton;

public class ARE_Subscript extends ARE_ABS_Style<AreSubscriptSpan> {

    private MaterialButton mSubscriptButton;
    private boolean mSubscriptChecked;
    private AREditText mEditText;

    /*
     Initializes the subscript style and sets the click listener
     for the provided material button.
     */
    public ARE_Subscript(MaterialButton button) {
        super(button.getContext());
        this.mSubscriptButton = button;
        setListenerForButton(this.mSubscriptButton);
    }

    /*
     Sets the target edit text where the subscript style
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
     Attaches a click listener to toggle the subscript status
     and apply the formatting to the selected text.
     */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubscriptChecked = !mSubscriptChecked;
                ARE_Helper.updateCheckStatus(ARE_Subscript.this, mSubscriptChecked);
                if (null != mEditText) {
                    applyStyle(mEditText.getEditableText(),
                            mEditText.getSelectionStart(),
                            mEditText.getSelectionEnd());
                }
            }
        });
    }

    /*
     Returns the material button used for the subscript style.
     */
    @Override
    public MaterialButton getButton() {
        return mSubscriptButton;
    }

    /*
     Updates the checked state of the subscript style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mSubscriptChecked = isChecked;
    }

    /*
     Returns the current checked state of the subscript style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mSubscriptChecked;
    }

    /*
     Creates and returns a new span instance for subscript text.
     */
    @Override
    public AreSubscriptSpan newSpan() {
        return new AreSubscriptSpan();
    }
}
