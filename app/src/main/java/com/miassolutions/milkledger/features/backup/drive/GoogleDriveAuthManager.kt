package com.miassolutions.milkledger.features.backup.drive

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class GoogleDriveAuthManager @Inject constructor() {
    
    fun getSignInIntent(activity: Activity): Intent {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        
        return GoogleSignIn.getClient(activity, signInOptions).signInIntent
    }
    
    fun hasDrivePermission(activity: Activity): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        return account != null &&
                GoogleSignIn.hasPermissions(
                    account,
                    Scope(DriveScopes.DRIVE_FILE)
                )
    }

    fun revokeDriveAccess(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (Exception?) -> Unit
    ) {
        val client = GoogleSignIn.getClient(activity, getGoogleSignInOptions())

        client.revokeAccess()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    private fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
    }
}