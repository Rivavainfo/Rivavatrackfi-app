package com.rivavafi.universal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prevent screenshots and screen recording for security
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_home)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val user = firebaseAuth.currentUser
        if (user == null) {
            navigateToLogin()
            return
        }

        val profileImage = findViewById<ImageView>(R.id.profile_image)
        val nameText = findViewById<TextView>(R.id.name_text)
        val emailText = findViewById<TextView>(R.id.email_text)
        val phoneText = findViewById<TextView>(R.id.phone_text)
        val uidText = findViewById<TextView>(R.id.uid_text)
        val logoutButton = findViewById<Button>(R.id.logout_button)

        uidText.text = "UID: ${user.uid}"

        if (user.photoUrl != null) {
            profileImage.load(user.photoUrl)
        } else {
            profileImage.setImageResource(android.R.drawable.ic_menu_camera)
        }

        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    nameText.text = "Name: ${document.getString("name") ?: "Not available"}"
                    emailText.text = "Email: ${document.getString("email") ?: "Not available"}"
                    phoneText.text = "Phone: ${document.getString("phone") ?: "Not available"}"
                } else {
                    nameText.text = "Name: ${user.displayName ?: "Not available"}"
                    emailText.text = "Email: ${user.email ?: "Not available"}"
                    phoneText.text = "Phone: ${user.phoneNumber ?: "Not available"}"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                nameText.text = "Name: ${user.displayName ?: "Not available"}"
                emailText.text = "Email: ${user.email ?: "Not available"}"
                phoneText.text = "Phone: ${user.phoneNumber ?: "Not available"}"
            }

        logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
