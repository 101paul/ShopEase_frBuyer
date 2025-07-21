package com.example.shopease.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.shopease.R
import com.example.shopease.databinding.ActivityOrderCompleteBinding

class OrderComplete : AppCompatActivity() {
    private lateinit var binding : ActivityOrderCompleteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val animView = findViewById<LottieAnimationView>(R.id.orderanimation)

        // explicitly start it (just in case autoPlay isn’t picking up)
        animView.playAnimation()
        UsersActivity()
    }
    private fun UsersActivity(){
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@OrderComplete,UsersActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        },5000)
    }
}