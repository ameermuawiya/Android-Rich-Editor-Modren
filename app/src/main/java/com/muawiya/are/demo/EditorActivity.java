package com.muawiya.are.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.muawiya.are.AREditor;
import com.muawiya.are.mentions.AreMentionManager;
import com.muawiya.are.mentions.MentionItem;
import com.muawiya.are.strategies.ImageStrategy;
import com.muawiya.are.strategies.MentionStrategy;
import com.muawiya.are.strategies.VideoStrategy;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends AppCompatActivity {

    private AREditor arEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        setupToolbar();
        setupEditor();
        setupMentionSystem();
        loadDemoContent();
    }

    /*
    Sets up the top app bar for the activity seamlessly.
    */
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /*
    Initializes core editor strategies natively without any extra boilerplate.
    */
    private void setupEditor() {
        arEditor = findViewById(R.id.areditor);
        
        if (arEditor != null) {
            arEditor.setImageStrategy(new ImageStrategy());
            arEditor.setVideoStrategy(new VideoStrategy());
            arEditor.setMentionStrategy(new MentionStrategy());
            
            arEditor.getARE().addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    invalidateOptionsMenu();
                }
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    /*
    Prepares and configures the complete mention system globally.
    */
    private void setupMentionSystem() {
        List<MentionItem> items = new ArrayList<>();

        items.add(new MentionItem("test_1", "Ameer Muawiya", Color.BLUE, "https://i.ibb.co/0SKJbmZ/upload-1771774175542.jpg"));
        items.add(new MentionItem("test_2", "Abu Bakr Siddique", Color.BLUE, "https://i.ibb.co/XrFFNPmB/upload-1771774241424.jpg"));
        items.add(new MentionItem("test_3", "Umar Farooq", Color.BLUE, "https://i.ibb.co/7xQWt2Xc/upload-1771774218429.jpg"));
        items.add(new MentionItem("test_4", "Usman Ghani", Color.BLUE, "https://i.ibb.co/svrg1Rkr/upload-1771774201176.jpg"));
        items.add(new MentionItem("test_5", "Ali Parwaz", Color.BLUE, "https://i.ibb.co/8DKPzKT2/upload-1771774190832.jpg"));

        AreMentionManager.getInstance()
                .setMentionsEnabled(true)
                .setPlaceholders(R.drawable.ic_account_placeholder, R.drawable.ic_error_placeholder)
                .setEmptyMessage("No Contact Result Found")
                .setMentionData(items)
                .sortMentions(AreMentionManager.SortType.NAME_ASCENDING)
                .setOnMentionSelectedListener(item -> Toast.makeText(EditorActivity.this, "Inserted: " + item.mName, Toast.LENGTH_SHORT).show());
    }

    /*
    Loads the initial demonstration HTML content into the editor gracefully.
    */
    private void loadDemoContent() {
        String demoHtml =
                "<html><body>"
                        + "<p style=\"text-align:start;\"><span style=\"font-size:28px;\"><b>The Message of Islam</b></span></p>"
                        + "<br>"
                        + "<p style=\"text-align:start;\">Islam invites humanity to <span style=\"color:#4CAF50;\"><b>truth</b></span>, <span style=\"color:#FF5722;\"><b>justice</b></span>, and <span style=\"color:#2196F3;\"><b>peace</b></span>.</p>"
                        + "<p style=\"text-align:start;\">It calls the heart to know <span style=\"background-color:#FFFF00;\"><b>One God</b></span>, to live with purpose, and to treat every human with dignity.</p>"
                        + "<blockquote><p><i>\"Indeed, in the remembrance of Allah do hearts find rest.\"</i></p><br></blockquote>"
                        + "<br>"
                        + "<p style=\"text-align:center;\"><video src=\"https://archive.org/download/55ArRahmanTheBeneficent_201711/103Al-asrtheDecliningDay.mp4\" uri=\"\" controls=\"controls\"></video></p>"
                        + "<p style=\"text-align:start;\">​</p>"
                        + "<hr />"
                        + "<p style=\"text-align:start;\"><b>Project Developer Note:</b> This rich text editor has been extensively upgraded with modern, robust features by lead developer <a href=\"#\" uKey=\"test_1\" uName=\"Ameer Muawiya\" style=\"color:#0000FF;\">@Ameer Muawiya</a>. Testing mentions with brothers: <a href=\"#\" uKey=\"test_2\" uName=\"Abu Bakr Siddique\" style=\"color:#0000FF;\">@Abu Bakr Siddique</a> <a href=\"#\" uKey=\"test_3\" uName=\"Umar Farooq\" style=\"color:#0000FF;\">@Umar Farooq</a> <a href=\"#\" uKey=\"test_4\" uName=\"Usman Ghani\" style=\"color:#0000FF;\">@Usman Ghani</a> <a href=\"#\" uKey=\"test_5\" uName=\"Ali Parwaz\" style=\"color:#0000FF;\">@Ali Parwaz</a></p>"
                        + "<br>"
                        + "<p style=\"text-align:start;\"><span style=\"font-size:22px;\"><b>Core Beliefs</b></span></p>"
                        + "<br>"
                        + "<ul>"
                        + "<li>​<b>Tawheed</b>: <span style=\"color:#795548;\">Belief in One God</span></li>"
                        + "<li>​<b>Prophethood</b>: Guidance through prophets</li>"
                        + "<li>​<b>Revelation</b>: <span style=\"background-color:#FFFF00;\">The Qur'an as the final message</span></li>"
                        + "<li>​<b>Hereafter</b>: Accountability and eternal life</li>"
                        + "</ul>"
                        + "<p>​</p>"
                        + "<hr />"
                        + "<p style=\"text-align:start;\"><span style=\"font-size:22px;\"><b>Why Islam</b></span></p>"
                        + "<p style=\"text-align:start;\">Islam is not just a religion. It is a <span style=\"background-color:#FFFF00;\"><i>complete way of life</i></span>.</p>"
                        + "<br>"
                        + "<ol>"
                        + "<li>​It connects the <b>soul</b> to its Creator</li>"
                        + "<li>​It balances <b>faith</b> and <b>reason</b></li>"
                        + "<li>​It builds <span style=\"color:#E91E63;\"><b>strong character</b></span></li>"
                        + "<li>​It establishes social justice</li>"
                        + "</ol>"
                        + "<hr />"
                        + "<p style=\"text-align:start;\"><span style=\"font-size:22px;\"><b>A Beautiful Reminder</b></span></p>"
                        + "<p style=\"text-align:start;\"><u>Islam teaches mercy to all creation</u></p>"
                        + "<br>"
                        + "<blockquote>"
                        + "<p style=\"text-align:start;\">Islam commands kindness to parents, neighbors, the poor,</p>"
                        + "<p style=\"text-align:start;\">and even to those who <span style=\"color:#F44336;\">disagree</span> with us.</p>"
                        + "</blockquote>"
                        + "<hr />"
                        + "<p style=\"text-align:start;\"><span style=\"font-size:22px;\"><b>Knowledge and Guidance</b></span></p>"
                        + "<p style=\"text-align:start;\">Read the <a href=\"https://quran.com\"><b>Holy Qur'an</b></a> online</p>"
                        + "<p style=\"text-align:start;\">Learn about Islam: <a href=\"https://www.islamreligion.com\">https://www.islamreligion.com</a></p>"
                        + "<hr />"
                        + "<p style=\"text-align:start;\"><span style=\"font-size:22px;\"><b>Text Styles Demo</b></span></p>"
                        + "<p style=\"text-align:start;\">Islam is <del>misunderstood</del> <b>clear</b> and <i>rational</i>.</p>"
                        + "<br>"
                        + "<p style=\"text-align:start;\"><span style=\"font-size:20px;\"><b>Declaration</b></span></p>"
                        + "<p>There is no god but Allah,</p>"
                        + "<p>and Muhammad is the Messenger of Allah.</p>"
                        + "<br>"
                        + "<p style=\"text-align:start;\"><span style=\"font-size:20px;\"><b>Personal Growth</b></span></p>"
                        + "<br>"
                        + "<ul>"
                        + "<li>​Believe sincerely</li>"
                        + "<li>​Pray regularly</li>"
                        + "<li>​Learn continuously</li>"
                        + "<li>​Improve character</li>"
                        + "</ul>"
                        + "<p style=\"text-align:center;\"><img src=\"https://mjc.org.za/wp-content/uploads/2018/11/pillars-of-islam-1024x576.jpg\" /></p>"
                        + "<br>"
                        + "<p style=\"text-align:start;\">The word <span style=\"background-color:#FFFF00;\">Islam</span> comes from <span style=\"color:#4CAF50;\">peace</span> and <span style=\"color:#2196F3;\">submission</span>.</p>"
                        + "<hr />"
                        + "<p style=\"text-align:start;\"><span style=\"font-size:22px;\"><b>Final Reflection</b></span></p>"
                        + "<p style=\"text-align:start;\">Islam does not force belief.</p>"
                        + "<p style=\"text-align:start;\">It <b>invites</b>, <b>explains</b>, and <b>waits</b>.</p>"
                        + "<p style=\"text-align:start;\"><i>The choice is yours.</i></p>"
                        + "</body></html>";

        if (arEditor != null) {
            arEditor.fromHtml(demoHtml);
        }
    }

    /*
    Inflates the editor menu to show the toolbar actions natively.
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    /*
    Automatically refreshes menu icons before display.
    Greys out Undo/Redo icons transparently using only the 4 minimal library methods.
    */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuUndo = menu.findItem(R.id.action_undo);
        MenuItem menuRedo = menu.findItem(R.id.action_redo);

        if (arEditor != null) {
            boolean canUndo = arEditor.getCanUndo();
            boolean canRedo = arEditor.getCanRedo();

            if (menuUndo != null) {
                menuUndo.setEnabled(canUndo);
                if (menuUndo.getIcon() != null) menuUndo.getIcon().setAlpha(canUndo ? 255 : 100);
            }
            if (menuRedo != null) {
                menuRedo.setEnabled(canRedo);
                if (menuRedo.getIcon() != null) menuRedo.getIcon().setAlpha(canRedo ? 255 : 100);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /*
    Handles all menu interactions cleanly via simple public library methods.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_undo) {
            if (arEditor != null && arEditor.getCanUndo()) {
                arEditor.undo();
                invalidateOptionsMenu(); // Instantly update icon colors
            }
            return true;
        } 
        else if (id == R.id.action_redo) {
            if (arEditor != null && arEditor.getCanRedo()) {
                arEditor.redo();
                invalidateOptionsMenu();
            }
            return true;
        } 
        else if (id == R.id.action_show_tv) {
            if (arEditor == null) return true;
            try {
                String html = arEditor.getHtml();
                Intent intent = new Intent(this, PreviewActivity.class);
                intent.putExtra(PreviewActivity.HTML_TEXT, html);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && arEditor != null) {
            arEditor.onActivityResult(requestCode, resultCode, data);
        }
    }
}
