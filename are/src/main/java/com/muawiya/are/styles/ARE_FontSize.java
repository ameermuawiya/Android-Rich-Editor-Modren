package com.muawiya.are.styles;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.muawiya.are.AREditText;
import com.muawiya.are.R;
import com.muawiya.are.spans.AreFontSizeSpan;
import com.muawiya.are.styles.toolbar.ARE_Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

public class ARE_FontSize extends ARE_ABS_Dynamic_Style<AreFontSizeSpan> {

    private MaterialButton mFontsizeButton;
    private ARE_Toolbar mToolbar;
    private AREditText mEditText;
    private static final int DEFAULT_FONT_SIZE = 18;
    private int mSize = DEFAULT_FONT_SIZE;
    private boolean mIsChecked;
    private Slider.OnChangeListener mSliderChangeListener;
    private int mSelStart = -1;
    private int mSelEnd = -1;

    /*
     Initializes the font size style and sets the click listener
     for the provided material button.
     */
    public ARE_FontSize(MaterialButton fontSizeButton, ARE_Toolbar toolbar) {
        super(fontSizeButton.getContext());
        this.mToolbar = toolbar;
        this.mFontsizeButton = fontSizeButton;
        setListenerForButton(this.mFontsizeButton);

        Slider slider = mToolbar.findViewById(R.id.are_fontsize_slider);
        if (slider != null) {
            mSliderChangeListener =
                    (sl, value, fromUser) -> {
                        if (fromUser) {
                            int size = Math.round(value);
                            onFontSizeChange(size);
                        }
                    };
            slider.addOnChangeListener(mSliderChangeListener);
        }
    }

    /*
     Sets the target edit text where the font size style
     will be applied.
     */
    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    /*
     Returns the currently attached edit text instance.
     */
    @Override
    public EditText getEditText() {
        return mEditText;
    }

    /*
     Attaches a click listener to toggle the font size slider visibility.
     */
    @Override
    public void setListenerForButton(final MaterialButton button) {
        button.setOnClickListener(v -> mToolbar.toggleFontSizeView());
    }

    /*
     Forces a refresh of the font size logic based on current selection.
     */
    public void forceRefresh() {
        if (mEditText != null) {
            onSelectionChanged(mEditText.getSelectionStart(), mEditText.getSelectionEnd());
        }
    }

    /*
     Monitors cursor position to update the slider UI dynamically based
     on the active font size span.
     */
    public void onSelectionChanged(int selStart, int selEnd) {
        if (mEditText == null) return;

        Editable editable = mEditText.getEditableText();
        int detectedSize = DEFAULT_FONT_SIZE;

        if (selStart < 0
                || selEnd < 0
                || selStart > editable.length()
                || selEnd > editable.length()) {
            mSize = detectedSize;
            updateSliderUI();
            return;
        }

        if (selStart != selEnd) {
            mSelStart = selStart;
            mSelEnd = selEnd;
            AreFontSizeSpan[] spans = editable.getSpans(selStart, selEnd, AreFontSizeSpan.class);
            for (AreFontSizeSpan span : spans) {
                int spanStart = editable.getSpanStart(span);
                int spanEnd = editable.getSpanEnd(span);
                if (spanStart <= selStart && spanEnd >= selEnd) {
                    detectedSize = span.getSize();
                    break;
                }
            }
        } else if (selStart > 0) {
            AreFontSizeSpan[] spans =
                    editable.getSpans(selStart - 1, selStart, AreFontSizeSpan.class);
            if (spans.length > 0) {
                detectedSize = spans[0].getSize();
            }
        }

        mSize = detectedSize;
        updateSliderUI();
    }

    /*
     Updates the slider value in the UI to match the detected font size.
     */
    private void updateSliderUI() {
        Slider slider = mToolbar.findViewById(R.id.are_fontsize_slider);
        if (slider == null) return;

        slider.removeOnChangeListener(mSliderChangeListener);

        float min = slider.getValueFrom();
        float max = slider.getValueTo();
        float safeValue = Math.max(min, Math.min(mSize, max));

        if (slider.getValue() != safeValue) {
            slider.setValue(safeValue);
        }

        slider.addOnChangeListener(mSliderChangeListener);
    }

    private void setSliderValueSafe(Slider slider, int desiredSize) {
        float min = slider.getValueFrom();
        float max = slider.getValueTo();
        float safeValue = Math.max(min, Math.min(desiredSize, max));

        slider.removeOnChangeListener(mSliderChangeListener);
        slider.setValue(safeValue);
        slider.addOnChangeListener(mSliderChangeListener);
        Log.d("FontSizeDebug", "Safe value set: " + safeValue);
    }

    /*
     Applies the newly selected font size from the slider to the text.
     */
    public void onFontSizeChange(int fontSize) {
        mIsChecked = true;
        mSize = fontSize;
        if (mEditText != null) {
            Editable editable = mEditText.getEditableText();
            int start = mEditText.getSelectionStart();
            int end = mEditText.getSelectionEnd();
            
            if (start == end || start < 0) {
                if (mSelStart >= 0 && mSelEnd >= 0 && mSelStart != mSelEnd && mSelEnd <= editable.length()) {
                    start = mSelStart;
                    end = mSelEnd;
                }
            }
            
            if (end > start) {
                applyNewStyle(editable, start, end, mSize);
                mEditText.setSelection(start, end);
            }
            // Force line metrics and layout recalculation to prevent text overlap
            float spacingExtra = mEditText.getLineSpacingExtra();
            float spacingMultiplier = mEditText.getLineSpacingMultiplier();
            mEditText.setLineSpacing(spacingExtra + 1.0f, spacingMultiplier);
            mEditText.setLineSpacing(spacingExtra, spacingMultiplier);
        }
    }

    /*
     Hook triggered when the feature is updated to apply the selected size.
     */
    @Override
    protected void featureChangedHook(int lastSpanFontSize) {
        mSize = (lastSpanFontSize > 0) ? lastSpanFontSize : DEFAULT_FONT_SIZE;
        updateSliderUI();
    }

    /*
     Applies the new font size if it differs from the existing span size.
     */
    @Override
    protected void changeSpanInsideStyle(
            Editable editable, int start, int end, AreFontSizeSpan existingSpan) {
        if (existingSpan.getSize() != mSize) {
            applyNewStyle(editable, start, end, mSize);
            if (mEditText != null) {
                // Force line metrics and layout recalculation to prevent text overlap
                float spacingExtra = mEditText.getLineSpacingExtra();
                float spacingMultiplier = mEditText.getLineSpacingMultiplier();
                mEditText.setLineSpacing(spacingExtra + 1.0f, spacingMultiplier);
                mEditText.setLineSpacing(spacingExtra, spacingMultiplier);
            }
        }
    }

    /*
     Creates and returns a new span instance for font size.
     */
    @Override
    public AreFontSizeSpan newSpan() {
        return new AreFontSizeSpan(mSize);
    }

    /*
     Creates a new font size span with the specified size.
     */
    @Override
    public AreFontSizeSpan newSpan(int size) {
        return new AreFontSizeSpan(size);
    }

    /*
     Returns the material button used for the font size style.
     */
    @Override
    public MaterialButton getButton() {
        return this.mFontsizeButton;
    }

    /*
     Updates the checked state of the font size style.
     */
    @Override
    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    /*
     Returns the current checked state of the font size style.
     */
    @Override
    public boolean getIsChecked() {
        return mIsChecked;
    }
}
