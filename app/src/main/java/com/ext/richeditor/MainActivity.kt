package com.ext.richeditor

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ext.rich_editor.RichEditor
import com.ext.rich_editor.RichEditorToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var editor: RichEditor
    private lateinit var toolbar: RichEditorToolbar

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editor.insertImage(it.toString())
            editor.focusEditor()
        }
    }

    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editor.insertVideo(it.toString())
            editor.focusEditor()
        }
    }

    private val audioPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editor.insertAudio(it.toString())
            editor.focusEditor()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Rich Text Editor"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupEditor()
    }

    private fun setupEditor() {
        editor = findViewById(R.id.richEditor)
        toolbar = findViewById(R.id.richEditorToolbar)

        // Connect toolbar with editor
        toolbar.setEditor(editor)

        // Setup toolbar action callbacks
        toolbar.onInsertImageClick = {
            showImageInsertDialog()
        }

        toolbar.onInsertVideoClick = {
            showVideoInsertDialog()
        }

        toolbar.onInsertAudioClick = {
            showAudioInsertDialog()
        }

        toolbar.onInsertYoutubeClick = {
            showYoutubeDialog()
        }

        toolbar.onInsertLinkClick = {
            showLinkDialog()
        }

        toolbar.onTextColorClick = {
            showColorPickerDialog(true)
        }

        toolbar.onBgColorClick = {
            showColorPickerDialog(false)
        }

        toolbar.onFontSizeClick = {
            showFontSizeDialog()
        }

        // Listen to text changes
        editor.setOnTextChangeListener(object : RichEditor.OnTextChangeListener {
            override fun onTextChange(html: String) {
                // Handle text changes
                // You can save to database, update character count, etc.
            }
        })

        // Set initial placeholder content
        editor.setHtml("<p>Start typing your content here...</p>")

        // Focus editor after a short delay to ensure WebView is ready
        editor.postDelayed({
            editor.focusEditor()
        }, 500)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.ext.rich_editor.R.menu.editor_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.ext.rich_editor.R.id.action_save -> {
                saveContent()
                true
            }
            com.ext.rich_editor.R.id.action_clear -> {
                clearContent()
                true
            }
            com.ext.rich_editor.R.id.action_preview -> {
                previewContent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showImageInsertDialog() {
        val options = arrayOf("From Gallery", "From URL")
        AlertDialog.Builder(this)
            .setTitle("Insert Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> imagePickerLauncher.launch("image/*")
                    1 -> showUrlInputDialog("Image URL") { url ->
                        editor.insertImage(url)
                        editor.focusEditor()
                    }
                }
            }
            .show()
    }

    private fun showVideoInsertDialog() {
        val options = arrayOf("From Gallery", "From URL")
        AlertDialog.Builder(this)
            .setTitle("Insert Video")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> videoPickerLauncher.launch("video/*")
                    1 -> showUrlInputDialog("Video URL") { url ->
                        editor.insertVideo(url)
                        editor.focusEditor()
                    }
                }
            }
            .show()
    }

    private fun showAudioInsertDialog() {
        val options = arrayOf("From Gallery", "From URL")
        AlertDialog.Builder(this)
            .setTitle("Insert Audio")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> audioPickerLauncher.launch("audio/*")
                    1 -> showUrlInputDialog("Audio URL") { url ->
                        editor.insertAudio(url)
                        editor.focusEditor()
                    }
                }
            }
            .show()
    }

    private fun showUrlInputDialog(title: String, onUrlEntered: (String) -> Unit) {
        val input = EditText(this).apply {
            hint = "Enter URL"
            setPadding(50, 40, 50, 40)
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Insert") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotEmpty()) {
                    onUrlEntered(url)
                } else {
                    Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                editor.focusEditor()
            }
            .setOnCancelListener {
                editor.focusEditor()
            }
            .show()
    }

    private fun showYoutubeDialog() {
        val input = EditText(this).apply {
            hint = "Enter YouTube Video ID or URL"
            setPadding(50, 40, 50, 40)
        }

        AlertDialog.Builder(this)
            .setTitle("Insert YouTube Video")
            .setView(input)
            .setPositiveButton("Insert") { _, _ ->
                val text = input.text.toString().trim()
                val videoId = extractYoutubeId(text)
                if (videoId.isNotEmpty()) {
                    editor.insertYoutube(videoId)
                    editor.focusEditor()
                } else {
                    Toast.makeText(this, "Invalid YouTube URL or ID", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                editor.focusEditor()
            }
            .setOnCancelListener {
                editor.focusEditor()
            }
            .show()
    }

    private fun showLinkDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val urlInput = EditText(this).apply {
            hint = "URL (required)"
            setPadding(0, 20, 0, 20)
        }

        val textInput = EditText(this).apply {
            hint = "Link Text (optional)"
            setPadding(0, 20, 0, 20)
        }

        layout.addView(urlInput)
        layout.addView(textInput)

        AlertDialog.Builder(this)
            .setTitle("Insert Link")
            .setView(layout)
            .setPositiveButton("Insert") { _, _ ->
                val url = urlInput.text.toString().trim()
                val text = textInput.text.toString().trim().ifEmpty { url }
                if (url.isNotEmpty()) {
                    editor.insertLink(url, text)
                    editor.focusEditor()
                } else {
//                    Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                editor.focusEditor()
            }
            .setOnCancelListener {
                editor.focusEditor()
            }
            .show()
    }

    private fun showColorPickerDialog(isTextColor: Boolean) {
        val colors = arrayOf(
            "#000000" to "Black",
            "#FF0000" to "Red",
            "#00FF00" to "Green",
            "#0000FF" to "Blue",
            "#FFFF00" to "Yellow",
            "#FF00FF" to "Magenta",
            "#00FFFF" to "Cyan",
            "#FFA500" to "Orange",
            "#800080" to "Purple",
            "#808080" to "Gray",
            "#FFFFFF" to "White"
        )

        val items = colors.map { it.second }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(if (isTextColor) "Select Text Color" else "Select Background Color")
            .setItems(items) { _, which ->
                val color = colors[which].first
                if (isTextColor) {
                    editor.setTextColor(color)
                } else {
                    editor.setTextBackgroundColor(color)
                }
                editor.focusEditor()
            }
            .setNegativeButton("Cancel") { _, _ ->
                editor.focusEditor()
            }
            .setOnCancelListener {
                editor.focusEditor()
            }
            .show()
    }

    private fun showFontSizeDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val textView = TextView(this).apply {
            text = "Font Size: 4"
            textSize = 16f
            setPadding(0, 0, 0, 20)
        }

        val seekBar = SeekBar(this).apply {
            max = 6
            progress = 3
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    textView.text = "Font Size: ${progress + 1}"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        layout.addView(textView)
        layout.addView(seekBar)

        AlertDialog.Builder(this)
            .setTitle("Select Font Size")
            .setView(layout)
            .setPositiveButton("Apply") { _, _ ->
                editor.setFontSize(seekBar.progress + 1)
                editor.focusEditor()
            }
            .setNegativeButton("Cancel") { _, _ ->
                editor.focusEditor()
            }
            .setOnCancelListener {
                editor.focusEditor()
            }
            .show()
    }

    private fun extractYoutubeId(input: String): String {
        return when {
            input.contains("youtube.com/watch?v=") -> {
                input.substringAfter("v=").substringBefore("&")
            }
            input.contains("youtu.be/") -> {
                input.substringAfter("youtu.be/").substringBefore("?")
            }
            input.length == 11 -> input // Assume it's already a video ID
            else -> ""
        }
    }

    private fun saveContent() {
        editor.getHtml { html ->
            // Here you can save to database, file, or SharedPreferences
            Toast.makeText(this, "Content saved!", Toast.LENGTH_SHORT).show()
            // Example: Save to SharedPreferences
            // getSharedPreferences("editor", MODE_PRIVATE).edit().putString("content", html).apply()
        }
    }

    private fun clearContent() {
        AlertDialog.Builder(this)
            .setTitle("Clear Content")
            .setMessage("Are you sure you want to clear all content?")
            .setPositiveButton("Clear") { _, _ ->
                editor.setHtml("")
                editor.focusEditor()
                Toast.makeText(this, "Content cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun previewContent() {
        editor.getHtml { html ->
            val dialog = AlertDialog.Builder(this)
                .setTitle("HTML Preview")
                .setMessage(html)
                .setPositiveButton("Copy") { _, _ ->
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("HTML", html)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "HTML copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Close", null)
                .create()

            dialog.show()
        }
    }

    override fun onBackPressed() {
        editor.getText { text ->
            if (text.trim().isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("Do you want to save your changes before leaving?")
                    .setPositiveButton("Save") { _, _ ->
                        saveContent()
                        super.onBackPressed()
                    }
                    .setNegativeButton("Discard") { _, _ ->
                        super.onBackPressed()
                    }
                    .setNeutralButton("Cancel", null)
                    .show()
            } else {
                super.onBackPressed()
            }
        }
    }
}