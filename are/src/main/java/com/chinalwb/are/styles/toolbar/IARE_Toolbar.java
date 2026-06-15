package com.chinalwb.are.styles.toolbar;

import android.content.Intent;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem;

import java.util.List;

public interface IARE_Toolbar {

    void addToolbarItem(IARE_ToolItem toolbarItem);

    List<IARE_ToolItem> getToolItems();

    void setEditText(AREditText editText);

    AREditText getEditText();

    com.chinalwb.are.styles.ARE_FontSize getFontSizeStyle();

    void onActivityResult(int requestCode, int resultCode, Intent data);

    boolean isFontSizeVisible();
}