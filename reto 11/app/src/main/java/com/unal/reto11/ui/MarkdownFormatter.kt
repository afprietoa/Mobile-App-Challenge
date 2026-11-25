package com.unal.reto11.ui

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan

/** Markdown mínimo: **negrita**, `código`, bloques ```código``` y viñetas "* " / "- ". */
object MarkdownFormatter {
    private val codeBg = Color.argb(40, 128, 128, 128)

    fun format(source: String): CharSequence {
        val out = SpannableStringBuilder()
        var inBlock = false
        source.split("\n").forEachIndexed { i, rawLine ->
            if (i > 0 && !(rawLine.trimStart().startsWith("```"))) out.append('\n')
            if (rawLine.trimStart().startsWith("```")) {
                if (inBlock && out.isNotEmpty() && out.last() != '\n') out.append('\n')
                inBlock = !inBlock
                return@forEachIndexed
            }
            if (inBlock) {
                val start = out.length
                out.append(rawLine)
                out.setSpan(TypefaceSpan("monospace"), start, out.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                out.setSpan(BackgroundColorSpan(codeBg), start, out.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                val line = bullet(rawLine)
                appendInline(out, line)
            }
        }
        return out
    }

    private fun bullet(line: String): String {
        val t = line.trimStart()
        return if (t.startsWith("* ") || t.startsWith("- ")) "• " + t.substring(2) else line
    }

    private fun appendInline(out: SpannableStringBuilder, line: String) {
        var i = 0
        while (i < line.length) {
            when {
                line.startsWith("**", i) -> {
                    val end = line.indexOf("**", i + 2)
                    if (end > i + 2) {
                        val s = out.length
                        out.append(line, i + 2, end)
                        out.setSpan(StyleSpan(Typeface.BOLD), s, out.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        i = end + 2
                    } else { out.append(line[i]); i++ }
                }
                line[i] == '`' -> {
                    val end = line.indexOf('`', i + 1)
                    if (end > i + 1) {
                        val s = out.length
                        out.append(line, i + 1, end)
                        out.setSpan(TypefaceSpan("monospace"), s, out.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        out.setSpan(BackgroundColorSpan(codeBg), s, out.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        i = end + 1
                    } else { out.append(line[i]); i++ }
                }
                else -> { out.append(line[i]); i++ }
            }
        }
    }
}
