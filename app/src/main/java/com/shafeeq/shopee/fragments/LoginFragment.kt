package com.shafeeq.shopee.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.shafeeq.shopee.MainActivity
import com.shafeeq.shopee.R
import com.shafeeq.shopee.utils.toast
import java.util.concurrent.TimeUnit


class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        val root = inflater.inflate(R.layout.fragment_login, container, false)
        val phoneNumberTv = root.findViewById<EditText>(R.id.phoneNumber)
        root.findViewById<Button>(R.id.submitButton).setOnClickListener {
            try {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumberTv.text.toString())
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(requireActivity())
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (ex: IllegalArgumentException) {
                context?.toast("Invalid phone number")
            }
        }
        if(null != auth.currentUser) {

        }
        return root
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    requireActivity().toast("Update User Interface here $user")
                    Log.d(TAG, "Update User Interface here $user")
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)
            if (e is FirebaseAuthInvalidCredentialsException) {
                context?.toast("invalid request")
                e.printStackTrace()
                return
            } else if (e is FirebaseTooManyRequestsException) {
                context?.toast("quota exceeded")
                return
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d(TAG, "onCodeSent:$verificationId")
        }
    }

    override fun getContext(): Context? {
        return requireActivity().applicationContext
    }

    companion object {
        const val TAG = "SHOPPE_DEBUG"
    }
}