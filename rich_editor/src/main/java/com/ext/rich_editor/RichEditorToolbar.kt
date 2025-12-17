package com.ext.rich_editor

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class RichEditorToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    enum class EditorAction(val iconRes: Int) {
        BOLD(R.drawable.ic_bold),
        ITALIC(R.drawable.ic_italic),
        UNDERLINE(R.drawable.ic_underline),
        STRIKETHROUGH(R.drawable.ic_strikethrough),
        SUBSCRIPT(R.drawable.ic_subscript),
        SUPERSCRIPT(R.drawable.ic_superscript),
        JUSTIFY_LEFT(R.drawable.ic_align_left),
        JUSTIFY_CENTER(R.drawable.ic_align_center),
        JUSTIFY_RIGHT(R.drawable.ic_align_right),
        BLOCKQUOTE(R.drawable.ic_blockquote),
        H1(R.drawable.ic_heading_1),
        H2(R.drawable.ic_heading_2),
        H3(R.drawable.ic_heading_3),
        H4(R.drawable.ic_heading_4),
        H5(R.drawable.ic_heading_5),
        H6(R.drawable.ic_heading_6),
        UNDO(R.drawable.ic_undo),
        REDO(R.drawable.ic_redo),
        INDENT(R.drawable.ic_indent),
        OUTDENT(R.drawable.ic_outdent),
        INSERT_IMAGE(R.drawable.ic_image),
        INSERT_VIDEO(R.drawable.ic_video),
        INSERT_AUDIO(R.drawable.ic_audio),
        INSERT_YOUTUBE(R.drawable.ic_youtube),
        INSERT_LINK(R.drawable.ic_link),
        CHECKBOX(R.drawable.ic_checkbox),
        TEXT_COLOR(R.drawable.ic_color_text),
        BG_COLOR(R.drawable.ic_color_background),
        FONT_SIZE(R.drawable.ic_font_size),
        UNORDERED_LIST(R.drawable.ic_list_bulleted),
        ORDERED_LIST(R.drawable.ic_list_numbered)
    }

    private val toolbarLayout: LinearLayout
    private var editor: RichEditor? = null
    private val actionButtons = mutableMapOf<EditorAction, ImageView>()

    private val activeColor = Color.parseColor("#2196F3")
    private val inactiveColor = Color.parseColor("#757575")

    private val defaultActions = listOf(
        EditorAction.BOLD, EditorAction.ITALIC, EditorAction.UNDERLINE,
        EditorAction.STRIKETHROUGH, EditorAction.UNORDERED_LIST, EditorAction.ORDERED_LIST,
        EditorAction.JUSTIFY_LEFT, EditorAction.JUSTIFY_CENTER, EditorAction.JUSTIFY_RIGHT,
        EditorAction.H1, EditorAction.H2, EditorAction.H3,
        EditorAction.BLOCKQUOTE, EditorAction.INSERT_IMAGE, EditorAction.INSERT_LINK,
        EditorAction.TEXT_COLOR, EditorAction.BG_COLOR, EditorAction.UNDO, EditorAction.REDO
    )

    init {
        isHorizontalScrollBarEnabled = false
        toolbarLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
        }
        addView(toolbarLayout)
        setupDefaultActions()
    }

    private fun setupDefaultActions() {
        addActions(defaultActions)
    }

    fun addActions(actions: List<EditorAction>) {
        actions.forEach { action ->
            val button = createActionButton(action)
            toolbarLayout.addView(button)
            actionButtons[action] = button
        }
    }

    fun addAction(action: EditorAction, position: Int = -1) {
        val button = createActionButton(action)
        if (position >= 0 && position < toolbarLayout.childCount) {
            toolbarLayout.addView(button, position)
        } else {
            toolbarLayout.addView(button)
        }
        actionButtons[action] = button
    }

    fun removeAction(action: EditorAction) {
        actionButtons[action]?.let { button ->
            toolbarLayout.removeView(button)
            actionButtons.remove(action)
        }
    }

    fun clearActions() {
        toolbarLayout.removeAllViews()
        actionButtons.clear()
    }

    private fun createActionButton(action: EditorAction): ImageView {
        return ImageView(context).apply {
            val size = dpToPx(40)
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = dpToPx(4)
            }

            background = createRippleDrawable()
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))

            // Set icon
            setImageResource(action.iconRes)
            setColorFilter(inactiveColor, PorterDuff.Mode.SRC_IN)

            contentDescription = getContentDescription(action)

            setOnClickListener {
                handleActionClick(action)
            }
        }
    }

    private fun getContentDescription(action: EditorAction): String {
        return when (action) {
            EditorAction.BOLD -> "Bold"
            EditorAction.ITALIC -> "Italic"
            EditorAction.UNDERLINE -> "Underline"
            EditorAction.STRIKETHROUGH -> "Strikethrough"
            EditorAction.SUBSCRIPT -> "Subscript"
            EditorAction.SUPERSCRIPT -> "Superscript"
            EditorAction.JUSTIFY_LEFT -> "Align Left"
            EditorAction.JUSTIFY_CENTER -> "Align Center"
            EditorAction.JUSTIFY_RIGHT -> "Align Right"
            EditorAction.BLOCKQUOTE -> "Blockquote"
            EditorAction.H1 -> "Heading 1"
            EditorAction.H2 -> "Heading 2"
            EditorAction.H3 -> "Heading 3"
            EditorAction.H4 -> "Heading 4"
            EditorAction.H5 -> "Heading 5"
            EditorAction.H6 -> "Heading 6"
            EditorAction.UNDO -> "Undo"
            EditorAction.REDO -> "Redo"
            EditorAction.INDENT -> "Indent"
            EditorAction.OUTDENT -> "Outdent"
            EditorAction.INSERT_IMAGE -> "Insert Image"
            EditorAction.INSERT_VIDEO -> "Insert Video"
            EditorAction.INSERT_AUDIO -> "Insert Audio"
            EditorAction.INSERT_YOUTUBE -> "Insert YouTube"
            EditorAction.INSERT_LINK -> "Insert Link"
            EditorAction.CHECKBOX -> "Insert Checkbox"
            EditorAction.TEXT_COLOR -> "Text Color"
            EditorAction.BG_COLOR -> "Background Color"
            EditorAction.FONT_SIZE -> "Font Size"
            EditorAction.UNORDERED_LIST -> "Bullet List"
            EditorAction.ORDERED_LIST -> "Numbered List"
        }
    }

    private fun handleActionClick(action: EditorAction) {
        when (action) {
            EditorAction.BOLD -> editor?.setBold()
            EditorAction.ITALIC -> editor?.setItalic()
            EditorAction.UNDERLINE -> editor?.setUnderline()
            EditorAction.STRIKETHROUGH -> editor?.setStrikethrough()
            EditorAction.SUBSCRIPT -> editor?.setSubscript()
            EditorAction.SUPERSCRIPT -> editor?.setSuperscript()
            EditorAction.JUSTIFY_LEFT -> editor?.setJustifyLeft()
            EditorAction.JUSTIFY_CENTER -> editor?.setJustifyCenter()
            EditorAction.JUSTIFY_RIGHT -> editor?.setJustifyRight()
            EditorAction.BLOCKQUOTE -> editor?.setBlockquote()
            EditorAction.H1 -> editor?.setHeading(1)
            EditorAction.H2 -> editor?.setHeading(2)
            EditorAction.H3 -> editor?.setHeading(3)
            EditorAction.H4 -> editor?.setHeading(4)
            EditorAction.H5 -> editor?.setHeading(5)
            EditorAction.H6 -> editor?.setHeading(6)
            EditorAction.UNDO -> editor?.undo()
            EditorAction.REDO -> editor?.redo()
            EditorAction.INDENT -> editor?.indent()
            EditorAction.OUTDENT -> editor?.outdent()
            EditorAction.UNORDERED_LIST -> editor?.insertUnorderedList()
            EditorAction.ORDERED_LIST -> editor?.insertOrderedList()
            EditorAction.CHECKBOX -> editor?.insertCheckbox()
            EditorAction.INSERT_IMAGE -> onInsertImageClick?.invoke()
            EditorAction.INSERT_VIDEO -> onInsertVideoClick?.invoke()
            EditorAction.INSERT_AUDIO -> onInsertAudioClick?.invoke()
            EditorAction.INSERT_YOUTUBE -> onInsertYoutubeClick?.invoke()
            EditorAction.INSERT_LINK -> onInsertLinkClick?.invoke()
            EditorAction.TEXT_COLOR -> onTextColorClick?.invoke()
            EditorAction.BG_COLOR -> onBgColorClick?.invoke()
            EditorAction.FONT_SIZE -> onFontSizeClick?.invoke()
        }

        // Keep focus on editor after button click
        postDelayed({ editor?.focusEditor() }, 100)
    }

    private fun createRippleDrawable(): android.graphics.drawable.Drawable {
        val attrs = intArrayOf(android.R.attr.selectableItemBackgroundBorderless)
        val ta = context.obtainStyledAttributes(attrs)
        val drawable = ta.getDrawable(0)
        ta.recycle()
        return drawable ?: android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun setEditor(richEditor: RichEditor) {
        editor = richEditor

        richEditor.setOnSelectionChangeListener(object : RichEditor.OnSelectionChangeListener {
            override fun onSelectionChange(formats: List<String>) {
                updateButtonStates(formats)
            }
        })
    }

    private fun updateButtonStates(formats: List<String>) {
        actionButtons.forEach { (action, button) ->
            val isActive = when (action) {
                EditorAction.BOLD -> formats.contains("bold")
                EditorAction.ITALIC -> formats.contains("italic")
                EditorAction.UNDERLINE -> formats.contains("underline")
                EditorAction.STRIKETHROUGH -> formats.contains("strikethrough")
                EditorAction.SUBSCRIPT -> formats.contains("subscript")
                EditorAction.SUPERSCRIPT -> formats.contains("superscript")
                EditorAction.JUSTIFY_LEFT -> formats.contains("justifyLeft")
                EditorAction.JUSTIFY_CENTER -> formats.contains("justifyCenter")
                EditorAction.JUSTIFY_RIGHT -> formats.contains("justifyRight")
                EditorAction.UNORDERED_LIST -> formats.contains("unorderedList")
                EditorAction.ORDERED_LIST -> formats.contains("orderedList")
                else -> false
            }

            button.setColorFilter(
                if (isActive) activeColor else inactiveColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    // Callbacks for actions requiring user input
    var onInsertImageClick: (() -> Unit)? = null
    var onInsertVideoClick: (() -> Unit)? = null
    var onInsertAudioClick: (() -> Unit)? = null
    var onInsertYoutubeClick: (() -> Unit)? = null
    var onInsertLinkClick: (() -> Unit)? = null
    var onTextColorClick: (() -> Unit)? = null
    var onBgColorClick: (() -> Unit)? = null
    var onFontSizeClick: (() -> Unit)? = null
}