package com.miassolutions.milkledger.utils.extensions


import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.miassolutions.milkledger.R
import java.util.Locale

fun TextView.setHighlightedText(
    value: String,
    query: String,
    @ColorRes highlightColorRes: Int = R.color.highlight_yellow
) {
    text = value.highlightSearchQuery(
        query = query,
        context = context,
        highlightColorRes = highlightColorRes
    )
}

fun TextView.highlightCurrentText(
    query: String,
    @ColorRes highlightColorRes: Int = R.color.highlight_yellow
) {
    text = text.toString().highlightSearchQuery(
        query = query,
        context = context,
        highlightColorRes = highlightColorRes
    )
}

fun String.highlightSearchQuery(
    query: String,
    context: Context,
    @ColorRes highlightColorRes: Int = R.color.highlight_yellow
): CharSequence {
    val cleanQuery = query.trim()

    if (cleanQuery.isBlank()) return this

    val originalText = this
    val spannable = SpannableString(originalText)

    val normalizedSource = buildNormalizedTextWithIndexMap(originalText)
    val normalizedQuery = cleanQuery.normalizeForHighlightSearch()

    if (normalizedQuery.isBlank()) return this
    if (normalizedSource.normalizedText.isBlank()) return this

    val highlightColor = ContextCompat.getColor(
        context,
        highlightColorRes
    )

    var matchStart = normalizedSource.normalizedText.indexOf(
        string = normalizedQuery,
        startIndex = 0,
        ignoreCase = true
    )

    while (matchStart >= 0) {
        val matchEnd = matchStart + normalizedQuery.length - 1

        val originalStart = normalizedSource.originalIndexes[matchStart]
        val originalEnd = normalizedSource.originalIndexes[matchEnd] + 1

        spannable.setSpan(
            BackgroundColorSpan(highlightColor),
            originalStart,
            originalEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            originalStart,
            originalEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        matchStart = normalizedSource.normalizedText.indexOf(
            string = normalizedQuery,
            startIndex = matchStart + normalizedQuery.length,
            ignoreCase = true
        )
    }

    return spannable
}

private data class NormalizedTextWithIndexMap(
    val normalizedText: String,
    val originalIndexes: List<Int>
)

private fun buildNormalizedTextWithIndexMap(
    text: String
): NormalizedTextWithIndexMap {
    val normalized = StringBuilder()
    val indexes = mutableListOf<Int>()

    text.forEachIndexed { index, char ->
        if (char.isLetterOrDigit()) {
            normalized.append(char.lowercaseChar())
            indexes.add(index)
        }
    }

    return NormalizedTextWithIndexMap(
        normalizedText = normalized.toString(),
        originalIndexes = indexes
    )
}

private fun String.normalizeForHighlightSearch(): String {
    return filter { it.isLetterOrDigit() }
        .lowercase(Locale.getDefault())
}