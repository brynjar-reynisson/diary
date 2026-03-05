package com.diary

import android.app.Application
import android.content.SharedPreferences
import com.diary.data.DiaryRepository
import com.diary.data.DropboxRepository
import com.diary.data.GoogleDriveRepository

class DiaryApplication : Application() {

    companion object {
        lateinit var instance: DiaryApplication
            private set

        // In-memory active provider tag: "google" or "dropbox"
        var activeProvider: String? = null
        var repository: DiaryRepository? = null
    }

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("diary_prefs", MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun saveDropboxToken(token: String) {
        prefs.edit().putString("dropbox_token", token).apply()
    }

    fun loadDropboxToken(): String? = prefs.getString("dropbox_token", null)

    fun clearDropboxToken() {
        prefs.edit().remove("dropbox_token").apply()
    }
}
