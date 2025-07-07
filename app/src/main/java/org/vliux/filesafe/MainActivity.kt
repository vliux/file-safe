package org.vliux.filesafe

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import org.vliux.filesafe.ui.theme.FileSafeTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "GooglePhotosAuth"
        private val PHOTOS_LIBRARY_SCOPE =
            Scope("https://www.googleapis.com/auth/photoslibrary.readonly")
    }

    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FileSafeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android", modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        // 1. Configure sign-in to request the user's ID, email address, and basic profile + Photos scope
        val options =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                .requestScopes(PHOTOS_LIBRARY_SCOPE).build()

        googleSignInClient = GoogleSignIn.getClient(this, options)

        // 2. Check if user is already signed in
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null && GoogleSignIn.hasPermissions(account, PHOTOS_LIBRARY_SCOPE)) {
            Log.d(TAG, "Already signed in. Access token ready.")
            fetchAccessToken(account)
        } else {
            Log.d(TAG, "Not signed in. Starting sign-in flow.")
            startSignIn()
        }
    }

    private fun startSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.d(TAG, "Sign-in successful.")
                fetchAccessToken(account)
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Sign-in failed: ${e.statusCode}")
        }
    }

    private fun fetchAccessToken(account: GoogleSignInAccount) {
        account.account?.let { acct ->
            val scope = "oauth2:${PHOTOS_LIBRARY_SCOPE.scopeUri}"
            Thread {
                try {
                    val token =
                        com.google.android.gms.auth.GoogleAuthUtil.getToken(this, acct, scope)
                    Log.d(TAG, "Access token: $token")
                    // Use the token with the Photo Picker API
                } catch (e: Exception) {
                    Log.e(TAG, "Error retrieving token: ${e.message}")
                }
            }.start()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FileSafeTheme {
        Greeting("Android")
    }
}