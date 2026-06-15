package com.chinalwb.are.styles;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.ListBulletSpan;
import com.chinalwb.are.spans.ListNumberSpan;
import com.chinalwb.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ARE_ListNumber extends ARE_ABS_FreeStyle {

    private MaterialButton mListNumberButton;
    private boolean mIsChecked;
    private boolean toMergeForward = false;

    /*
     Initializes the numbered list style and sets the click listener
     for the provided material button.
     */
    public ARE_ListNumber(MaterialButton button, ARE_Toolbar toolbar) {
        super(toolbar);
        this.mListNumberButton = button;
        setListenerForButton(this.mListNumberButton);
    }

    /*
     Attaches a click listener to handle the conversion of text lines
     into numbered lists and manages interactions with bulleted lists.
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
                ListBulletSpan[] listBulletSpans = editable.getSpans(selectionStart,
                        selectionEnd, ListBulletSpan.class);
                if (null != listBulletSpans && listBulletSpans.length > 0) {
                    changeListBulletSpanToListNumberSpan(editable, listBulletSpans);
                    return;
                }

                ListNumberSpan[] listNumberSpans = editable.getSpans(start, end,
                        ListNumberSpan.class);
                if (null == listNumberSpans || listNumberSpans.length == 0) {
                    int thisNumber = 1;
                    ListNumberSpan[] aheadListItemSpans = editable.getSpans(
                            start - 2, start - 1, ListNumberSpan.class);
                    if (null != aheadListItemSpans && aheadListItemSpans.length > 0) {
                        ListNumberSpan previousListItemSpan = aheadListItemSpans[aheadListItemSpans.length - 1];
                        if (null != previousListItemSpan) {
                            int pStart = editable.getSpanStart(previousListItemSpan);
                            int pEnd = editable.getSpanEnd(previousListItemSpan);

                            if (editable.charAt(pEnd - 1) == Constants.CHAR_NEW_LINE) {
                                editable.removeSpan(previousListItemSpan);
                                editable.setSpan(previousListItemSpan, pStart,
                                        pEnd - 1,
                                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            }

                            int previousNumber = previousListItemSpan.getNumber();
                            thisNumber = previousNumber + 1;
                            makeLineAsList(thisNumber);
                        }
                    } else {
                        thisNumber = 1;
                        makeLineAsList(1);
                    }

                    reNumberBehindListItemSpans(end, editable, thisNumber);
                } else {
                    ListNumberSpan currentLineListItemSpan = listNumberSpans[0];
                    int spanEnd = editable.getSpanEnd(currentLineListItemSpan);
                    editable.removeSpan(currentLineListItemSpan);

                    editable.insert(spanEnd, Constants.ZERO_WIDTH_SPACE_STR);
                    editable.delete(spanEnd, spanEnd + 1);

                    reNumberBehindListItemSpans(spanEnd, editable, 0);
                }
            }
        });
    }

    /*
     Applies the formatting logic and handles span combinations when text
     is edited or deleted within the numbered lists.
     */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        ListNumberSpan[] listSpans = editable.getSpans(start, end, ListNumberSpan.class);
        if (null == listSpans || listSpans.length == 0) {
            return;
        }

        if (end > start) {
            char c = editable.charAt(end - 1);
            if (c == Constants.CHAR_NEW_LINE) {
                int listSpanSize = listSpans.length;
                int previousListSpanIndex = listSpanSize - 1;
                if (previousListSpanIndex > -1) {
                    ListNumberSpan previousListSpan = listSpans[previousListSpanIndex];
                    int lastListItemSpanStartPos = editable.getSpanStart(previousListSpan);
                    int lastListItemSpanEndPos = editable.getSpanEnd(previousListSpan);
                    CharSequence listItemSpanContent = editable.subSequence(
                            lastListItemSpanStartPos, lastListItemSpanEndPos);

                    if (isEmptyListItemSpan(listItemSpanContent)) {
                        editable.removeSpan(previousListSpan);
                        editable.delete(lastListItemSpanStartPos, lastListItemSpanEndPos);
                        reNumberBehindListItemSpans(lastListItemSpanStartPos, editable, 0);
                        return;
                    } else {
                        if (end > lastListItemSpanStartPos) {
                            editable.removeSpan(previousListSpan);
                            editable.setSpan(previousListSpan,
                                    lastListItemSpanStartPos, end - 1,
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                    }
                    int lastListItemNumber = previousListSpan.getNumber();
                    int thisNumber = lastListItemNumber + 1;
                    ListNumberSpan newListItemSpan = makeLineAsList(thisNumber);
                    end = editable.getSpanEnd(newListItemSpan);
                    reNumberBehindListItemSpans(end, editable, thisNumber);
                }
            }
        } else {
            int spanStart = editable.getSpanStart(listSpans[0]);
            int spanEnd = editable.getSpanEnd(listSpans[0]);
            ListNumberSpan theFirstSpan = listSpans[0];
            if (listSpans.length > 1) {
                int theFirstSpanNumber = theFirstSpan.getNumber();
                for (ListNumberSpan lns : listSpans) {
                    if (lns.getNumber() < theFirstSpanNumber) {
                        theFirstSpan = lns;
                    }
                }
                spanStart = editable.getSpanStart(theFirstSpan);
                spanEnd = editable.getSpanEnd(theFirstSpan);
            }

            if (spanStart >= spanEnd) {
                for (ListNumberSpan listSpan : listSpans) {
                    editable.removeSpan(listSpan);
                }

                if (spanStart > 0) {
                    editable.delete(spanStart - 1, spanEnd);
                }

                if (editable.length() > spanEnd) {
                    ListNumberSpan[] spansBehind = editable.getSpans(spanEnd, spanEnd + 1, ListNumberSpan.class);
                    if (spansBehind.length > 0) {
                        int removedNumber = theFirstSpan.getNumber();
                        reNumberBehindListItemSpans(spanStart, editable, removedNumber - 1);
                    }
                }
            } else if (start == spanStart) {
                return;
            } else if (start == spanEnd) {
                if (editable.length() > start) {
                    if (editable.charAt(start) == Constants.CHAR_NEW_LINE) {
                        ListNumberSpan[] spans = editable.getSpans(start, start, ListNumberSpan.class);
                        if (spans.length > 0) {
                            mergeForward(editable, theFirstSpan, spanStart, spanEnd);
                        } else {
                            editable.removeSpan(spans[0]);
                        }
                    } else {
                        mergeForward(editable, theFirstSpan, spanStart, spanEnd);
                    }
                }
            } else if (start > spanStart && end < spanEnd) {
                return;
            } else {
                int previousNumber = theFirstSpan.getNumber();
                reNumberBehindListItemSpans(end, editable, previousNumber);
            }
        }
    }

    /*
     Merges consecutive numbered list spans to maintain proper formatting and spacing.
     */
    protected void mergeForward(Editable editable, ListNumberSpan listSpan, int spanStart, int spanEnd) {
        if (editable.length() <= spanEnd + 1) {
            return;
        }
        ListNumberSpan[] targetSpans = editable.getSpans(spanEnd, spanEnd + 1, ListNumberSpan.class);
        if (targetSpans == null || targetSpans.length == 0) {
            reNumberBehindListItemSpans(spanEnd, editable, listSpan.getNumber());
            return;
        }
        ListNumberSpan firstTargetSpan = targetSpans[0];
        ListNumberSpan lastTargetSpan = targetSpans[0];

        if (targetSpans.length > 0) {
            int firstTargetSpanNumber = firstTargetSpan.getNumber();
            int lastTargetSpanNumber = lastTargetSpan.getNumber();
            for (ListNumberSpan lns : targetSpans) {
                int lnsNumber = lns.getNumber();
                if (lnsNumber < firstTargetSpanNumber) {
                    firstTargetSpan = lns;
                    firstTargetSpanNumber = lnsNumber;
                }
                if (lnsNumber > lastTargetSpanNumber) {
                    lastTargetSpan = lns;
                    lastTargetSpanNumber = lnsNumber;
                }
            }
        }
        int targetStart = editable.getSpanStart(firstTargetSpan);
        int targetEnd = editable.getSpanEnd(lastTargetSpan);

        int targetLength = targetEnd - targetStart;
        spanEnd = spanEnd + targetLength;
        for (ListNumberSpan targetSpan : targetSpans) {
            editable.removeSpan(targetSpan);
        }
        ListNumberSpan[] compositeSpans = editable.getSpans(spanStart, spanEnd, ListNumberSpan.class);
        for (ListNumberSpan lns : compositeSpans) {
            editable.removeSpan(lns);
        }
        editable.setSpan(listSpan, spanStart, spanEnd,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        reNumberBehindListItemSpans(spanEnd, editable, listSpan.getNumber());
    }

    /*
     Checks if the current list item span is empty.
     */
    private boolean isEmptyListItemSpan(CharSequence listItemSpanContent) {
        int spanLen = listItemSpanContent.length();
        return spanLen == 2;
    }

    /*
     Formats the current line into a numbered list item.
     */
    private ListNumberSpan makeLineAsList(int num) {
        EditText editText = getEditText();
        int currentLine = Util.getCurrentCursorLine(editText);
        int start = Util.getThisLineStart(editText, currentLine);
        int end = Util.getThisLineEnd(editText, currentLine);
        Editable editable = editText.getText();
        editable.insert(start, Constants.ZERO_WIDTH_SPACE_STR);
        start = Util.getThisLineStart(editText, currentLine);
        end = Util.getThisLineEnd(editText, currentLine);

        if (end > 0 && editable.charAt(end - 1) == Constants.CHAR_NEW_LINE) {
            end--;
        }

        ListNumberSpan listItemSpan = new ListNumberSpan(num);
        editable.setSpan(listItemSpan, start, end,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return listItemSpan;
    }

    /*
     Updates the numbering of list items after an edit or a split.
     */
    public static void reNumberBehindListItemSpans(int end, Editable editable, int thisNumber) {
        ListNumberSpan[] behindListItemSpans = editable.getSpans(end + 1,
                end + 2, ListNumberSpan.class);
        if (null != behindListItemSpans && behindListItemSpans.length > 0) {
            int total = behindListItemSpans.length;
            int index = 0;
            for (ListNumberSpan listItemSpan : behindListItemSpans) {
                int newNumber = ++thisNumber;
                listItemSpan.setNumber(newNumber);
                ++index;
                if (total == index) {
                    int newSpanEnd = editable.getSpanEnd(listItemSpan);
                    reNumberBehindListItemSpans(newSpanEnd, editable, newNumber);
                }
            }
        }
    }

    /*
     Changes existing bullet list spans into numbered list spans
     and restarts the numbering sequence behind it.
     */
    protected void changeListBulletSpanToListNumberSpan(Editable editable, ListBulletSpan[] listBulletSpans) {
        if (null == listBulletSpans || listBulletSpans.length == 0) {
            return;
        }

        int len = listBulletSpans.length;
        ListBulletSpan lastListBulletSpan = listBulletSpans[len - 1];

        int lastListNumberSpanEnd = editable.getSpanEnd(lastListBulletSpan);
        int previousListNumber = 0;

        ListBulletSpan firstListBulletSpan = listBulletSpans[0];
        int firstListBulletSpanStart = editable.getSpanStart(firstListBulletSpan);
        if (firstListBulletSpanStart > 2) {
            ListNumberSpan[] previousListNumberSpans = editable.getSpans(
                    firstListBulletSpanStart - 2,
                    firstListBulletSpanStart - 1,
                    ListNumberSpan.class);
            if (null != previousListNumberSpans && previousListNumberSpans.length > 0) {
                previousListNumber = previousListNumberSpans[previousListNumberSpans.length - 1].getNumber();
            }
        }

        for (ListBulletSpan listBulletSpan : listBulletSpans) {
            int start = editable.getSpanStart(listBulletSpan);
            int end = editable.getSpanEnd(listBulletSpan);

            editable.removeSpan(listBulletSpan);
            previousListNumber++;
            ListNumberSpan listNumberSpan = new ListNumberSpan(previousListNumber);
            editable.setSpan(listNumberSpan, start, end,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        editable.insert(lastListNumberSpanEnd, Constants.ZERO_WIDTH_SPACE_STR);
        editable.delete(lastListNumberSpanEnd + 1, lastListNumberSpanEnd + 1);

        ARE_ListNumber.reNumberBehindListItemSpans(lastListNumberSpanEnd + 1,
                editable, previousListNumber);
    }

    /*
     Returns the material button used for the numbered list style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mListNumberButton;
    }

    /*
     Updates the checked state of the numbered list style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    /*
     Returns the current checked state of the numbered list style.
     */
    @Override
    public boolean getIsChecked() {
        return this.mIsChecked;
    }
}
