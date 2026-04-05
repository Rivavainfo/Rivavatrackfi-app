package com.rivavafi.universal

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        val user = firebaseAuth.currentUser
        if (user == null) {
            navigateToLogin()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 64, 48, 64)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val profileImage = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(240, 240)
        }

        val nameText = TextView(this).apply {
            text = "Name: ${user.displayName ?: "Unknown"}"
            textSize = 18f
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val emailText = TextView(this).apply {
            text = "Email: ${user.email ?: "Not available"}"
            textSize = 16f
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val logoutButton = Button(this).apply {
            text = "Logout"
            setOnClickListener {
                firebaseAuth.signOut()
                navigateToLogin()
            }
        }

        profileImage.load(user.photoUrl)

        root.addView(profileImage)
        root.addView(nameText)
        root.addView(emailText)
        root.addView(logoutButton)

        setContentView(root)
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
