package com.muawiya.are.styles;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.muawiya.are.AREditText;
import com.muawiya.are.Constants;
import com.muawiya.are.Util;
import com.muawiya.are.spans.AreQuoteSpan;
import com.google.android.material.button.MaterialButton;

public class ARE_Quote implements IARE_Style {

    private MaterialButton mQuoteButton;
    private boolean mQuoteChecked;
    private AREditText mEditText;
    private boolean mRemovedNewLine;

    /*
    Initializes the quote style and sets the click listener
    for the provided material button.
    */
    public ARE_Quote(MaterialButton quoteButton) {
        this.mQuoteButton = quoteButton;
        setListenerForButton(this.mQuoteButton);
    }

    /*
    Sets the target edit text where the quote style
    will be applied.
    */
    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    /*
    Attaches a click listener to toggle the blockquote status
    and apply or remove the formatting to the current line.
    */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuoteChecked = !mQuoteChecked;
                ARE_Helper.updateCheckStatus(ARE_Quote.this, mQuoteChecked);
                if (null != mEditText) {
                    if (mQuoteChecked) {
                        makeLineAsQuote();
                    } else {
                        removeQuote();
                    }
                }
            }
        });
    }

    /*
    Applies the quote span to the current line where the cursor is positioned.
    Fetches the dynamic color from Constants to support Light/Dark themes.
    */
    private void makeLineAsQuote() {
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

        AreQuoteSpan[] existingQuoteSpans = editable.getSpans(start, end, AreQuoteSpan.class);
        if (existingQuoteSpans != null && existingQuoteSpans.length > 0) {
            return;
        }
        if (start > 2) {
            existingQuoteSpans = editable.getSpans(start - 2, start, AreQuoteSpan.class);
            if (existingQuoteSpans != null && existingQuoteSpans.length > 0) {
                int quoteStart = editable.getSpanStart(existingQuoteSpans[0]);
                editable.setSpan(existingQuoteSpans[0], quoteStart, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                return;
            }
        }
        
        // Fetch the dynamic quote color based on the current theme
        int quoteColor = Constants.getQuoteColor(mEditText.getContext());
        AreQuoteSpan quoteSpan = new AreQuoteSpan(quoteColor);
        
        editable.setSpan(quoteSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ARE_Helper.updateCheckStatus(ARE_Quote.this, true);
    }

    /*
    Removes the quote span from the current line where the cursor is positioned.
    */
    private void removeQuote() {
        EditText editText = getEditText();
        Editable editable = editText.getText();
        int currentLine = Util.getCurrentCursorLine(editText);
        int start = Util.getThisLineStart(editText, currentLine);
        int end = Util.getThisLineEnd(editText, currentLine);
        AreQuoteSpan[] quoteSpans = null;
        if (start == 0) {
            quoteSpans = editable.getSpans(start, end, AreQuoteSpan.class);
            editable.removeSpan(quoteSpans[0]);
            return;
        } else {
            quoteSpans = editable.getSpans(start - 1, end, AreQuoteSpan.class);
        }
        if (quoteSpans == null || quoteSpans.length == 0) {
            quoteSpans = editable.getSpans(start, end, AreQuoteSpan.class);
            if (quoteSpans != null && quoteSpans.length == 0) {
                return;
            }
        }
        int quoteStart = editable.getSpanStart(quoteSpans[0]);
        editable.removeSpan(quoteSpans[0]);
        if (start > quoteStart) {
            editable.setSpan(quoteSpans[0], quoteStart, start - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    /*
    Handles new lines and text deletions inside a quote span block.
    */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        AreQuoteSpan[] quoteSpans = editable.getSpans(start, end, AreQuoteSpan.class);
        if (null == quoteSpans || quoteSpans.length == 0) {
            return;
        }

        if (end > start) {
            char c = editable.charAt(end - 1);
            if (c == Constants.CHAR_NEW_LINE) {
                editable.append(Constants.ZERO_WIDTH_SPACE_STR);
            }
        } else {
            AreQuoteSpan quoteSpan = quoteSpans[0];
            int spanStart = editable.getSpanStart(quoteSpan);
            int spanEnd = editable.getSpanEnd(quoteSpan);
            
            if (spanStart == spanEnd) {
                setChecked(false);
                ARE_Helper.updateCheckStatus(ARE_Quote.this, false);
                removeQuote();
            }
            if (end > 2) {
                if (mRemovedNewLine) {
                    mRemovedNewLine = false;
                    return;
                }
                char pChar = editable.charAt(end - 1);
                if (pChar == Constants.CHAR_NEW_LINE) {
                    mRemovedNewLine = true;
                    editable.delete(end - 1, end);
                }
            }
        }
    }

    /*
    Returns the material button used for the quote style.
    */
    @Override
    public MaterialButton getButton() {
        return this.mQuoteButton;
    }

    /*
    Updates the checked state of the quote style.
    */
    @Override
    public void setChecked(boolean isChecked) {
        this.mQuoteChecked = isChecked;
    }

    /*
    Returns the current checked state of the quote style.
    */
    @Override
    public boolean getIsChecked() {
        return this.mQuoteChecked;
    }

    /*
    Returns the currently attached edit text instance.
    */
    @Override
    public EditText getEditText() {
        return this.mEditText;
    }
}
