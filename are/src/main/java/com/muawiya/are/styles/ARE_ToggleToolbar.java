package com.muawiya.are.styles;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.muawiya.are.AREditor;
import com.muawiya.are.R;
import com.google.android.material.button.MaterialButton;

public class ARE_ToggleToolbar {

    private static final String PREFS_NAME = "ARE_Settings";
    private static final String KEY_TOOLBAR_TOP = "toolbar_is_top";
    
    private MaterialButton mButton;
    private AREditor mEditor;
    private boolean mIsChecked;

    public ARE_ToggleToolbar(MaterialButton button, AREditor editor) {
        this.mButton = button;
        this.mEditor = editor;
        setListener();
        updateIcon();
    }

    /*
     Attaches click listener to the up/down toggle button.
     */
    private void setListener() {
        if (mButton != null) {
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePosition();
                    
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
     Triggers the position change in the editor and updates state.
     */
    private void togglePosition() {
        if (mEditor == null) return;

        try {
            mEditor.toggleToolbarPosition();
            boolean isNowTop = mEditor.getToolbarAlignment() == AREditor.ToolbarAlignment.TOP;
            saveState(isNowTop);
            updateIcon();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     Updates the button icon based on current toolbar position.
     */
    public void updateIcon() {
        if (mButton == null || mEditor == null) return;

        try {
            boolean isTop = mEditor.getToolbarAlignment() == AREditor.ToolbarAlignment.TOP;
            mButton.setIconResource(isTop ? R.drawable.ic_action_downward : R.drawable.ic_action_upward);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     Saves the toolbar position state in SharedPreferences.
     */
    private void saveState(boolean isTop) {
        if (mEditor == null || mEditor.getContext() == null) return;

        try {
            SharedPreferences prefs = mEditor.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_TOOLBAR_TOP, isTop).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     Checks if the saved toolbar position is at the top.
     */
    public static boolean isToolbarTop(Context context) {
        if (context == null) return false;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_TOOLBAR_TOP, false);
    }
    
    /*
     Returns the current checked state of the toggle toolbar style.
     */
    public boolean getIsChecked() {
        return this.mIsChecked;
    }
}
