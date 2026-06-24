package com.muawiya.are.spans;

import android.text.style.URLSpan;

public class AreUrlSpan extends URLSpan implements ARE_Clickable_Span {

    /*
     * Constructs the URL span with the provided URL string.
     */
    public AreUrlSpan(String url) {
        super(url);
    }

}
