package com.chinalwb.are.helper;

import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import java.util.LinkedList;

public class UndoRedoHelper {

    private boolean mIsUndoOrRedo = false;
    private final EditHistory mEditHistory;
    private final EditTextChangeListener mChangeListener;
    private final TextView mTextView;

    /*
     * Initializes the robust Undo/Redo helper for the specified TextView.
     * It sets up the history tracking and attaches the text change listener.
     */
    public UndoRedoHelper(TextView textView) {
        mTextView = textView;
        mEditHistory = new EditHistory();
        mChangeListener = new EditTextChangeListener();
        mTextView.addTextChangedListener(mChangeListener);
    }

    /*
     * Disconnects this undo/redo listener from the text view.
     * This is useful to prevent memory leaks when the editor is destroyed.
     */
    public void disconnect() {
        mTextView.removeTextChangedListener(mChangeListener);
    }

    /*
     * Sets the maximum history size for the undo/redo stack.
     * It automatically trims the history if it exceeds the specified limit.
     */
    public void setMaxHistorySize(int maxHistorySize) {
        mEditHistory.setMaxHistorySize(maxHistorySize);
    }

    /*
     * Clears the entire undo/redo history completely.
     * Use this when resetting the editor or loading new initial text.
     */
    public void clearHistory() {
        mEditHistory.clear();
    }

    /*
     * Checks if an undo operation is currently available in the stack.
     * Returns true if there is at least one valid action to undo.
     */
    public boolean getCanUndo() {
        return mEditHistory.canUndo();
    }

    /*
     * Performs the undo operation securely.
     * Restores the exact text, all rich text formatting, and original cursor selection.
     */
    public void undo() {
        EditItem edit = mEditHistory.getPrevious();
        if (edit == null) return;

        Editable text = mTextView.getEditableText();
        int start = edit.mmStart;
        int end = start + (edit.mmAfter != null ? edit.mmAfter.length() : 0);

        mIsUndoOrRedo = true;
        text.replace(start, end, edit.mmBefore);
        mIsUndoOrRedo = false;

        restoreSelection(text, edit.beforeSelectionStart, edit.beforeSelectionEnd);
        smoothScrollToPosition(edit.beforeSelectionEnd);
    }

    /*
     * Checks if a redo operation is currently available in the stack.
     * Returns true if there is at least one undone action to redo.
     */
    public boolean getCanRedo() {
        return mEditHistory.canRedo();
    }

    /*
     * Performs the redo operation securely.
     * Restores the exact text, all rich text formatting, and original cursor selection.
     */
    public void redo() {
        EditItem edit = mEditHistory.getNext();
        if (edit == null) return;

        Editable text = mTextView.getEditableText();
        int start = edit.mmStart;
        int end = start + (edit.mmBefore != null ? edit.mmBefore.length() : 0);

        mIsUndoOrRedo = true;
        text.replace(start, end, edit.mmAfter);
        mIsUndoOrRedo = false;

        restoreSelection(text, edit.afterSelectionStart, edit.afterSelectionEnd);
        smoothScrollToPosition(edit.afterSelectionEnd);
    }

    /*
     * Safely restores the cursor selection ensuring bounds are not exceeded.
     * It prevents the app from crashing if indices are out of text boundaries.
     */
    private void restoreSelection(Editable text, int startPos, int endPos) {
        int len = text.length();
        int safeStart = Math.min(len, Math.max(0, startPos));
        int safeEnd = Math.min(len, Math.max(0, endPos));
        Selection.setSelection(text, safeStart, safeEnd);
    }

    /*
     * Smoothly scrolls the parent view to the specified cursor position.
     * Keeps the active editing area visible to the user without sudden jumps.
     */
    private void smoothScrollToPosition(int pos) {
        if (mTextView.getLayout() != null && mTextView.getParent() instanceof View) {
            mTextView.postDelayed(() -> {
                try {
                    int safePos = Math.min(mTextView.length(), Math.max(0, pos));
                    int line = mTextView.getLayout().getLineForOffset(safePos);
                    int y = mTextView.getLayout().getLineBottom(line) + mTextView.getPaddingTop();
                    View parent = (View) mTextView.getParent();
                    if (parent instanceof NestedScrollView) {
                        int targetY = y - (parent.getHeight() / 2);
                        ((NestedScrollView) parent).smoothScrollTo(0, Math.max(0, targetY));
                    }
                } catch (Exception ignored) {}
            }, 100);
        }
    }

    /*
     * Manages the edit history stack securely.
     * Handles adding, navigating, and trimming the history items.
     */
    private final class EditHistory {
        private int mmPosition = 0;
        private int mmMaxHistorySize = -1;
        private final LinkedList<EditItem> mmHistory = new LinkedList<>();

        /*
         * Clears all items in the history list and resets the position.
         */
        private void clear() {
            mmPosition = 0;
            mmHistory.clear();
        }

        /*
         * Adds a new validated edit item to the history list.
         * Removes any undone forward history before adding the new item.
         */
        private void add(EditItem item) {
            while (mmHistory.size() > mmPosition) {
                mmHistory.removeLast();
            }
            mmHistory.add(item);
            mmPosition++;

            if (mmMaxHistorySize >= 0) {
                trimHistory();
            }
        }

        /*
         * Updates the maximum history limit and trims the list if needed.
         */
        private void setMaxHistorySize(int maxHistorySize) {
            mmMaxHistorySize = maxHistorySize;
            if (mmMaxHistorySize >= 0) trimHistory();
        }

        /*
         * Removes the oldest history items when the maximum limit is reached.
         */
        private void trimHistory() {
            while (mmHistory.size() > mmMaxHistorySize) {
                mmHistory.removeFirst();
                mmPosition--;
            }
            if (mmPosition < 0) mmPosition = 0;
        }

        /*
         * Checks if the current position allows for an undo operation.
         */
        private boolean canUndo() {
            return mmPosition > 0;
        }

        /*
         * Checks if the current position allows for a redo operation.
         */
        private boolean canRedo() {
            return mmPosition < mmHistory.size();
        }

        /*
         * Retrieves the previous edit item for undo operation and updates position.
         */
        private EditItem getPrevious() {
            if (mmPosition == 0) return null;
            mmPosition--;
            return mmHistory.get(mmPosition);
        }

        /*
         * Retrieves the next edit item for redo operation and updates position.
         */
        private EditItem getNext() {
            if (mmPosition >= mmHistory.size()) return null;
            EditItem item = mmHistory.get(mmPosition);
            mmPosition++;
            return item;
        }
    }

    /*
     * Represents a single immutable unit of editing action.
     * Contains deep copies of text, spans, and cursor positions for exact restoration.
     */
    private static final class EditItem {
        private final int mmStart;
        private final CharSequence mmBefore;
        private final CharSequence mmAfter;
        private final int beforeSelectionStart;
        private final int beforeSelectionEnd;
        private final int afterSelectionStart;
        private final int afterSelectionEnd;

        /*
         * Constructs an EditItem holding the differences and states of a text change.
         */
        public EditItem(int start, CharSequence before, CharSequence after,
                        int bSelStart, int bSelEnd, int aSelStart, int aSelEnd) {
            mmStart = start;
            mmBefore = before;
            mmAfter = after;
            beforeSelectionStart = bSelStart;
            beforeSelectionEnd = bSelEnd;
            afterSelectionStart = aSelStart;
            afterSelectionEnd = aSelEnd;
        }
    }

    /*
     * Listens to real-time text changes safely.
     * Captures spans dynamically and ensures empty changes are not recorded.
     */
    private final class EditTextChangeListener implements TextWatcher {
        private CharSequence mBeforeChange;
        private int mBeforeSelectionStart;
        private int mBeforeSelectionEnd;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (mIsUndoOrRedo) return;
            // Using SpannableStringBuilder.valueOf to force a deep copy of spans
            mBeforeChange = SpannableStringBuilder.valueOf(s.subSequence(start, start + count));
            mBeforeSelectionStart = Selection.getSelectionStart(s);
            mBeforeSelectionEnd = Selection.getSelectionEnd(s);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mIsUndoOrRedo) return;
            CharSequence mAfterChange = SpannableStringBuilder.valueOf(s.subSequence(start, start + count));
            int mAfterSelectionStart = Selection.getSelectionStart(s);
            int mAfterSelectionEnd = Selection.getSelectionEnd(s);

            // Avoid adding dummy history if there is no actual change
            if (!mBeforeChange.toString().equals(mAfterChange.toString()) || count > 0 || before > 0) {
                mEditHistory.add(new EditItem(
                        start, mBeforeChange, mAfterChange,
                        mBeforeSelectionStart, mBeforeSelectionEnd,
                        mAfterSelectionStart, mAfterSelectionEnd
                ));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }
}
