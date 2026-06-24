package com.chinalwb.are.helper;

import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import com.chinalwb.are.AREditText;

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

    public void updateLastEditItemHtmlModes(boolean before, boolean after) {
        if (mEditHistory != null && !mEditHistory.mmHistory.isEmpty()) {
            int lastPos = mEditHistory.mmHistory.size() - 1;
            EditItem lastItem = mEditHistory.mmHistory.get(lastPos);
            EditItem updatedItem = new EditItem(
                lastItem.mmStart, lastItem.mmBefore, lastItem.mmAfter,
                lastItem.beforeSelectionStart, lastItem.beforeSelectionEnd,
                lastItem.afterSelectionStart, lastItem.afterSelectionEnd,
                before, after
            );
            mEditHistory.mmHistory.set(lastPos, updatedItem);
        }
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

        mIsUndoOrRedo = true;
        setHtmlMode(edit.beforeHtmlMode);
        
        Editable text = mTextView.getEditableText();
        int len = text.length();

        if (edit.beforeHtmlMode != edit.afterHtmlMode) {
            text.replace(0, len, cloneCharSequence(edit.mmBefore));
        } else {
            int start = edit.mmStart;
            int end = start + (edit.mmAfter != null ? edit.mmAfter.length() : 0);
            if (start >= 0 && end <= len) {
                text.replace(start, end, cloneCharSequence(edit.mmBefore));
            } else {
                text.replace(0, len, cloneCharSequence(edit.mmBefore));
            }
        }
        mIsUndoOrRedo = false;

        restoreSelection(text, edit.beforeSelectionStart, edit.beforeSelectionEnd);
        smoothScrollToPosition(edit.beforeSelectionEnd);
    }

    /*
     * Checks if a redo operation is currently available in the stack.
     * Returns true if there is at least one valid action to redo.
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

        mIsUndoOrRedo = true;
        setHtmlMode(edit.afterHtmlMode);

        Editable text = mTextView.getEditableText();
        int len = text.length();

        if (edit.beforeHtmlMode != edit.afterHtmlMode) {
            text.replace(0, len, cloneCharSequence(edit.mmAfter));
        } else {
            int start = edit.mmStart;
            int end = start + (edit.mmBefore != null ? edit.mmBefore.length() : 0);
            if (start >= 0 && end <= len) {
                text.replace(start, end, cloneCharSequence(edit.mmAfter));
            } else {
                text.replace(0, len, cloneCharSequence(edit.mmAfter));
            }
        }
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

    private boolean isHtmlMode() {
        if (mTextView instanceof AREditText) {
            return ((AREditText) mTextView).isHtmlMode();
        }
        return false;
    }

    private void setHtmlMode(boolean htmlMode) {
        if (mTextView instanceof AREditText) {
            ((AREditText) mTextView).setHtmlMode(htmlMode);
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
        private final boolean beforeHtmlMode;
        private final boolean afterHtmlMode;

        /*
         * Constructs an EditItem holding the differences and states of a text change.
         */
        public EditItem(int start, CharSequence before, CharSequence after,
                        int bSelStart, int bSelEnd, int aSelStart, int aSelEnd,
                        boolean bHtmlMode, boolean aHtmlMode) {
            mmStart = start;
            mmBefore = before;
            mmAfter = after;
            beforeSelectionStart = bSelStart;
            beforeSelectionEnd = bSelEnd;
            afterSelectionStart = aSelStart;
            afterSelectionEnd = aSelEnd;
            beforeHtmlMode = bHtmlMode;
            afterHtmlMode = aHtmlMode;
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
        private boolean mBeforeHtmlMode;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (mIsUndoOrRedo) return;
            mBeforeChange = cloneCharSequence(s.subSequence(start, start + count));
            mBeforeSelectionStart = Selection.getSelectionStart(s);
            mBeforeSelectionEnd = Selection.getSelectionEnd(s);
            mBeforeHtmlMode = isHtmlMode();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mIsUndoOrRedo) return;
            CharSequence mAfterChange = cloneCharSequence(s.subSequence(start, start + count));
            int mAfterSelectionStart = Selection.getSelectionStart(s);
            int mAfterSelectionEnd = Selection.getSelectionEnd(s);
            boolean afterHtmlMode = isHtmlMode();

            // Avoid adding dummy history if there is no actual change
            if (!mBeforeChange.toString().equals(mAfterChange.toString()) || count > 0 || before > 0) {
                mEditHistory.add(new EditItem(
                        start, mBeforeChange, mAfterChange,
                        mBeforeSelectionStart, mBeforeSelectionEnd,
                        mAfterSelectionStart, mAfterSelectionEnd,
                        mBeforeHtmlMode, afterHtmlMode
                ));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    private CharSequence cloneCharSequence(CharSequence source) {
        if (source == null) return null;
        if (!(source instanceof android.text.Spanned)) {
            return source.toString();
        }
        android.text.Spanned spanned = (android.text.Spanned) source;
        SpannableStringBuilder builder = new SpannableStringBuilder(spanned.toString());
        Object[] spans = spanned.getSpans(0, spanned.length(), Object.class);
        for (Object oldSpan : spans) {
            int start = spanned.getSpanStart(oldSpan);
            int end = spanned.getSpanEnd(oldSpan);
            int flags = spanned.getSpanFlags(oldSpan);
            Object newSpan = cloneSpan(oldSpan);
            if (newSpan != null) {
                builder.setSpan(newSpan, start, end, flags);
            }
        }
        return builder;
    }

    private Object cloneSpan(Object original) {
        if (original == null) return null;
        try {
            // Check custom Spans first
            if (original instanceof com.chinalwb.are.spans.AreBoldSpan) {
                return new com.chinalwb.are.spans.AreBoldSpan();
            }
            if (original instanceof com.chinalwb.are.spans.AreItalicSpan) {
                return new com.chinalwb.are.spans.AreItalicSpan();
            }
            if (original instanceof com.chinalwb.are.spans.AreUnderlineSpan) {
                return new com.chinalwb.are.spans.AreUnderlineSpan();
            }
            if (original instanceof com.chinalwb.are.spans.AreSubscriptSpan) {
                return new com.chinalwb.are.spans.AreSubscriptSpan();
            }
            if (original instanceof com.chinalwb.are.spans.AreSuperscriptSpan) {
                return new com.chinalwb.are.spans.AreSuperscriptSpan();
            }
            if (original instanceof com.chinalwb.are.spans.ListBulletSpan) {
                return new com.chinalwb.are.spans.ListBulletSpan();
            }
            if (original instanceof com.chinalwb.are.spans.ListNumberSpan) {
                return new com.chinalwb.are.spans.ListNumberSpan(((com.chinalwb.are.spans.ListNumberSpan) original).getNumber());
            }
            if (original instanceof com.chinalwb.are.spans.AreForegroundColorSpan) {
                return new com.chinalwb.are.spans.AreForegroundColorSpan(((com.chinalwb.are.spans.AreForegroundColorSpan) original).getForegroundColor());
            }
            if (original instanceof com.chinalwb.are.spans.AreFontSizeSpan) {
                return new com.chinalwb.are.spans.AreFontSizeSpan(((com.chinalwb.are.spans.AreFontSizeSpan) original).getSize());
            }
            if (original instanceof com.chinalwb.are.spans.AreUrlSpan) {
                return new com.chinalwb.are.spans.AreUrlSpan(((com.chinalwb.are.spans.AreUrlSpan) original).getURL());
            }
            if (original instanceof com.chinalwb.are.spans.AreQuoteSpan) {
                try {
                    java.lang.reflect.Field field = com.chinalwb.are.spans.AreQuoteSpan.class.getDeclaredField("mQuoteColor");
                    field.setAccessible(true);
                    int color = (int) field.get(original);
                    return new com.chinalwb.are.spans.AreQuoteSpan(color);
                } catch (Exception e) {
                    return new com.chinalwb.are.spans.AreQuoteSpan();
                }
            }
            if (original instanceof com.chinalwb.are.spans.AreHrSpan) {
                return new com.chinalwb.are.spans.AreHrSpan();
            }
            if (original instanceof com.chinalwb.are.spans.AreMentionSpan) {
                return new com.chinalwb.are.spans.AreMentionSpan(((com.chinalwb.are.spans.AreMentionSpan) original).getMentionItem());
            }
            if (original instanceof com.chinalwb.are.spans.AreImageSpan) {
                com.chinalwb.are.spans.AreImageSpan img = (com.chinalwb.are.spans.AreImageSpan) original;
                if (img.getUri() != null) {
                    return new com.chinalwb.are.spans.AreImageSpan(mTextView.getContext(), img.getUri());
                } else if (img.getURL() != null) {
                    return new com.chinalwb.are.spans.AreImageSpan(mTextView.getContext(), img.getDrawable(), img.getURL());
                } else if (img.getResId() != 0) {
                    return new com.chinalwb.are.spans.AreImageSpan(mTextView.getContext(), img.getResId());
                }
            }
            if (original instanceof com.chinalwb.are.spans.AreVideoSpan) {
                com.chinalwb.are.spans.AreVideoSpan vid = (com.chinalwb.are.spans.AreVideoSpan) original;
                android.graphics.Bitmap bitmap = null;
                android.graphics.drawable.Drawable drawable = vid.getDrawable();
                if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                    bitmap = ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();
                }
                return new com.chinalwb.are.spans.AreVideoSpan(mTextView.getContext(), bitmap, vid.getVideoPath(), vid.getVideoUrl());
            }

            // Standard Android spans
            if (original instanceof android.text.style.StyleSpan) {
                return new android.text.style.StyleSpan(((android.text.style.StyleSpan) original).getStyle());
            }
            if (original instanceof android.text.style.UnderlineSpan) {
                return new android.text.style.UnderlineSpan();
            }
            if (original instanceof android.text.style.StrikethroughSpan) {
                return new android.text.style.StrikethroughSpan();
            }
            if (original instanceof android.text.style.SubscriptSpan) {
                return new android.text.style.SubscriptSpan();
            }
            if (original instanceof android.text.style.SuperscriptSpan) {
                return new android.text.style.SuperscriptSpan();
            }
            if (original instanceof android.text.style.ForegroundColorSpan) {
                return new android.text.style.ForegroundColorSpan(((android.text.style.ForegroundColorSpan) original).getForegroundColor());
            }
            if (original instanceof android.text.style.BackgroundColorSpan) {
                return new android.text.style.BackgroundColorSpan(((android.text.style.BackgroundColorSpan) original).getBackgroundColor());
            }
            if (original instanceof android.text.style.AbsoluteSizeSpan) {
                return new android.text.style.AbsoluteSizeSpan(((android.text.style.AbsoluteSizeSpan) original).getSize(), ((android.text.style.AbsoluteSizeSpan) original).getDip());
            }
            if (original instanceof android.text.style.AlignmentSpan.Standard) {
                return new android.text.style.AlignmentSpan.Standard(((android.text.style.AlignmentSpan.Standard) original).getAlignment());
            }
            if (original instanceof android.text.style.URLSpan) {
                return new android.text.style.URLSpan(((android.text.style.URLSpan) original).getURL());
            }
            if (original instanceof android.text.style.QuoteSpan) {
                try {
                    java.lang.reflect.Field field = android.text.style.QuoteSpan.class.getDeclaredField("mColor");
                    field.setAccessible(true);
                    int color = (int) field.get(original);
                    return new android.text.style.QuoteSpan(color);
                } catch (Exception e) {
                    try {
                        java.lang.reflect.Method method = android.text.style.QuoteSpan.class.getMethod("getColor");
                        int color = (int) method.invoke(original);
                        return new android.text.style.QuoteSpan(color);
                    } catch (Exception e2) {
                        return new android.text.style.QuoteSpan();
                    }
                }
            }
            if (original instanceof android.text.style.LeadingMarginSpan.Standard) {
                android.text.style.LeadingMarginSpan.Standard lms = (android.text.style.LeadingMarginSpan.Standard) original;
                return new android.text.style.LeadingMarginSpan.Standard(lms.getLeadingMargin(true), lms.getLeadingMargin(false));
            }
            if (original instanceof android.text.style.TypefaceSpan) {
                return new android.text.style.TypefaceSpan(((android.text.style.TypefaceSpan) original).getFamily());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
