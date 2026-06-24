package com.muawiya.are;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

import com.muawiya.are.android.inner.Html;
import com.muawiya.are.events.AREMovementMethod;
import com.muawiya.are.helper.UndoRedoHelper;
import com.muawiya.are.render.AreImageGetter;
import com.muawiya.are.render.AreTagHandler;
import com.muawiya.are.spans.ARE_Clickable_Span;
import com.muawiya.are.spans.AreForegroundColorSpan;
import com.muawiya.are.spans.AreHrSpan;
import com.muawiya.are.spans.AreImageSpan;
import com.muawiya.are.spans.AreSubscriptSpan;
import com.muawiya.are.spans.AreSuperscriptSpan;
import com.muawiya.are.spans.AreUnderlineSpan;
import com.muawiya.are.spans.AreUrlSpan;
import com.muawiya.are.strategies.MentionStrategy;
import com.muawiya.are.strategies.ImageStrategy;
import com.muawiya.are.strategies.VideoStrategy;
import com.muawiya.are.styles.ARE_Helper;
import com.muawiya.are.styles.IARE_Style;
import com.muawiya.are.styles.toolbar.ARE_Toolbar;
import com.muawiya.are.styles.toolbar.IARE_Toolbar;
import com.muawiya.are.styles.toolitems.IARE_ToolItem;

import java.util.ArrayList;
import java.util.List;

public class AREditText extends AppCompatEditText {

    private IARE_Toolbar mToolbar;
    private static boolean LOG = false;
    private static boolean MONITORING = true;
    private ARE_Toolbar mFixedToolbar;
    private List<IARE_Style> mToolbarStylesList = new ArrayList<>();
    private Context mContext;
    private TextWatcher mTextWatcher;
    private MentionStrategy mMentionStrategy;
    private VideoStrategy mVideoStrategy;
    private ImageStrategy mImageStrategy;

    public AREditText(Context context) {
        this(context, null);
    }

    public AREditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AREditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initGlobalValues();
        init();
        setupListener();
    }

    private void initGlobalValues() {
        int[] wh = Util.getScreenWidthAndHeight(mContext);
        Constants.SCREEN_WIDTH = wh[0];
        Constants.SCREEN_HEIGHT = wh[1];
    }

    /*
    Initializes core configurations and attaches view to mention manager.
    Hardcoded padding is removed to allow XML customization. Scrollbars enabled.
    */
    private void init() {
        try {
            useSoftwareLayerOnAndroid8();
            this.setFocusableInTouchMode(true);
            this.setVerticalScrollBarEnabled(true);
            this.setInputType(
                    android.view.inputmethod.EditorInfo.TYPE_CLASS_TEXT
                            | android.view.inputmethod.EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
                            | android.view.inputmethod.EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            
            this.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, Constants.DEFAULT_FONT_SIZE);
            com.muawiya.are.spans.AreFontSizeSpan.sCurrentScaleFactor = 1.0f;

            com.muawiya.are.mentions.AreMentionManager.attachView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        com.muawiya.are.spans.AreFontSizeSpan.sCurrentScaleFactor = 1.0f;
    }

    private void paste(ClipData clip) {
        Editable mText = this.getEditableText();
        int min = 0;
        int max = mText.length();
        if (clip != null) {
            boolean didFirst = false;
            for (int i = 0; i < clip.getItemCount(); i++) {
                final CharSequence paste;
                paste = getClipItemCharSequence(clip.getItemAt(i));
                if (paste != null) {
                    if (!didFirst) {
                        Selection.setSelection((Spannable) mText, max);
                        ((Editable) mText).replace(min, max, paste);
                        didFirst = true;
                    } else {
                        ((Editable) mText).insert(getSelectionEnd(), "\n");
                        ((Editable) mText).insert(getSelectionEnd(), paste);
                    }
                }
            }
        }
    }

    @TargetApi(16)
    private CharSequence getClipItemCharSequence(ClipData.Item itemAt) {
        CharSequence text = getText();
        if (text instanceof Spanned) {
            return text;
        }
        String htmlText = itemAt.getHtmlText();
        if (htmlText != null) {
            try {
                Html.ImageGetter imageGetter = new AreImageGetter(mContext, this);
                Html.TagHandler tagHandler = new AreTagHandler();
                CharSequence newText =
                        Html.fromHtml(
                                htmlText,
                                Html.FROM_HTML_MODE_COMPACT,
                                imageGetter,
                                tagHandler);
                if (newText != null) {
                    return newText;
                }
            } catch (RuntimeException e) {
            }
        }
        return itemAt.coerceToStyledText(mContext);
    }

    /*
    Handles touch events to manage scrolling in MIN mode and cursor placement.
    Disallows parent scroll view from intercepting touch when inner scrolling is needed.
    */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Allow inner scrolling when maxLines is reached (MIN mode)
        if (hasFocus() && (canScrollVertically(-1) || canScrollVertically(1))) {
            getParent().requestDisallowInterceptTouchEvent(true);
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
        }

        int off = AREMovementMethod.getTextOffset(this, this.getEditableText(), event);
        ARE_Clickable_Span[] clickableSpans =
                this.getText().getSpans(off, off, ARE_Clickable_Span.class);

        if (clickableSpans.length == 1 && clickableSpans[0] instanceof AreImageSpan) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void setupListener() {
        setupTextWatcher();
    }

    private void setupTextWatcher() {
        mTextWatcher =
                new TextWatcher() {
                    int startPos = 0;
                    int endPos = 0;

                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!MONITORING) return;
                        this.startPos = start;
                        this.endPos = start + count;
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() == 0) {
                            Object[] spans = s.getSpans(0, 0, Object.class);
                            for (Object span : spans) {
                                if (!(span instanceof android.text.NoCopySpan)) {
                                    s.removeSpan(span);
                                }
                            }
                            resetAllStyles();
                        }
                        if (!MONITORING) return;
                        for (IARE_Style style : mToolbarStylesList) {
                            style.applyStyle(s, startPos, endPos);
                        }
                    }
                };
        this.addTextChangedListener(mTextWatcher);
    }

    public void setToolbar(IARE_Toolbar toolbar) {
        mToolbarStylesList.clear();
        this.mToolbar = toolbar;
        this.mToolbar.setEditText(this);
        for (IARE_ToolItem toolItem : toolbar.getToolItems()) {
            mToolbarStylesList.add(toolItem.getStyle());
        }
    }

    public void setFixedToolbar(ARE_Toolbar fixedToolbar) {
        mFixedToolbar = fixedToolbar;
        if (mFixedToolbar != null) {
            mToolbarStylesList = mFixedToolbar.getStylesList();
        }
    }

    public void resetAllStyles() {
        if (mToolbarStylesList != null) {
            for (IARE_Style style : mToolbarStylesList) {
                ARE_Helper.updateCheckStatus(style, false);
            }
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text == null || text.length() == 0) {
            Editable editable = getEditableText();
            if (editable != null) {
                editable.clear();
                Object[] spans = editable.getSpans(0, 0, Object.class);
                for (Object span : spans) {
                    if (!(span instanceof android.text.NoCopySpan)) {
                        editable.removeSpan(span);
                    }
                }
            }
            resetAllStyles();
        }
        super.setText(text, type);
    }

    private UndoRedoHelper mUndoRedoHelper;

    public void setUndoRedoHelper(UndoRedoHelper helper) {
        this.mUndoRedoHelper = helper;
    }

    public UndoRedoHelper getUndoRedoHelper() {
        return this.mUndoRedoHelper;
    }

    public boolean isHtmlMode() {
        if (mFixedToolbar != null && mFixedToolbar.getShowSourceStyle() != null) {
            return mFixedToolbar.getShowSourceStyle().isShowingSource();
        }
        return false;
    }

    public void setHtmlMode(boolean htmlMode) {
        if (mFixedToolbar != null && mFixedToolbar.getShowSourceStyle() != null) {
            mFixedToolbar.getShowSourceStyle().toggleToShowSource(htmlMode);
        }
    }

    private boolean isComposingSpan(Editable editable, Object span) {
        return (editable.getSpanFlags(span) & Spanned.SPAN_COMPOSING) == Spanned.SPAN_COMPOSING;
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
        if (mToolbar != null) {
            for (IARE_ToolItem toolItem : mToolbar.getToolItems()) {
                toolItem.onSelectionChanged(selStart, selEnd);
            }
            if (mToolbar.isFontSizeVisible() && mToolbar.getFontSizeStyle() != null) {
                mToolbar.getFontSizeStyle().onSelectionChanged(selStart, selEnd);
            }
        }

        if (mFixedToolbar == null) return;

        if (mFixedToolbar.getFontSizeStyle() != null) {
            mFixedToolbar.getFontSizeStyle().onSelectionChanged(selStart, selEnd);
        }

        boolean boldExists = false;
        boolean italicsExists = false;
        boolean underlinedExists = false;
        boolean strikethroughExists = false;
        boolean subscriptExists = false;
        boolean superscriptExists = false;
        boolean backgroundColorExists = false;
        boolean quoteExists = false;
        boolean hrExists = false;
        boolean fontColorExists = false;
        boolean linkExists = false;

        Editable editable = this.getEditableText();

        if (selStart > 0 && selStart == selEnd) {
            CharacterStyle[] styleSpans =
                    editable.getSpans(selStart - 1, selStart, CharacterStyle.class);

            for (CharacterStyle styleSpan : styleSpans) {
                if (isComposingSpan(editable, styleSpan)) continue;

                if (styleSpan instanceof StyleSpan) {
                    if (((StyleSpan) styleSpan).getStyle() == Typeface.BOLD) {
                        boldExists = true;
                    } else if (((StyleSpan) styleSpan).getStyle() == Typeface.ITALIC) {
                        italicsExists = true;
                    }
                } else if (styleSpan instanceof AreUnderlineSpan
                        || styleSpan instanceof android.text.style.UnderlineSpan) {
                    underlinedExists = true;
                } else if (styleSpan instanceof StrikethroughSpan) {
                    strikethroughExists = true;
                } else if (styleSpan instanceof BackgroundColorSpan) {
                    backgroundColorExists = true;
                } else if (styleSpan instanceof AreForegroundColorSpan
                        || styleSpan instanceof ForegroundColorSpan) {
                    fontColorExists = true;
                } else if (styleSpan instanceof AreUrlSpan || styleSpan instanceof URLSpan) {
                    linkExists = true;
                }
            }

            QuoteSpan[] quoteSpans = editable.getSpans(selStart - 1, selStart, QuoteSpan.class);
            if (quoteSpans != null && quoteSpans.length > 0) quoteExists = true;

            AreSubscriptSpan[] subscriptSpans =
                    editable.getSpans(selStart - 1, selStart, AreSubscriptSpan.class);
            if (subscriptSpans != null && subscriptSpans.length > 0) subscriptExists = true;

            AreSuperscriptSpan[] superscriptSpans =
                    editable.getSpans(selStart - 1, selStart, AreSuperscriptSpan.class);
            if (superscriptSpans != null && superscriptSpans.length > 0) superscriptExists = true;

            AreHrSpan[] hrSpans = editable.getSpans(selStart - 1, selStart, AreHrSpan.class);
            if (hrSpans != null && hrSpans.length > 0) hrExists = true;

        } else {
            CharacterStyle[] styleSpans = editable.getSpans(selStart, selEnd, CharacterStyle.class);

            for (CharacterStyle styleSpan : styleSpans) {
                if (isComposingSpan(editable, styleSpan)) continue;

                if (styleSpan instanceof StyleSpan) {
                    if (((StyleSpan) styleSpan).getStyle() == Typeface.BOLD) {
                        if (editable.getSpanStart(styleSpan) <= selStart
                                && editable.getSpanEnd(styleSpan) >= selEnd) {
                            boldExists = true;
                        }
                    } else if (((StyleSpan) styleSpan).getStyle() == Typeface.ITALIC) {
                        if (editable.getSpanStart(styleSpan) <= selStart
                                && editable.getSpanEnd(styleSpan) >= selEnd) {
                            italicsExists = true;
                        }
                    } else if (((StyleSpan) styleSpan).getStyle() == Typeface.BOLD_ITALIC) {
                        if (editable.getSpanStart(styleSpan) <= selStart
                                && editable.getSpanEnd(styleSpan) >= selEnd) {
                            italicsExists = true;
                            boldExists = true;
                        }
                    }
                } else if (styleSpan instanceof AreUnderlineSpan
                        || styleSpan instanceof android.text.style.UnderlineSpan) {
                    if (editable.getSpanStart(styleSpan) <= selStart
                            && editable.getSpanEnd(styleSpan) >= selEnd) {
                        underlinedExists = true;
                    }
                } else if (styleSpan instanceof StrikethroughSpan) {
                    if (editable.getSpanStart(styleSpan) <= selStart
                            && editable.getSpanEnd(styleSpan) >= selEnd) {
                        strikethroughExists = true;
                    }
                } else if (styleSpan instanceof BackgroundColorSpan) {
                    if (editable.getSpanStart(styleSpan) <= selStart
                            && editable.getSpanEnd(styleSpan) >= selEnd) {
                        backgroundColorExists = true;
                    }
                } else if (styleSpan instanceof AreForegroundColorSpan
                        || styleSpan instanceof ForegroundColorSpan) {
                    if (editable.getSpanStart(styleSpan) <= selStart
                            && editable.getSpanEnd(styleSpan) >= selEnd) {
                        fontColorExists = true;
                    }
                } else if (styleSpan instanceof AreUrlSpan || styleSpan instanceof URLSpan) {
                    if (editable.getSpanStart(styleSpan) <= selStart
                            && editable.getSpanEnd(styleSpan) >= selEnd) {
                        linkExists = true;
                    }
                }
            }

            AreHrSpan[] hrSpans = editable.getSpans(selStart, selEnd, AreHrSpan.class);
            if (hrSpans != null && hrSpans.length > 0) hrExists = true;
        }

        ARE_Helper.updateCheckStatus(mFixedToolbar.getBoldStyle(), boldExists);
        ARE_Helper.updateCheckStatus(mFixedToolbar.getItalicStyle(), italicsExists);
        ARE_Helper.updateCheckStatus(mFixedToolbar.getUnderlineStyle(), underlinedExists);
        ARE_Helper.updateCheckStatus(mFixedToolbar.getStrikethroughStyle(), strikethroughExists);
        ARE_Helper.updateCheckStatus(mFixedToolbar.getSubscriptStyle(), subscriptExists);
        ARE_Helper.updateCheckStatus(mFixedToolbar.getSuperscriptStyle(), superscriptExists);
        ARE_Helper.updateCheckStatus(mFixedToolbar.getBackgroundColoStyle(), backgroundColorExists);
        ARE_Helper.updateCheckStatus(mFixedToolbar.getQuoteStyle(), quoteExists);
        ARE_Helper.updateCheckStatus(mFixedToolbar.getHrStyle(), hrExists);
        ARE_Helper.updateCheckStatus(mFixedToolbar.getTextColorStyle(), fontColorExists);
        ARE_Helper.updateCheckStatus(mFixedToolbar.getLinkStyle(), linkExists);
    }

    /*
    Parses HTML, securely removes disabled mentions, and automatically upgrades
    default blockquotes and background colors to use dynamic Material 3 theme colors.
    */
    public void fromHtml(String html) {
        try {
            com.muawiya.are.android.inner.Html.sContext = mContext;
            com.muawiya.are.android.inner.Html.ImageGetter imageGetter =
                    new AreImageGetter(mContext, this);
            com.muawiya.are.android.inner.Html.TagHandler tagHandler = new AreTagHandler();
            Spanned spanned =
                    com.muawiya.are.android.inner.Html.fromHtml(
                            html,
                            com.muawiya.are.android.inner.Html.FROM_HTML_MODE_COMPACT,
                            imageGetter,
                            tagHandler);
            stopMonitor();
            
            Editable editable = this.getEditableText();
            if (editable != null) {
                editable.clear();
                Object[] spans = editable.getSpans(0, 0, Object.class);
                for (Object span : spans) {
                    if (!(span instanceof android.text.NoCopySpan)) {
                        editable.removeSpan(span);
                    }
                }
            }
            resetAllStyles();

            int previousLength = editable.length();
            
            // Trim trailing newlines to prevent blank lines from accumulating at the end
            CharSequence trimmedSpanned = spanned;
            int endPos = spanned.length();
            while (endPos > 0 && spanned.charAt(endPos - 1) == '\n') {
                endPos--;
            }
            if (endPos < spanned.length()) {
                trimmedSpanned = spanned.subSequence(0, endPos);
            }
            
            editable.append(trimmedSpanned);

            android.text.style.QuoteSpan[] quoteSpans = editable.getSpans(previousLength, editable.length(), android.text.style.QuoteSpan.class);
            int dynamicQuoteColor = Constants.getQuoteColor(mContext);
            for (android.text.style.QuoteSpan span : quoteSpans) {
                int start = editable.getSpanStart(span);
                int end = editable.getSpanEnd(span);
                int flags = editable.getSpanFlags(span);
                editable.removeSpan(span); 
                editable.setSpan(new com.muawiya.are.spans.AreQuoteSpan(dynamicQuoteColor), start, end, flags);
            }

            android.text.style.BackgroundColorSpan[] bgSpans = editable.getSpans(previousLength, editable.length(), android.text.style.BackgroundColorSpan.class);
            int dynamicHighlightColor = Constants.getDefaultHighlightColor(mContext);
            for (android.text.style.BackgroundColorSpan span : bgSpans) {
                int start = editable.getSpanStart(span);
                int end = editable.getSpanEnd(span);
                int flags = editable.getSpanFlags(span);
                editable.removeSpan(span); 
                editable.setSpan(new android.text.style.BackgroundColorSpan(dynamicHighlightColor), start, end, flags);
            }

            if (getMentionStrategy() == null || !getMentionStrategy().isEnabled()) {
                com.muawiya.are.spans.AreMentionSpan[] spans =
                        editable.getSpans(
                                0, editable.length(), com.muawiya.are.spans.AreMentionSpan.class);
                for (com.muawiya.are.spans.AreMentionSpan span : spans) {
                    editable.removeSpan(span);
                }
            }
            startMonitor();
            this.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        float spacingExtra = getLineSpacingExtra();
                        float spacingMultiplier = getLineSpacingMultiplier();
                        setLineSpacing(spacingExtra + 0.1f, spacingMultiplier);
                        setLineSpacing(spacingExtra, spacingMultiplier);
                        requestLayout();
                        invalidate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanUpComposingSpans() {
        Editable editable = getEditableText();
        if (editable == null) return;
        Object[] spans = editable.getSpans(0, editable.length(), Object.class);
        for (Object span : spans) {
            if ((editable.getSpanFlags(span) & Spanned.SPAN_COMPOSING) == Spanned.SPAN_COMPOSING) {
                editable.removeSpan(span);
            }
        }
    }

    public String getHtml() {
        cleanUpComposingSpans();
        StringBuffer html = new StringBuffer();
        html.append("<html><body>");
        String editTextHtml =
                Html.toHtml(getEditableText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
        html.append(editTextHtml);
        html.append("</body></html>");
        return html.toString().replaceAll(Constants.ZERO_WIDTH_SPACE_STR_ESCAPE, "");
    }

    public void useSoftwareLayerOnAndroid8() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public static void startMonitor() {
        MONITORING = true;
    }

    public static void stopMonitor() {
        MONITORING = false;
    }

    public void setMentionStrategy(MentionStrategy strategy) {
        this.mMentionStrategy = strategy;
    }

    public MentionStrategy getMentionStrategy() {
        return this.mMentionStrategy;
    }

    public void setVideoStrategy(VideoStrategy videoStrategy) {
        mVideoStrategy = videoStrategy;
    }

    public VideoStrategy getVideoStrategy() {
        return mVideoStrategy;
    }

    public void setImageStrategy(ImageStrategy imageStrategy) {
        mImageStrategy = imageStrategy;
    }

    public ImageStrategy getImageStrategy() {
        return mImageStrategy;
    }
}
