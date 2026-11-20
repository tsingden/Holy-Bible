package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R
import com.example.data.model.PageTurnSoundStyle
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class PaperSoundPlayer(context: Context) {

    private val soundPool: SoundPool
    private val styleSamplePairs = HashMap<PageTurnSoundStyle, Pair<Int, Int>>()
    private val loadedSampleIds = ConcurrentHashMap.newKeySet<Int>()

    @Volatile
    var enabled: Boolean = true

    @Volatile
    var soundStyle: PageTurnSoundStyle = PageTurnSoundStyle.REALISTIC_BIBLE

    @Volatile
    var volume: Float = 0.72f

    @Volatile
    private var lastRandomStyle: PageTurnSoundStyle? = null

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSampleIds.add(sampleId)
            }
        }

        loadStyle(context, PageTurnSoundStyle.REALISTIC_BIBLE, R.raw.page_flip_forward, R.raw.page_flip_backward)
        loadStyle(context, PageTurnSoundStyle.SOFT_PAPER, R.raw.page_soft_paper_fwd, R.raw.page_soft_paper_bwd)
        loadStyle(context, PageTurnSoundStyle.OLD_VINTAGE_BIBLE, R.raw.page_old_bible_fwd, R.raw.page_old_bible_bwd)
        loadStyle(context, PageTurnSoundStyle.GENTLE_RUSTLE, R.raw.page_gentle_rustle_fwd, R.raw.page_gentle_rustle_bwd)
        loadStyle(context, PageTurnSoundStyle.CLASSIC_HARDCOVER, R.raw.page_classic_hardcover_fwd, R.raw.page_classic_hardcover_bwd)
        loadStyle(context, PageTurnSoundStyle.FAST_FLIP, R.raw.page_fast_flip_fwd, R.raw.page_fast_flip_bwd)
        loadStyle(context, PageTurnSoundStyle.SLOW_SOFT, R.raw.page_slow_soft_fwd, R.raw.page_slow_soft_bwd)
        loadStyle(context, PageTurnSoundStyle.ANTIQUE_PARCHMENT, R.raw.page_antique_parchment_fwd, R.raw.page_antique_parchment_bwd)
        loadStyle(context, PageTurnSoundStyle.NATURAL_MOVEMENT, R.raw.page_natural_movement_fwd, R.raw.page_natural_movement_bwd)
        loadStyle(context, PageTurnSoundStyle.SOFT_DIGITAL, R.raw.page_soft_digital_fwd, R.raw.page_soft_digital_bwd)
    }

    private fun loadStyle(
        context: Context,
        style: PageTurnSoundStyle,
        forwardResId: Int,
        backwardResId: Int
    ) {
        val fwdId = runCatching { soundPool.load(context, forwardResId, 1) }.getOrDefault(0)
        val bwdId = runCatching { soundPool.load(context, backwardResId, 1) }.getOrDefault(0)
        styleSamplePairs[style] = fwdId to bwdId
    }

    fun playForward() {
        playTurn(forward = true)
    }

    fun playBackward() {
        playTurn(forward = false)
    }

    fun preview(style: PageTurnSoundStyle, forward: Boolean = true) {
        if (!style.isAudible) return
        val resolved = resolvePlayableStyle(style)
        val previewGain = volume.coerceIn(0.28f, 1.0f)
        playSample(resolved, forward = forward, baseGain = previewGain)
    }

    private fun playTurn(forward: Boolean) {
        val currentStyle = soundStyle
        if (!enabled || !currentStyle.isAudible) return
        val baseGain = volume.coerceIn(0f, 1f)
        if (baseGain <= 0.01f) return
        val resolved = resolvePlayableStyle(currentStyle)
        playSample(resolved, forward = forward, baseGain = baseGain)
    }

    private fun resolvePlayableStyle(style: PageTurnSoundStyle): PageTurnSoundStyle {
        if (style != PageTurnSoundStyle.RANDOM) return style
        val pool = PageTurnSoundStyle.audibleStyles
        val candidates = if (pool.size > 1 && lastRandomStyle != null) {
            pool.filter { it != lastRandomStyle }
        } else {
            pool
        }
        val picked = candidates[Random.nextInt(candidates.size)]
        lastRandomStyle = picked
        return picked
    }

    private fun playSample(style: PageTurnSoundStyle, forward: Boolean, baseGain: Float) {
        val pair = styleSamplePairs[style] ?: styleSamplePairs[PageTurnSoundStyle.REALISTIC_BIBLE] ?: return
        val sampleId = if (forward) pair.first else pair.second
        if (sampleId == 0 || !loadedSampleIds.contains(sampleId)) return

        // Subtle organic jitter so repeated turns never sound mechanical or identical
        val rate = if (forward) {
            0.97f + Random.nextFloat() * 0.06f
        } else {
            0.96f + Random.nextFloat() * 0.06f
        }
        val organicGain = (baseGain * (0.93f + Random.nextFloat() * 0.09f)).coerceIn(0f, 1f)
        soundPool.play(sampleId, organicGain, organicGain, 1, 0, rate)
    }

    fun release() {
        soundPool.release()
    }
}
