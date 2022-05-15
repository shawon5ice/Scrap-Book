package com.ssquare.scrapbook.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.ssquare.scrapbook.MainActivity
import com.ssquare.scrapbook.common.Validator
import com.ssquare.scrapbook.databinding.ActivitySignInBinding

class SignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private val mAuth = FirebaseAuth.getInstance()
    private val logInBtnCLicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toSignUpBtn.setOnClickListener {
            startActivity(Intent(applicationContext, SignUp::class.java))
            finish()
        }

        binding.signInBtn.setOnClickListener {
            signInUser()
        }
        textWatecher()
    }

    private fun signInUser() {
        val email = binding.signInEmail.editText!!.text.toString().trim()
        val password = binding.signInPassword.editText!!.text.toString().trim()

        if(!Validator().isValidEmail(email)){
            binding.signInEmail.error = "Badly formatted Email"
        }

        when{
            email.isEmpty() -> binding.signInEmail.error = "You must enter your registered email"
            password.isEmpty() -> binding.signInPassword.error = "Password can not be empty"
        }




        if (TextUtils.isEmpty(binding.signInEmail.error) && TextUtils.isEmpty(binding.signInPassword.error)) {
            try {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                "You have been signed in successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                            finish()
                        } else {
                            val message = task.exception!!.message.toString()
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                message,
                                Snackbar.LENGTH_SHORT
                            )
                                .setAction(
                                    "CLOSE",
                                    View.OnClickListener { action ->

                                    })
                                .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                                .show()
                        }
                    }
            } finally {

            }
        }

    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser?.uid != null) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    private fun textWatecher() {
        binding.signInEmail.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty() && logInBtnCLicked) {
                    binding.signInEmail.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        binding.signInPassword.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty() && logInBtnCLicked) {
                    binding.signInPassword.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

    }
}