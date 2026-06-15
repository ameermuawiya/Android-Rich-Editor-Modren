package com.chinalwb.are.styles;

import android.content.Context;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.Constants;
import com.google.android.material.button.MaterialButton;

public class ARE_BackgroundColor extends ARE_ABS_Style<BackgroundColorSpan> {

    private MaterialButton mBackgroundButton;
    private boolean mBackgroundChecked;
    private Context mContext;
    private AREditText mEditText;

    /*
    Initializes the background color style and sets the click listener.
    Removes hardcoded colors to fetch them dynamically upon span creation.
    */
    public ARE_BackgroundColor(MaterialButton button) {
        super(button.getContext());
        this.mContext = button.getContext();
        this.mBackgroundButton = button;
        setListenerForButton(this.mBackgroundButton);
    }

    /*
    Sets the target edit text where the background color style
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
    Attaches a click listener to toggle the background color status
    and apply the formatting to the selected text.
    */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundChecked = !mBackgroundChecked;
                ARE_Helper.updateCheckStatus(ARE_BackgroundColor.this, mBackgroundChecked);
                if (null != mEditText) {
                    applyStyle(mEditText.getEditableText(), mEditText.getSelectionStart(), mEditText.getSelectionEnd());
                }
            }
        });
    }

    /*
    Returns the material button used for the background color style.
    */
    @Override
    public MaterialButton getButton() {
        return this.mBackgroundButton;
    }

    /*
    Updates the checked state of the background color style.
    */
    @Override
    public void setChecked(boolean isChecked) {
        this.mBackgroundChecked = isChecked;
    }

    /*
    Returns the current checked state of the background color style.
    */
    @Override
    public boolean getIsChecked() {
        return this.mBackgroundChecked;
    }

    /*
    Creates and returns a new span instance for the background color.
    Fetches the dynamic transparent theme color in real-time.
    */
    @Override
    public BackgroundColorSpan newSpan() {
        int dynamicColor = Constants.getDefaultHighlightColor(mContext);
        return new BackgroundColorSpan(dynamicColor);
    }
}
