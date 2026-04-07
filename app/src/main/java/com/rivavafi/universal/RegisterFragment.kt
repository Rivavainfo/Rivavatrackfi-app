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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val nameInput = view.findViewById<EditText>(R.id.name_input)
        val emailInput = view.findViewById<EditText>(R.id.email_input)
        val passwordInput = view.findViewById<EditText>(R.id.password_input)
        val registerButton = view.findViewById<Button>(R.id.register_button)
        val loginText = view.findViewById<TextView>(R.id.login_text)

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            (activity as? LoginActivity)?.showProgress()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid
                        if (uid != null) {
                            lifecycleScope.launch {
                                try {
                                    AuthHelper.saveUserToFirestore(
                                        uid = uid,
                                        name = name,
                                        email = email,
                                        phone = "",
                                        provider = "email"
                                    )
                                    (activity as? LoginActivity)?.hideProgress()
                                    startActivity(Intent(activity, HomeActivity::class.java))
                                    activity?.finish()
                                } catch (e: Exception) {
                                    (activity as? LoginActivity)?.hideProgress()
                                    Toast.makeText(context, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        (activity as? LoginActivity)?.hideProgress()
                        Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        loginText.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
