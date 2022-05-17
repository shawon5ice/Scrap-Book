package com.ssquare.scrapbook.authentication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.service.autofill.OnClickAction
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.ssquare.scrapbook.MainActivity
import com.ssquare.scrapbook.R
import com.ssquare.scrapbook.common.Validator
import com.ssquare.scrapbook.databinding.ActivitySignUpBinding
import com.ssquare.scrapbook.models.User

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    private var registerClicked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toSignInBtn.setOnClickListener {
            startActivity(Intent(applicationContext, SignIn::class.java))
            finish()
        }

        binding.signUpBtn.setOnClickListener {
            createAccount()
        }
        textWatecher()
    }


    private fun createAccount() {
        registerClicked = true

        val fullName = binding.signUpFullName.editText!!.text.toString().trim()
        val userName = binding.signUpUserName.editText!!.text.toString().trim()
        val email = binding.signUpEmail.editText!!.text.toString().trim()
        val pass1 = binding.signUpPassword.editText!!.text.toString().trim()
        val pass2 = binding.signUpPassword2.editText!!.text.toString().trim()

        if (!Validator().isValidEmail(email)) {
            binding.signUpEmail.error = "Badly formatted Email"
        }

        emptyError(fullName, userName, email, pass1, pass2)
        if (pass1.isNotEmpty() && pass2.isNotEmpty()) {
            passwordCheck(pass1, pass2)
        }

        if (TextUtils.isEmpty(binding.signUpFullName.error) && TextUtils.isEmpty(binding.signUpUserName.error) &&
            TextUtils.isEmpty(binding.signUpEmail.error) && TextUtils.isEmpty(binding.signUpPassword.error)
        ) {
            val progressDialog = ProgressDialog(this@SignUp)
            progressDialog.setTitle("Kotlin Progress Bar")
            progressDialog.setMessage("Application is loading, please wait")
            progressDialog.show()


            binding.signUpBtn.isEnabled = false

            binding.progressBar.visibility = View.VISIBLE


            try {

                FirebaseDatabase.getInstance().getReference()
                    .child("userNames").child(userName).addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("Snapshot",snapshot.toString())
                        if(snapshot.exists()){
                            binding.signUpUserName.error = "UserName already taken"
                        }else{
                            createUserAndSaveInfo(email,pass1,fullName,userName,progressDialog)
                            saveUserName(userName)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            } finally {
                binding.signUpBtn.isEnabled = true
                progressDialog.dismiss()
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun saveUserName(userName:String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("userNames")
        val mAuth = FirebaseAuth.getInstance()
        val userNameMap = HashMap<String, Any>()
        userNameMap[userName] = true
        userRef.child(userName).setValue(userNameMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    applicationContext,
                    "Successfully UserName Saved in ScrapBook",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val message = task.exception!!.message.toString()
                Toast.makeText(
                    applicationContext,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
                mAuth.signOut()
            }
        }
    }

    private fun createUserAndSaveInfo(
        email:String, pass1: String,fullName: String,userName: String,progressDialog: ProgressDialog
    ) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.createUserWithEmailAndPassword(email, pass1)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserInfo(
                        fullName,
                        userName,
                        email,
                        progressDialog
                    )
                } else {
                    val message = task.exception!!.message.toString()
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        message,
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction(
                            "CLOSE"
                        ) {

                        }
                        .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                        .show()
                }
            }
    }

    private fun saveUserInfo(
        fullName: String,
        userName: String,
        email: String,
        progressDialog: ProgressDialog
    ) {
        val mAuth = FirebaseAuth.getInstance()
        val currentUserId = mAuth.currentUser!!.uid
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullName
        userMap["username"] = userName
        userMap["email"] = email
        userMap["bio"] = "Hey There, I'm using ScrapBook"
        userMap["profile_image"] =
            "https://firebasestorage.googleapis.com/v0/b/scrap-book-163ee.appspot.com/o/Default_images%2Fprofile.png?alt=media&token=6cb69f05-11db-41cd-8515-f88b6a56bde3"

        userRef.child(currentUserId).setValue(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    applicationContext,
                    "Successfully Signed Up in ScrapBook",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@SignUp, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                val message = task.exception!!.message.toString()
                Toast.makeText(
                    applicationContext,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
                mAuth.signOut()
            }
        }
        progressDialog.dismiss()
    }


    private fun passwordCheck(pass1: String, pass2: String) {
        if (pass1.length < 6) {
            binding.signUpPassword.error = "At least 6 characters is required"
        } else if (pass1 != pass2) {
            binding.signUpPassword.error = "Password doesn't match"
        }
    }

    private fun emptyError(
        fullName: String,
        userName: String,
        email: String,
        pass1: String,
        pass2: String
    ) {
        if (TextUtils.isEmpty(fullName)) {
            binding.signUpFullName.error = "Full Name is required"
        }
        if (TextUtils.isEmpty(userName)) {
            binding.signUpUserName.error = "UserName is required"
        }
        if (TextUtils.isEmpty(email)) {
            binding.signUpEmail.error = "Email is required"
        }
        if (TextUtils.isEmpty(pass1)) {
            binding.signUpPassword.error = "Password is required"
        }
        if (TextUtils.isEmpty(pass2)) {
            binding.signUpPassword2.error = "Password is required"
        }

    }

    private fun textWatecher() {
        binding.signUpFullName.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty() && registerClicked) {
                    binding.signUpFullName.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        binding.signUpUserName.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty() && registerClicked) {
                    binding.signUpUserName.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        binding.signUpEmail.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty() && registerClicked) {
                    binding.signUpEmail.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        binding.signUpPassword.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty() && registerClicked) {
                    binding.signUpPassword.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        binding.signUpPassword2.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty() && registerClicked) {
                    binding.signUpPassword2.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }
}