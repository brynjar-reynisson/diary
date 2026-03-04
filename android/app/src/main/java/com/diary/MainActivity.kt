package com.diary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.diary.data.DropboxRepository
import com.diary.data.GoogleDriveRepository
import com.diary.ui.DiaryNavHost
import com.diary.ui.theme.DiaryTheme
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Handles the consent screen shown when Drive access hasn't been granted yet
    private val authorizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val authResult = Identity.getAuthorizationClient(this)
                .getAuthorizationResultFromIntent(result.data)
            val token = authResult.accessToken
            if (token != null) {
                DiaryApplication.activeProvider = "google"
                DiaryApplication.repository = GoogleDriveRepository(token)
                googleAuthSuccess?.invoke()
            } else {
                googleAuthError?.invoke("No access token received")
            }
        } else {
            googleAuthError?.invoke("Authorization cancelled")
        }
    }

    var googleAuthSuccess: (() -> Unit)? = null
    var googleAuthError: ((String) -> Unit)? = null

    fun startGoogleSignIn() {
        val serverClientId = getString(R.string.default_web_client_id)
        val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val credentialManager = CredentialManager.create(this)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Step 1: authenticate (who the user is)
                credentialManager.getCredential(this@MainActivity, request)
                // Step 2: authorize Drive access (what the app can do)
                requestDriveAuthorization()
            } catch (e: GetCredentialException) {
                googleAuthError?.invoke(e.message ?: "Sign-in failed")
            }
        }
    }

    private fun requestDriveAuthorization() {
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_FILE)))
            .build()

        Identity.getAuthorizationClient(this)
            .authorize(authorizationRequest)
            .addOnSuccessListener { authResult ->
                if (authResult.hasResolution()) {
                    // User needs to approve Drive access — show consent screen
                    authorizationLauncher.launch(
                        IntentSenderRequest.Builder(authResult.pendingIntent!!.intentSender).build()
                    )
                } else {
                    // Already authorized — use token directly
                    val token = authResult.accessToken
                    if (token != null) {
                        DiaryApplication.activeProvider = "google"
                        DiaryApplication.repository = GoogleDriveRepository(token)
                        googleAuthSuccess?.invoke()
                    } else {
                        googleAuthError?.invoke("No access token received")
                    }
                }
            }
            .addOnFailureListener { e ->
                googleAuthError?.invoke(e.message ?: "Drive authorization failed")
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore Dropbox token if available
        val storedToken = (application as DiaryApplication).loadDropboxToken()
        if (storedToken != null && DiaryApplication.activeProvider == null) {
            DiaryApplication.activeProvider = "dropbox"
            DiaryApplication.repository = DropboxRepository(storedToken)
        }

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
            pendingDropboxCode = code
        }
    }

    companion object {
        var pendingDropboxCode: String? = null
    }
}
