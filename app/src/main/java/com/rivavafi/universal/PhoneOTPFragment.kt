package com.rivavafi.universal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PhoneOTPFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_phone_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val phoneInput = view.findViewById<EditText>(R.id.phone_input)
        val sendOtpButton = view.findViewById<Button>(R.id.send_otp_button)
        val otpInput = view.findViewById<EditText>(R.id.otp_input)
        val verifyOtpButton = view.findViewById<Button>(R.id.verify_otp_button)
        val phoneInputLayout = view.findViewById<LinearLayout>(R.id.phone_input_layout)
        val otpInputLayout = view.findViewById<LinearLayout>(R.id.otp_input_layout)
        val backText = view.findViewById<TextView>(R.id.back_text)

        sendOtpButton.setOnClickListener {
            val phone = phoneInput.text.toString().trim()
            if (phone.isEmpty()) {
                Toast.makeText(context, "Please enter phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            (activity as? LoginActivity)?.showProgress()

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(credential, phone)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        (activity as? LoginActivity)?.hideProgress()
                        (activity as? LoginActivity)?.showErrorDialog(
                            "Verification Failed",
                            e.message ?: "Unknown error occurred"
                        )
                    }

                    override fun onCodeSent(
                        verId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        (activity as? LoginActivity)?.hideProgress()
                        verificationId = verId
                        phoneInputLayout.visibility = View.GONE
                        otpInputLayout.visibility = View.VISIBLE
                        Toast.makeText(context, "OTP Sent", Toast.LENGTH_SHORT).show()
                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        verifyOtpButton.setOnClickListener {
            val code = otpInput.text.toString().trim()
            if (code.isEmpty() || verificationId == null) {
                Toast.makeText(context, "Please enter valid OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            (activity as? LoginActivity)?.showProgress()
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential, phoneInput.text.toString().trim())
        }

        backText.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, phone: String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        lifecycleScope.launch {
                            try {
                                AuthHelper.saveUserToFirestore(
                                    uid = user.uid,
                                    name = user.displayName ?: "Phone User",
                                    email = user.email ?: "",
                                    phone = phone,
                                    provider = "phone"
                                )
                                (activity as? LoginActivity)?.hideProgress()
                                startActivity(Intent(activity, HomeActivity::class.java))
                                activity?.finish()
                            } catch (e: Exception) {
                                (activity as? LoginActivity)?.hideProgress()
                                (activity as? LoginActivity)?.showErrorDialog(
                                    "Data Save Error",
                                    e.message ?: "Failed to save user data"
                                )
                            }
                        }
                    }
                } else {
                    (activity as? LoginActivity)?.hideProgress()
                    (activity as? LoginActivity)?.showErrorDialog(
                        "Login Failed",
                        task.exception?.message ?: "Unknown error occurred"
                    )
                }
            }
    }
}
