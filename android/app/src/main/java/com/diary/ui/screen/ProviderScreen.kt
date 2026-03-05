package com.diary.ui.screen

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.DiaryApplication
import com.diary.MainActivity
import com.diary.data.DropboxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// Replace with your Dropbox app key
private const val DROPBOX_APP_KEY = "YOUR_DROPBOX_APP_KEY"
private const val DROPBOX_REDIRECT_URI = "diaryapp://dropbox-auth"

@Composable
fun ProviderScreen(
    activity: MainActivity,
    onAuthenticated: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Check for pending Dropbox code on resume (after deep link callback)
    LaunchedEffect(Unit) {
        val code = MainActivity.pendingDropboxCode
        if (code != null) {
            MainActivity.pendingDropboxCode = null
            loading = true
            scope.launch {
                try {
                    val token = exchangeDropboxCode(code)
                    DiaryApplication.instance.saveDropboxToken(token)
                    DiaryApplication.activeProvider = "dropbox"
                    DiaryApplication.repository = DropboxRepository(token)
                    onAuthenticated()
                } catch (e: Exception) {
                    errorMsg = "Dropbox auth failed: ${e.message}"
                } finally {
                    loading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "My Diary",
            fontSize = 28.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose where to store your entries",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.height(40.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = {
                    loading = true
                    errorMsg = null
                    activity.googleAuthSuccess = {
                        loading = false
                        onAuthenticated()
                    }
                    activity.googleAuthError = { msg ->
                        loading = false
                        errorMsg = msg
                    }
                    activity.startGoogleSignIn()
                }
            ) {
                Text("Connect Google Drive")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061FE)),
                onClick = {
                    val authUrl = buildDropboxAuthUrl()
                    CustomTabsIntent.Builder().build()
                        .launchUrl(activity, Uri.parse(authUrl))
                }
            ) {
                Text("Connect Dropbox")
            }
        }

        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun buildDropboxAuthUrl(): String {
    val params = listOf(
        "client_id" to DROPBOX_APP_KEY,
        "response_type" to "code",
        "redirect_uri" to DROPBOX_REDIRECT_URI,
        "token_access_type" to "offline",
    ).joinToString("&") { (k, v) -> "$k=${Uri.encode(v)}" }
    return "https://www.dropbox.com/oauth2/authorize?$params"
}

private suspend fun exchangeDropboxCode(code: String): String = withContext(Dispatchers.IO) {
    // NOTE: In production, do this on your backend to keep secrets safe.
    // For this sample, the secret is embedded (replace placeholders).
    val DROPBOX_APP_SECRET = "YOUR_DROPBOX_APP_SECRET"

    val client = OkHttpClient()
    val body = FormBody.Builder()
        .add("code", code)
        .add("grant_type", "authorization_code")
        .add("redirect_uri", DROPBOX_REDIRECT_URI)
        .build()

    val credentials = okhttp3.Credentials.basic(DROPBOX_APP_KEY, DROPBOX_APP_SECRET)
    val request = Request.Builder()
        .url("https://api.dropbox.com/oauth2/token")
        .addHeader("Authorization", credentials)
        .post(body)
        .build()

    client.newCall(request).execute().use { resp ->
        val json = resp.body?.string() ?: error("Empty response")
        JSONObject(json).getString("access_token")
    }
}
