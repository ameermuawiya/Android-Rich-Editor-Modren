package com.muawiya.are;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

public class Constants {

    public static final String ZERO_WIDTH_SPACE_STR = "\u200B";
    public static final int ZERO_WIDTH_SPACE_INT = 8203;
    public static final String ZERO_WIDTH_SPACE_STR_ESCAPE = "&#8203;";
    public static final char CHAR_NEW_LINE = '\n';
    public static final int DEFAULT_FONT_SIZE = 18;
    public static int SCREEN_WIDTH = 0;
    public static int SCREEN_HEIGHT = 0;

    /* Live Updatable Toolbar Color */
    public static Integer CUSTOM_TOOLBAR_BACKGROUND_COLOR = null;

    /*
    Fetches the active toolbar background color. 
    Returns the custom color if set, otherwise defaults to Transparent.
    */
    public static int getToolbarBackgroundColor(Context context) {
        if (CUSTOM_TOOLBAR_BACKGROUND_COLOR != null) return CUSTOM_TOOLBAR_BACKGROUND_COLOR;
        return Color.TRANSPARENT; 
    }

    /*
    Material 3 background color for image placeholders.
    Adapts automatically to light/dark themes.
    */
    public static int getImagePlaceholderColor(Context context) {
        return getThemeColor(context, R.attr.colorSurfaceVariant, Color.LTGRAY);
    }

    /*
    Material 3 background color for video placeholders.
    */
    public static int getVideoPlaceholderColor(Context context) {
        return getThemeColor(context, R.attr.colorSurfaceContainerHighest, Color.DKGRAY);
    }

    /*
    Material 3 background color specifically for error states (e.g., broken image).
    */
    public static int getErrorBackgroundColor(Context context) {
        return getThemeColor(context, R.attr.colorErrorContainer, Color.parseColor("#FFCDD2"));
    }

    /*
    Material 3 color for icons inside the placeholder (e.g., placeholder icon, broken image icon).
    */
    public static int getPlaceholderIconColor(Context context) {
        return getThemeColor(context, R.attr.colorOnSurfaceVariant, Color.GRAY);
    }

    /*
    Material 3 color for the error icon specifically.
    */
    public static int getErrorIconColor(Context context) {
        return getThemeColor(context, R.attr.colorOnErrorContainer, Color.RED);
    }

    /*
    Material 3 color for the circular background of the play button.
    Uses inverse surface for high contrast visibility on videos.
    (Fixed the attribute name to match attrs.xml)
    */
    public static int getPlayButtonBgColor(Context context) {
        return getThemeColor(context, R.attr.colorInverseSurface, Color.BLACK);
    }

    /*
    Material 3 color for the play button triangle icon itself.
    (Fixed the attribute name to match attrs.xml)
    */
    public static int getPlayButtonIconColor(Context context) {
        return getThemeColor(context, R.attr.colorInverseOnSurface, Color.WHITE);
    }

    /*
    Fetches the dynamic color for the Blockquote vertical stripe.
    Uses the primary theme color to make the quote stand out beautifully.
    */
    public static int getQuoteColor(Context context) {
        return getThemeColor(context, R.attr.colorPrimary, Color.BLUE);
    }
    
        /*
    Returns the dynamic highlight background color for text.
    Uses the Material 3 Tertiary color with 40% transparency (0x66 alpha).
    This provides a much clearer, vibrant marker effect while keeping selection handles visible.
    */
    public static int getDefaultHighlightColor(Context context) {
        // colorTertiary is a vibrant accent color, perfect for distinct highlighting
        int baseColor = getThemeColor(context, R.attr.colorTertiaryContainer, Color.YELLOW);
        // Apply 40% opacity (0x66) to the strong base color
        return (baseColor & 0x00FFFFFF) | 0x88000000;
    }

    /*
    Extracts color attributes dynamically from the current active theme.
    */
    public static int getThemeColor(Context context, int attrResId, int fallbackColor) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data;
        }
        return fallbackColor;
    }
}
