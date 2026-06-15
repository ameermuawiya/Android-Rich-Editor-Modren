package com.chinalwb.are.mentions;

import android.content.Context;
import android.text.Spannable;
import android.widget.TextView;

import com.chinalwb.are.spans.AreMentionSpan;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Global independent manager for handling mention states and behaviors.
 * Provides a chainable API for external modules.
 */
public class AreMentionManager {

    private static AreMentionManager sInstance;

    private boolean mIsEnabled = false;
    private List<MentionItem> mMentionItems;
    private Map<String, MentionItem> mItemRegistry;
    private List<WeakReference<TextView>> mAttachedViews;

    private String mEmptyMessage = "No matching contacts";
    private int mPlaceholderId = 0;
    private int mErrorId = 0;

    private OnMentionSelectedListener mSelectedListener;
    private OnMentionClickListener mClickListener;

    public enum SortType {
        NAME_ASCENDING, NAME_DESCENDING, DATE_ASCENDING, DATE_DESCENDING
    }

    public interface OnMentionSelectedListener {
        void onSelected(MentionItem item);
    }

    public interface OnMentionClickListener {
        void onClick(Context context, MentionItem item);
    }

    public static AreMentionManager getInstance() {
        if (sInstance == null) {
            synchronized (AreMentionManager.class) {
                if (sInstance == null) {
                    sInstance = new AreMentionManager();
                }
            }
        }
        return sInstance;
    }

    private AreMentionManager() {
        mMentionItems = new ArrayList<>();
        mItemRegistry = new HashMap<>();
        mAttachedViews = new ArrayList<>();
    }

    public AreMentionManager setMentionsEnabled(boolean enabled) {
        this.mIsEnabled = enabled;
        return this;
    }

    public AreMentionManager setMentionData(List<MentionItem> items) {
        try {
            this.mMentionItems.clear();
            this.mItemRegistry.clear();

            if (items != null) {
                this.mMentionItems.addAll(items);
                for (MentionItem item : items) {
                    if (item.mKey != null) {
                        this.mItemRegistry.put(item.mKey, item);
                    }
                }
            }
            cleanInvalidMentionsFromAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public AreMentionManager sortMentions(SortType sortType) {
        try {
            if (this.mMentionItems == null || this.mMentionItems.isEmpty()) return this;

            Collections.sort(this.mMentionItems, (o1, o2) -> {
                switch (sortType) {
                    case NAME_ASCENDING: return o1.mName.compareToIgnoreCase(o2.mName);
                    case NAME_DESCENDING: return o2.mName.compareToIgnoreCase(o1.mName);
                    case DATE_ASCENDING: return Long.compare(o1.mDateAdded, o2.mDateAdded);
                    case DATE_DESCENDING: return Long.compare(o2.mDateAdded, o1.mDateAdded);
                    default: return 0;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public AreMentionManager setPlaceholders(int placeholderId, int errorId) {
        this.mPlaceholderId = placeholderId;
        this.mErrorId = errorId;
        return this;
    }

    public AreMentionManager setEmptyMessage(String emptyMessage) {
        if (emptyMessage != null && !emptyMessage.trim().isEmpty()) {
            this.mEmptyMessage = emptyMessage;
        }
        return this;
    }

    public AreMentionManager setOnMentionSelectedListener(OnMentionSelectedListener listener) {
        this.mSelectedListener = listener;
        return this;
    }

    public AreMentionManager setOnMentionClickListener(OnMentionClickListener listener) {
        this.mClickListener = listener;
        return this;
    }

    public static void attachView(TextView textView) {
        try {
            if (textView == null) return;
            AreMentionManager manager = getInstance();
            manager.mAttachedViews.removeIf(ref -> ref.get() == null);

            boolean attached = false;
            for (WeakReference<TextView> ref : manager.mAttachedViews) {
                if (ref.get() == textView) attached = true;
            }
            if (!attached) manager.mAttachedViews.add(new WeakReference<>(textView));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanInvalidMentionsFromAllViews() {
        try {
            for (WeakReference<TextView> ref : mAttachedViews) {
                TextView textView = ref.get();
                if (textView != null && textView.getText() instanceof Spannable) {
                    Spannable spannable = (Spannable) textView.getText();
                    AreMentionSpan[] spans = spannable.getSpans(0, spannable.length(), AreMentionSpan.class);

                    for (AreMentionSpan span : spans) {
                        if (!mItemRegistry.containsKey(span.getUserKey())) {
                            spannable.removeSpan(span);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MentionItem getOriginalItem(String key) {
        return key != null ? this.mItemRegistry.get(key) : null;
    }

    public void triggerOnItemSelected(MentionItem item) {
        if (mSelectedListener != null) mSelectedListener.onSelected(item);
    }

    public boolean performClick(Context context, AreMentionSpan span) {
        try {
            if (mClickListener != null && span != null) {
                MentionItem original = this.mItemRegistry.get(span.getUserKey());
                if (original != null) {
                    mClickListener.onClick(context, original);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isEnabled() { return mIsEnabled; }
    public List<MentionItem> getMentionItems() { return mMentionItems; }
    public String getEmptyMessage() { return mEmptyMessage; }
    public int getPlaceholderId() { return mPlaceholderId; }
    public int getErrorId() { return mErrorId; }
}