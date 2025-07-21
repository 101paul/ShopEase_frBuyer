package com.example.shopease.viewModels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.shopease.utilites.Utils
import com.example.shopease.models.users
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val _verificationId = MutableStateFlow<String?>(null)
    private val _otpSent = MutableStateFlow(false)
    val otpSent = _otpSent
    private val _isSignedSuccessfully = MutableStateFlow(false)
    val isSignedSuccessfully = _isSignedSuccessfully
    private val _CurrentUser = MutableStateFlow(false)
    val CurrentUser = _CurrentUser
    init{
        Utils.getAuthInstance().currentUser?.let{
            _CurrentUser.value = true
        }
    }
   fun sendOtp(userNumber : String,activity : Activity){
       val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

           override fun onVerificationCompleted(credential: PhoneAuthCredential) {

           }

           override fun onVerificationFailed(e: FirebaseException) {

               // Show a message and update the UI
           }

           override fun onCodeSent(
               verificationId: String,
               token: PhoneAuthProvider.ForceResendingToken,
           ) {
                _verificationId.value = verificationId
               _otpSent.value = true

               // Save verification ID and resending token so we can use them later

           }
       }
       val options = PhoneAuthOptions.newBuilder(Utils.getAuthInstance())
           .setPhoneNumber("+91$userNumber") // Phone number to verify
           .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
           .setActivity(activity) // Activity (for callback binding)
           .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
           .build()
       PhoneAuthProvider.verifyPhoneNumber(options)
   }
    fun signInWithPhoneAuthCredential(otp : String , userNumber : String , Users : users) {
        val credential = PhoneAuthProvider.getCredential(_verificationId.value.toString(), otp)


            Utils.getAuthInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Users.uid!!).setValue(Users)
                        _isSignedSuccessfully.value = true
                    } else {
                        // Sign in failed, display a message and update the UI
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
        }

}
