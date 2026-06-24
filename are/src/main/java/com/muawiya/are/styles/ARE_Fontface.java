package com.muawiya.are.styles;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.muawiya.are.Constants;
import com.muawiya.are.spans.MyTypefaceSpan;
import com.muawiya.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;

public class ARE_Fontface extends ARE_ABS_FreeStyle {

    private MaterialButton mFontfaceButton;

    /*
     Initializes the fontface style and sets the click listener
     for the provided material button.
     */
    public ARE_Fontface(MaterialButton button, ARE_Toolbar toolbar) {
        super(toolbar);
        this.mFontfaceButton = button;
        setListenerForButton(this.mFontfaceButton);
    }

    /*
     Attaches a click listener to apply a custom typeface span
     to the selected text.
     */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = getEditText();
                int selectionStart = editText.getSelectionStart();
                int selectionEnd = editText.getSelectionEnd();

                AssetManager assetManager = editText.getContext().getAssets();
                Typeface typeface = Typeface.createFromAsset(assetManager, "fonts/walkway.ttf");
                MyTypefaceSpan typefaceSpan = new MyTypefaceSpan(typeface);
                if (selectionStart == selectionEnd) {
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    ssb.append(Constants.ZERO_WIDTH_SPACE_STR);
                    ssb.setSpan(typefaceSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                    editText.getEditableText().replace(selectionStart, selectionEnd, ssb);
                } else {
                    editText.getEditableText().setSpan(typefaceSpan, selectionStart, selectionEnd,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                }
            }
        });
    }

    /*
     Applies the style formatting. Not required for dynamic fontface.
     */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        // Do nothing
    }

    /*
     Returns the material button used for the fontface style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mFontfaceButton;
    }

    /*
     Updates the checked state of the fontface style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        // Do nothing.
    }
}
