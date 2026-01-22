package com.ext.rich_editor

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.view.inputmethod.InputMethodManager
import org.json.JSONArray

class RichEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    interface OnTextChangeListener {
        fun onTextChange(html: String)
    }

    interface OnSelectionChangeListener {
        fun onSelectionChange(formats: List<String>)
    }

    private var textChangeListener: OnTextChangeListener? = null
    private var selectionChangeListener: OnSelectionChangeListener? = null
    private var isReady = false
    private val pendingCommands = mutableListOf<String>()

    init {
        configureWebView()
        addJavascriptInterface(EditorBridge(), "AndroidBridge")
        loadEditorHtml()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        webChromeClient = WebChromeClient()

        // Enable keyboard input
        isFocusable = true
        isFocusableInTouchMode = true

        setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    if (!hasFocus()) {
                        requestFocus()
                    }
                }
            }
            false
        }
    }

    private fun loadEditorHtml() {
        val html = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { 
            height: 100%;
            -webkit-text-size-adjust: 100%;
        }
        body { 
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            font-size: 16px;
            padding: 16px;
            color: #333;
            line-height: 1.6;
        }
        #editor {
            min-height: 200px;
            outline: none;
            word-wrap: break-word;
            overflow-wrap: break-word;
            -webkit-user-select: text;
            user-select: text;
        }
        #editor:focus {
            outline: none;
        }
        #editor img { 
            max-width: 100%; 
            height: auto; 
            display: block;
            margin: 8px 0;
        }
        #editor video { 
            max-width: 100%;
            display: block;
            margin: 8px 0;
        }
        #editor audio { 
            width: 100%;
            margin: 8px 0;
        }
        #editor iframe { 
            max-width: 100%;
            margin: 8px 0;
        }
        blockquote {
            border-left: 4px solid #2196F3;
            padding-left: 16px;
            margin: 12px 0;
            color: #555;
            font-style: italic;
        }
        h1 { font-size: 2em; margin: 0.67em 0; font-weight: bold; }
        h2 { font-size: 1.5em; margin: 0.75em 0; font-weight: bold; }
        h3 { font-size: 1.17em; margin: 0.83em 0; font-weight: bold; }
        h4 { font-size: 1em; margin: 1.12em 0; font-weight: bold; }
        h5 { font-size: 0.83em; margin: 1.5em 0; font-weight: bold; }
        h6 { font-size: 0.75em; margin: 1.67em 0; font-weight: bold; }
        ul, ol { padding-left: 32px; margin: 8px 0; }
        .checkbox-item {
            display: flex;
            align-items: flex-start;
            margin: 6px 0;
        }
        .checkbox-item input[type="checkbox"] {
            margin-right: 8px;
            margin-top: 4px;
            cursor: pointer;
        }
        .checkbox-item span {
            flex: 1;
        }
        a {
            color: #2196F3;
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div id="editor" contenteditable="true"></div>
    <script>
        const editor = document.getElementById('editor');
        let updateTimeout;

        // Focus editor on load
        window.addEventListener('load', function() {
            editor.focus();
        });

        // Handle input
        editor.addEventListener('input', () => {
            clearTimeout(updateTimeout);
            updateTimeout = setTimeout(() => {
                AndroidBridge.onTextChange(editor.innerHTML);
            }, 300);
        });

        // Handle selection changes
        document.addEventListener('selectionchange', updateSelectionState);
        editor.addEventListener('mouseup', updateSelectionState);
        editor.addEventListener('keyup', updateSelectionState);
        editor.addEventListener('focus', updateSelectionState);

        function updateSelectionState() {
            const formats = [];
            
            if (document.queryCommandState('bold')) formats.push('bold');
            if (document.queryCommandState('italic')) formats.push('italic');
            if (document.queryCommandState('underline')) formats.push('underline');
            if (document.queryCommandState('strikeThrough')) formats.push('strikethrough');
            if (document.queryCommandState('subscript')) formats.push('subscript');
            if (document.queryCommandState('superscript')) formats.push('superscript');
            if (document.queryCommandState('justifyLeft')) formats.push('justifyLeft');
            if (document.queryCommandState('justifyCenter')) formats.push('justifyCenter');
            if (document.queryCommandState('justifyRight')) formats.push('justifyRight');
            if (document.queryCommandState('insertUnorderedList')) formats.push('unorderedList');
            if (document.queryCommandState('insertOrderedList')) formats.push('orderedList');
            
            AndroidBridge.onSelectionChange(JSON.stringify(formats));
        }

        function exec(command, value = null) {
            editor.focus();
            document.execCommand(command, false, value);
            editor.focus();
            updateSelectionState();
        }

        function setBold() { exec('bold'); }
        function setItalic() { exec('italic'); }
        function setUnderline() { exec('underline'); }
        function setStrikethrough() { exec('strikeThrough'); }
        function setSubscript() { exec('subscript'); }
        function setSuperscript() { exec('superscript'); }
        
        function setJustifyLeft() { exec('justifyLeft'); }
        function setJustifyCenter() { exec('justifyCenter'); }
        function setJustifyRight() { exec('justifyRight'); }
        
        function setBlockquote() { exec('formatBlock', 'blockquote'); }
        function setHeading(level) { exec('formatBlock', '<h' + level + '>'); }
        
        function undo() { exec('undo'); }
        function redo() { exec('redo'); }
        
        function indent() { exec('indent'); }
        function outdent() { exec('outdent'); }
        
        function insertUnorderedList() { exec('insertUnorderedList'); }
        function insertOrderedList() { exec('insertOrderedList'); }
        
        function setTextColor(color) { exec('foreColor', color); }
        function setTextBackgroundColor(color) { exec('backColor', color); }
        function setFontSize(size) { exec('fontSize', size); }
        
        function insertImage(url) {
            exec('insertImage', url);
        }
        
        function insertVideo(url) {
            const video = '<video controls src="' + url + '" style="max-width:100%;"></video>';
            exec('insertHTML', video);
        }
        
        function insertAudio(url) {
            const audio = '<audio controls src="' + url + '" style="width:100%;"></audio>';
            exec('insertHTML', audio);
        }
        
        function insertYoutube(videoId) {
            const iframe = '<iframe width="100%" height="315" src="https://www.youtube.com/embed/' + 
                          videoId + '" frameborder="0" allowfullscreen></iframe>';
            exec('insertHTML', iframe);
        }
        
        function insertLink(url, text) {
            if (window.getSelection().toString()) {
                exec('createLink', url);
            } else {
                const link = '<a href="' + url + '">' + text + '</a>';
                exec('insertHTML', link);
            }
        }
        
        function insertCheckbox(text) {
            const checkbox = '<div class="checkbox-item"><input type="checkbox" onclick="return true;">' + 
                           '<span contenteditable="true">' + (text || 'Checkbox item') + '</span></div>';
            exec('insertHTML', checkbox);
        }
        
        function setHtml(html) {
            editor.innerHTML = html;
        }
        
        function getHtml() {
            return editor.innerHTML;
        }
        
        function getText() {
            return editor.innerText;
        }
        
        function focus() {
            editor.focus();
        }
        
        // Signal ready
        AndroidBridge.onReady();
    </script>
</body>
</html>
        """.trimIndent()

        loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
    }

    private inner class EditorBridge {
        @JavascriptInterface
        fun onReady() {
            isReady = true
            post {
                pendingCommands.forEach { executeJavaScript(it) }
                pendingCommands.clear()
                // Auto-focus editor when ready
                focusEditor()
            }
        }

        @JavascriptInterface
        fun onTextChange(html: String) {
            post { textChangeListener?.onTextChange(html) }
        }

        @JavascriptInterface
        fun onSelectionChange(formatsJson: String) {
            post {
                try {
                    val formats = mutableListOf<String>()
                    val jsonArray = JSONArray(formatsJson)
                    for (i in 0 until jsonArray.length()) {
                        formats.add(jsonArray.getString(i))
                    }
                    selectionChangeListener?.onSelectionChange(formats)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    internal fun executeJavaScript(script: String) {
        if (isReady) {
            evaluateJavascript("javascript:$script", null)
        } else {
            pendingCommands.add(script)
        }
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        requestFocus()
        return super.onTouchEvent(event)
    }

    // Formatting methods
    fun setBold() = executeJavaScript("setBold()")
    fun setItalic() = executeJavaScript("setItalic()")
    fun setUnderline() = executeJavaScript("setUnderline()")
    fun setStrikethrough() = executeJavaScript("setStrikethrough()")
    fun setSubscript() = executeJavaScript("setSubscript()")
    fun setSuperscript() = executeJavaScript("setSuperscript()")

    fun setJustifyLeft() = executeJavaScript("setJustifyLeft()")
    fun setJustifyCenter() = executeJavaScript("setJustifyCenter()")
    fun setJustifyRight() = executeJavaScript("setJustifyRight()")

    fun setBlockquote() = executeJavaScript("setBlockquote()")
    fun setHeading(level: Int) = executeJavaScript("setHeading($level)")

    fun undo() = executeJavaScript("undo()")
    fun redo() = executeJavaScript("redo()")

    fun indent() = executeJavaScript("indent()")
    fun outdent() = executeJavaScript("outdent()")

    fun insertUnorderedList() = executeJavaScript("insertUnorderedList()")
    fun insertOrderedList() = executeJavaScript("insertOrderedList()")

    fun setTextColor(color: String) = executeJavaScript("setTextColor('$color')")
    fun setTextBackgroundColor(color: String) = executeJavaScript("setTextBackgroundColor('$color')")
    fun setFontSize(size: Int) = executeJavaScript("setFontSize($size)")

    fun insertImage(url: String) = executeJavaScript("insertImage('$url')")
    fun insertVideo(url: String) = executeJavaScript("insertVideo('$url')")
    fun insertAudio(url: String) = executeJavaScript("insertAudio('$url')")
    fun insertYoutube(videoId: String) = executeJavaScript("insertYoutube('$videoId')")
    fun insertLink(url: String, text: String = url) {
        val escapedUrl = url.replace("'", "\\'")
        val escapedText = text.replace("'", "\\'")
        executeJavaScript("insertLink('$escapedUrl', '$escapedText')")
    }
    fun insertCheckbox(text: String = "Checkbox item") {
        val escapedText = text.replace("'", "\\'")
        executeJavaScript("insertCheckbox('$escapedText')")
    }

    fun setHtml(html: String) {
        val escaped = html.replace("'", "\\'").replace("\n", "\\n").replace("\r", "")
        executeJavaScript("setHtml('$escaped')")
    }

    fun getHtml(callback: (String) -> Unit) {
        evaluateJavascript("javascript:getHtml()") { html ->
            callback(html?.removeSurrounding("\"")?.replace("\\u003C", "<")?.replace("\\n", "\n") ?: "")
        }
    }

    fun getText(callback: (String) -> Unit) {
        evaluateJavascript("javascript:getText()") { text ->
            callback(text?.removeSurrounding("\"") ?: "")
        }
    }

    fun focusEditor() {
        executeJavaScript("focus()")
        post {
            requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun setOnTextChangeListener(listener: OnTextChangeListener) {
        textChangeListener = listener
    }

    fun setOnSelectionChangeListener(listener: OnSelectionChangeListener) {
        selectionChangeListener = listener
    }
}