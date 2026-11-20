package com.example.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.example.R
import com.example.data.model.BibleBook
import com.example.data.model.BibleLanguage
import com.example.data.model.BibleVerse
import com.example.data.model.DailyVerseData
import com.example.data.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Normalizer
import java.util.Calendar
import java.util.zip.GZIPInputStream

class BibleAssetDatabase(private val context: Context) {

    @Volatile
    private var database: SQLiteDatabase? = null
    private val lock = Any()

    private fun ensureOpen(): SQLiteDatabase {
        database?.let { if (it.isOpen) return it }
        synchronized(lock) {
            database?.let { if (it.isOpen) return it }
            val dbFile = context.getDatabasePath(DB_NAME)
            dbFile.parentFile?.mkdirs()
            val opened = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            val currentDbVersion = opened.version

            if (currentDbVersion < DB_SCHEMA_VERSION) {
                opened.execSQL("DROP TABLE IF EXISTS books")
            }

            opened.execSQL(
                """
                CREATE TABLE IF NOT EXISTS books (
                    id INTEGER PRIMARY KEY,
                    testament TEXT NOT NULL,
                    abbrev TEXT NOT NULL,
                    name_en TEXT NOT NULL,
                    name_ne TEXT NOT NULL,
                    name_ko TEXT NOT NULL,
                    chapter_count INTEGER NOT NULL
                )
                """.trimIndent()
            )
            opened.execSQL(
                """
                CREATE TABLE IF NOT EXISTS verses (
                    language_code TEXT NOT NULL,
                    book_id INTEGER NOT NULL,
                    chapter INTEGER NOT NULL,
                    verse INTEGER NOT NULL,
                    text TEXT NOT NULL,
                    search_norm TEXT NOT NULL,
                    PRIMARY KEY (language_code, book_id, chapter, verse)
                )
                """.trimIndent()
            )
            opened.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_verses_lang_bc ON verses(language_code, book_id, chapter)"
            )
            opened.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_verses_bcv ON verses(book_id, chapter, verse)"
            )

            // Seed or refresh 66 books metadata
            val count = opened.compileStatement("SELECT COUNT(*) FROM books").simpleQueryForLong()
            if (count < 66L) {
                seedBooksMetadata(opened)
            }

            // Migrate older databases cleanly while preserving existing ULB, English & Korean packs
            if (currentDbVersion < 3) {
                opened.execSQL("DELETE FROM verses WHERE language_code IN ('he', 'el', 'taj')")
                val existingNeBooks = mutableSetOf<Int>()
                opened.rawQuery(
                    "SELECT DISTINCT book_id FROM verses WHERE language_code = ?",
                    arrayOf(BibleLanguage.NEPALI.code)
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        existingNeBooks.add(cursor.getInt(0))
                    }
                }
                opened.execSQL(
                    "DELETE FROM verses WHERE language_code = ?",
                    arrayOf(BibleLanguage.NEPALI.code)
                )
                database = opened
                if (existingNeBooks.isNotEmpty()) {
                    installBooksIntoDb(opened, BibleLanguage.NEPALI, existingNeBooks)
                }
                opened.version = DB_SCHEMA_VERSION
            } else if (currentDbVersion < DB_SCHEMA_VERSION) {
                database = opened
                opened.version = DB_SCHEMA_VERSION
            } else {
                database = opened
            }
            return opened
        }
    }

    private fun seedBooksMetadata(db: SQLiteDatabase) {
        val jsonText = context.resources.openRawResource(R.raw.bible_books_meta).use { raw ->
            GZIPInputStream(raw).bufferedReader(Charsets.UTF_8).readText()
        }
        val array = JSONArray(jsonText)
        db.beginTransaction()
        try {
            val stmt = db.compileStatement(
                "INSERT OR REPLACE INTO books (id, testament, abbrev, name_en, name_ne, name_ko, chapter_count) VALUES (?, ?, ?, ?, ?, ?, ?)"
            )
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                stmt.clearBindings()
                stmt.bindLong(1, obj.getInt("id").toLong())
                stmt.bindString(2, obj.getString("testament"))
                stmt.bindString(3, obj.optString("abbrev", ""))
                stmt.bindString(4, obj.getString("nameEn"))
                stmt.bindString(5, obj.getString("nameNe"))
                stmt.bindString(6, obj.optString("nameKo", ""))
                stmt.bindLong(7, obj.getInt("chapterCount").toLong())
                stmt.executeInsert()
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    suspend fun warmup() = withContext(Dispatchers.IO) {
        ensureOpen()
    }

    private fun rawResForLanguage(language: BibleLanguage): Int = when (language) {
        BibleLanguage.ENGLISH -> R.raw.pack_en
        BibleLanguage.NEPALI -> R.raw.pack_ne
        BibleLanguage.ONCV -> R.raw.pack_ne_oncv
        BibleLanguage.KOREAN -> R.raw.pack_ko
    }

    /**
     * Installs all verses for a single book in [language] from the verified compressed pack
     * into the device's persistent SQLite storage, returning installed byte count.
     */
    suspend fun installBookFromPack(language: BibleLanguage, bookId: Int): Long =
        withContext(Dispatchers.IO) {
            installBooksFromPack(language, setOf(bookId))[bookId] ?: 0L
        }

    /**
     * Batch-installs a set of books for [language] in a single fast stream pass.
     */
    suspend fun installBooksFromPack(
        language: BibleLanguage,
        bookIds: Set<Int>
    ): Map<Int, Long> = withContext(Dispatchers.IO) {
        if (bookIds.isEmpty()) return@withContext emptyMap()
        val db = ensureOpen()
        installBooksIntoDb(db, language, bookIds)
    }

    private fun installBooksIntoDb(
        db: SQLiteDatabase,
        language: BibleLanguage,
        bookIds: Set<Int>
    ): Map<Int, Long> {
        val byteTotals = HashMap<Int, Long>()
        val rawRes = rawResForLanguage(language)

        db.beginTransaction()
        try {
            val stmt: SQLiteStatement = db.compileStatement(
                "INSERT OR REPLACE INTO verses (language_code, book_id, chapter, verse, text, search_norm) VALUES (?, ?, ?, ?, ?, ?)"
            )
            context.resources.openRawResource(rawRes).use { raw ->
                GZIPInputStream(raw).use { gz ->
                    BufferedReader(InputStreamReader(gz, Charsets.UTF_8)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val parts = line!!.split('\t', limit = 4)
                            if (parts.size < 4) continue
                            val bid = parts[0].toIntOrNull() ?: continue
                            if (!bookIds.contains(bid)) continue
                            val ch = parts[1].toIntOrNull() ?: continue
                            val v = parts[2].toIntOrNull() ?: continue
                            val txt = parts[3]
                            val norm = normalizeForSearch(txt, language)

                            stmt.clearBindings()
                            stmt.bindString(1, language.code)
                            stmt.bindLong(2, bid.toLong())
                            stmt.bindLong(3, ch.toLong())
                            stmt.bindLong(4, v.toLong())
                            stmt.bindString(5, txt)
                            stmt.bindString(6, norm)
                            stmt.executeInsert()

                            byteTotals[bid] = (byteTotals[bid] ?: 0L) + txt.toByteArray(Charsets.UTF_8).size
                        }
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return byteTotals
    }

    suspend fun removeBook(language: BibleLanguage, bookId: Int) = withContext(Dispatchers.IO) {
        val db = ensureOpen()
        db.delete(
            "verses",
            "language_code = ? AND book_id = ?",
            arrayOf(language.code, bookId.toString())
        )
    }

    suspend fun removeLanguage(language: BibleLanguage) = withContext(Dispatchers.IO) {
        val db = ensureOpen()
        db.delete("verses", "language_code = ?", arrayOf(language.code))
    }

    suspend fun isBookDownloaded(language: BibleLanguage, bookId: Int): Boolean =
        withContext(Dispatchers.IO) {
            val db = ensureOpen()
            val stmt = db.compileStatement(
                "SELECT EXISTS(SELECT 1 FROM verses WHERE language_code = ? AND book_id = ? LIMIT 1)"
            )
            stmt.bindString(1, language.code)
            stmt.bindLong(2, bookId.toLong())
            stmt.simpleQueryForLong() == 1L
        }

    suspend fun getDatabaseFileSizeBytes(): Long = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (dbFile.exists()) dbFile.length() else 0L
    }

    suspend fun getAllBooks(): List<BibleBook> = withContext(Dispatchers.IO) {
        val db = ensureOpen()
        val books = mutableListOf<BibleBook>()
        db.rawQuery(
            "SELECT id, testament, abbrev, name_en, name_ne, name_ko, chapter_count FROM books ORDER BY id ASC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                books.add(
                    BibleBook(
                        id = cursor.getInt(0),
                        testament = cursor.getString(1),
                        abbrev = cursor.getString(2),
                        nameEn = cursor.getString(3),
                        nameNe = cursor.getString(4),
                        nameKo = cursor.getString(5),
                        chapterCount = cursor.getInt(6)
                    )
                )
            }
        }
        books
    }

    suspend fun getBook(bookId: Int): BibleBook? = withContext(Dispatchers.IO) {
        val db = ensureOpen()
        db.rawQuery(
            "SELECT id, testament, abbrev, name_en, name_ne, name_ko, chapter_count FROM books WHERE id = ?",
            arrayOf(bookId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                BibleBook(
                    id = cursor.getInt(0),
                    testament = cursor.getString(1),
                    abbrev = cursor.getString(2),
                    nameEn = cursor.getString(3),
                    nameNe = cursor.getString(4),
                    nameKo = cursor.getString(5),
                    chapterCount = cursor.getInt(6)
                )
            } else null
        }
    }

    suspend fun getChapterVerses(bookId: Int, chapter: Int): List<BibleVerse> =
        withContext(Dispatchers.IO) {
            val db = ensureOpen()
            val byVerse = sortedMapOf<Int, MutableMap<String, String>>()
            db.rawQuery(
                "SELECT verse, language_code, text FROM verses WHERE book_id = ? AND chapter = ? ORDER BY verse ASC",
                arrayOf(bookId.toString(), chapter.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val vNum = cursor.getInt(0)
                    val lang = cursor.getString(1)
                    val txt = cursor.getString(2)
                    byVerse.getOrPut(vNum) { HashMap() }[lang] = txt
                }
            }
            byVerse.entries.mapIndexed { idx, (vNum, map) ->
                BibleVerse(
                    id = idx + 1,
                    bookId = bookId,
                    chapter = chapter,
                    verse = vNum,
                    textEn = map[BibleLanguage.ENGLISH.code] ?: "",
                    textNe = map[BibleLanguage.NEPALI.code] ?: "",
                    textOncv = map[BibleLanguage.ONCV.code] ?: "",
                    textKo = map[BibleLanguage.KOREAN.code] ?: ""
                )
            }
        }

    suspend fun getChapterVerseCountFromAnyPack(bookId: Int, chapter: Int): Int =
        withContext(Dispatchers.IO) {
            val db = ensureOpen()
            val installedCount = db.compileStatement(
                "SELECT COUNT(DISTINCT verse) FROM verses WHERE book_id = $bookId AND chapter = $chapter"
            ).simpleQueryForLong().toInt()
            if (installedCount > 0) return@withContext installedCount

            // Fallback count directly from English KJV pack if user deleted local books
            var maxVerse = 0
            context.resources.openRawResource(R.raw.pack_en).use { raw ->
                GZIPInputStream(raw).use { gz ->
                    BufferedReader(InputStreamReader(gz, Charsets.UTF_8)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val parts = line!!.split('\t', limit = 4)
                            if (parts.size < 3) continue
                            if (parts[0].toIntOrNull() == bookId && parts[1].toIntOrNull() == chapter) {
                                val v = parts[2].toIntOrNull() ?: 0
                                if (v > maxVerse) maxVerse = v
                            }
                        }
                    }
                }
            }
            maxVerse.coerceAtLeast(1)
        }

    suspend fun getVerse(bookId: Int, chapter: Int, verse: Int): BibleVerse? =
        withContext(Dispatchers.IO) {
            val db = ensureOpen()
            val map = HashMap<String, String>()
            db.rawQuery(
                "SELECT language_code, text FROM verses WHERE book_id = ? AND chapter = ? AND verse = ?",
                arrayOf(bookId.toString(), chapter.toString(), verse.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    map[cursor.getString(0)] = cursor.getString(1)
                }
            }
            if (map.isEmpty()) return@withContext null
            BibleVerse(
                id = verse,
                bookId = bookId,
                chapter = chapter,
                verse = verse,
                textEn = map[BibleLanguage.ENGLISH.code] ?: "",
                textNe = map[BibleLanguage.NEPALI.code] ?: "",
                textOncv = map[BibleLanguage.ONCV.code] ?: "",
                textKo = map[BibleLanguage.KOREAN.code] ?: ""
            )
        }

    suspend fun getDailyVerse(): DailyVerseData? = withContext(Dispatchers.IO) {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val (bookId, chapter, verse) = DAILY_VERSE_COORDS[dayOfYear % DAILY_VERSE_COORDS.size]
        val book = getBook(bookId) ?: return@withContext null
        val v = getVerse(bookId, chapter, verse) ?: return@withContext null
        DailyVerseData(book, v)
    }

    suspend fun searchVerses(
        query: String,
        testamentFilter: String? = null,
        languageFilter: BibleLanguage? = null
    ): List<SearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()
        val db = ensureOpen()

        val normGeneral = normalizeForSearch(trimmed, languageFilter ?: detectLikelyLanguage(trimmed))
        val pattern = "%$normGeneral%"

        val matchingCoords = mutableListOf<Triple<Int, Int, Int>>()
        val matchedLangMap = HashMap<Triple<Int, Int, Int>, BibleLanguage>()

        val sql = buildString {
            append(
                """
                SELECT v.book_id, v.chapter, v.verse, v.language_code
                FROM verses v
                INNER JOIN books b ON v.book_id = b.id
                WHERE (v.search_norm LIKE ? OR LOWER(b.name_en) LIKE ? OR REPLACE(REPLACE(b.name_ne, char(8205), ''), char(8204), '') LIKE ? OR b.name_ko LIKE ?)
                """.trimIndent()
            )
            if (!testamentFilter.isNullOrBlank()) {
                append(" AND b.testament = ?")
            }
            if (languageFilter != null) {
                append(" AND v.language_code = ?")
            }
            append(" GROUP BY v.book_id, v.chapter, v.verse ORDER BY v.book_id ASC, v.chapter ASC, v.verse ASC LIMIT 100")
        }

        val args = buildList {
            add(pattern)
            add("%${trimmed.lowercase()}%")
            add("%${trimmed.replace("\u200D", "").replace("\u200C", "")}%")
            add("%$trimmed%")
            if (!testamentFilter.isNullOrBlank()) add(testamentFilter)
            if (languageFilter != null) add(languageFilter.code)
        }.toTypedArray()

        db.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                val coord = Triple(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2))
                matchingCoords.add(coord)
                matchedLangMap[coord] = BibleLanguage.fromCode(cursor.getString(3))
            }
        }

        if (matchingCoords.isEmpty()) return@withContext emptyList()

        val booksMap = getAllBooks().associateBy { it.id }
        val results = mutableListOf<SearchResult>()
        for (coord in matchingCoords) {
            val b = booksMap[coord.first] ?: continue
            val fullVerse = getVerse(coord.first, coord.second, coord.third) ?: continue
            results.add(
                SearchResult(
                    bookId = b.id,
                    bookNameEn = b.nameEn,
                    bookNameNe = b.nameNe,
                    testament = b.testament,
                    chapter = coord.second,
                    verse = coord.third,
                    matchedLanguage = matchedLangMap[coord] ?: BibleLanguage.ENGLISH,
                    textEn = fullVerse.textEn,
                    textNe = fullVerse.textNe,
                    textOncv = fullVerse.textOncv,
                    textKo = fullVerse.textKo
                )
            )
        }
        results
    }

    private fun detectLikelyLanguage(query: String): BibleLanguage {
        return when {
            query.any { it in '\uAC00'..'\uD7AF' || it in '\u1100'..'\u11FF' || it in '\u3130'..'\u318F' } -> BibleLanguage.KOREAN
            query.any { it in '\u0900'..'\u097F' } -> BibleLanguage.NEPALI
            else -> BibleLanguage.ENGLISH
        }
    }

    companion object {
        private const val DB_NAME = "offline_bible_installed.db"
        private const val DB_SCHEMA_VERSION = 4

        fun normalizeForSearch(text: String, language: BibleLanguage): String {
            return when (language) {
                BibleLanguage.ENGLISH -> text.lowercase()
                BibleLanguage.NEPALI, BibleLanguage.ONCV -> {
                    text.replace("\u200D", "").replace("\u200C", "")
                }
                BibleLanguage.KOREAN -> {
                    Normalizer.normalize(text.trim(), Normalizer.Form.NFC)
                }
            }
        }

        private val DAILY_VERSE_COORDS = listOf(
            Triple(43, 3, 16),   // John 3:16
            Triple(19, 23, 1),   // Psalm 23:1
            Triple(20, 3, 5),    // Proverbs 3:5
            Triple(50, 4, 13),   // Philippians 4:13
            Triple(45, 8, 28),   // Romans 8:28
            Triple(6, 1, 9),     // Joshua 1:9
            Triple(23, 41, 10),  // Isaiah 41:10
            Triple(40, 11, 28),  // Matthew 11:28
            Triple(19, 119, 105),// Psalm 119:105
            Triple(46, 13, 4),   // 1 Corinthians 13:4
            Triple(24, 29, 11),  // Jeremiah 29:11
            Triple(19, 46, 1),   // Psalm 46:1
            Triple(1, 1, 1),     // Genesis 1:1
            Triple(40, 5, 14),   // Matthew 5:14
            Triple(19, 121, 1),  // Psalm 121:1
            Triple(45, 12, 2),   // Romans 12:2
            Triple(50, 4, 6),    // Philippians 4:6
            Triple(23, 40, 31),  // Isaiah 40:31
            Triple(43, 14, 6),   // John 14:6
            Triple(48, 5, 22),   // Galatians 5:22
            Triple(58, 11, 1),   // Hebrews 11:1
            Triple(19, 27, 1),   // Psalm 27:1
            Triple(40, 6, 33),   // Matthew 6:33
            Triple(43, 1, 1)     // John 1:1
        )
    }
}
