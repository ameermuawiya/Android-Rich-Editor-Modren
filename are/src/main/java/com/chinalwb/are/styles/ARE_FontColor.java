package com.chinalwb.are.styles;

import android.text.Editable;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.Util;
import com.chinalwb.are.colorpicker.ColorPickerListener;
import com.chinalwb.are.spans.AreForegroundColorSpan;
import com.chinalwb.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;

public class ARE_FontColor extends ARE_ABS_Dynamic_Style<AreForegroundColorSpan> {

    private MaterialButton mFontColorButton;
    private ARE_Toolbar mToolbar;
    private AREditText mEditText;
    private int mColor = -1;
    private boolean mIsChecked;

    private ColorPickerListener mColorPickerListener = new ColorPickerListener() {
        @Override
        public void onPickColor(int color) {
            mColor = color;
            if (null != mEditText) {
                Editable editable = mEditText.getEditableText();
                int start = mEditText.getSelectionStart();
                int end = mEditText.getSelectionEnd();

                if (end > start) {
                    applyNewStyle(editable, start, end, mColor);
                }
            }
        }
    };

    /*
     Initializes the font color style and sets the click listener
     for the provided material button.
     */
    public ARE_FontColor(MaterialButton fontColorButton, ARE_Toolbar toolbar) {
        super(fontColorButton.getContext());
        this.mFontColorButton = fontColorButton;
        this.mToolbar = toolbar;
        setListenerForButton(this.mFontColorButton);
    }

    /*
     Sets the target edit text where the font color style
     will be applied.
     */
    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    /*
     Attaches a click listener to toggle the color palette
     for font color selection.
     */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mToolbar.toggleColorPalette(mColorPickerListener);
            }
        });
    }

    /*
     Applies the newly selected color if it differs from the existing span color.
     */
    @Override
    protected void changeSpanInsideStyle(Editable editable, int start, int end, AreForegroundColorSpan existingSpan) {
        int currentColor = existingSpan.getForegroundColor();
        if (currentColor != mColor) {
            Util.log("color changed before: " + currentColor + ", new == " + mColor);
            applyNewStyle(editable, start, end, mColor);
            logAllFontColorSpans(editable);
        }
    }

    private void logAllFontColorSpans(Editable editable) {
        ForegroundColorSpan[] listItemSpans = editable.getSpans(0,
                editable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : listItemSpans) {
            int ss = editable.getSpanStart(span);
            int se = editable.getSpanEnd(span);
            Util.log("List All: " + " :: start == " + ss + ", end == " + se);
        }
    }

    /*
     Creates and returns a new span instance for font color.
     */
    @Override
    public AreForegroundColorSpan newSpan() {
        return new AreForegroundColorSpan(this.mColor);
    }

    /*
     Returns the material button used for the font color style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mFontColorButton;
    }

    /*
     Updates the checked state of the font color style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    /*
     Returns the current checked state of the font color style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mIsChecked;
    }

    /*
     Returns the currently attached edit text instance.
     */
    @Override
    public EditText getEditText() {
        return this.mEditText;
    }

    /*
     Creates a new foreground color span with the specified color.
     */
    @Override
    protected AreForegroundColorSpan newSpan(int color) {
        return new AreForegroundColorSpan(color);
    }

    /*
     Hook triggered when the feature is updated to apply the selected palette color.
     */
    @Override
    protected void featureChangedHook(int lastSpanColor) {
        mColor = lastSpanColor;
        mToolbar.setColorPaletteColor(mColor);
    }
}
