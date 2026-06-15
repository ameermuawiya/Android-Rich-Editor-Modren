package com.chinalwb.are.demo;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.chinalwb.are.mentions.AreMentionManager;
import com.chinalwb.are.render.AreTextView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PreviewActivity extends AppCompatActivity {

    public static final String HTML_TEXT = "html";
    private AreTextView areTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        setupToolbar();
        setupMentionClickListener();
        renderHtml();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /*
     * Sets up a global independent click listener for mentions.
     */
    private void setupMentionClickListener() {
        AreMentionManager.getInstance().setOnMentionClickListener((context, item) -> {
            try {
                ImageView imageView = new ImageView(context);
                int sizeInPx = (int) (80 * context.getResources().getDisplayMetrics().density);
                android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(sizeInPx, sizeInPx);
                params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
                params.topMargin = 40;
                params.bottomMargin = 20;
                imageView.setLayoutParams(params);

                int placeholderId = AreMentionManager.getInstance().getPlaceholderId();
                int errorId = AreMentionManager.getInstance().getErrorId();

                if (item.mAvatarUrl != null && !item.mAvatarUrl.trim().isEmpty()) {
                    Glide.with(context)
                            .load(item.mAvatarUrl)
                            .placeholder(placeholderId != 0 ? placeholderId : android.R.color.transparent)
                            .error(errorId != 0 ? errorId : android.R.color.transparent)
                            .circleCrop()
                            .into(imageView);
                } else if (placeholderId != 0) {
                    imageView.setImageResource(placeholderId);
                }

                String message = "User Key: " + item.mKey;
                new MaterialAlertDialogBuilder(context)
                        .setTitle(item.mName)
                        .setMessage(message)
                        .setView(imageView)
                        .setPositiveButton("Close", null)
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void renderHtml() {
        areTextView = findViewById(R.id.areTextView);

        // Native static call to auto-manage mention span lifecycles
        AreMentionManager.attachView(areTextView);

        String html = getIntent().getStringExtra(HTML_TEXT);
        if (html != null && !html.isEmpty()) {
            areTextView.fromHtml(html);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AreMentionManager.getInstance().setOnMentionClickListener(null);
    }
}