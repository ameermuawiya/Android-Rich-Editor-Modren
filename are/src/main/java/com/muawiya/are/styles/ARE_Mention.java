package com.muawiya.are.styles;

import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;

import com.muawiya.are.AREditText;
import com.muawiya.are.mentions.AreMentionPopup;
import com.muawiya.are.mentions.MentionItem;
import com.muawiya.are.spans.AreMentionSpan;
import com.muawiya.are.strategies.MentionStrategy;
import com.muawiya.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;

/*
 * Detects trigger inputs and interacts with the Popup.
 */
public class ARE_Mention extends ARE_ABS_FreeStyle {

    private AREditText mEditText;
    private MaterialButton mMentionButton;
    private boolean mIsChecked;
    private AreMentionPopup mPopup;
    private int mCurrentIndex = -1;

    public ARE_Mention(ARE_Toolbar toolbar) {
        super(toolbar);
    }

    public void setEditText(AREditText editText) {
        this.mEditText = editText;
        setupMentionTracker();
    }

    private void setupMentionTracker() {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MentionStrategy strategy = mEditText.getMentionStrategy();
                if (strategy == null || !strategy.isEnabled()) {
                    closePopup(); return;
                }

                int cursor = mEditText.getSelectionStart();
                if (cursor <= 0) { closePopup(); return; }

                String text = s.toString();
                int atPos = -1;

                for (int i = cursor - 1; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (c == ' ' || c == '\n') break;
                    if (c == '@') { atPos = i; break; }
                }

                if (atPos != -1) {
                    String query = text.substring(atPos + 1, cursor);
                    if (query.length() > 25) { closePopup(); return; }
                    mCurrentIndex = atPos;
                    showPopup(query);
                    return;
                }
                closePopup();
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void showPopup(String query) {
        if (mPopup == null) mPopup = new AreMentionPopup(mContext, this, mEditText);
        mPopup.filterAndShow(query);
    }

    public void closePopup() {
        if (mPopup != null) mPopup.dismiss();
    }

    public void insertSelectedMentionItem(MentionItem item) {
        if (mCurrentIndex == -1 || mEditText == null) return;

        Editable editable = mEditText.getEditableText();
        int cursor = mEditText.getSelectionStart();

        if (mCurrentIndex >= 0 && cursor > mCurrentIndex && cursor <= editable.length()) {
            String insertText = "@" + item.mName + " ";
            editable.replace(mCurrentIndex, cursor, insertText);

            AreMentionSpan span = new AreMentionSpan(item);
            editable.setSpan(span, mCurrentIndex, mCurrentIndex + insertText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        MentionStrategy strategy = mEditText.getMentionStrategy();
        if (strategy != null) strategy.onItemSelected(item);

        closePopup();
        mCurrentIndex = -1;
    }

    @Override public void applyStyle(Editable editable, int start, int end) {}
    @Override public void setListenerForButton(MaterialButton button) { this.mMentionButton = button; }
    @Override public MaterialButton getButton() { return this.mMentionButton; }
    @Override public void setChecked(boolean isChecked) { this.mIsChecked = isChecked; }
    @Override public boolean getIsChecked() { return this.mIsChecked; }
}