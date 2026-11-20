package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val verseKey: String, // "bookId:chapter:verse"
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val bookNameEn: String,
    val bookNameNe: String,
    val previewEn: String,
    val previewNe: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey val verseKey: String,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val bookNameEn: String,
    val bookNameNe: String,
    val previewEn: String,
    val previewNe: String,
    val colorKey: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val verseKey: String,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val bookNameEn: String,
    val bookNameNe: String,
    val versePreviewEn: String,
    val versePreviewNe: String,
    val noteText: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey val chapterKey: String, // "bookId:chapter"
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val bookNameEn: String,
    val bookNameNe: String,
    val visitedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "downloaded_books",
    primaryKeys = ["languageCode", "bookId"]
)
data class DownloadedBookEntity(
    val languageCode: String,
    val bookId: Int,
    val status: String, // NOT_DOWNLOADED, QUEUED, DOWNLOADING, PAUSED, DOWNLOADED
    val bytesSize: Long,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface UserBibleDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE verseKey = :verseKey")
    suspend fun deleteBookmark(verseKey: String)

    @Query("SELECT * FROM highlights ORDER BY updatedAt DESC")
    fun observeHighlights(): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE bookId = :bookId AND chapter = :chapter")
    fun observeChapterHighlights(bookId: Int, chapter: Int): Flow<List<HighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHighlight(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE verseKey = :verseKey")
    suspend fun deleteHighlight(verseKey: String)

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE bookId = :bookId AND chapter = :chapter")
    fun observeChapterNotes(bookId: Int, chapter: Int): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE verseKey = :verseKey")
    suspend fun deleteNote(verseKey: String)

    @Query("SELECT * FROM reading_history ORDER BY visitedAt DESC LIMIT 60")
    fun observeHistory(): Flow<List<ReadingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(entry: ReadingHistoryEntity)

    @Query("DELETE FROM reading_history")
    suspend fun clearHistory()

    // Offline Download State Persistence
    @Query("SELECT * FROM downloaded_books ORDER BY languageCode ASC, bookId ASC")
    fun observeDownloadedBooks(): Flow<List<DownloadedBookEntity>>

    @Query("SELECT * FROM downloaded_books")
    suspend fun getDownloadedBooksOnce(): List<DownloadedBookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownloadedBook(entity: DownloadedBookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownloadedBooks(entities: List<DownloadedBookEntity>)

    @Query("DELETE FROM downloaded_books WHERE languageCode = :languageCode AND bookId = :bookId")
    suspend fun deleteDownloadedBook(languageCode: String, bookId: Int)

    @Query("DELETE FROM downloaded_books WHERE languageCode = :languageCode")
    suspend fun deleteDownloadedLanguage(languageCode: String)
}

@Database(
    entities = [
        BookmarkEntity::class,
        HighlightEntity::class,
        NoteEntity::class,
        ReadingHistoryEntity::class,
        DownloadedBookEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class UserBibleDatabase : RoomDatabase() {
    abstract fun userBibleDao(): UserBibleDao

    companion object {
        @Volatile
        private var INSTANCE: UserBibleDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS downloaded_books (
                        languageCode TEXT NOT NULL,
                        bookId INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        bytesSize INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(languageCode, bookId)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): UserBibleDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    UserBibleDatabase::class.java,
                    "user_bible_annotations.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
