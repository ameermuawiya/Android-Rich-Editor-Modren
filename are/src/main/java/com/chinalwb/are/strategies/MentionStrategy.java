package com.chinalwb.are.strategies;

import com.chinalwb.are.mentions.AreMentionManager;
import com.chinalwb.are.mentions.MentionItem;
import java.util.List;

/*
 * Strategy consumed by the editor natively.
 * Delegates internal checks to the global manager.
 */
public class MentionStrategy {

    public boolean isEnabled() {
        return AreMentionManager.getInstance().isEnabled();
    }

    public List<MentionItem> getMentionItems() {
        return AreMentionManager.getInstance().getMentionItems();
    }

    public String getEmptyMessage() {
        return AreMentionManager.getInstance().getEmptyMessage();
    }

    public int getPlaceholderId() {
        return AreMentionManager.getInstance().getPlaceholderId();
    }

    public int getErrorId() {
        return AreMentionManager.getInstance().getErrorId();
    }

    public void onItemSelected(MentionItem item) {
        AreMentionManager.getInstance().triggerOnItemSelected(item);
    }
}