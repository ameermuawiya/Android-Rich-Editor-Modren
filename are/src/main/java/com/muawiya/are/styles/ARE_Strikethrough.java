package com.muawiya.are.styles;

import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.muawiya.are.AREditText;
import com.google.android.material.button.MaterialButton;

public class ARE_Strikethrough extends ARE_ABS_Style<StrikethroughSpan> {

    private MaterialButton mStrikethroughButton;
    private boolean mStrikethroughChecked;
    private AREditText mEditText;

    /*
     Initializes the strikethrough style and sets the click listener
     for the provided material button.
     */
    public ARE_Strikethrough(MaterialButton strikethroughButton) {
        super(strikethroughButton.getContext());
        this.mStrikethroughButton = strikethroughButton;
        setListenerForButton(this.mStrikethroughButton);
    }

    /*
     Sets the target edit text where the strikethrough style
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
     Attaches a click listener to toggle the strikethrough status
     and apply the formatting to the selected text.
     */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStrikethroughChecked = !mStrikethroughChecked;
                ARE_Helper.updateCheckStatus(ARE_Strikethrough.this, mStrikethroughChecked);
                if (null != mEditText) {
                    applyStyle(mEditText.getEditableText(), mEditText.getSelectionStart(), mEditText.getSelectionEnd());
                }
            }
        });
    }

    /*
     Returns the material button used for the strikethrough style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mStrikethroughButton;
    }

    /*
     Updates the checked state of the strikethrough style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mStrikethroughChecked = isChecked;
    }

    /*
     Returns the current checked state of the strikethrough style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mStrikethroughChecked;
    }

    /*
     Creates and returns a new span instance for strikethrough text.
     */
    @Override
    public StrikethroughSpan newSpan() {
        return new StrikethroughSpan();
    }
}
