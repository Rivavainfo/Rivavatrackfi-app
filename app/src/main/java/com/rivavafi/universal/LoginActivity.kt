package com.rivavafi.universal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SecurityUtil.isDeviceRooted()) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Security Alert")
                .setMessage("This app cannot run on a rooted device for security reasons.")
                .setCancelable(false)
                .setPositiveButton("Exit") { _, _ ->
                    finish()
                }
                .show()
            return
        }

        // Prevent screenshots and screen recording for security
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.progress_bar)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (firebaseAuth.currentUser != null) {
            navigateToHome()
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EmailLoginFragment())
                .commit()
        }
    }

    fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    fun showErrorDialog(title: String, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun startGoogleLogin() {
        showProgress()
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != RC_SIGN_IN) return

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken.isNullOrEmpty()) {
                hideProgress()
                Log.e(TAG, "Google Sign-In failed: ID token is null or empty")
                return
            }

            firebaseAuthWithGoogle(idToken)
        } catch (e: ApiException) {
            hideProgress()
            Log.e(TAG, "Google Sign-In failed (resultCode=$resultCode)", e)
            val errorMessage = e.message ?: "Unknown error occurred"
            showErrorDialog("Google Sign-In Failed", "Status code: ${e.statusCode}\n$errorMessage")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        lifecycleScope.launch {
                            try {
                                AuthHelper.saveUserToFirestore(
                                    uid = user.uid,
                                    name = user.displayName ?: "User",
                                    email = user.email ?: "",
                                    phone = user.phoneNumber ?: "",
                                    provider = "google"
                                )
                                hideProgress()
                                navigateToHome()
                            } catch (e: Exception) {
                                hideProgress()
                                showErrorDialog("Data Save Error", e.message ?: "Failed to save user data")
                            }
                        }
                    }
                } else {
                    hideProgress()
                    Log.e(TAG, "Firebase auth with Google failed", task.exception)
                    showErrorDialog("Authentication Failed", task.exception?.message ?: "Unknown error occurred")
                }
            }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 1001
    }
}
