package com.muawiya.are.spans;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import com.muawiya.are.mentions.AreMentionManager;
import com.muawiya.are.mentions.MentionItem;

/*
 * Represents the rendered visual state of a mention in the editor.
 */
public class AreMentionSpan extends ReplacementSpan implements ARE_Span, ARE_Clickable_Span {

    private MentionItem mMentionItem;
    private String mUserKey;
    private String mUserName;
    private int mColor;

    public AreMentionSpan(MentionItem item) {
        this.mMentionItem = item;
        this.mUserKey = item.mKey;
        this.mUserName = item.mName;
        this.mColor = item.mColor;
    }

    public MentionItem getMentionItem() {
        try {
            MentionItem original = AreMentionManager.getInstance().getOriginalItem(mUserKey);
            if (original != null) return original;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mMentionItem == null) mMentionItem = new MentionItem(mUserKey, mUserName, mColor);
        return mMentionItem;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) paint.measureText(text, start, end);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        paint.setColor(Color.TRANSPARENT);
        float width = paint.measureText(text.toString(), start, end);
        canvas.drawRect(x, top, x + width, bottom, paint);
        paint.setColor(0xFF000000 | mColor);
        canvas.drawText(text, start, end, x, (float) y, paint);
    }

    @Override
    public String getHtml() {
        return "<a href=\"#\" uKey=\"" + mUserKey + "\" uName=\"" + mUserName + "\" style=\"color:#" + String.format("%06X", 0xFFFFFF & mColor) + ";\">@" + this.mUserName + "</a>";
    }

    public String getUserName() { return this.mUserName; }
    public String getUserKey() { return this.mUserKey; }
}