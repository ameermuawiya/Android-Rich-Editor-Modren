package com.muawiya.are;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.core.widget.NestedScrollView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.muawiya.are.helper.UndoRedoHelper;
import com.muawiya.are.strategies.MentionStrategy;
import com.muawiya.are.strategies.ImageStrategy;
import com.muawiya.are.strategies.VideoStrategy;
import com.muawiya.are.styles.ARE_ToggleToolbar;
import com.muawiya.are.styles.toolbar.ARE_Toolbar;

public class AREditor extends RelativeLayout {

    public enum ToolbarAlignment {
        BOTTOM,
        TOP,
    }

    private Context mContext;
    private AttributeSet mAttrs;
    private ARE_Toolbar mToolbar;
    private NestedScrollView mAreScrollView;
    private AREditText mAre;
    private ToolbarAlignment mToolbarAlignment = ToolbarAlignment.BOTTOM;
    private boolean mHideToolbar = false;
    private ARE_FocusChangeListener mFocusListener;
    
    private int mPreviousKeypadHeight = -1;
    private Runnable mScrollRunnable;

    private UndoRedoHelper mUndoRedoHelper;

    public AREditor(Context context) {
        this(context, null);
    }

    public AREditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AREditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        this.mAttrs = attrs;
        this.init(attrs);
    }

    /*
    Initializes the editor component and sets up its default behavior.
    */
    private void init(AttributeSet attrs) {
        initAttrs(attrs);
        
        if (ARE_ToggleToolbar.isToolbarTop(mContext)) {
            mToolbarAlignment = ToolbarAlignment.TOP;
        }
        
        initGlobal();
        doLayout();
        setupKeyboardScrollFix();
    }

    /*
    Initializes the core UI containers including the toolbar and scroll view.
    */
    private void initGlobal() {
        this.mToolbar = new ARE_Toolbar(mContext);
        this.mToolbar.setId(R.id.are_toolbar);
        this.mToolbar.setEditor(this);

        this.mAreScrollView = new NestedScrollView(mContext);
        mAreScrollView.setFillViewport(true);
        this.mAreScrollView.setId(R.id.are_scrollview);
    }

    /*
    Parses custom XML attributes including the dynamic live colors and toolbar alignment.
    */
    private void initAttrs(AttributeSet attrs) {
        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.are);
        int toolbarAlignmentInt = ta.getInt(R.styleable.are_toolbarAlignment, ToolbarAlignment.BOTTOM.ordinal());
        this.mToolbarAlignment = ToolbarAlignment.values()[toolbarAlignmentInt];
        this.mHideToolbar = ta.getBoolean(R.styleable.are_hideToolbar, mHideToolbar);
        
        if (ta.hasValue(R.styleable.are_toolbarBackgroundColor)) {
            Constants.CUSTOM_TOOLBAR_BACKGROUND_COLOR = ta.getColor(R.styleable.are_toolbarBackgroundColor, 0);
        }
        ta.recycle();
    }

    /*
    Toggles the toolbar position between top and bottom dynamically.
    */
    public void toggleToolbarPosition() {
        try {
            ToolbarAlignment newPosition = (mToolbarAlignment == ToolbarAlignment.BOTTOM)
                    ? ToolbarAlignment.TOP
                    : ToolbarAlignment.BOTTOM;

            TransitionSet transition = new TransitionSet();
            transition.addTransition(new ChangeBounds());
            transition.setDuration(300);
            TransitionManager.beginDelayedTransition(this, transition);

            mToolbarAlignment = newPosition;
            doLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ToolbarAlignment getToolbarAlignment() {
        return mToolbarAlignment;
    }

    /*
    Constructs the overall layout dynamically and auto-initializes the Undo/Redo tracking.
    */
    private void doLayout() {
        if (this.indexOfChild(mToolbar) > -1) {
            this.removeView(mToolbar);
        }
        if (this.indexOfChild(mAreScrollView) > -1) {
            mAreScrollView.removeAllViews();
            this.removeView(mAreScrollView);
        }

        LayoutParams toolbarParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LayoutParams scrollViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        if (mToolbarAlignment == ToolbarAlignment.BOTTOM) {
            toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            scrollViewParams.addRule(RelativeLayout.ABOVE, mToolbar.getId());
            scrollViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        } else {
            toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            scrollViewParams.addRule(RelativeLayout.BELOW, mToolbar.getId());
            scrollViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        }

        mToolbar.setLayoutParams(toolbarParams);
        mAreScrollView.setLayoutParams(scrollViewParams);

        if (mToolbar != null) {
            mToolbar.updateContainersPosition(mToolbarAlignment == ToolbarAlignment.TOP);
            mToolbar.setVisibility(mHideToolbar ? View.GONE : View.VISIBLE);
            mToolbar.applyDynamicThemeColor();
        }

        if (mAre == null) {
            mAre = new AREditText(mContext, mAttrs);
            mAre.setId(Math.abs("AREditText_Stable_ID".hashCode()));
            mAre.setSaveEnabled(true);
        }

                if (mUndoRedoHelper == null) {
            mUndoRedoHelper = new UndoRedoHelper(mAre);
            mAre.setUndoRedoHelper(mUndoRedoHelper);
            mUndoRedoHelper.setMaxHistorySize(200);
        }


        if (mAreScrollView.indexOfChild(mAre) == -1) {
            NestedScrollView.LayoutParams editTextLayoutParams = new NestedScrollView.LayoutParams(
                    NestedScrollView.LayoutParams.MATCH_PARENT, 
                    NestedScrollView.LayoutParams.WRAP_CONTENT);
            
            mAreScrollView.addView(mAre, editTextLayoutParams);
        }

        this.mToolbar.setEditText(mAre);
        this.mAre.setFixedToolbar(mToolbar);

        if (mAre.getImageStrategy() != null && mToolbar.getImageStyle() != null) {
            mToolbar.getImageStyle().setEditText(mAre);
        }

        this.addView(mAreScrollView);
        this.addView(mToolbar);
    }

    /*
    Monitors keyboard state and smoothly adjusts the scroll position natively without jerks.
    */
    private void setupKeyboardScrollFix() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                getWindowVisibleDisplayFrame(r);
                int screenHeight = getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (mPreviousKeypadHeight == -1) {
                    mPreviousKeypadHeight = keypadHeight;
                    return;
                }

                if (keypadHeight > screenHeight * 0.15 && mPreviousKeypadHeight <= screenHeight * 0.15) {
                    if (mAre != null && mAre.hasFocus() && mAreScrollView != null) {
                        if (mScrollRunnable != null) {
                            mAre.removeCallbacks(mScrollRunnable);
                        }
                        mScrollRunnable = () -> {
                            int pos = mAre.getSelectionEnd();
                            if (pos >= 0 && mAre.getLayout() != null) {
                                int line = mAre.getLayout().getLineForOffset(pos);
                                int y = mAre.getLayout().getLineBottom(line) + mAre.getPaddingTop();
                                mAreScrollView.smoothScrollTo(0, y - (mAreScrollView.getHeight() / 2));
                            }
                        };
                        mAre.postDelayed(mScrollRunnable, 150); 
                    }
                } 
                else if (keypadHeight <= screenHeight * 0.15 && mPreviousKeypadHeight > screenHeight * 0.15) {
                    if (mAre != null && mAreScrollView != null) {
                        if (mScrollRunnable != null) {
                            mAre.removeCallbacks(mScrollRunnable);
                        }
                        mScrollRunnable = () -> {
                            int maxScroll = Math.max(0, mAreScrollView.getChildAt(0).getHeight() - mAreScrollView.getHeight());
                            if (mAreScrollView.getScrollY() > maxScroll) {
                                mAreScrollView.smoothScrollTo(0, maxScroll);
                            }
                        };
                        mAre.postDelayed(mScrollRunnable, 50);
                    }
                }
                mPreviousKeypadHeight = keypadHeight;
            }
        });
    }

    /*
    Delegates HTML parsing securely to AREditText and clears initial undo history cleanly.
    */
    public void fromHtml(String html) {
        if (this.mAre != null) {
            this.mAre.fromHtml(html);
            if (mUndoRedoHelper != null) {
                mAre.postDelayed(() -> mUndoRedoHelper.clearHistory(), 200);
            }
        }
    }

    public String getHtml() {
        if (this.mAre != null) return this.mAre.getHtml();
        return "";
    }

    public AREditText getARE() { return this.mAre; }

    /*
    Public API to undo the last action.
    */
    public void undo() {
        if (mUndoRedoHelper != null) mUndoRedoHelper.undo();
    }

    /*
    Public API to redo the last reverted action.
    */
    public void redo() {
        if (mUndoRedoHelper != null) mUndoRedoHelper.redo();
    }

    /*
    Public API to check if an undo operation is available.
    */
    public boolean getCanUndo() {
        return mUndoRedoHelper != null && mUndoRedoHelper.getCanUndo();
    }

    /*
    Public API to check if a redo operation is available.
    */
    public boolean getCanRedo() {
        return mUndoRedoHelper != null && mUndoRedoHelper.getCanRedo();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            if (this.mToolbar != null) this.mToolbar.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void setToolbarAlignment(ToolbarAlignment alignment) {
        mToolbarAlignment = alignment;
        doLayout();
    }

    public void setHideToolbar(boolean hideToolbar) {
        mHideToolbar = hideToolbar;
        doLayout();
    }

    public void setHint(CharSequence hint) { if (mAre != null) mAre.setHint(hint); }
    public void setHint(int resid) { if (mAre != null) mAre.setHint(resid); }
    public CharSequence getHint() { return mAre != null ? mAre.getHint() : null; }
    public void setText(CharSequence text) { if (mAre != null) mAre.setText(text); }
    public Editable getText() { return mAre != null ? mAre.getText() : null; }
    public void setTextColor(int color) { if (mAre != null) mAre.setTextColor(color); }
    public void setHintTextColor(int color) { if (mAre != null) mAre.setHintTextColor(color); }
    public void setTextSize(float size) { if (mAre != null) mAre.setTextSize(size); }
    public void setTextSize(int unit, float size) { if (mAre != null) mAre.setTextSize(unit, size); }
    public void setTypeface(Typeface tf) { if (mAre != null) mAre.setTypeface(tf); }
    public void setInputType(int type) { if (mAre != null) mAre.setInputType(type); }
    public void setSelection(int index) { if (mAre != null) mAre.setSelection(index); }
    public void setSelection(int start, int stop) { if (mAre != null) mAre.setSelection(start, stop); }
    public void setMentionStrategy(MentionStrategy mentionStrategy) { this.mAre.setMentionStrategy(mentionStrategy); }
    public void setVideoStrategy(VideoStrategy videoStrategy) { this.mAre.setVideoStrategy(videoStrategy); }
    public void setImageStrategy(ImageStrategy imageStrategy) { this.mAre.setImageStrategy(imageStrategy); }

    public interface ARE_FocusChangeListener {
        void onFocusChanged(AREditor arEditor, boolean hasFocus);
    }

    public void setAreFocusChangeListener(ARE_FocusChangeListener listener) {
        this.mFocusListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN && mFocusListener != null) {
            mFocusListener.onFocusChanged(this, true);
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setToolbarBackgroundColor(int color) {
        Constants.CUSTOM_TOOLBAR_BACKGROUND_COLOR = color;
        if (mToolbar != null) mToolbar.applyDynamicThemeColor();
    }
}
