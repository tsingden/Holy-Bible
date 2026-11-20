package com.example.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.session.MediaSession
import android.os.Build
import android.os.IBinder
import com.example.BibleApplication
import com.example.MainActivity
import com.example.R
import com.example.data.model.SystemLanguage
import com.example.ui.localization.SystemLocaleManager
import com.example.util.NepaliFormatUtils

class BibleAudioService : Service() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaSession = MediaSession(this, "BibleAudioSession").apply {
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val controller = (application as BibleApplication).audioController
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, buildNotification(controller.state.value))
            }
            ACTION_UPDATE -> {
                val nm = getSystemService(NotificationManager::class.java)
                nm?.notify(NOTIFICATION_ID, buildNotification(controller.state.value))
            }
            ACTION_TOGGLE -> {
                controller.togglePlayPause()
                val nm = getSystemService(NotificationManager::class.java)
                nm?.notify(NOTIFICATION_ID, buildNotification(controller.state.value))
            }
            ACTION_NEXT -> controller.nextVerse()
            ACTION_PREV -> controller.previousVerse()
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(state: AudioPlaybackState): Notification {
        createNotificationChannel()
        val sysLang = SystemLocaleManager.currentLanguage
        val strings = SystemLocaleManager.strings(sysLang)

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevPending = servicePendingIntent(ACTION_PREV, 1)
        val togglePending = servicePendingIntent(ACTION_TOGGLE, 2)
        val nextPending = servicePendingIntent(ACTION_NEXT, 3)
        val stopPending = servicePendingIntent(ACTION_STOP, 4)

        val playPauseIcon = if (state.isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseLabel = if (state.isPlaying) strings.audioNotifPause else strings.audioNotifPlay

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        val chStr = NepaliFormatUtils.formatNumber(state.chapter, sysLang)
        val vStr = NepaliFormatUtils.formatNumber(state.currentVerse, sysLang)
        val notifTitle = if (sysLang == SystemLanguage.NEPALI) {
            "${state.bookNameNe} $chStr:$vStr"
        } else {
            "${state.bookNameEn} $chStr:$vStr"
        }
        val secondaryName = if (sysLang == SystemLanguage.NEPALI) state.bookNameEn else state.bookNameNe

        return builder
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notifTitle)
            .setContentText("$secondaryName • ${strings.audioLanguageName(state.audioLanguage)}")
            .setContentIntent(openAppIntent)
            .setOngoing(state.isPlaying)
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_previous, strings.audioNotifPrevious, prevPending).build())
            .addAction(Notification.Action.Builder(playPauseIcon, playPauseLabel, togglePending).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_next, strings.audioNotifNext, nextPending).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, strings.audioNotifStop, stopPending).build())
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    private fun servicePendingIntent(action: String, reqCode: Int): PendingIntent {
        val intent = Intent(this, BibleAudioService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            reqCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val strings = SystemLocaleManager.strings()
            val channel = NotificationChannel(
                CHANNEL_ID,
                strings.audioNotifChannelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = strings.audioNotifChannelDesc
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "bible_audio_channel"
        const val NOTIFICATION_ID = 7001
        const val ACTION_START = "com.example.audio.START"
        const val ACTION_UPDATE = "com.example.audio.UPDATE"
        const val ACTION_TOGGLE = "com.example.audio.TOGGLE"
        const val ACTION_NEXT = "com.example.audio.NEXT"
        const val ACTION_PREV = "com.example.audio.PREV"
        const val ACTION_STOP = "com.example.audio.STOP"
    }
}
