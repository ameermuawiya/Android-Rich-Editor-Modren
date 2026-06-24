package com.chinalwb.are.styles;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.AreLeadingMarginSpan;
import com.chinalwb.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;

public class ARE_IndentRight extends ARE_ABS_FreeStyle {

    private MaterialButton mIndentButton;
    private boolean mIsChecked;

    /*
     Initializes the right indent style and sets the click listener
     for the provided material button.
     */
    public ARE_IndentRight(MaterialButton indentButton, ARE_Toolbar toolbar) {
        super(toolbar);
        this.mIndentButton = indentButton;
        setListenerForButton(indentButton);
    }

    /*
     Attaches a click listener to increase the indentation level 
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
                AreLeadingMarginSpan[] existingLeadingSpans = editable.getSpans(start, end, AreLeadingMarginSpan.class);
                if (null != existingLeadingSpans && existingLeadingSpans.length == 1) {
                    AreLeadingMarginSpan currentLeadingMarginSpan = existingLeadingSpans[0];
                    int originalEnd = editable.getSpanEnd(currentLeadingMarginSpan);
                    editable.removeSpan(currentLeadingMarginSpan);
                
                    currentLeadingMarginSpan.increaseLevel();
                    editable.setSpan(currentLeadingMarginSpan, start, originalEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    editable.insert(start, Constants.ZERO_WIDTH_SPACE_STR);
                    start = Util.getThisLineStart(editText, currentLine);
                    end = Util.getThisLineEnd(editText, currentLine);
                    AreLeadingMarginSpan leadingMarginSpan = new AreLeadingMarginSpan(mContext);
                    leadingMarginSpan.increaseLevel();
                    editable.setSpan(leadingMarginSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
        });
    }

    /*
     Applies the right indent logic and handles consecutive line spacing.
     */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        AreLeadingMarginSpan[] leadingSpans = editable.getSpans(start, end, AreLeadingMarginSpan.class);
        if (null == leadingSpans || leadingSpans.length == 0) {
            return;
        }

        if (end > start) {
            char c = editable.charAt(end - 1);
            if (c == Constants.CHAR_NEW_LINE) {
                int leadingSpanSize = leadingSpans.length;
                int previousLeadingSpanIndex = leadingSpanSize - 1;
                if (previousLeadingSpanIndex > -1) {
                    AreLeadingMarginSpan previousLeadingSpan = leadingSpans[previousLeadingSpanIndex];
                    int lastLeadingItemSpanStartPos = editable.getSpanStart(previousLeadingSpan);

                    if (end > lastLeadingItemSpanStartPos) {
                        editable.removeSpan(previousLeadingSpan);
                        editable.setSpan(previousLeadingSpan, lastLeadingItemSpanStartPos, end - 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                
                    makeLineAsLeadingSpan(previousLeadingSpan.getLevel());
                }
            }
        } else {
            int spanStart = editable.getSpanStart(leadingSpans[0]);
            int spanEnd = editable.getSpanEnd(leadingSpans[0]);

            if (spanStart >= spanEnd) {
                editable.removeSpan(leadingSpans[0]);

                if (spanStart > 0) {
                    editable.delete(spanStart - 1, spanEnd);
                }
            }
        }
    }

    /*
     Marks the active line with the specified indentation level span.
     */
    private AreLeadingMarginSpan makeLineAsLeadingSpan(int level) {
        EditText editText = getEditText();
        int currentLine = Util.getCurrentCursorLine(editText);
        int start = Util.getThisLineStart(editText, currentLine);
        int end = Util.getThisLineEnd(editText, currentLine);
        Editable editable = editText.getText();
        editable.insert(start, Constants.ZERO_WIDTH_SPACE_STR);
        start = Util.getThisLineStart(editText, currentLine);
        end = Util.getThisLineEnd(editText, currentLine);

        if (editable.charAt(end - 1) == Constants.CHAR_NEW_LINE) {
            end--;
        }

        AreLeadingMarginSpan leadingMarginSpan = new AreLeadingMarginSpan(mContext);
        leadingMarginSpan.setLevel(level);
        editable.setSpan(leadingMarginSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return leadingMarginSpan;
    }

    /*
     Returns the material button used for the right indent style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mIndentButton;
    }

    /*
     Updates the checked state of the right indent style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    /*
     Returns the current checked state of the right indent style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mIsChecked;
    }
}
