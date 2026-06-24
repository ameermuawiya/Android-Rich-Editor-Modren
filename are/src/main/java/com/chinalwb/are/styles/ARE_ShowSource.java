package com.chinalwb.are.styles;

import android.view.View;
import com.chinalwb.are.R;

import com.chinalwb.are.AREditText;
import com.google.android.material.button.MaterialButton;

public class ARE_ShowSource {

    private MaterialButton mButton;
    private AREditText mEditText;
    private boolean isShowingSource = false;
    private boolean mIsChecked;

    public ARE_ShowSource(MaterialButton button) {
        this.mButton = button;
        setListener();
    }

    /*
     Sets the AREditText instance to toggle source code.
     */
    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    /*
     Attaches click listener to the toggle button.
     */
    private void setListener() {
        if (mButton != null) {
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSource();
                    
                    // Future Logic: Uncomment the below lines to enable the checkable state
                    // mIsChecked = !mIsChecked;
                    // if (mButton != null) {
                    //     mButton.setChecked(mIsChecked);
                    // }
                }
            });
        }
    }

    public boolean isShowingSource() {
        return isShowingSource;
    }

    public void toggleToShowSource(boolean showSource) {
        if (this.isShowingSource != showSource) {
            this.isShowingSource = showSource;
            if (mButton != null) {
                mButton.setIconResource(showSource ? R.drawable.ic_code_off : R.drawable.ic_code);
            }
        }
    }

    /*
     Toggles between raw HTML source code and rendered rich text.
     */
    public void toggleSource() {
        if (mEditText == null) return;

        try {
            com.chinalwb.are.helper.UndoRedoHelper undoRedo = mEditText.getUndoRedoHelper();
            if (!isShowingSource) {
                int selectionStart = mEditText.getSelectionStart();
                int selectionEnd = mEditText.getSelectionEnd();

                String html = mEditText.getHtml();
                mEditText.setText(html);
                if (undoRedo != null) {
                    undoRedo.updateLastEditItemHtmlModes(false, true);
                }
                isShowingSource = true;
                if (mButton != null) {
                    mButton.setIconResource(R.drawable.ic_code_off);
                }

                int newSel = Math.min(html.length(), Math.max(0, selectionStart));
                mEditText.setSelection(newSel, newSel);
            } else {
                int selectionStart = mEditText.getSelectionStart();
                int selectionEnd = mEditText.getSelectionEnd();

                String rawHtml = mEditText.getText().toString();
                mEditText.setText("");
                mEditText.fromHtml(rawHtml);
                if (undoRedo != null) {
                    undoRedo.updateLastEditItemHtmlModes(true, false);
                }
                isShowingSource = false;
                if (mButton != null) {
                    mButton.setIconResource(R.drawable.ic_code);
                }

                int len = mEditText.length();
                int newSel = Math.min(len, Math.max(0, selectionStart));
                mEditText.setSelection(newSel, newSel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
     Returns the current checked state of the show source style.
     */
    public boolean getIsChecked() {
        return this.mIsChecked;
    }
}
