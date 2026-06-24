package com.muawiya.are.styles;

import com.google.android.material.button.MaterialButton;

public class ARE_Helper {

    /*
     Updates the checked status of the given formatting style.
     Applies Material Design checked state logic directly to the button.
     */
    public static void updateCheckStatus(IARE_Style areStyle, boolean checked) {
        if (areStyle != null) {
            areStyle.setChecked(checked);
            MaterialButton button = areStyle.getButton();
            if (button != null) {
                button.setChecked(checked);
            }
        }
    }
}
