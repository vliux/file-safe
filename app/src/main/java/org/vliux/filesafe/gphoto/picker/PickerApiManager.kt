package org.vliux.filesafe.gphoto.picker

import android.accounts.Account
import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

open class PickerApiManager {

    @WorkerThread
    fun getToken(context: Context): String? {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
            .requestScopes(PHOTOS_LIBRARY_SCOPE)
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)

        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null && GoogleSignIn.hasPermissions(account, PHOTOS_LIBRARY_SCOPE)) {
            val acc = account.account
            return acc?.let {
                Log.d(TAG, "User already signed in. Fetching access token.")
                return getAccessTokenFromAccount(context, it)
            }
        }
        Log.d(TAG, "User not signed in or permissions not granted.")
        return null
    }

    @WorkerThread
    private fun getAccessTokenFromAccount(context: Context, account: Account): String? {
        try {
            val scope = "oauth2:${PHOTOS_LIBRARY_SCOPE.scopeUri}"
            return com.google.android.gms.auth.GoogleAuthUtil.getToken(
                context,
                account,
                scope
            )
            Log.i(TAG, "Access Token successfully retrieved.")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching access token", e)
        }
        return null
    }

    companion object {
        private const val TAG = "PickerApiManager"
        private val PHOTOS_LIBRARY_SCOPE =
            Scope("https://www.googleapis.com/auth/photoslibrary.readonly")
    }
}