package com.chinalwb.are.render;

import android.text.Editable;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;

import com.chinalwb.are.android.inner.Html;
import org.xml.sax.XMLReader;

import java.util.Stack;

public class AreTagHandler implements Html.TagHandler {

    private static final Stack<ListTag> mLists = new Stack<>();

    private abstract static class ListTag {}
    private static class UL extends ListTag {}
    private static class OL extends ListTag {
        public int level = 1;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("ul")) {
            if (opening) {
                mLists.push(new UL());
            } else {
                mLists.pop();
            }
        } else if (tag.equalsIgnoreCase("ol")) {
            if (opening) {
                mLists.push(new OL());
            } else {
                mLists.pop();
            }
        } else if (tag.equalsIgnoreCase("li")) {
            if (opening) {
                startLi(output);
            } else {
                endLi(output);
            }
        }
    }

    private void startLi(Editable output) {
        if (mLists.isEmpty()) return;

        ListTag current = mLists.peek();
        int len = output.length();

        // Bullet + Number
        if (current instanceof UL) {
            output.append("\u2022  ");
            output.setSpan(new BulletSpan(8), len, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (current instanceof OL) {
            OL ol = (OL) current;
            output.append(ol.level + ".  ");
            ol.level++;
        }

        output.setSpan(new LeadingMarginSpan.Standard(40), len, output.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    private void endLi(Editable output) {
        if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
            output.append("\n");
        }
    }
}