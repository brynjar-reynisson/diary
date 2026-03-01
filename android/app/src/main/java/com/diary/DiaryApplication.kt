package com.diary

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun getPrefs(): SharedPreferences =
        getSharedPreferences("diary_prefs", Context.MODE_PRIVATE)

    fun saveDropboxToken(token: String) {
        getPrefs().edit().putString("dropbox_token", token).apply()
    }

    fun loadDropboxToken(): String? = getPrefs().getString("dropbox_token", null)

    fun clearDropboxToken() {
        getPrefs().edit().remove("dropbox_token").apply()
    }
}
