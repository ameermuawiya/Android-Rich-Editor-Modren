package com.chinalwb.are.styles.toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Layout.Alignment;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.transition.Slide;
import androidx.transition.TransitionManager;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.AREditor;
import com.chinalwb.are.Constants;
import com.chinalwb.are.R;
import com.chinalwb.are.activities.Are_VideoPlayerActivity;
import com.chinalwb.are.colorpicker.ColorPickerListener;
import com.chinalwb.are.colorpicker.ColorPickerView;
import com.chinalwb.are.styles.ARE_Alignment;
import com.chinalwb.are.styles.ARE_Mention;
import com.chinalwb.are.styles.ARE_BackgroundColor;
import com.chinalwb.are.styles.ARE_Bold;
import com.chinalwb.are.styles.ARE_ClearFormat;
import com.chinalwb.are.styles.ARE_FontColor;
import com.chinalwb.are.styles.ARE_FontSize;
import com.chinalwb.are.styles.ARE_Fontface;
import com.chinalwb.are.styles.ARE_Hr;
import com.chinalwb.are.styles.ARE_Image;
import com.chinalwb.are.styles.ARE_IndentLeft;
import com.chinalwb.are.styles.ARE_IndentRight;
import com.chinalwb.are.styles.ARE_Italic;
import com.chinalwb.are.styles.ARE_Link;
import com.chinalwb.are.styles.ARE_ListBullet;
import com.chinalwb.are.styles.ARE_ListNumber;
import com.chinalwb.are.styles.ARE_Quote;
import com.chinalwb.are.styles.ARE_ShowSource;
import com.chinalwb.are.styles.ARE_Strikethrough;
import com.chinalwb.are.styles.ARE_Subscript;
import com.chinalwb.are.styles.ARE_Superscript;
import com.chinalwb.are.styles.ARE_ToggleToolbar;
import com.chinalwb.are.styles.ARE_Underline;
import com.chinalwb.are.styles.ARE_Video;
import com.chinalwb.are.styles.IARE_Style;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

public class ARE_Toolbar extends LinearLayout {

    public static final int REQ_IMAGE = 1;
    public static final int REQ_AT = 2;
    public static final int REQ_VIDEO_CHOOSE = 3;
    public static final int REQ_VIDEO = 4;
    private boolean isFontSizeVisible = false;

    private Activity mContext;
    private AREditText mEditText;
    private AREditor mEditorContainer;
    private ArrayList<IARE_Style> mStylesList = new ArrayList<>();

    private LinearLayout mFontSizeContainer;
    private Slider mFontSizeSlider;
    private ColorPickerView mColorPalette;
    private HorizontalScrollView mFormattingToolsBar;

    private ARE_FontSize mFontSizeStyle;
    private ARE_Fontface mFontfaceStyle;
    private ARE_Bold mBoldStyle;
    private ARE_Italic mItalicStyle;
    private ARE_Underline mUnderlineStyle;
    private ARE_Strikethrough mStrikethroughStyle;
    private ARE_Hr mHrStyle;
    private ARE_Subscript mSubscriptStyle;
    private ARE_Superscript mSuperscriptStyle;
    private ARE_Quote mQuoteStyle;
    private ARE_FontColor mFontColorStyle;
    private ARE_BackgroundColor mBackgroundColoStyle;
    private ARE_Link mLinkStyle;
    private ARE_ListNumber mListNumberStyle;
    private ARE_ListBullet mListBulletStyle;
    private ARE_IndentRight mIndentRightStyle;
    private ARE_IndentLeft mIndentLeftStyle;
    private ARE_Alignment mAlignLeft;
    private ARE_Alignment mAlignCenter;
    private ARE_Alignment mAlignRight;
    private ARE_Image mImageStyle;
    private ARE_Video mVideoStyle;
    private ARE_Mention mMentionStyle;

    private ARE_ClearFormat mClearFormMentionStyle;
    private ARE_ShowSource mShowSourceStyle;
    private ARE_ToggleToolbar mToggleToolbarStyle;

    private MaterialButton mFontsizeButton;
    private MaterialButton mFontfaceButton;
    private MaterialButton mBoldButton;
    private MaterialButton mItalicButton;
    private MaterialButton mUnderlineButton;
    private MaterialButton mStrikethroughButton;
    private MaterialButton mHrButton;
    private MaterialButton mSubscriptButton;
    private MaterialButton mSuperscriptButton;
    private MaterialButton mQuoteButton;
    private MaterialButton mFontColorButton;
    private MaterialButton mBackgroundButton;
    private MaterialButton mLinkButton;
    private MaterialButton mRteListNumber;
    private MaterialButton mRteListBullet;
    private MaterialButton mRteIndentRight;
    private MaterialButton mRteIndentLeft;
    private MaterialButton mRteAlignLeft;
    private MaterialButton mRteAlignCenter;
    private MaterialButton mRteAlignRight;
    private MaterialButton mRteInsertImage;
    private MaterialButton mRteInsertVideo;
    private MaterialButton mRteAtButton;
    
    private MaterialButton mClearFormatButton;
    private MaterialButton mShowSourceButton;
    private MaterialButton mUpDownButton;

    public ARE_Toolbar(Context context) {
        this(context, null);
    }

    public ARE_Toolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ARE_Toolbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = (Activity) context;
        init();
    }

    /*
    Initializes the layout, views, and all formatting styles for the toolbar.
    */
    private void init() {
        LayoutInflater layoutInflater = LayoutInflater.from(this.mContext);
        layoutInflater.inflate(R.layout.are_formating_toolbar, this, true);
        this.setOrientation(LinearLayout.VERTICAL);
        initViews();
        initStyles();
    }

    /*
    Finds and binds all the layout containers and MaterialButtons by their respective IDs.
    */
    private void initViews() {
        this.mFontSizeContainer = this.findViewById(R.id.rteFontSizeContainer);
        this.mFontSizeSlider = this.findViewById(R.id.are_fontsize_slider);
        this.mColorPalette = this.findViewById(R.id.rteColorPalette);
        this.mFormattingToolsBar = this.findViewById(R.id.FormatingToolsBar);

        this.mFontsizeButton = this.findViewById(R.id.rteFontsize);
        this.mFontfaceButton = this.findViewById(R.id.rteFontface);
        this.mBoldButton = this.findViewById(R.id.rteBold);
        this.mItalicButton = this.findViewById(R.id.rteItalic);
        this.mUnderlineButton = this.findViewById(R.id.rteUnderline);
        this.mStrikethroughButton = this.findViewById(R.id.rteStrikethrough);
        this.mHrButton = this.findViewById(R.id.rteHr);
        this.mSubscriptButton = this.findViewById(R.id.rteSubscript);
        this.mSuperscriptButton = this.findViewById(R.id.rteSuperscript);
        this.mQuoteButton = this.findViewById(R.id.rteQuote);
        this.mFontColorButton = this.findViewById(R.id.rteFontColor);
        this.mBackgroundButton = this.findViewById(R.id.rteBackground);
        this.mLinkButton = this.findViewById(R.id.rteLink);
        this.mRteListNumber = this.findViewById(R.id.rteListNumber);
        this.mRteListBullet = this.findViewById(R.id.rteListBullet);
        this.mRteIndentRight = this.findViewById(R.id.rteIndentRight);
        this.mRteIndentLeft = this.findViewById(R.id.rteIndentLeft);
        this.mRteAlignLeft = this.findViewById(R.id.rteAlignLeft);
        this.mRteAlignCenter = this.findViewById(R.id.rteAlignCenter);
        this.mRteAlignRight = this.findViewById(R.id.rteAlignRight);
        this.mRteInsertImage = this.findViewById(R.id.rteInsertImage);
        this.mRteInsertVideo = this.findViewById(R.id.rteInsertVideo);
        this.mRteAtButton = this.findViewById(R.id.rteAt);

        this.mClearFormatButton = this.findViewById(R.id.rteClearFormat);
        this.mShowSourceButton = this.findViewById(R.id.rteShowSource);
        this.mUpDownButton = this.findViewById(R.id.rteUpDown);
    }

    /*
    Initializes style classes and passes the respective material buttons to them.
    */
    private void initStyles() {
        this.mFontSizeStyle = new ARE_FontSize(this.mFontsizeButton, this);
        this.mFontfaceStyle = new ARE_Fontface(this.mFontfaceButton, this);
        this.mBoldStyle = new ARE_Bold(this.mBoldButton);
        this.mItalicStyle = new ARE_Italic(this.mItalicButton);
        this.mUnderlineStyle = new ARE_Underline(this.mUnderlineButton);
        this.mStrikethroughStyle = new ARE_Strikethrough(this.mStrikethroughButton);
        this.mHrStyle = new ARE_Hr(this.mHrButton, this);
        this.mSubscriptStyle = new ARE_Subscript(this.mSubscriptButton);
        this.mSuperscriptStyle = new ARE_Superscript(this.mSuperscriptButton);
        this.mQuoteStyle = new ARE_Quote(this.mQuoteButton);
        this.mFontColorStyle = new ARE_FontColor(this.mFontColorButton, this);
        this.mBackgroundColoStyle = new ARE_BackgroundColor(this.mBackgroundButton);
        
        this.mLinkStyle = new ARE_Link(this.mLinkButton, this);
        this.mListNumberStyle = new ARE_ListNumber(this.mRteListNumber, this);
        this.mListBulletStyle = new ARE_ListBullet(this.mRteListBullet, this);
        this.mIndentRightStyle = new ARE_IndentRight(this.mRteIndentRight, this);
        this.mIndentLeftStyle = new ARE_IndentLeft(this.mRteIndentLeft, this);
        this.mAlignLeft = new ARE_Alignment(this.mRteAlignLeft, Alignment.ALIGN_NORMAL, this);
        this.mAlignCenter = new ARE_Alignment(this.mRteAlignCenter, Alignment.ALIGN_CENTER, this);
        this.mAlignRight = new ARE_Alignment(this.mRteAlignRight, Alignment.ALIGN_OPPOSITE, this);
        this.mImageStyle = new ARE_Image(this.mRteInsertImage);
        this.mVideoStyle = new ARE_Video(this.mRteInsertVideo);
        this.mMentionStyle = new ARE_Mention(this);

        this.mClearFormMentionStyle = new ARE_ClearFormat(this.mClearFormatButton, this.mContext);
        this.mShowSourceStyle = new ARE_ShowSource(this.mShowSourceButton);

        this.mStylesList.add(this.mFontSizeStyle);
        this.mStylesList.add(this.mFontfaceStyle);
        this.mStylesList.add(this.mBoldStyle);
        this.mStylesList.add(this.mItalicStyle);
        this.mStylesList.add(this.mUnderlineStyle);
        this.mStylesList.add(this.mStrikethroughStyle);
        this.mStylesList.add(this.mHrStyle);
        this.mStylesList.add(this.mSubscriptStyle);
        this.mStylesList.add(this.mSuperscriptStyle);
        this.mStylesList.add(this.mQuoteStyle);
        this.mStylesList.add(this.mFontColorStyle);
        this.mStylesList.add(this.mBackgroundColoStyle);
        this.mStylesList.add(this.mLinkStyle);
        this.mStylesList.add(this.mListNumberStyle);
        this.mStylesList.add(this.mListBulletStyle);
        this.mStylesList.add(this.mIndentRightStyle);
        this.mStylesList.add(this.mIndentLeftStyle);
        this.mStylesList.add(this.mAlignLeft);
        this.mStylesList.add(this.mAlignCenter);
        this.mStylesList.add(this.mAlignRight);
        this.mStylesList.add(this.mImageStyle);
        this.mStylesList.add(this.mVideoStyle);
        this.mStylesList.add(this.mMentionStyle);
    }

    /*
    Connects the editor instance to the toggle toolbar action.
    */
    public void setEditor(AREditor editor) {
        this.mEditorContainer = editor;
        this.mToggleToolbarStyle = new ARE_ToggleToolbar(this.mUpDownButton, editor);
    }

    /*
    Shifts the positions of extra panels to stick naturally with the main toolbar items.
    */
    public void updateContainersPosition(boolean isTop) {
        try {
            if (mFormattingToolsBar != null) {
                this.removeView(mFormattingToolsBar);
                if (isTop) {
                    this.addView(mFormattingToolsBar, 0);
                } else {
                    this.addView(mFormattingToolsBar);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Sets the target EditText and binds the toolbar styles to it.
    */
    public void setEditText(AREditText editText) {
        this.mEditText = editText;
        bindToolbar();
    }

    /*
    Links each initialized style to the current EditText instance.
    */
    private void bindToolbar() {
        this.mFontSizeStyle.setEditText(this.mEditText);
        this.mBoldStyle.setEditText(this.mEditText);
        this.mItalicStyle.setEditText(this.mEditText);
        this.mUnderlineStyle.setEditText(this.mEditText);
        this.mStrikethroughStyle.setEditText(this.mEditText);
        this.mHrStyle.setEditText(this.mEditText);
        this.mSubscriptStyle.setEditText(this.mEditText);
        this.mSuperscriptStyle.setEditText(this.mEditText);
        this.mQuoteStyle.setEditText(this.mEditText);
        this.mFontColorStyle.setEditText(this.mEditText);
        this.mBackgroundColoStyle.setEditText(this.mEditText);
        this.mLinkStyle.setEditText(this.mEditText);
        this.mImageStyle.setEditText(this.mEditText);
        this.mVideoStyle.setEditText(this.mEditText);
        this.mMentionStyle.setEditText(this.mEditText);
        
        this.mClearFormMentionStyle.setEditText(this.mEditText);
        this.mShowSourceStyle.setEditText(this.mEditText);
    }

    public AREditText getEditText() { return this.mEditText; }
    public IARE_Style getBoldStyle() { return this.mBoldStyle; }
    public ARE_Link getLinkStyle() { return this.mLinkStyle; }
    public ARE_FontSize getFontSizeStyle() { return this.mFontSizeStyle; }
    public ARE_Italic getItalicStyle() { return this.mItalicStyle; }
    public ARE_Underline getUnderlineStyle() { return mUnderlineStyle; }
    public ARE_Strikethrough getStrikethroughStyle() { return mStrikethroughStyle; }
    public ARE_Hr getHrStyle() { return mHrStyle; }
    public ARE_Subscript getSubscriptStyle() { return this.mSubscriptStyle; }
    public ARE_Superscript getSuperscriptStyle() { return this.mSuperscriptStyle; }
    public ARE_Quote getQuoteStyle() { return mQuoteStyle; }
    public ARE_FontColor getTextColorStyle() { return this.mFontColorStyle; }
    public ARE_BackgroundColor getBackgroundColoStyle() { return mBackgroundColoStyle; }
    public ARE_Image getImageStyle() { return mImageStyle; }
    public ARE_ShowSource getShowSourceStyle() { return mShowSourceStyle; }
    public List<IARE_Style> getStylesList() { return this.mStylesList; }

    /*
    Applies a smooth slide animation to the layout whenever a child view is shown or hidden.
    Slide direction adapts automatically based on the toolbar's position (Top or Bottom).
    */
    private void beginSmoothTransition() {
        if (this.getParent() instanceof ViewGroup) {
            Slide slideTransition = new Slide();
            slideTransition.setDuration(250); // Fast and snappy
            // Determine slide direction based on toolbar alignment
            if (mEditorContainer != null && mEditorContainer.getToolbarAlignment() == AREditor.ToolbarAlignment.TOP) {
                slideTransition.setSlideEdge(Gravity.TOP);
            } else {
                slideTransition.setSlideEdge(Gravity.BOTTOM);
            }
            TransitionManager.beginDelayedTransition(this, slideTransition);
        }
    }

    /*
    Toggles the visibility of the color palette view with a smooth slide animation.
    */
    public void toggleColorPalette(ColorPickerListener colorPickerListener) {
        if (mColorPalette == null) return;
        mColorPalette.setColorPickerListener(colorPickerListener);
        
        beginSmoothTransition();
        
        if (mColorPalette.getVisibility() == View.VISIBLE) {
            mColorPalette.setVisibility(View.GONE);
        } else {
            if (mFontSizeContainer != null && mFontSizeContainer.getVisibility() == View.VISIBLE) {
                mFontSizeContainer.setVisibility(View.GONE);
                isFontSizeVisible = false;
            }
            mColorPalette.setVisibility(View.VISIBLE);
        }
    }

    /*
    Toggles the visibility of the font size selection view with a smooth slide animation.
    */
    public void toggleFontSizeView() {
        if (mFontSizeContainer == null) return;
        
        beginSmoothTransition();
        
        if (mFontSizeContainer.getVisibility() == View.VISIBLE) {
            mFontSizeContainer.setVisibility(View.GONE);
            isFontSizeVisible = false;
        } else {
            if (mColorPalette != null && mColorPalette.getVisibility() == View.VISIBLE) {
                mColorPalette.setVisibility(View.GONE);
            }
            mFontSizeContainer.setVisibility(View.VISIBLE);
            isFontSizeVisible = true;
            if (mFontSizeStyle != null && mEditText != null) {
                mFontSizeStyle.onSelectionChanged(
                        mEditText.getSelectionStart(), mEditText.getSelectionEnd());
            }
        }
    }

    public boolean isFontSizeVisible() { return isFontSizeVisible; }

    public void setColorPaletteColor(int color) {
        if (mColorPalette != null) mColorPalette.setColor(color);
    }
    
    public void openVideoPlayer(Uri uri) {
        Intent intent = new Intent();
        intent.setClass(mContext, Are_VideoPlayerActivity.class);
        intent.setData(uri);
        mContext.startActivityForResult(intent, REQ_VIDEO);
    }

    /*
    Handles activity results generated from various formatting dialogs and features.
    */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            if (REQ_IMAGE == requestCode) {
                Uri uri = data.getData();
                if (uri != null) {
                    if (mEditText.getImageStrategy() != null && mImageStyle.shouldUploadSelectedImage()) {
                        mEditText.getImageStrategy().uploadAndInsertImage(uri, mImageStyle);
                    } else {
                        mImageStyle.insertImage(uri, com.chinalwb.are.spans.AreImageSpan.ImageType.URI);
                    }
                }
            } else if (REQ_VIDEO_CHOOSE == requestCode) {
                openVideoPlayer(data.getData());
            } else if (REQ_VIDEO == requestCode) {
                this.mVideoStyle.insertVideo(data.getData(), data.getStringExtra(Are_VideoPlayerActivity.VIDEO_URL));
            }
        }
    }

    /*
    Applies the dynamic background color to the toolbar and its expandable containers.
    Ensures UI consistency across the entire bottom tool panel.
    */
    public void applyDynamicThemeColor() {
        int bgColor = com.chinalwb.are.Constants.getToolbarBackgroundColor(mContext);
        if (mFormattingToolsBar != null) mFormattingToolsBar.setBackgroundColor(bgColor);
        if (mFontSizeContainer != null) mFontSizeContainer.setBackgroundColor(bgColor);
        if (mColorPalette != null) mColorPalette.setBackgroundColor(bgColor);
    }
}
