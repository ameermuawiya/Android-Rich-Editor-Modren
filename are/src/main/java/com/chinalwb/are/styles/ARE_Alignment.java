package com.chinalwb.are.styles;

import android.text.Editable;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.AlignmentSpan.Standard;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;

public class ARE_Alignment extends ARE_ABS_FreeStyle {

    private MaterialButton mAlignmentButton;
    private Alignment mAlignment;
    private boolean mIsChecked;

    /*
     Initializes the alignment style and sets the click listener
     for the provided MaterialButton.
     */
    public ARE_Alignment(MaterialButton button, Alignment alignment, ARE_Toolbar toolbar) {
        super(toolbar);
        this.mAlignmentButton = button;
        this.mAlignment = alignment;
        setListenerForButton(this.mAlignmentButton);
    }

    /*
     Attaches a click listener to the alignment button to apply
     the corresponding alignment span to the selected text line.
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
                
                Editable editable = editText.getEditableText();
                
                Standard[] alignmentSpans = editable.getSpans(start, end, Standard.class);
                if (null != alignmentSpans) {
                    for (Standard span : alignmentSpans) {
                        editable.removeSpan(span);
                    }
                }
                
                AlignmentSpan alignCenterSpan = new Standard(mAlignment);
                if (start == end) {
                    editable.insert(start, Constants.ZERO_WIDTH_SPACE_STR);
                    end = Util.getThisLineEnd(editText, currentLine);
                }
                editable.setSpan(alignCenterSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        });
    }

    /*
     Applies the alignment logic and handles line breaks dynamically
     while the user is typing.
     */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        AlignmentSpan[] alignmentSpans = editable.getSpans(start, end, AlignmentSpan.class);
        if (null == alignmentSpans || alignmentSpans.length == 0) {
            return;
        }
        
        Alignment alignment = alignmentSpans[0].getAlignment();
        if (mAlignment != alignment) {
            return;
        }

        if (end > start) {
            char c = editable.charAt(end - 1);
            if (c == Constants.CHAR_NEW_LINE) {
                int alignmentSpansSize = alignmentSpans.length;
                int previousAlignmentSpanIndex = alignmentSpansSize - 1;
                if (previousAlignmentSpanIndex > -1) {
                    AlignmentSpan previousAlignmentSpan = alignmentSpans[previousAlignmentSpanIndex];
                    int lastAlignmentSpanStartPos = editable.getSpanStart(previousAlignmentSpan);
                    if (end > lastAlignmentSpanStartPos) {
                        editable.removeSpan(previousAlignmentSpan);
                        editable.setSpan(previousAlignmentSpan, lastAlignmentSpanStartPos, end - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                
                    markLineAsAlignmentSpan(mAlignment);
                }  
            }
        } 
        else {
            int spanStart = editable.getSpanStart(alignmentSpans[0]);
            int spanEnd = editable.getSpanEnd(alignmentSpans[0]);

            if (spanStart >= spanEnd) {
                editable.removeSpan(alignmentSpans[0]);

                if (spanStart > 0) {
                    editable.delete(spanStart - 1, spanEnd);
                }
            }
        }
    }

    /*
     Marks the newly created line with the active alignment span.
     */
    private void markLineAsAlignmentSpan(Alignment alignment) {
        EditText editText = getEditText();
        int currentLine = Util.getCurrentCursorLine(editText);
        int start = Util.getThisLineStart(editText, currentLine);
        int end = Util.getThisLineEnd(editText, currentLine);
        Editable editable = editText.getText();
        editable.insert(start, Constants.ZERO_WIDTH_SPACE_STR);
        start = Util.getThisLineStart(editText, currentLine);
        end = Util.getThisLineEnd(editText, currentLine);

        if (end < 1) {
            return;
        }

        if (editable.charAt(end - 1) == Constants.CHAR_NEW_LINE) {
            end--;
        }

        AlignmentSpan alignmentSpan = new Standard(alignment);
        editable.setSpan(alignmentSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }

    /*
     Returns the MaterialButton of this style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mAlignmentButton;
    }

    /*
     Sets the checked state of the alignment style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    /*
     Returns the current checked state.
     */
    @Override
    public boolean getIsChecked() {
        return this.mIsChecked;
    }
}
