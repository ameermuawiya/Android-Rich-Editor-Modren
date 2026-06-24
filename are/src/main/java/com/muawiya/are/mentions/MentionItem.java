package com.muawiya.are.mentions;

import android.graphics.Color;
import java.io.Serializable;

/*
 * Data model representing a single mention entity.
 */
public class MentionItem implements Serializable {
    public String mName;
    public String mKey;
    public int mColor;
    public String mAvatarUrl; 
    public Object mPayload;
    public long mDateAdded;

    public MentionItem(String key, String name) {
        this(key, name, Color.BLUE, null);
    }

    public MentionItem(String key, String name, int color) {
        this(key, name, color, null);
    }

    public MentionItem(String key, String name, int color, String avatarUrl) {
        this.mKey = key;
        this.mName = name;
        this.mColor = color;
        this.mAvatarUrl = avatarUrl;
        this.mDateAdded = System.currentTimeMillis(); 
    }
}