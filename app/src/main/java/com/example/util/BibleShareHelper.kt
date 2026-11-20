package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.BibleVerse
import com.example.ui.localization.SystemLocaleManager

object BibleShareHelper {

    fun formatVerse(
        book: BibleBook,
        verse: BibleVerse,
        primaryLanguage: BibleLanguage = BibleLanguage.ENGLISH,
        secondaryLanguage: BibleLanguage = BibleLanguage.NEPALI
    ): String {
        val text1 = verse.textFor(primaryLanguage)
        val text2 = if (secondaryLanguage != primaryLanguage) verse.textFor(secondaryLanguage) else ""
        return buildString {
            if (text1.isNotBlank()) {
                append("“$text1”")
            }
            if (text2.isNotBlank()) {
                if (isNotEmpty()) append("\n\n")
                append("“$text2”")
            }
            if (isEmpty()) {
                val fallback = verse.textEn.ifBlank { verse.textNe.ifBlank { verse.textOncv } }
                append("“$fallback”")
            }
            append("\n\n— ${book.nameFor(primaryLanguage)} ${verse.chapter}:${verse.verse}")
            if (secondaryLanguage != primaryLanguage) {
                append(" (${book.nameFor(secondaryLanguage)} ${verse.chapter}:${verse.verse})")
            }
            append("\n${SystemLocaleManager.strings().shareAppBrand}")
        }
    }

    fun copyVerse(
        context: Context,
        book: BibleBook,
        verse: BibleVerse,
        primaryLanguage: BibleLanguage = BibleLanguage.ENGLISH,
        secondaryLanguage: BibleLanguage = BibleLanguage.NEPALI
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText("Bible Verse", formatVerse(book, verse, primaryLanguage, secondaryLanguage))
        )
        Toast.makeText(context, SystemLocaleManager.strings().toastVerseCopied, Toast.LENGTH_SHORT).show()
    }

    fun shareGeneric(
        context: Context,
        book: BibleBook,
        verse: BibleVerse,
        primaryLanguage: BibleLanguage = BibleLanguage.ENGLISH,
        secondaryLanguage: BibleLanguage = BibleLanguage.NEPALI
    ) {
        val text = formatVerse(book, verse, primaryLanguage, secondaryLanguage)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, SystemLocaleManager.strings().shareChooserTitle))
    }

    fun shareToWhatsApp(
        context: Context,
        book: BibleBook,
        verse: BibleVerse,
        primaryLanguage: BibleLanguage = BibleLanguage.ENGLISH,
        secondaryLanguage: BibleLanguage = BibleLanguage.NEPALI
    ) {
        val text = formatVerse(book, verse, primaryLanguage, secondaryLanguage)
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        try {
            context.startActivity(whatsappIntent)
        } catch (_: Exception) {
            Toast.makeText(context, SystemLocaleManager.strings().toastWhatsappNotFound, Toast.LENGTH_SHORT).show()
            shareGeneric(context, book, verse, primaryLanguage, secondaryLanguage)
        }
    }

    fun shareToFacebook(
        context: Context,
        book: BibleBook,
        verse: BibleVerse,
        primaryLanguage: BibleLanguage = BibleLanguage.ENGLISH,
        secondaryLanguage: BibleLanguage = BibleLanguage.NEPALI
    ) {
        val text = formatVerse(book, verse, primaryLanguage, secondaryLanguage)
        copyVerse(context, book, verse, primaryLanguage, secondaryLanguage)
        val fbIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.facebook.katana")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        try {
            context.startActivity(fbIntent)
        } catch (_: Exception) {
            Toast.makeText(context, SystemLocaleManager.strings().toastFacebookCopied, Toast.LENGTH_SHORT).show()
            shareGeneric(context, book, verse, primaryLanguage, secondaryLanguage)
        }
    }
}
