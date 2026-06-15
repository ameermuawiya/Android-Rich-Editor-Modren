# Android Rich Text Editor (Modern Edition) 🚀

[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![AndroidX](https://img.shields.io/badge/AndroidX-Supported-success.svg)](#)
[![Material3](https://img.shields.io/badge/Material%203-Expressive-orange.svg)](#)
[![Contributions Welcome](https://img.shields.io/badge/Contributions-Welcome-brightgreen.svg)](https://github.com/ameermuawiya/Android-Rich-Editor-Modren)

Welcome to the **Modernized Android Rich Text Editor (ARE)**! This repository is a heavily upgraded, modern, and robust version of the original ARE library. Re-engineered and modernized by **Ameer Muawiya**, it now features a stunning Material 3 UI, advanced asynchronous media loading, and highly interactive mention systems.

This library natively leverages Android Spans to generate clean, standard HTML, making it the perfect choice for email clients, note-taking apps, and forum software.

![ARE Demo](https://raw.githubusercontent.com/chinalwb/are/master/ARE/demo/new-demo.gif)

---

## 📝 What's New in Modern Edition (v2.0)

* 🎨 **Material 3 UI:** Complete visual overhaul using Material Design 3 components (Sliders, Filled Tonal Buttons, modern colors).
* 🛠️ **Layout Fixes:** Resolved all overlapping issues; the editor no longer hides behind the Material Toolbar.
* ⚡ **Gradle 9.0:** Upgraded to the latest Android tooling and Gradle 9.0 for lightning-fast builds.
* 🖼️ **Media Upgrades:** Enhanced Image & Video insertion with beautiful rounded corners and server upload support.
* 🎬 **Async Video Thumbnails:** High-performance video frame extraction without freezing the main UI thread.
* 🔄 **Undo & Redo System:** Native history tracking added! *(Note: Still needs more work to perfectly track styling history. **[Contributions are highly welcome!](https://github.com/ameermuawiya/Android-Rich-Editor-Modren)**)*
* ✨ **New Toolbar Actions:** Added 'Toggle Toolbar Position', 'View HTML Source Code', and 'Clear All Formats' buttons.
* 📏 **Modern Font Slider:** Replaced the legacy Seekbar with a sleek, precise Material 3 Slider for font size adjustments.
* 🏷️ **Dynamic Mentions:** Replaced the old mention screen with a beautiful, interactive `@` popup directly inside the editor.
* 🎢 **Smooth Animations:** High-performance, battery-friendly slide transitions for toolbar movements and control panels.
* 🐛 **Stability:** Patched various crashes and improved overall editing behavior.
* 📱 **Screen Enhancements:** Upgraded and optimized the video player and photo preview screens.

---

## ✨ Supported Features

* **Formatting:** Bold, Italic, Underline, Strikethrough, Blockquote
* **Styling:** Text Color (Foreground), Background Color (Highlight), Dynamic Font Size
* **Positioning:** Superscript, Subscript
* **Layout:** Align Left, Center, Right, Bullet List, Numeric List, Dividing Line (HR)
* **Media & Links:** * Insert Hyperlinks
  * Insert Image (from Gallery or URL)
  * Insert Video (from Local URI or URL)
* **Advanced:** * **Interactive Mentions:** Type `@` to open a smart contact search popup.
  * Clear Formatting
  * View Source Code
  * Toggle Toolbar (Top/Bottom)

---

## 📦 Integration

Currently, the library is included as a single, unified local module. To add it to your project:

1. Clone or download the repository (`https://github.com/ameermuawiya/Android-Rich-Editor-Modren`).
2. Copy the `are` module folder into your project's root directory.
3. Open your project's `settings.gradle` file and include the module:
   include ':app', ':are'

4. In your app's `build.gradle`, add the dependency as a local module along with Glide for media handling:
   dependencies {
       implementation 'com.github.bumptech.glide:glide:4.x.x' // Required for media loading
       implementation project(':are')
   }

5. Sync your project with Gradle.

---

## 🛠️ Usage Guide: The Editor (`AREditor`)

### 1. XML Layout (Editor)
You can easily add the editor directly into your XML layout. It natively supports standard Android `EditText` attributes like padding, background, hint, etc.

<com.chinalwb.are.AREditor
    android:id="@+id/areditor"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:hint="Write something amazing..." />

### 2. Java Initialization
To fully utilize the editor, initialize its strategies and handle the activity result for media picking.

import com.chinalwb.are.AREditor;
import com.chinalwb.are.mentions.AreMentionManager;
import com.chinalwb.are.mentions.MentionItem;
import com.chinalwb.are.strategies.ImageStrategy;
import com.chinalwb.are.strategies.MentionStrategy;
import com.chinalwb.are.strategies.VideoStrategy;

public class EditorActivity extends AppCompatActivity {

    private AREditor arEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        arEditor = findViewById(R.id.areditor);
        
        // 1. Setup Editor Core Strategies
        if (arEditor != null) {
            arEditor.setImageStrategy(new ImageStrategy());
            arEditor.setVideoStrategy(new VideoStrategy());
            arEditor.setMentionStrategy(new MentionStrategy());
        }

        // 2. Setup the Mention System Globally
        List<MentionItem> items = new ArrayList<>();
        items.add(new MentionItem("user_1", "Ameer Muawiya", Color.BLUE, "https://link-to-avatar.jpg"));
        items.add(new MentionItem("user_2", "John Doe", Color.BLUE, "https://link-to-avatar.jpg"));

        AreMentionManager.getInstance()
                .setMentionsEnabled(true)
                .setPlaceholders(R.drawable.ic_account_placeholder, R.drawable.ic_error_placeholder)
                .setEmptyMessage("No Contact Result Found")
                .setMentionData(items) // Inject your data here
                .sortMentions(AreMentionManager.SortType.NAME_ASCENDING);

        // 3. Load existing HTML (Optional)
        if (arEditor != null) {
            arEditor.fromHtml("<html><body> Hello </body></html>");
        }
    }

    // IMPORTANT: Required for Image and Video insertion to work!
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && arEditor != null) {
            arEditor.onActivityResult(requestCode, resultCode, data);
        }
    }
}

### 3. Core Editor APIs

| Method | Description |
| :--- | :--- |
| arEditor.fromHtml(String) | Parses existing HTML and renders it into the editor. |
| arEditor.getHtml() | Extracts the current content and returns it as a clean HTML string. |
| arEditor.undo() | Reverts the last text or media insertion action. |
| arEditor.redo() | Reapplies the previously reverted action. |
| arEditor.getCanUndo() | Returns true if an undo operation is available in the history stack. |
| arEditor.getCanRedo() | Returns true if a redo operation is available in the history stack. |

---

## 📖 Usage Guide: The Viewer (`AreTextView`)

To properly display the generated HTML and handle interactive elements like mentions or media clicks natively, you must use the `AreTextView`.

### 1. XML Layout (Viewer)
<com.chinalwb.are.render.AreTextView
    android:id="@+id/areTextView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />

### 2. Java Initialization (Preview Activity)
import com.chinalwb.are.render.AreTextView;
import com.chinalwb.are.mentions.AreMentionManager;

public class PreviewActivity extends AppCompatActivity {

    private AreTextView areTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        areTextView = findViewById(R.id.areTextView);
        
        // 1. Attach the view to manage mention lifecycles natively
        AreMentionManager.attachView(areTextView);
        
        // 2. Setup Interactivity for Mentions (Optional)
        AreMentionManager.getInstance().setOnMentionClickListener((context, item) -> {
            // Handle the click (e.g., Show a dialog or navigate to user profile)
            Toast.makeText(context, "Clicked: " + item.mName, Toast.LENGTH_SHORT).show();
        });

        // 3. Load the HTML string to display
        String html = "<html><body><b>Awesome Content!</b></body></html>";
        if (html != null && !html.isEmpty()) {
            areTextView.fromHtml(html);
        }
    }

    // Clean up listeners to prevent memory leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AreMentionManager.getInstance().setOnMentionClickListener(null);
    }
}

---

## 🤝 Open for Work & Contributions

This modern version was carefully engineered by **Ameer Muawiya**. 
I am open to work! Feel free to contact me at **ameermuawiya604@gmail.com** if you have an opening or require custom feature implementation.

**Calling all Developers:** The Undo/Redo styling history still needs some polishing. I highly encourage developers to fork, contribute, and submit pull requests to make this library even more powerful! 
**👉 [Contribute Here](https://github.com/ameermuawiya/Android-Rich-Editor-Modren)**

---

## 🙏 Special Thanks
A massive and special thanks to **[chinalwb](https://github.com/chinalwb/Android-Rich-text-Editor)**, the original creator of this library. The core architecture and initial brilliant idea provided a solid foundation for this modern upgrade. All original credits go to them for initiating this amazing project.

*If this library saves your time, please consider giving it a ⭐!*
