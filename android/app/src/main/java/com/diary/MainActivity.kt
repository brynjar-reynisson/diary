package com.diary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.diary.data.DropboxRepository
import com.diary.data.GoogleDriveRepository
import com.diary.ui.DiaryNavHost
import com.diary.ui.theme.DiaryTheme
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class MainActivity : ComponentActivity() {

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            if (account != null) {
                DiaryApplication.activeProvider = "google"
                DiaryApplication.repository = GoogleDriveRepository(this, account)
                googleSignInSuccess?.invoke()
            }
        } catch (e: Exception) {
            googleSignInError?.invoke(e.message ?: "Google sign-in failed")
        }
    }

    var googleSignInSuccess: (() -> Unit)? = null
    var googleSignInError: ((String) -> Unit)? = null

    fun startGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        val client = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(client.signInIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore Dropbox token if available
        val storedToken = (application as DiaryApplication).loadDropboxToken()
        if (storedToken != null && DiaryApplication.activeProvider == null) {
            DiaryApplication.activeProvider = "dropbox"
            DiaryApplication.repository = DropboxRepository(storedToken)
        }

        // Handle Dropbox deep-link intent on cold start
        handleIntent(intent)

        setContent {
            DiaryTheme {
                DiaryNavHost(activity = this)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "diaryapp" && data.host == "dropbox-auth") {
            val code = data.getQueryParameter("code") ?: return
            // Handled asynchronously in ProviderScreen via DropboxRepository.exchangeCode()
            pendingDropboxCode = code
        }
    }

    companion object {
        var pendingDropboxCode: String? = null
    }
}
