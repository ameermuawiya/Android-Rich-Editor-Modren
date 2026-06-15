# How to use?

## 1. Include AREditor into your project
Currently, the library is included as local modules. To add it to your project:
1. Clone or download the repository from [Github ARE](https://github.com/chinalwb/Android-Rich-text-Editor), then unzip it.
2. Copy the module folder `are` into your project's root directory.
3. Open your project's `settings.gradle` file and include the modules. For example:
   ```gradle
   include ':app', ':are'

 * In your app's build.gradle, add the dependencies:
   implementation project(':are')

 * Sync your project with Gradle files.
2. XML Layout Integration
The Editor (AREditor)
Add the AREditor to your layout XML where you want the user to input rich text.
Note: The AREditor natively supports all standard Android EditText attributes (such as padding, background, gravity, textSize, textColor, hint, etc.). You can customize it exactly as you would a normal EditText. It also supports custom attributes for toolbar alignment and expand mode.
<com.chinalwb.are.AREditor
    android:id="@+id/areditor"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:hint="Write something amazing..."
    android:textSize="18sp"
    android:textColor="?attr/colorOnSurface"
    android:gravity="top|start"
    are:expandMode="FULL"
    are:hideToolbar="false"
    are:toolbarAlignment="BOTTOM" />

The Renderer (AreTextView)
To display the generated HTML, use AreTextView. Just like the editor, it fully supports all standard Android TextView attributes.
<com.chinalwb.are.render.AreTextView
    android:id="@+id/areTextView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textSize="16sp" />

3. Initialization & Setup (Java)
Setting up the Editor Strategies
The editor uses a strategy pattern to handle different rich media insertions. You must initialize these strategies in your Activity/Fragment. Don't forget to pass onActivityResult to the editor so it can handle media selections!
AREditor arEditor = findViewById(R.id.areditor);

// Apply strategies natively
arEditor.setImageStrategy(new ImageStrategy());
arEditor.setVideoStrategy(new VideoStrategy());
arEditor.setMentionStrategy(new MentionStrategy());

// Example: Loading initial HTML
arEditor.fromHtml("<html><body><p>Hello World!</p></body></html>");

// Pass the result down to the editor for image/video pickers to work
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && data != null && arEditor != null) {
        arEditor.onActivityResult(requestCode, resultCode, data);
    }
}

The Mention System
The library includes a powerful, built-in Mention system. Typing @ directly inside the editor triggers a beautiful, interactive popup showing your provided contact list.
Configure the system globally using AreMentionManager:
// 1. Prepare your mention data
List<MentionItem> items = new ArrayList<>();
items.add(new MentionItem("user_1", "Ameer Muawiya", Color.BLUE, "[https://link-to-avatar.jpg](https://link-to-avatar.jpg)"));
items.add(new MentionItem("user_2", "Abu Bakr Siddique", Color.BLUE, "[https://link-to-avatar.jpg](https://link-to-avatar.jpg)"));

// 2. Configure the manager using API
AreMentionManager.getInstance()
        .setMentionsEnabled(true)
        .setPlaceholders(R.drawable.ic_account_placeholder, R.drawable.ic_error_placeholder)
        .setEmptyMessage("No Contact Result Found")
        .setMentionData(items)
        .sortMentions(AreMentionManager.SortType.NAME_ASCENDING)
        .setOnMentionSelectedListener(item -> {
            Toast.makeText(this, "Inserted: " + item.mName, Toast.LENGTH_SHORT).show();
        });

Setting up the Preview (AreTextView)
When rendering the HTML in an AreTextView, you must attach the view to the AreMentionManager to automatically manage the mention span lifecycles. You can also set a global click listener for when a user taps on a mention inside the text.
AreTextView areTextView = findViewById(R.id.areTextView);

// Attach the view to manage mention lifecycles natively
AreMentionManager.attachView(areTextView);

// Set up a click listener for interacting with mentions in the preview
AreMentionManager.getInstance().setOnMentionClickListener((context, item) -> {
    // Handle the click event (e.g., show a profile dialog or navigate to a user screen)
    Toast.makeText(context, "Clicked on: " + item.mName, Toast.LENGTH_SHORT).show();
});

// Render the HTML
areTextView.fromHtml(yourHtmlString);

Note: Remember to clear the listener in your Activity's onDestroy() to prevent memory leaks (AreMentionManager.getInstance().setOnMentionClickListener(null);).
4. Core API Methods
 * arEditor.fromHtml(String html): Loads existing HTML content into the editor and renders it as rich text.
 * arEditor.getHtml(): Extracts the current editor content and returns it as a cleanly formatted HTML string.
 * areTextView.fromHtml(String html): Parses and displays HTML content with full support for custom spans (like Mentions, Videos, and Images) inside the renderer.