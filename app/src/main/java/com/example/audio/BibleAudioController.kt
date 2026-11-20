package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.AudioLanguage
import com.example.data.model.BibleBook
import com.example.data.model.BibleVerse
import com.example.data.repository.BibleRepository
import com.example.ui.localization.SystemLocaleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val isActiveSession: Boolean = false,
    val bookId: Int = 1,
    val bookNameEn: String = "Genesis",
    val bookNameNe: String = "उत्पत्ति",
    val chapter: Int = 1,
    val currentVerse: Int = 1,
    val totalVersesInChapter: Int = 0,
    val audioLanguage: AudioLanguage = AudioLanguage.BILINGUAL,
    val speed: Float = 1.0f,
    val isEnglishAvailable: Boolean = true,
    val isNepaliAvailable: Boolean = false,
    val isKoreanAvailable: Boolean = false,
    val preferOncvInBilingual: Boolean = false,
    val statusNote: String? = null
)

class BibleAudioController(
    private val context: Context,
    private val repository: BibleRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeak = false

    private var currentBook: BibleBook? = null
    private var currentVerses: List<BibleVerse> = emptyList()
    private var currentIndex: Int = 0
    private var bilingualPhase: Int = 0 // 0 = EN, 1 = NE

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
                val engine = tts ?: return@TextToSpeech
                val enAvail = engine.isLanguageAvailable(Locale.US) >= TextToSpeech.LANG_AVAILABLE ||
                    engine.isLanguageAvailable(Locale.ENGLISH) >= TextToSpeech.LANG_AVAILABLE
                val neLocale = Locale.forLanguageTag("ne-NP")
                val neAvail = engine.isLanguageAvailable(neLocale) >= TextToSpeech.LANG_AVAILABLE ||
                    engine.isLanguageAvailable(Locale.forLanguageTag("ne")) >= TextToSpeech.LANG_AVAILABLE
                val koAvail = engine.isLanguageAvailable(Locale.KOREAN) >= TextToSpeech.LANG_AVAILABLE ||
                    engine.isLanguageAvailable(Locale.KOREA) >= TextToSpeech.LANG_AVAILABLE

                _state.update {
                    it.copy(
                        isEnglishAvailable = enAvail,
                        isNepaliAvailable = neAvail,
                        isKoreanAvailable = koAvail
                    )
                }

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        scope.launch {
                            handleUtteranceFinished()
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        scope.launch {
                            handleUtteranceFinished()
                        }
                    }
                })

                if (pendingSpeak) {
                    pendingSpeak = false
                    speakCurrent()
                }
            }
        }
    }

    fun startChapterAudio(
        book: BibleBook,
        verses: List<BibleVerse>,
        startVerse: Int,
        language: AudioLanguage,
        speed: Float,
        preferOncvInBilingual: Boolean = false
    ) {
        if (verses.isEmpty()) return
        currentBook = book
        currentVerses = verses
        val idx = verses.indexOfFirst { it.verse == startVerse }.coerceAtLeast(0)
        currentIndex = idx
        bilingualPhase = if (language == AudioLanguage.NEPALI || language == AudioLanguage.ONCV) 1 else 0

        _state.update {
            it.copy(
                isPlaying = true,
                isActiveSession = true,
                bookId = book.id,
                bookNameEn = book.nameEn,
                bookNameNe = book.nameNe,
                chapter = verses[idx].chapter,
                currentVerse = verses[idx].verse,
                totalVersesInChapter = verses.size,
                audioLanguage = language,
                speed = speed,
                preferOncvInBilingual = preferOncvInBilingual,
                statusNote = buildVoiceNotice(language, it.isNepaliAvailable, it.isKoreanAvailable)
            )
        }

        startForegroundService()
        if (ttsReady) {
            speakCurrent()
        } else {
            pendingSpeak = true
        }
    }

    fun togglePlayPause() {
        val s = _state.value
        if (!s.isActiveSession) return
        if (s.isPlaying) {
            tts?.stop()
            _state.update { it.copy(isPlaying = false) }
            updateServiceNotification()
        } else {
            _state.update { it.copy(isPlaying = true) }
            speakCurrent()
            updateServiceNotification()
        }
    }

    fun nextVerse() {
        if (!_state.value.isActiveSession || currentVerses.isEmpty()) return
        tts?.stop()
        if (currentIndex + 1 < currentVerses.size) {
            currentIndex++
            bilingualPhase = 0
            updateStateForCurrentVerse()
            if (_state.value.isPlaying) speakCurrent()
        } else {
            advanceToNextChapter()
        }
    }

    fun previousVerse() {
        if (!_state.value.isActiveSession || currentVerses.isEmpty()) return
        tts?.stop()
        if (currentIndex > 0) {
            currentIndex--
            bilingualPhase = 0
            updateStateForCurrentVerse()
            if (_state.value.isPlaying) speakCurrent()
        }
    }

    fun setLanguage(language: AudioLanguage) {
        _state.update {
            it.copy(
                audioLanguage = language,
                statusNote = buildVoiceNotice(language, it.isNepaliAvailable, it.isKoreanAvailable)
            )
        }
        if (_state.value.isPlaying) {
            tts?.stop()
            bilingualPhase = if (language == AudioLanguage.NEPALI || language == AudioLanguage.ONCV) 1 else 0
            speakCurrent()
        }
        updateServiceNotification()
    }

    fun setSpeed(speed: Float) {
        _state.update { it.copy(speed = speed) }
        if (_state.value.isPlaying) {
            tts?.stop()
            speakCurrent()
        }
    }

    fun stopAudio() {
        pendingSpeak = false
        tts?.stop()
        _state.update { it.copy(isPlaying = false, isActiveSession = false, statusNote = null) }
        val intent = Intent(context, BibleAudioService::class.java).apply {
            action = BibleAudioService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun refreshLocalizedStatus() {
        if (!_state.value.isActiveSession) return
        _state.update {
            it.copy(statusNote = buildVoiceNotice(it.audioLanguage, it.isNepaliAvailable, it.isKoreanAvailable))
        }
        updateServiceNotification()
    }

    private fun buildVoiceNotice(
        language: AudioLanguage,
        nepaliAvail: Boolean,
        koreanAvail: Boolean
    ): String? {
        val strings = SystemLocaleManager.strings()
        return when {
            !nepaliAvail && (language == AudioLanguage.NEPALI || language == AudioLanguage.ONCV || language == AudioLanguage.BILINGUAL) ->
                strings.ttsNepaliMissingNotice
            !koreanAvail && language == AudioLanguage.KOREAN ->
                strings.ttsKoreanMissingNotice
            else -> null
        }
    }

    private fun updateStateForCurrentVerse() {
        val verse = currentVerses.getOrNull(currentIndex) ?: return
        _state.update {
            it.copy(
                chapter = verse.chapter,
                currentVerse = verse.verse,
                totalVersesInChapter = currentVerses.size
            )
        }
        updateServiceNotification()
    }

    private fun handleUtteranceFinished() {
        if (!_state.value.isPlaying) return
        val lang = _state.value.audioLanguage
        if (lang == AudioLanguage.BILINGUAL && bilingualPhase == 0) {
            bilingualPhase = 1
            speakCurrent()
            return
        }

        bilingualPhase = 0
        if (currentIndex + 1 < currentVerses.size) {
            currentIndex++
            updateStateForCurrentVerse()
            speakCurrent()
        } else {
            advanceToNextChapter()
        }
    }

    private fun advanceToNextChapter() {
        val book = currentBook ?: return
        val currentChap = currentVerses.firstOrNull()?.chapter ?: _state.value.chapter
        scope.launch {
            var nextBook = book
            var nextChap = currentChap + 1
            if (nextChap > book.chapterCount) {
                val nextId = if (book.id < 66) book.id + 1 else 1
                nextBook = repository.getBook(nextId) ?: return@launch
                nextChap = 1
            }
            val nextVerses = repository.getChapterVerses(nextBook.id, nextChap)
            if (nextVerses.isNotEmpty()) {
                currentBook = nextBook
                currentVerses = nextVerses
                currentIndex = 0
                bilingualPhase = 0
                _state.update {
                    it.copy(
                        bookId = nextBook.id,
                        bookNameEn = nextBook.nameEn,
                        bookNameNe = nextBook.nameNe,
                        chapter = nextChap,
                        currentVerse = nextVerses[0].verse,
                        totalVersesInChapter = nextVerses.size
                    )
                }
                updateServiceNotification()
                if (_state.value.isPlaying) speakCurrent()
            } else {
                stopAudio()
            }
        }
    }

    private fun speakCurrent() {
        val engine = tts ?: return
        if (!ttsReady || !_state.value.isPlaying) return
        val verse = currentVerses.getOrNull(currentIndex) ?: return

        val langMode = _state.value.audioLanguage
        val textToSpeak: String
        val targetLocale: Locale

        when (langMode) {
            AudioLanguage.KOREAN -> {
                textToSpeak = verse.textKo
                targetLocale = if (engine.isLanguageAvailable(Locale.KOREAN) >= TextToSpeech.LANG_AVAILABLE) {
                    Locale.KOREAN
                } else {
                    Locale.US
                }
            }
            AudioLanguage.NEPALI -> {
                textToSpeak = verse.textNe.ifBlank { verse.textOncv }
                val neNp = Locale.forLanguageTag("ne-NP")
                targetLocale = if (engine.isLanguageAvailable(neNp) >= TextToSpeech.LANG_AVAILABLE) {
                    neNp
                } else {
                    val hiIn = Locale.forLanguageTag("hi-IN")
                    if (engine.isLanguageAvailable(hiIn) >= TextToSpeech.LANG_AVAILABLE) hiIn else Locale.US
                }
            }
            AudioLanguage.ONCV -> {
                textToSpeak = verse.textOncv.ifBlank { verse.textNe }
                val neNp = Locale.forLanguageTag("ne-NP")
                targetLocale = if (engine.isLanguageAvailable(neNp) >= TextToSpeech.LANG_AVAILABLE) {
                    neNp
                } else {
                    val hiIn = Locale.forLanguageTag("hi-IN")
                    if (engine.isLanguageAvailable(hiIn) >= TextToSpeech.LANG_AVAILABLE) hiIn else Locale.US
                }
            }
            AudioLanguage.ENGLISH -> {
                textToSpeak = verse.textEn
                targetLocale = Locale.US
            }
            AudioLanguage.BILINGUAL -> {
                val speakNepali = bilingualPhase == 1
                val nepaliText = if (_state.value.preferOncvInBilingual) {
                    verse.textOncv.ifBlank { verse.textNe }
                } else {
                    verse.textNe.ifBlank { verse.textOncv }
                }
                textToSpeak = if (speakNepali) nepaliText else verse.textEn
                targetLocale = if (speakNepali) {
                    val neNp = Locale.forLanguageTag("ne-NP")
                    if (engine.isLanguageAvailable(neNp) >= TextToSpeech.LANG_AVAILABLE) {
                        neNp
                    } else {
                        val hiIn = Locale.forLanguageTag("hi-IN")
                        if (engine.isLanguageAvailable(hiIn) >= TextToSpeech.LANG_AVAILABLE) hiIn else Locale.US
                    }
                } else {
                    Locale.US
                }
            }
        }

        if (textToSpeak.isBlank()) {
            handleUtteranceFinished()
            return
        }

        engine.language = targetLocale
        engine.setSpeechRate(_state.value.speed)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "verse_${verse.key}_$bilingualPhase")
        }
        engine.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "verse_${verse.key}_$bilingualPhase")
    }

    private fun startForegroundService() {
        val intent = Intent(context, BibleAudioService::class.java).apply {
            action = BibleAudioService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun updateServiceNotification() {
        if (!_state.value.isActiveSession) return
        val intent = Intent(context, BibleAudioService::class.java).apply {
            action = BibleAudioService.ACTION_UPDATE
        }
        context.startService(intent)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
