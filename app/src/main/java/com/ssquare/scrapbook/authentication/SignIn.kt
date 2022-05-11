package com.ssquare.scrapbook.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ssquare.scrapbook.databinding.ActivitySignInBinding

class SignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toSignUpBtn.setOnClickListener {
            startActivity(Intent(applicationContext, SignUp::class.java))
            finish()
        }
    }
}