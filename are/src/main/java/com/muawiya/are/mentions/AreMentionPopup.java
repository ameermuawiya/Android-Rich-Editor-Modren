package com.muawiya.are.mentions;

import android.content.Context;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.muawiya.are.AREditText;
import com.muawiya.are.R;
import com.muawiya.are.Util;
import com.muawiya.are.strategies.MentionStrategy;
import com.muawiya.are.styles.ARE_Mention;
import com.google.android.material.listitem.ListItemLayout;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/*
 * Handles popup window rendering and lists mention data.
 * Contains internal list adapter to minimize class clutter.
 */
public class AreMentionPopup {

    private Context mContext;
    private PopupWindow mPopupWindow;
    private RecyclerView mRecyclerView;
    private TextView mEmptyText;
    private CircularProgressIndicator mLoadingProgress;
    private View mContentLayout;
    private InternalAdapter mAdapter;
    private ARE_Mention mAreMentionStyle;
    private AREditText mEditText;

    public AreMentionPopup(Context context, ARE_Mention style, AREditText editText) {
        this.mContext = context;
        this.mAreMentionStyle = style;
        this.mEditText = editText;
        initPopup();
    }

    private void initPopup() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.are_mention_popup_view, null);
        mRecyclerView = view.findViewById(R.id.are_popup_list);
        mEmptyText = view.findViewById(R.id.are_popup_empty);
        mLoadingProgress = view.findViewById(R.id.are_popup_loading);
        mContentLayout = view.findViewById(R.id.are_popup_content_layout);

        mPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(false);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setElevation(10f);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new InternalAdapter(mContext, new ArrayList<>(), mEditText.getMentionStrategy());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(item -> mAreMentionStyle.insertSelectedMentionItem(item));
    }

    public void filterAndShow(String query) {
        MentionStrategy strategy = mEditText.getMentionStrategy();
        if (strategy == null) return;

        List<MentionItem> masterList = strategy.getMentionItems();
        if (masterList == null) {
            mLoadingProgress.setVisibility(View.VISIBLE);
            mContentLayout.setVisibility(View.GONE);
            updatePopupSizeAndShow();
            return;
        }

        mLoadingProgress.setVisibility(View.GONE);
        mContentLayout.setVisibility(View.VISIBLE);

        List<MentionItem> filteredList = new ArrayList<>();
        String cleanQuery = query.replace(" ", "").toLowerCase();

        for (MentionItem item : masterList) {
            String cleanName = item.mName.replace(" ", "").toLowerCase();
            if (cleanName.contains(cleanQuery)) {
                filteredList.add(item);
            }
        }

        mAdapter.setData(filteredList);

        if (filteredList.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyText.setVisibility(View.VISIBLE);
            mEmptyText.setText(strategy.getEmptyMessage() != null ? strategy.getEmptyMessage() : "No match found");
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyText.setVisibility(View.GONE);
        }
        updatePopupSizeAndShow();
    }

    private void updatePopupSizeAndShow() {
        if (mAdapter.getItemCount() > 3) {
            mPopupWindow.setHeight(Util.getPixelByDp(mContext, 240));
        } else {
            mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        Layout layout = mEditText.getLayout();
        if (layout != null) {
            int pos = mEditText.getSelectionStart();
            int line = layout.getLineForOffset(pos);
            int x = (int) layout.getPrimaryHorizontal(pos) + mEditText.getPaddingLeft();
            int y = layout.getLineBottom(line) + mEditText.getPaddingTop();

            int[] screenPos = new int[2];
            mEditText.getLocationInWindow(screenPos);
            int screenX = screenPos[0] + x - mEditText.getScrollX();
            int screenY = screenPos[1] + y - mEditText.getScrollY();

            if (mPopupWindow.isShowing()) {
                mPopupWindow.update(screenX, screenY, -1, -1);
            } else {
                mPopupWindow.showAtLocation(mEditText, Gravity.NO_GRAVITY, screenX, screenY);
            }
        }
    }

    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    /*
     * Internal adapter nested to reduce package clutter.
     */
    private static class InternalAdapter extends RecyclerView.Adapter<InternalAdapter.ViewHolder> {
        private Context context;
        private List<MentionItem> itemsList;
        private MentionStrategy strategy;
        private OnItemClickListener listener;

        interface OnItemClickListener { void onItemClick(MentionItem item); }

        InternalAdapter(Context context, List<MentionItem> itemsList, MentionStrategy strategy) {
            this.context = context;
            this.itemsList = itemsList;
            this.strategy = strategy;
        }

        void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }
        void setData(List<MentionItem> itemsList) {
            this.itemsList = itemsList != null ? itemsList : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.are_mention_item_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MentionItem item = itemsList.get(position);
            holder.textView.setText(item.mName);

            if (item.mAvatarUrl != null && !item.mAvatarUrl.trim().isEmpty()) {
                holder.imageView.setVisibility(View.VISIBLE);
                com.bumptech.glide.RequestBuilder<android.graphics.drawable.Drawable> rb = Glide.with(context).load(item.mAvatarUrl);
                if (strategy.getPlaceholderId() != 0) rb = rb.placeholder(strategy.getPlaceholderId());
                if (strategy.getErrorId() != 0) rb = rb.error(strategy.getErrorId());
                rb.circleCrop().into(holder.imageView);
            } else {
                holder.imageView.setVisibility(View.INVISIBLE);
            }

            View clickTarget = (holder.itemView instanceof ViewGroup && ((ViewGroup) holder.itemView).getChildCount() > 0)
                    ? ((ViewGroup) holder.itemView).getChildAt(0) : holder.itemView;
            clickTarget.setOnClickListener(v -> { if (listener != null) listener.onItemClick(item); });

            if (holder.itemView instanceof ListItemLayout) {
                ((ListItemLayout) holder.itemView).updateAppearance(position, getItemCount());
            }
        }

        @Override public int getItemCount() { return itemsList.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView; TextView textView;
            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.are_view_at_item_image);
                textView = itemView.findViewById(R.id.are_view_at_item_name);
            }
        }
    }
}