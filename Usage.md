# ARE (Android Rich Editor) Modern Edition — Integration & Usage Guide 🛠️

Welcome to the comprehensive developer guide for **ARE (Android Rich Editor) Modern Edition**. This guide covers step-by-step instructions for integrating the rich editor and preview views into your Android applications, utilizing XML layouts, configuring global strategies, and unlocking advanced custom capabilities.

---

## 📋 Table of Contents
1. [Prerequisites & Core Requirements](#-prerequisites--core-requirements)
2. [Step 1: Project Integration](#-step-1-project-integration)
3. [Step 2: XML Layout Integration](#-step-2-xml-layout-integration)
   - [The Editor (`AREditor`)](#the-editor-areditor)
   - [The Renderer (`AreTextView`)](#the-renderer-aretextview)
4. [Step 3: Editor Strategy & Lifecycle Initialization](#-step-3-editor-strategy--lifecycle-initialization)
5. [Step 4: The Mention System Deep-Dive (@ Mentions)](#-step-4-the-mention-system-deep-dive--mentions)
   - [Understanding `MentionItem`](#understanding-mentionitem)
   - [Sorting Logic & `SortType`](#sorting-logic--sorttype)
   - [Configuring `AreMentionManager` Globally](#configuring-arementionmanager-globally)
   - [Rendering and Attaching Preview click handlers](#rendering-and-attaching-preview-click-handlers)
6. [Step 5: Media Strategies & Cloud Uploads (Advanced)](#-step-5-media-strategies--cloud-uploads-advanced)
7. [Core API Methods Reference](#-core-api-methods-reference)

---

## 📌 Prerequisites & Core Requirements
To support rich media operations, image loading, and video thumbnails safely, the library relies on standard Android libraries:
* **Minimum SDK:** Android 21 (Lollipop) or higher.
* **Target & Compile SDK:** Android 36 (or matching your main module).
* **Core Dependencies:** Ensure your project has **Glide** and **TagSoup** available.

---

## 📦 Step 1: Project Integration

Since the library is included as a local unified module, follow these steps to add it to your project:

1. Copy the `are` module folder into your project's root directory.
2. In your root `settings.gradle.kts` (or `settings.gradle`), include the module:
   ```kotlin
   include(":app", ":are")
   ```
3. In your app's `app/build.gradle.kts` (or `build.gradle`), add the local module dependency along with media loading libraries:
   ```kotlin
   dependencies {
       // Local ARE Module
       implementation(project(":are"))

       // Required for Rich Media parsing and loading
       implementation("org.ccil.cowan.tagsoup:tagsoup:1.2.1")
       implementation("com.github.bumptech.glide:glide:5.0.5")
       annotationProcessor("com.github.bumptech.glide:compiler:5.0.5")
   }
   ```
4. Sync your project with Gradle.

---

## 🎨 Step 2: XML Layout Integration

The library provides custom views that can be dropped directly into XML layouts. Both views support standard styling attributes like padding, margins, gravity, text sizes, and background drawables.

### The Editor (`AREditor`)
`AREditor` is a container that holds the editing workspace and the sliding Material 3 formatting toolbar. 

```xml
<com.muawiya.are.AREditor
    android:id="@+id/areditor"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:hint="Write something amazing..."
    android:textSize="18sp"
    android:gravity="top|start"
    are:hideToolbar="false"
    are:toolbarAlignment="BOTTOM"
    are:toolbarBackgroundColor="?attr/colorSurfaceContainer" />
```

#### Custom Editor XML Attributes:
| Attribute | Format | Default | Description |
| :--- | :--- | :--- | :--- |
| `are:hideToolbar` | `boolean` | `false` | Sets whether the formatting style toolbar is visible or hidden. |
| `are:toolbarAlignment` | `enum` | `BOTTOM` | Position of the formatting toolbar. Can be `TOP` (1) or `BOTTOM` (0). |
| `are:toolbarBackgroundColor` | `color\|reference`| `?attr/colorSurface` | Sets a custom background color or drawable for the style toolbar. |

---

### The Renderer (`AreTextView`)
`AreTextView` is used to parse, render, and display the generated HTML content perfectly with native span execution, rounded corner media rendering, and touch responsiveness.

```xml
<com.muawiya.are.render.AreTextView
    android:id="@+id/areTextView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textSize="16sp"
    android:lineSpacingMultiplier="1.2"
    are:zoomEnabled="true"
    are:minZoom="1.0"
    are:maxZoom="3.0" />
```

#### Custom TextView XML Attributes:
| Attribute | Format | Default | Description |
| :--- | :--- | :--- | :--- |
| `are:zoomEnabled` | `boolean` | `false` | Enables double-tap or pinch zooming natively on the rendered text layout. |
| `are:minZoom` | `float` | `1.0` | Minimum zoom multiplier. |
| `are:maxZoom` | `float` | `3.0` | Maximum zoom multiplier. |

---

## ⚡ Step 3: Editor Strategy & Lifecycle Initialization

In your editing Activity or Fragment, you must bind the editor components and configure strategies to govern how media insertions (images/videos) and custom features are processed.

```java
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.muawiya.are.AREditor;
import com.muawiya.are.strategies.ImageStrategy;
import com.muawiya.are.strategies.MentionStrategy;
import com.muawiya.are.strategies.VideoStrategy;

public class EditorActivity extends AppCompatActivity {

    private AREditor arEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        arEditor = findViewById(R.id.areditor);
        
        if (arEditor != null) {
            // Bind Core Media and Interactive Strategies
            arEditor.setImageStrategy(new ImageStrategy());
            arEditor.setVideoStrategy(new VideoStrategy());
            arEditor.setMentionStrategy(new MentionStrategy());
        }
    }

    // 🔴 CRITICAL REQUIREMENT: Required for Image and Video picker dialogs to insert media!
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && arEditor != null) {
            arEditor.onActivityResult(requestCode, resultCode, data);
        }
    }
}
```

---

## 🏷️ Step 4: The Mention System Deep-Dive (@ Mentions)

The modernized Mention System provides an interactive, in-editor popup list when typing `@`. It is managed globally via `AreMentionManager` and structured through `MentionItem`.

### Understanding `MentionItem`
Each entity suggested in the mention list is represented by a `MentionItem` model.
```java
public class MentionItem implements Serializable {
    public String mKey;        // Unique identifier (e.g., User ID: "user_102")
    public String mName;       // Display name visible in text (e.g., "Ameer Muawiya")
    public int mColor;         // Text color for the mention Span (e.g., Color.BLUE)
    public String mAvatarUrl;  // Avatar URL rendered in popup rows
    public Object mPayload;    // Optional custom metadata object
    public long mDateAdded;    // Millisecond timestamp representing addition time
}
```
* **Date Added Initialization:** By default, calling any standard constructor initializes `mDateAdded = System.currentTimeMillis()`. Developers can manually override this field directly on the instance (e.g., `item.mDateAdded = customTimestampLong`) to govern historical or database record ordering.

### Sorting Logic & `SortType`
When inserting lists of contacts, you can order them dynamically via `AreMentionManager.getInstance().sortMentions(SortType)`. Sorting is performed under the hood using a case-insensitive name comparator or primitive timestamp comparators:

```java
public enum SortType {
    NAME_ASCENDING,    // Alphabetical sort (A to Z) case-insensitive on item name (o1.mName vs o2.mName)
    NAME_DESCENDING,   // Reverse alphabetical sort (Z to A) case-insensitive on item name (o2.mName vs o1.mName)
    DATE_ASCENDING,    // Chronological sort (oldest added first) comparing long timestamps (o1.mDateAdded vs o2.mDateAdded)
    DATE_DESCENDING    // Reverse chronological sort (newest added first) comparing long timestamps (o2.mDateAdded vs o1.mDateAdded)
}
```

---

### Configuring `AreMentionManager` Globally
Initialize and configure your dataset globally in your application or when setting up the editor activity:

```java
import android.graphics.Color;
import com.muawiya.are.mentions.AreMentionManager;
import com.muawiya.are.mentions.MentionItem;
import java.util.ArrayList;
import java.util.List;

private void configureMentionSystem() {
    // 1. Prepare your data list
    List<MentionItem> contacts = new ArrayList<>();
    
    // Default constructor (sets color to Blue, avatar to null, date to current time)
    contacts.add(new MentionItem("user_101", "Ameer Muawiya"));
    
    // Explicit color constructor
    contacts.add(new MentionItem("user_102", "Abu Bakr Siddique", Color.RED));
    
    // Fully specified avatar constructor
    MentionItem user3 = new MentionItem("user_103", "Umar Farooq", Color.BLUE, "https://link-to-avatar.jpg");
    // Manually setting historical date order if desired
    user3.mDateAdded = 1600000000000L; 
    contacts.add(user3);

    // 2. Configure and bind using chainable Manager API
    AreMentionManager.getInstance()
            .setMentionsEnabled(true) // Turns mention popup parsing on/off
            .setPlaceholders(R.drawable.ic_account_placeholder, R.drawable.ic_error_placeholder) // Avatar fallbacks
            .setEmptyMessage("No contacts match your query") // Displayed when contact filters yield zero results
            .setMentionData(contacts) // Register contact list
            .sortMentions(AreMentionManager.SortType.NAME_ASCENDING) // Apply dynamic sorting
            .setOnMentionSelectedListener(item -> {
                // Fired immediately when the user taps an option in the editor popup
                Toast.makeText(this, "Inserted mention for: " + item.mName, Toast.LENGTH_SHORT).show();
            });
}
```

---

### Rendering and Attaching Preview Click Handlers
To display the HTML output of mentions (rendered as stylized `<a href="#" uKey="..." uName="...">@Name</a>` tags) and receive touch interactions on those items, you must link your preview view:

```java
import com.muawiya.are.render.AreTextView;
import com.muawiya.are.mentions.AreMentionManager;

public class PreviewActivity extends AppCompatActivity {

    private AreTextView areTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        areTextView = findViewById(R.id.areTextView);

        // 1. ATTACH THE VIEW: Native call to let the manager parse mention spans
        AreMentionManager.attachView(areTextView);

        // 2. REGISTER CLICKS: Handles when users tap @mentions inside the text
        AreMentionManager.getInstance().setOnMentionClickListener((context, item) -> {
            // Implement profile navigation, modal specs dialog, or simple greeting
            Toast.makeText(context, "Navigating to " + item.mName + "'s profile (Key: " + item.mKey + ")", Toast.LENGTH_LONG).show();
        });

        // 3. Render raw HTML containing mention anchor tags
        String html = "<html><body>Project built by <a href=\"#\" uKey=\"user_101\" uName=\"Ameer Muawiya\" style=\"color:#0000FF;\">@Ameer Muawiya</a>!</body></html>";
        areTextView.fromHtml(html);
    }

    // 🔴 CRITICAL SAFETY STEP: Clear click listener in onDestroy() to prevent Context leaks!
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AreMentionManager.getInstance().setOnMentionClickListener(null);
    }
}
```

---

## ☁️ Step 5: Media Strategies & Cloud Uploads (Advanced)

By default, ARE handles file loading locally. However, you can write custom implementations of `ImageStrategy` and `VideoStrategy` to intercept file picking, upload raw files to cloud servers (such as Firebase Storage, AWS S3, or your API servers), and insert the final public URL into the document.

### Custom Video Strategy Example:
```java
import android.content.Context;
import android.net.Uri;
import com.muawiya.are.strategies.VideoStrategy;

public class CustomCloudVideoStrategy extends VideoStrategy {

    @Override
    public String uploadVideo(Context context, Uri fileUri, UploadProgressListener listener) {
        // 1. Process local fileUri (e.g. read stream, prepare MultipartBody)
        // 2. Loop upload chunks and trigger listener.onProgressUpdate(percent)
        // 3. Complete and return the public web URL
        String cloudUrl = "https://yourcloud.com/uploads/video_" + System.currentTimeMillis() + ".mp4";
        return cloudUrl;
    }

    @Override
    public void cancelUpload() {
        // Triggered if the user clicks 'Cancel' on the upload progress dialog
        // Cancel your OkHttp call or upload thread here
    }
}
```
Once implemented, simply inject it into the editor during onCreate:
```java
arEditor.setVideoStrategy(new CustomCloudVideoStrategy());
```

---

## 📖 Core API Methods Reference

### `AREditor` API Methods:
* `fromHtml(String html)`: Parses clean HTML strings, loading all style formats, interactive mentions, links, and media frames into the live workspace.
* `getHtml()`: Extracts content and styles from the live workspace, exporting them as fully compliant, standard HTML syntax.
* `undo()`: Reverts the last textual change or media insertion.
* `redo()`: Reapplies the previously undone action.
* `getCanUndo()`: Query whether there are actions left in the undo history stack.
* `getCanRedo()`: Query whether there are actions left in the redo history stack.

### `AreTextView` API Methods:
* `fromHtml(String html)`: Compiles the supplied HTML string, builds appropriate custom Spans (such as custom interactive video thumbnails or mention markers), and renders them in the TextView.

---
*Developed with ❤️ for developers building rich interactive Android experiences.*
