package com.muawiya.are.styles;

import com.muawiya.are.spans.AreImageSpan;

/*
 Interface for handling image insertion capabilities.
 */
public interface IARE_Image {
    public void insertImage(final Object src, final AreImageSpan.ImageType type);
}
