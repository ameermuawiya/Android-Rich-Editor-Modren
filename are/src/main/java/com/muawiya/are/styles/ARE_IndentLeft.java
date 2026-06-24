package com.muawiya.are.styles;

import android.text.Editable;
import android.text.Spannable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.muawiya.are.Util;
import com.muawiya.are.spans.AreLeadingMarginSpan;
import com.muawiya.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;

public class ARE_IndentLeft extends ARE_ABS_FreeStyle {

    private MaterialButton mIndentButton;
    private boolean mIsChecked;

    /*
     Initializes the left indent style and sets the click listener
     for the provided material button.
     */
    public ARE_IndentLeft(MaterialButton indentButton, ARE_Toolbar toolbar) {
        super(toolbar);
        this.mIndentButton = indentButton;
        setListenerForButton(indentButton);
    }

    /*
     Attaches a click listener to decrease the indentation level 
     of the currently selected text line.
     */
    @Override
    public void setListenerForButton(MaterialButton button) {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = getEditText();
                int currentLine = Util.getCurrentCursorLine(editText);
                int start = Util.getThisLineStart(editText, currentLine);
                int end = Util.getThisLineEnd(editText, currentLine);

                Editable editable = editText.getText();
                AreLeadingMarginSpan[] existingLMSpans = editable.getSpans(start, end, AreLeadingMarginSpan.class);
                if (null != existingLMSpans && existingLMSpans.length == 1) {
                    AreLeadingMarginSpan currentLeadingMarginSpan = existingLMSpans[0];
                    int originalEnd = editable.getSpanEnd(currentLeadingMarginSpan);
                    editable.removeSpan(currentLeadingMarginSpan);
                    int currentLevel = currentLeadingMarginSpan.decreaseLevel();
                    if (currentLevel > 0) {
                        editable.setSpan(currentLeadingMarginSpan, start, originalEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                }
            }
        });
    }

    /*
     Applies the style formatting. Not required for left indent.
     */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        // Do nothing
    }

    /*
     Returns the material button used for the left indent style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mIndentButton;
    }

    /*
     Updates the checked state of the left indent style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    /*
     Returns the current checked state of the left indent style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mIsChecked;
    }
}
