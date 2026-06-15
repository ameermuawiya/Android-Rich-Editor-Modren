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

    /*
     Toggles between raw HTML source code and rendered rich text.
     */
    private void toggleSource() {
        if (mEditText == null) return;

        try {
            if (!isShowingSource) {
                String html = mEditText.getHtml();
                mEditText.setText(html);
                isShowingSource = true;
                mButton.setIconResource(R.drawable.ic_code_off);
            } else {
                String rawHtml = mEditText.getText().toString();
                mEditText.setText("");
                mEditText.fromHtml(rawHtml);
                isShowingSource = false;
                mButton.setIconResource(R.drawable.ic_code);
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
