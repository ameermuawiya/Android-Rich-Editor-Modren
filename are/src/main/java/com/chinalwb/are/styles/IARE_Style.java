package com.chinalwb.are.styles;

import android.text.Editable;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;

public interface IARE_Style {

  /*
   * For styles like Bold / Italic / Underline, by clicking the button,
   * we should change the UI, so user can notice that this style takes 
   * effect now.
   */
  public void setListenerForButton(MaterialButton button);
  
  /*
   * Apply the style to the change start at start end at end.
   */
  public void applyStyle(Editable editable, int start, int end);
  
  /*
   * Returns the MaterialButton of this style.
   */
  public MaterialButton getButton();
  
  /*
   * Sets if this style is checked.
   */
  public void setChecked(boolean isChecked);

  /*
   * Returns if current style is checked.
   */
  public boolean getIsChecked();
  
  /*
   * Gets the EditText being operated.
   */
  public EditText getEditText();
}
