package com.example.data.download

import com.example.data.db.BibleAssetDatabase
import com.example.data.db.DownloadedBookEntity
import com.example.data.db.UserBibleDao
import com.example.data.model.BibleLanguage
import com.example.data.model.BookDownloadState
import com.example.data.model.DownloadStatus
import com.example.data.model.LanguageDownloadSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield

class BibleDownloadManager(
    private val assetDb: BibleAssetDatabase,
    private val userDao: UserBibleDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queueMutex = Mutex()
    private var workerJob: Job? = null
    private var initialized = false

    private val _totalStorageBytes = MutableStateFlow(0L)
    val totalStorageBytes: StateFlow<Long> = _totalStorageBytes.asStateFlow()

    private val _dataVersion = MutableStateFlow(0)
    val dataVersion: StateFlow<Int> = _dataVersion.asStateFlow()

    val bookStates: StateFlow<Map<Pair<String, Int>, BookDownloadState>> =
        userDao.observeDownloadedBooks()
            .map { list ->
                list.associate { entity ->
                    val status = runCatching { DownloadStatus.valueOf(entity.status) }
                        .getOrDefault(DownloadStatus.NOT_DOWNLOADED)
                    (entity.languageCode to entity.bookId) to BookDownloadState(
                        languageCode = entity.languageCode,
                        bookId = entity.bookId,
                        status = status,
                        bytesSize = entity.bytesSize
                    )
                }
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    val downloadedBookKeys: StateFlow<Set<Pair<String, Int>>> =
        bookStates
            .map { map ->
                map.filterValues { it.status == DownloadStatus.DOWNLOADED }.keys
            }
            .stateIn(scope, SharingStarted.Eagerly, emptySet())

    val languageSummaries: StateFlow<List<LanguageDownloadSummary>> =
        bookStates
            .combine(totalStorageBytes) { states, _ ->
                BibleLanguage.entries.map { lang ->
                    val supported = lang.supportedBookIds
                    var downloadedCount = 0
                    var queuedOrActive = 0
                    var downloading = 0
                    var paused = 0
                    var totalBytes = 0L

                    for (bookId in supported) {
                        val st = states[lang.code to bookId]
                        when (st?.status) {
                            DownloadStatus.DOWNLOADED -> {
                                downloadedCount++
                                totalBytes += st.bytesSize
                            }
                            DownloadStatus.DOWNLOADING -> {
                                queuedOrActive++
                                downloading++
                            }
                            DownloadStatus.QUEUED -> {
                                queuedOrActive++
                            }
                            DownloadStatus.PAUSED -> {
                                paused++
                            }
                            else -> Unit
                        }
                    }

                    LanguageDownloadSummary(
                        language = lang,
                        totalBooks = supported.size,
                        downloadedBooks = downloadedCount,
                        queuedOrActiveBooks = queuedOrActive,
                        isDownloading = downloading > 0 || queuedOrActive > 0,
                        isPaused = paused > 0 && downloading == 0 && queuedOrActive == 0,
                        totalBytesOnDisk = totalBytes
                    )
                }
            }
            .stateIn(scope, SharingStarted.Eagerly, defaultSummaries())

    suspend fun initialize(
        seedDefaultsIfEmpty: Boolean = true,
        seedOncvIfMissing: Boolean = true
    ) {
        queueMutex.withLock {
            if (initialized) return
            assetDb.warmup()
            val existing = userDao.getDownloadedBooksOnce()
            if (existing.isEmpty() && seedDefaultsIfEmpty) {
                // Pre-install core English (KJV), Nepali (ULB), and Nepali (ONCV) offline packs on first launch
                val enIds = BibleLanguage.ENGLISH.supportedBookIds.toSet()
                val enSizes = assetDb.installBooksFromPack(BibleLanguage.ENGLISH, enIds)
                val neIds = BibleLanguage.NEPALI.supportedBookIds.toSet()
                val neSizes = assetDb.installBooksFromPack(BibleLanguage.NEPALI, neIds)
                val oncvIds = BibleLanguage.ONCV.supportedBookIds.toSet()
                val oncvSizes = assetDb.installBooksFromPack(BibleLanguage.ONCV, oncvIds)

                val now = System.currentTimeMillis()
                val entities = buildList {
                    for (bookId in enIds) {
                        add(
                            DownloadedBookEntity(
                                languageCode = BibleLanguage.ENGLISH.code,
                                bookId = bookId,
                                status = DownloadStatus.DOWNLOADED.name,
                                bytesSize = enSizes[bookId] ?: 0L,
                                updatedAt = now
                            )
                        )
                    }
                    for (bookId in neIds) {
                        add(
                            DownloadedBookEntity(
                                languageCode = BibleLanguage.NEPALI.code,
                                bookId = bookId,
                                status = DownloadStatus.DOWNLOADED.name,
                                bytesSize = neSizes[bookId] ?: 0L,
                                updatedAt = now
                            )
                        )
                    }
                    for (bookId in oncvIds) {
                        add(
                            DownloadedBookEntity(
                                languageCode = BibleLanguage.ONCV.code,
                                bookId = bookId,
                                status = DownloadStatus.DOWNLOADED.name,
                                bytesSize = oncvSizes[bookId] ?: 0L,
                                updatedAt = now
                            )
                        )
                    }
                }
                userDao.upsertDownloadedBooks(entities)
            } else {
                // Remove obsolete language entries from previous versions (he, el, taj)
                val validCodes = BibleLanguage.entries.map { it.code }.toSet()
                val obsoleteCodes = existing.map { it.languageCode }.distinct().filter { it !in validCodes }
                for (code in obsoleteCodes) {
                    userDao.deleteDownloadedLanguage(code)
                }

                // Seed ONCV once for existing users upgrading from prior version
                val hasOncv = existing.any { it.languageCode == BibleLanguage.ONCV.code }
                if (seedOncvIfMissing && !hasOncv) {
                    val oncvIds = BibleLanguage.ONCV.supportedBookIds.toSet()
                    val oncvSizes = assetDb.installBooksFromPack(BibleLanguage.ONCV, oncvIds)
                    val now = System.currentTimeMillis()
                    userDao.upsertDownloadedBooks(
                        oncvIds.map { bookId ->
                            DownloadedBookEntity(
                                languageCode = BibleLanguage.ONCV.code,
                                bookId = bookId,
                                status = DownloadStatus.DOWNLOADED.name,
                                bytesSize = oncvSizes[bookId] ?: 0L,
                                updatedAt = now
                            )
                        }
                    )
                }

                // Reset any interrupted DOWNLOADING entries from a previous process back to QUEUED
                val interrupted = existing.filter {
                    it.languageCode in validCodes && it.status == DownloadStatus.DOWNLOADING.name
                }
                if (interrupted.isNotEmpty()) {
                    userDao.upsertDownloadedBooks(
                        interrupted.map { it.copy(status = DownloadStatus.QUEUED.name) }
                    )
                }
            }
            _totalStorageBytes.value = assetDb.getDatabaseFileSizeBytes()
            _dataVersion.value += 1
            initialized = true
        }
        startQueueIfNeeded()
    }

    suspend fun downloadCompleteBible() {
        initialize(seedDefaultsIfEmpty = false)
        val existing = userDao.getDownloadedBooksOnce().associateBy { it.languageCode to it.bookId }
        val toQueue = mutableListOf<DownloadedBookEntity>()
        val now = System.currentTimeMillis()
        for (lang in BibleLanguage.entries) {
            for (bookId in lang.supportedBookIds) {
                val cur = existing[lang.code to bookId]
                if (cur == null || cur.status != DownloadStatus.DOWNLOADED.name) {
                    toQueue.add(
                        DownloadedBookEntity(
                            languageCode = lang.code,
                            bookId = bookId,
                            status = DownloadStatus.QUEUED.name,
                            bytesSize = cur?.bytesSize ?: 0L,
                            updatedAt = now
                        )
                    )
                }
            }
        }
        if (toQueue.isNotEmpty()) {
            userDao.upsertDownloadedBooks(toQueue)
            startQueueIfNeeded()
        }
    }

    suspend fun downloadLanguage(language: BibleLanguage) {
        initialize(seedDefaultsIfEmpty = false)
        val existing = userDao.getDownloadedBooksOnce().associateBy { it.languageCode to it.bookId }
        val toQueue = mutableListOf<DownloadedBookEntity>()
        val now = System.currentTimeMillis()
        for (bookId in language.supportedBookIds) {
            val cur = existing[language.code to bookId]
            if (cur == null || cur.status != DownloadStatus.DOWNLOADED.name) {
                toQueue.add(
                    DownloadedBookEntity(
                        languageCode = language.code,
                        bookId = bookId,
                        status = DownloadStatus.QUEUED.name,
                        bytesSize = cur?.bytesSize ?: 0L,
                        updatedAt = now
                    )
                )
            }
        }
        if (toQueue.isNotEmpty()) {
            userDao.upsertDownloadedBooks(toQueue)
            startQueueIfNeeded()
        }
    }

    suspend fun downloadBook(language: BibleLanguage, bookId: Int) {
        if (!language.supportsBook(bookId)) return
        initialize(seedDefaultsIfEmpty = false)
        val existing = userDao.getDownloadedBooksOnce()
            .firstOrNull { it.languageCode == language.code && it.bookId == bookId }
        if (existing?.status == DownloadStatus.DOWNLOADED.name) return

        userDao.upsertDownloadedBook(
            DownloadedBookEntity(
                languageCode = language.code,
                bookId = bookId,
                status = DownloadStatus.QUEUED.name,
                bytesSize = 0L
            )
        )
        startQueueIfNeeded()
    }

    suspend fun pauseLanguage(language: BibleLanguage) {
        val existing = userDao.getDownloadedBooksOnce()
        val toPause = existing.filter {
            it.languageCode == language.code &&
                (it.status == DownloadStatus.QUEUED.name || it.status == DownloadStatus.DOWNLOADING.name)
        }
        if (toPause.isNotEmpty()) {
            userDao.upsertDownloadedBooks(
                toPause.map { it.copy(status = DownloadStatus.PAUSED.name, updatedAt = System.currentTimeMillis()) }
            )
        }
    }

    suspend fun pauseAll() {
        val existing = userDao.getDownloadedBooksOnce()
        val toPause = existing.filter {
            it.status == DownloadStatus.QUEUED.name || it.status == DownloadStatus.DOWNLOADING.name
        }
        if (toPause.isNotEmpty()) {
            userDao.upsertDownloadedBooks(
                toPause.map { it.copy(status = DownloadStatus.PAUSED.name, updatedAt = System.currentTimeMillis()) }
            )
        }
    }

    suspend fun resumeLanguage(language: BibleLanguage) {
        val existing = userDao.getDownloadedBooksOnce()
        val toResume = existing.filter {
            it.languageCode == language.code && it.status == DownloadStatus.PAUSED.name
        }
        if (toResume.isNotEmpty()) {
            userDao.upsertDownloadedBooks(
                toResume.map { it.copy(status = DownloadStatus.QUEUED.name, updatedAt = System.currentTimeMillis()) }
            )
            startQueueIfNeeded()
        }
    }

    suspend fun resumeAll() {
        val existing = userDao.getDownloadedBooksOnce()
        val toResume = existing.filter { it.status == DownloadStatus.PAUSED.name }
        if (toResume.isNotEmpty()) {
            userDao.upsertDownloadedBooks(
                toResume.map { it.copy(status = DownloadStatus.QUEUED.name, updatedAt = System.currentTimeMillis()) }
            )
            startQueueIfNeeded()
        }
    }

    suspend fun cancelLanguage(language: BibleLanguage) {
        val existing = userDao.getDownloadedBooksOnce()
        val active = existing.filter {
            it.languageCode == language.code && it.status != DownloadStatus.DOWNLOADED.name
        }
        for (item in active) {
            userDao.deleteDownloadedBook(item.languageCode, item.bookId)
        }
    }

    suspend fun cancelBook(language: BibleLanguage, bookId: Int) {
        val existing = userDao.getDownloadedBooksOnce()
            .firstOrNull { it.languageCode == language.code && it.bookId == bookId }
        if (existing != null && existing.status != DownloadStatus.DOWNLOADED.name) {
            userDao.deleteDownloadedBook(language.code, bookId)
        }
    }

    suspend fun deleteLanguage(language: BibleLanguage) {
        pauseLanguage(language)
        queueMutex.withLock {
            assetDb.removeLanguage(language)
            userDao.deleteDownloadedLanguage(language.code)
            _totalStorageBytes.value = assetDb.getDatabaseFileSizeBytes()
            _dataVersion.value += 1
        }
    }

    suspend fun deleteBook(language: BibleLanguage, bookId: Int) {
        queueMutex.withLock {
            assetDb.removeBook(language, bookId)
            userDao.deleteDownloadedBook(language.code, bookId)
            _totalStorageBytes.value = assetDb.getDatabaseFileSizeBytes()
            _dataVersion.value += 1
        }
    }

    private fun startQueueIfNeeded() {
        if (workerJob?.isActive == true) return
        workerJob = scope.launch {
            processQueue()
        }
    }

    private suspend fun processQueue() {
        while (currentCoroutineContext().isActive) {
            val next = userDao.getDownloadedBooksOnce()
                .firstOrNull { it.status == DownloadStatus.QUEUED.name }
                ?: break

            val lang = BibleLanguage.fromCode(next.languageCode)
            userDao.upsertDownloadedBook(
                next.copy(status = DownloadStatus.DOWNLOADING.name, updatedAt = System.currentTimeMillis())
            )

            val installedBytes = queueMutex.withLock {
                // Re-check status in case paused or cancelled right before acquiring lock
                val fresh = userDao.getDownloadedBooksOnce()
                    .firstOrNull { it.languageCode == next.languageCode && it.bookId == next.bookId }
                if (fresh == null || fresh.status == DownloadStatus.PAUSED.name) {
                    return@withLock null
                }
                val bytes = assetDb.installBookFromPack(lang, next.bookId)
                // Re-check once more before marking downloaded
                val postCheck = userDao.getDownloadedBooksOnce()
                    .firstOrNull { it.languageCode == next.languageCode && it.bookId == next.bookId }
                if (postCheck == null || postCheck.status == DownloadStatus.PAUSED.name) {
                    assetDb.removeBook(lang, next.bookId)
                    return@withLock null
                }
                userDao.upsertDownloadedBook(
                    DownloadedBookEntity(
                        languageCode = next.languageCode,
                        bookId = next.bookId,
                        status = DownloadStatus.DOWNLOADED.name,
                        bytesSize = bytes,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                _totalStorageBytes.value = assetDb.getDatabaseFileSizeBytes()
                _dataVersion.value += 1
                bytes
            }

            if (installedBytes != null) {
                yield()
            }
        }
    }

    private fun defaultSummaries(): List<LanguageDownloadSummary> {
        return BibleLanguage.entries.map { lang ->
            LanguageDownloadSummary(
                language = lang,
                totalBooks = lang.supportedBookIds.size,
                downloadedBooks = 0,
                queuedOrActiveBooks = 0,
                isDownloading = false,
                isPaused = false,
                totalBytesOnDisk = 0L
            )
        }
    }
}
