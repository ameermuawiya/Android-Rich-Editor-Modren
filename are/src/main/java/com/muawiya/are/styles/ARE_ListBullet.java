package com.muawiya.are.styles;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.muawiya.are.Constants;
import com.muawiya.are.Util;
import com.muawiya.are.spans.ListBulletSpan;
import com.muawiya.are.spans.ListNumberSpan;
import com.muawiya.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;

public class ARE_ListBullet extends ARE_ABS_FreeStyle {

    private MaterialButton mListBulletButton;
    private boolean mIsChecked;

    /*
     Initializes the bullet list style and sets the click listener
     for the provided material button.
     */
    public ARE_ListBullet(MaterialButton button, ARE_Toolbar toolbar) {
        super(toolbar);
        this.mListBulletButton = button;
        setListenerForButton(this.mListBulletButton);
    }

    /*
     Attaches a click listener to handle the conversion of text lines
     into bulleted lists and manages interactions with numbered lists.
     */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = getEditText();
                int currentLine = Util.getCurrentCursorLine(editText);
                int start = Util.getThisLineStart(editText, currentLine);
                int end = Util.getThisLineEnd(editText, currentLine);

                Editable editable = editText.getText();

                int selectionStart = editText.getSelectionStart();
                int selectionEnd = editText.getSelectionEnd();
                ListNumberSpan[] listNumberSpans = editable.getSpans(selectionStart,
                        selectionEnd, ListNumberSpan.class);
                if (null != listNumberSpans && listNumberSpans.length > 0) {
                    changeListNumberSpanToListBulletSpan(editable, listNumberSpans);
                    return;
                }

                ListBulletSpan[] listBulletSpans = editable.getSpans(start,
                        end, ListBulletSpan.class);
                if (null == listBulletSpans || listBulletSpans.length == 0) {
                    ListBulletSpan[] aheadListItemSpans = editable.getSpans(
                            start - 2, start - 1, ListBulletSpan.class);
                    if (null != aheadListItemSpans && aheadListItemSpans.length > 0) {
                        ListBulletSpan previousListItemSpan = aheadListItemSpans[aheadListItemSpans.length - 1];
                        if (null != previousListItemSpan) {
                            int pStart = editable.getSpanStart(previousListItemSpan);
                            int pEnd = editable.getSpanEnd(previousListItemSpan);

                            if (editable.charAt(pEnd - 1) == Constants.CHAR_NEW_LINE) {
                                editable.removeSpan(previousListItemSpan);
                                editable.setSpan(previousListItemSpan, pStart,
                                        pEnd - 1,
                                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            }

                            makeLineAsBullet();
                        }
                    } else {
                        makeLineAsBullet();
                    }
                } else {
                    editable.removeSpan(listBulletSpans[0]);
                }
            }
        });
    }

    /*
     Applies the formatting logic and handles span combinations when text
     is edited or deleted within the lists.
     */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        ListBulletSpan[] listSpans = editable.getSpans(start, end, ListBulletSpan.class);
        if (null == listSpans || listSpans.length == 0) {
            return;
        }

        if (end > start) {
            char c = editable.charAt(end - 1);
            if (c == Constants.CHAR_NEW_LINE) {
                int listSpanSize = listSpans.length;
                int previousListSpanIndex = listSpanSize - 1;
                if (previousListSpanIndex > -1) {
                    ListBulletSpan previousListSpan = listSpans[previousListSpanIndex];
                    int lastListItemSpanStartPos = editable.getSpanStart(previousListSpan);
                    int lastListItemSpanEndPos = editable.getSpanEnd(previousListSpan);
                    CharSequence listItemSpanContent = editable.subSequence(lastListItemSpanStartPos, lastListItemSpanEndPos);

                    if (isEmptyListItemSpan(listItemSpanContent)) {
                        editable.removeSpan(previousListSpan);
                        editable.delete(lastListItemSpanStartPos, lastListItemSpanEndPos);
                        return;
                    } else {
                        if (end > lastListItemSpanStartPos) {
                            editable.removeSpan(previousListSpan);
                            editable.setSpan(previousListSpan,
                                    lastListItemSpanStartPos, end - 1,
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                    }
                    makeLineAsBullet();
                }
            }
        } else {
            ListBulletSpan theFirstSpan = listSpans[0];
            if (listSpans.length > 0) {
                FindFirstAndLastBulletSpan findFirstAndLastBulletSpan = new FindFirstAndLastBulletSpan(editable, listSpans).invoke();
                theFirstSpan = findFirstAndLastBulletSpan.getFirstTargetSpan();
            }
            int spanStart = editable.getSpanStart(theFirstSpan);
            int spanEnd = editable.getSpanEnd(theFirstSpan);

            if (spanStart >= spanEnd) {
                for (ListBulletSpan listSpan : listSpans) {
                    editable.removeSpan(listSpan);
                }

                if (spanStart > 0) {
                    editable.delete(spanStart - 1, spanEnd);
                }
            } else if (start == spanStart) {
                return;
            } else if (start == spanEnd) {
                if (editable.length() > start) {
                    if (editable.charAt(start) == Constants.CHAR_NEW_LINE) {
                        ListBulletSpan[] spans = editable.getSpans(start, start, ListBulletSpan.class);
                        if (spans.length > 0) {
                            mergeForward(editable, theFirstSpan, spanStart, spanEnd);
                        }
                    } else {
                        mergeForward(editable, theFirstSpan, spanStart, spanEnd);
                    }
                }
            } else if (start > spanStart && end < spanEnd) {
                return;
            }
        }
    }

    /*
     Merges consecutive bullet list spans to maintain proper formatting and spacing.
     */
    protected void mergeForward(Editable editable, ListBulletSpan listSpan, int spanStart, int spanEnd) {
        if (editable.length() <= spanEnd + 1) {
            return;
        }
        ListBulletSpan[] targetSpans = editable.getSpans(spanEnd, spanEnd + 1, ListBulletSpan.class);
        if (targetSpans == null || targetSpans.length == 0) {
            return;
        }

        FindFirstAndLastBulletSpan findFirstAndLastBulletSpan = new FindFirstAndLastBulletSpan(editable, targetSpans).invoke();
        ListBulletSpan firstTargetSpan = findFirstAndLastBulletSpan.getFirstTargetSpan();
        ListBulletSpan lastTargetSpan = findFirstAndLastBulletSpan.getLastTargetSpan();
        int targetStart = editable.getSpanStart(firstTargetSpan);
        int targetEnd = editable.getSpanEnd(lastTargetSpan);

        int targetLength = targetEnd - targetStart;
        spanEnd = spanEnd + targetLength;

        for (ListBulletSpan targetSpan : targetSpans) {
            editable.removeSpan(targetSpan);
        }
        ListBulletSpan[] compositeSpans = editable.getSpans(spanStart, spanEnd, ListBulletSpan.class);
        for (ListBulletSpan lns : compositeSpans) {
            editable.removeSpan(lns);
        }
        editable.setSpan(listSpan, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    /*
     Checks if the current list item span is empty.
     */
    private boolean isEmptyListItemSpan(CharSequence listItemSpanContent) {
        int spanLen = listItemSpanContent.length();
        return spanLen == 2;
    }

    /*
     Formats the current line into a bullet list item.
     */
    private ListBulletSpan makeLineAsBullet() {
        EditText editText = getEditText();
        int currentLine = Util.getCurrentCursorLine(editText);
        int start = Util.getThisLineStart(editText, currentLine);
        Editable editable = editText.getText();
        editable.insert(start, Constants.ZERO_WIDTH_SPACE_STR);
        start = Util.getThisLineStart(editText, currentLine);
        int end = Util.getThisLineEnd(editText, currentLine);

        if (end < 1) {
            return null;
        }
        if (editable.charAt(end - 1) == Constants.CHAR_NEW_LINE) {
            end--;
        }

        ListBulletSpan BulletListItemSpan = new ListBulletSpan();
        editable.setSpan(BulletListItemSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return BulletListItemSpan;
    }

    /*
     Changes existing numbered list spans into bullet list spans
     and restarts the numbering sequence behind it.
     */
    private void changeListNumberSpanToListBulletSpan(Editable editable, ListNumberSpan[] listNumberSpans) {
        if (null == listNumberSpans || listNumberSpans.length == 0) {
            return;
        }

        int len = listNumberSpans.length;
        ListNumberSpan lastListNumberSpan = listNumberSpans[len - 1];
        int lastListNumberSpanEnd = editable.getSpanEnd(lastListNumberSpan);
        
        editable.insert(lastListNumberSpanEnd, Constants.ZERO_WIDTH_SPACE_STR);
        editable.delete(lastListNumberSpanEnd + 1, lastListNumberSpanEnd + 1);
        
        ARE_ListNumber.reNumberBehindListItemSpans(lastListNumberSpanEnd + 1, editable, 0);
        
        for (ListNumberSpan listNumberSpan : listNumberSpans) {
            int start = editable.getSpanStart(listNumberSpan);
            int end = editable.getSpanEnd(listNumberSpan);
            
            editable.removeSpan(listNumberSpan);
            ListBulletSpan listBulletSpan = new ListBulletSpan();
            editable.setSpan(listBulletSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    /*
     Returns the material button used for the bullet list style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mListBulletButton;
    }

    /*
     Updates the checked state of the bullet list style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    /*
     Returns the current checked state of the bullet list style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mIsChecked;
    }

    /*
     Helper class to find the first and last spans from an array
     of target spans within an editable text block.
     */
    private class FindFirstAndLastBulletSpan {
        private Editable editable;
        private ListBulletSpan[] targetSpans;
        private ListBulletSpan firstTargetSpan;
        private ListBulletSpan lastTargetSpan;

        public FindFirstAndLastBulletSpan(Editable editable, ListBulletSpan... targetSpans) {
            this.editable = editable;
            this.targetSpans = targetSpans;
        }

        public ListBulletSpan getFirstTargetSpan() {
            return firstTargetSpan;
        }

        public ListBulletSpan getLastTargetSpan() {
            return lastTargetSpan;
        }

        public FindFirstAndLastBulletSpan invoke() {
            firstTargetSpan = targetSpans[0];
            lastTargetSpan = targetSpans[0];
            if (targetSpans.length > 0) {
                int firstTargetSpanStart = editable.getSpanStart(firstTargetSpan);
                int lastTargetSpanEnd = editable.getSpanEnd(firstTargetSpan);
                for (ListBulletSpan lns : targetSpans) {
                    int lnsStart = editable.getSpanStart(lns);
                    int lnsEnd = editable.getSpanEnd(lns);
                    if (lnsStart < firstTargetSpanStart) {
                        firstTargetSpan = lns;
                        firstTargetSpanStart = lnsStart;
                    }
                    if (lnsEnd > lastTargetSpanEnd) {
                        lastTargetSpan = lns;
                        lastTargetSpanEnd = lnsEnd;
                    }
                }
            }
            return this;
        }
    }
}
