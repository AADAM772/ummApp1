package com.example.ummapp

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.ummapp.data.Row
import com.example.ummapp.data.Tone
import com.example.ummapp.data.formatTime

fun View.applySystemBarPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
}

/** Inflates item_row.xml for [row] and adds it to [parent]. */
fun addRowView(parent: ViewGroup, row: Row, onClick: (() -> Unit)? = null): View {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
    view.findViewById<TextView>(R.id.rowTitle).text = row.title

    val subtitle = view.findViewById<TextView>(R.id.rowSubtitle)
    subtitle.text = row.subtitle
    subtitle.isVisible = row.subtitle.isNotEmpty()

    val time = view.findViewById<TextView>(R.id.rowTime)
    time.text = row.time?.let { "🕒 ${formatTime(it)}" }
    time.isVisible = row.time != null

    val tag = view.findViewById<TextView>(R.id.rowTag)
    tag.text = row.tag
    tag.isVisible = row.tag.isNotEmpty()
    val tagColor = when (row.tone) {
        Tone.GOOD -> R.color.green
        Tone.WARN -> R.color.orange
        Tone.BAD -> R.color.red
        Tone.NEUTRAL -> R.color.primary
    }
    tag.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(parent.context, tagColor))

    if (onClick != null) view.setOnClickListener { onClick() }
    parent.addView(view)
    return view
}
