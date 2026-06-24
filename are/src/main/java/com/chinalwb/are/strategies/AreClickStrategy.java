package com.chinalwb.are.strategies;

import android.content.Context;
import android.text.style.URLSpan;

import com.chinalwb.are.spans.ARE_Clickable_Span;
import com.chinalwb.are.spans.AreMentionSpan;
import com.chinalwb.are.spans.AreImageSpan;
import com.chinalwb.are.spans.AreVideoSpan;

public interface AreClickStrategy {

    /*
     * Triggers action when an at-mention span is clicked.
     */
    boolean onClickAt(Context context, AreMentionSpan atSpan);

    /*
     * Triggers action when an image span is clicked.
     */
    boolean onClickImage(Context context, AreImageSpan imageSpan);

    /*
     * Triggers action when a URL span is clicked.
     */
    boolean onClickUrl(Context context, URLSpan urlSpan);

    /*
     * Triggers action when a video span is clicked.
     */
    boolean onClickVideo(Context context, AreVideoSpan videoSpan);
}
