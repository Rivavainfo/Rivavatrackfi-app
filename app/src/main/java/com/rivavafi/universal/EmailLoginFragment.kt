package com.rivavafi.universal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class EmailLoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_email_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val emailInput = view.findViewById<EditText>(R.id.email_input)
        val passwordInput = view.findViewById<EditText>(R.id.password_input)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val googleLoginButton = view.findViewById<Button>(R.id.google_login_button)
        val phoneLoginButton = view.findViewById<Button>(R.id.phone_login_button)
        val registerText = view.findViewById<TextView>(R.id.register_text)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            (activity as? LoginActivity)?.showProgress()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    (activity as? LoginActivity)?.hideProgress()
                    if (task.isSuccessful) {
                        startActivity(Intent(activity, HomeActivity::class.java))
                        activity?.finish()
                    } else {
                        Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        googleLoginButton.setOnClickListener {
            (activity as? LoginActivity)?.startGoogleLogin()
        }

        phoneLoginButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PhoneOTPFragment())
                .addToBackStack(null)
                .commit()
        }

        registerText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
