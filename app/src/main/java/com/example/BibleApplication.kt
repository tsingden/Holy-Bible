package com.example

import android.app.Application
import com.example.audio.BibleAudioController
import com.example.audio.PaperSoundPlayer
import com.example.data.db.BibleAssetDatabase
import com.example.data.db.UserBibleDatabase
import com.example.data.download.BibleDownloadManager
import com.example.data.prefs.BiblePreferences
import com.example.data.repository.BibleRepository

class BibleApplication : Application() {

    lateinit var repository: BibleRepository
        private set

    lateinit var downloadManager: BibleDownloadManager
        private set

    lateinit var paperSoundPlayer: PaperSoundPlayer
        private set

    lateinit var audioController: BibleAudioController
        private set

    override fun onCreate() {
        super.onCreate()
        val assetDb = BibleAssetDatabase(this)
        val userDb = UserBibleDatabase.getInstance(this)
        val prefs = BiblePreferences(this)
        val dao = userDb.userBibleDao()

        downloadManager = BibleDownloadManager(assetDb, dao)
        repository = BibleRepository(assetDb, dao, prefs, downloadManager)
        paperSoundPlayer = PaperSoundPlayer(this)
        audioController = BibleAudioController(this, repository)
    }

    override fun onTerminate() {
        paperSoundPlayer.release()
        audioController.release()
        super.onTerminate()
    }
}
