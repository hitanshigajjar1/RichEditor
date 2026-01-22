package com.ext.rich_editor

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout

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

    private lateinit var toolbarLayout: LinearLayout
    private var editor: RichEditor? = null
    private val actionButtons = mutableMapOf<EditorAction, ImageView>()

    // Customizable attributes - using backing fields to prevent premature refresh
    private var _iconSize: Int = dpToPx(24)
    private var _buttonSize: Int = dpToPx(40)
    private var _buttonPadding: Int = dpToPx(8)
    private var _buttonSpacing: Int = dpToPx(4)
    private var _activeColor: Int = Color.parseColor("#2196F3")
    private var _inactiveColor: Int = Color.parseColor("#757575")
    private var _toolbarBackgroundColor: Int = Color.TRANSPARENT
    private var _toolbarPaddingHorizontal: Int = dpToPx(8)
    private var _toolbarPaddingVertical: Int = dpToPx(4)
    private var _enableRipple: Boolean = true
    private var _buttonCornerRadius: Int = dpToPx(4)

    var iconSize: Int
        get() = _iconSize
        set(value) {
            _iconSize = value
            if (::toolbarLayout.isInitialized) refreshButtonSizes()
        }

    var buttonSize: Int
        get() = _buttonSize
        set(value) {
            _buttonSize = value
            if (::toolbarLayout.isInitialized) refreshButtonSizes()
        }

    var buttonPadding: Int
        get() = _buttonPadding
        set(value) {
            _buttonPadding = value
            if (::toolbarLayout.isInitialized) refreshButtonPadding()
        }

    var buttonSpacing: Int
        get() = _buttonSpacing
        set(value) {
            _buttonSpacing = value
            if (::toolbarLayout.isInitialized) refreshButtonSpacing()
        }

    var activeColor: Int
        get() = _activeColor
        set(value) {
            _activeColor = value
            if (::toolbarLayout.isInitialized) refreshButtonStates()
        }

    var inactiveColor: Int
        get() = _inactiveColor
        set(value) {
            _inactiveColor = value
            if (::toolbarLayout.isInitialized) refreshButtonStates()
        }

    var toolbarBackgroundColor: Int
        get() = _toolbarBackgroundColor
        set(value) {
            _toolbarBackgroundColor = value
            if (::toolbarLayout.isInitialized) toolbarLayout.setBackgroundColor(value)
        }

    var toolbarPaddingHorizontal: Int
        get() = _toolbarPaddingHorizontal
        set(value) {
            _toolbarPaddingHorizontal = value
            if (::toolbarLayout.isInitialized) refreshToolbarPadding()
        }

    var toolbarPaddingVertical: Int
        get() = _toolbarPaddingVertical
        set(value) {
            _toolbarPaddingVertical = value
            if (::toolbarLayout.isInitialized) refreshToolbarPadding()
        }

    var enableRipple: Boolean
        get() = _enableRipple
        set(value) {
            _enableRipple = value
            if (::toolbarLayout.isInitialized) refreshButtonBackgrounds()
        }

    var buttonCornerRadius: Int
        get() = _buttonCornerRadius
        set(value) {
            _buttonCornerRadius = value
            if (::toolbarLayout.isInitialized) refreshButtonBackgrounds()
        }

    private val defaultActions = listOf(
        EditorAction.BOLD, EditorAction.ITALIC, EditorAction.UNDERLINE,
        EditorAction.STRIKETHROUGH, EditorAction.UNORDERED_LIST, EditorAction.ORDERED_LIST,
        EditorAction.JUSTIFY_LEFT, EditorAction.JUSTIFY_CENTER, EditorAction.JUSTIFY_RIGHT,
        EditorAction.H1, EditorAction.H2, EditorAction.H3,
        EditorAction.BLOCKQUOTE, EditorAction.INSERT_IMAGE, EditorAction.INSERT_LINK,
        EditorAction.TEXT_COLOR, EditorAction.BG_COLOR, EditorAction.UNDO, EditorAction.REDO
    )

    init {
        // Read custom attributes BEFORE initializing layout
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RichEditorToolbar,
            0, 0
        ).apply {
            try {
                _iconSize = getDimensionPixelSize(R.styleable.RichEditorToolbar_iconSize, dpToPx(24))
                _buttonSize = getDimensionPixelSize(R.styleable.RichEditorToolbar_buttonSize, dpToPx(40))
                _buttonPadding = getDimensionPixelSize(R.styleable.RichEditorToolbar_buttonPadding, dpToPx(8))
                _buttonSpacing = getDimensionPixelSize(R.styleable.RichEditorToolbar_buttonSpacing, dpToPx(4))
                _activeColor = getColor(R.styleable.RichEditorToolbar_activeColor, Color.parseColor("#2196F3"))
                _inactiveColor = getColor(R.styleable.RichEditorToolbar_inactiveColor, Color.parseColor("#757575"))
                _toolbarBackgroundColor = getColor(R.styleable.RichEditorToolbar_toolbarBackgroundColor, Color.TRANSPARENT)
                _toolbarPaddingHorizontal = getDimensionPixelSize(R.styleable.RichEditorToolbar_toolbarPaddingHorizontal, dpToPx(8))
                _toolbarPaddingVertical = getDimensionPixelSize(R.styleable.RichEditorToolbar_toolbarPaddingVertical, dpToPx(4))
                _enableRipple = getBoolean(R.styleable.RichEditorToolbar_enableRipple, true)
                _buttonCornerRadius = getDimensionPixelSize(R.styleable.RichEditorToolbar_buttonCornerRadius, dpToPx(4))
            } finally {
                recycle()
            }
        }

        // NOW initialize the layout with the values from attributes
        isHorizontalScrollBarEnabled = false
        toolbarLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(_toolbarBackgroundColor)
            setPadding(_toolbarPaddingHorizontal, _toolbarPaddingVertical, _toolbarPaddingHorizontal, _toolbarPaddingVertical)
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
            layoutParams = LinearLayout.LayoutParams(_buttonSize, _buttonSize).apply {
                marginEnd = _buttonSpacing
            }

            background = createButtonBackground()
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(_buttonPadding, _buttonPadding, _buttonPadding, _buttonPadding)

            // Set icon
            setImageResource(action.iconRes)
            setColorFilter(_inactiveColor, PorterDuff.Mode.SRC_IN)

            // Set icon size by adjusting the image
            adjustViewBounds = true
            maxWidth = _iconSize
            maxHeight = _iconSize

            contentDescription = getContentDescription(action)

            setOnClickListener {
                handleActionClick(action)
            }
        }
    }

    private fun createButtonBackground(): android.graphics.drawable.Drawable {
        if (_enableRipple) {
            val attrs = intArrayOf(android.R.attr.selectableItemBackgroundBorderless)
            val ta = context.obtainStyledAttributes(attrs)
            val drawable = ta.getDrawable(0)
            ta.recycle()
            return drawable ?: android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
        } else {
            return createRoundedBackground()
        }
    }

    private fun createRoundedBackground(): android.graphics.drawable.Drawable {
        val shape = android.graphics.drawable.GradientDrawable()
        shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        shape.cornerRadius = _buttonCornerRadius.toFloat()
        shape.setColor(Color.TRANSPARENT)
        return shape
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
                if (isActive) _activeColor else _inactiveColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    // Refresh methods for dynamic updates
    private fun refreshButtonSizes() {
        actionButtons.values.forEach { button ->
            (button.layoutParams as? LinearLayout.LayoutParams)?.apply {
                width = _buttonSize
                height = _buttonSize
            }
            button.maxWidth = _iconSize
            button.maxHeight = _iconSize
            button.requestLayout()
        }
    }

    private fun refreshButtonPadding() {
        actionButtons.values.forEach { button ->
            button.setPadding(_buttonPadding, _buttonPadding, _buttonPadding, _buttonPadding)
        }
    }

    private fun refreshButtonSpacing() {
        actionButtons.values.forEach { button ->
            (button.layoutParams as? LinearLayout.LayoutParams)?.marginEnd = _buttonSpacing
        }
        toolbarLayout.requestLayout()
    }

    private fun refreshButtonStates() {
        // Refresh colors on all buttons
        actionButtons.forEach { (_, button) ->
            // Keep current color filter, just update the available colors
            val currentFilter = button.colorFilter
            if (currentFilter != null) {
                // Re-apply current state
                post {
                    editor?.let {
                        // Trigger a selection change update
                        updateButtonStates(emptyList())
                    }
                }
            }
        }
    }

    private fun refreshToolbarPadding() {
        toolbarLayout.setPadding(
            _toolbarPaddingHorizontal,
            _toolbarPaddingVertical,
            _toolbarPaddingHorizontal,
            _toolbarPaddingVertical
        )
    }

    private fun refreshButtonBackgrounds() {
        actionButtons.values.forEach { button ->
            button.background = createButtonBackground()
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