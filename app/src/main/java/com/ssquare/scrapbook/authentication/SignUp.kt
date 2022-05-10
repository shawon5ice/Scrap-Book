package com.ssquare.scrapbook.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ssquare.scrapbook.R
import com.ssquare.scrapbook.databinding.ActivitySignInBinding
import com.ssquare.scrapbook.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toSignInBtn.setOnClickListener {
            startActivity(Intent(applicationContext,SignIn::class.java))
            finish()
        }
    }
}