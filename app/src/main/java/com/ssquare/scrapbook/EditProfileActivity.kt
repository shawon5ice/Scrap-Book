package com.ssquare.scrapbook

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.ssquare.scrapbook.authentication.SignIn
import com.ssquare.scrapbook.databinding.ActivityEditProfileBinding
import com.ssquare.scrapbook.models.User


class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private var checker = false
    private lateinit var profileImageView: ImageView
    private lateinit var oldUserName: String
    private lateinit var email: String
    private lateinit var oldProfileImg: String
    private val fireBaseUserID = FirebaseAuth.getInstance().currentUser?.uid
    private var picStorageRef: StorageReference? = null
    var mGetContent: ActivityResultLauncher<String>? = null
    private lateinit var resultUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        profileImageView = binding.profileImageChangeIV
        val view = binding.root
        setContentView(view)

        picStorageRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")



        setUserInfoFromUID()


        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(applicationContext, SignIn::class.java))
        }
        binding.editProfileCloseBtn.setOnClickListener {
            finish()
        }

        binding.editProfileSaveBtn.setOnClickListener {
            if (checker) {
                if (TextUtils.isEmpty(binding.profileFUllName.error) && TextUtils.isEmpty(binding.profileUserName.error) &&
                    TextUtils.isEmpty(binding.profileBio.error)
                ) {
                    updateUserInfoWithImage()
                }
            } else {
                if (TextUtils.isEmpty(binding.profileFUllName.error) && TextUtils.isEmpty(binding.profileUserName.error) &&
                    TextUtils.isEmpty(binding.profileBio.error)
                ) {
                    updateInfoWithoutImg()
                }
            }
        }


        emptyError(
            binding.profileFUllName.editText?.text.toString(),
            binding.profileUserName.editText?.text.toString(),
            binding.profileBio.editText?.text.toString()
        )

        //Handling image from memory
        binding.profileImageChangeBtn.setOnClickListener {
            mGetContent?.launch("image/*")
        }
        mGetContent = registerForActivityResult(GetContent(), ActivityResultCallback { result ->
            intent = Intent(this@EditProfileActivity, CropperActivity::class.java)
            intent.putExtra("DATA", result.toString())
            startActivityForResult(intent, 101)
        })


        textWatcher()

    }

    private fun updateUserInfoWithImage() {

        val fileRef = picStorageRef!!.child(fireBaseUserID.toString() + ".jpg")
        var uploadTask: StorageTask<*>
        uploadTask = fileRef.putFile(resultUri!!)
        fileRef.putFile(resultUri!!)
            .addOnCompleteListener {
                OnCompleteListener<Uri> {

                }
            }.addOnProgressListener {
                fileRef.downloadUrl.addOnSuccessListener {
                    updateWithImg(it.toString())
                }
            }.addOnFailureListener {
                OnFailureListener {
                    Toast.makeText(applicationContext, "Image upload failed!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == -1 && requestCode == 101) {
            val result = data?.getStringExtra("RESULT")
            Log.d("img", "onActivityResult: $data")

            if (result != null) {
                checker = true
                resultUri = Uri.parse(result)
                binding.profileImageChangeIV.setImageURI(resultUri)
            } else {
                Toast.makeText(applicationContext, "Please select an image", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun emptyError(
        fullName: String,
        userName: String,
        bio: String,
    ) {
        if (TextUtils.isEmpty(fullName)) {
            binding.profileFUllName.error = "Full Name is required"
        }
        if (TextUtils.isEmpty(userName)) {
            binding.profileUserName.error = "UserName is required"
        }
        if (TextUtils.isEmpty(bio)) {
            binding.profileBio.error = "Bio can not be empty"
        }

    }

    private fun updateWithImg(profileImg: String) {
        val userName = binding.profileUserName.editText?.text.toString()
        if (userName == oldUserName) {
            val userRef = FirebaseDatabase.getInstance().reference.child("Users")

            val userMap = HashMap<String, Any>()
            userMap["fullname"] = binding.profileFUllName.editText?.text.toString()
            userMap["username"] = userName
            userMap["email"] = email
            userMap["uid"] = fireBaseUserID.toString()
            userMap["profile_image"] = profileImg
            userMap["bio"] = binding.profileBio.editText?.text.toString()

            userRef.child(fireBaseUserID.toString()).setValue(userMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Successfully updated profile info",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent =
                            Intent(this@EditProfileActivity, MainActivity::class.java)
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
                    }
                }
        } else {
            FirebaseDatabase.getInstance().reference
                .child("userNames").child(userName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("Snapshot", snapshot.toString())
                        if (snapshot.exists()) {
                            binding.profileUserName.error = "UserName already taken"
                        } else {
                            val userRef = FirebaseDatabase.getInstance().reference.child("Users")

                            val userMap = HashMap<String, Any>()
                            userMap["fullname"] = binding.profileFUllName.editText?.text.toString()
                            userMap["username"] = userName
                            userMap["email"] = email
                            userMap["profile_image"] = profileImg
                            userMap["uid"] = fireBaseUserID.toString()
                            userMap["bio"] = binding.profileBio.editText?.text.toString()

                            val userNameRef =
                                FirebaseDatabase.getInstance().reference.child("userNames")
                            val userNameMap = HashMap<String, Any>()
                            userNameMap[userName] = true
                            userNameRef.child(userName).setValue(userNameMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                    } else {
                                        val message = task.exception!!.message.toString()
                                        Toast.makeText(
                                            applicationContext,
                                            message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            userNameRef.child(oldUserName).removeValue()

                            userRef.child(fireBaseUserID.toString()).setValue(userMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Successfully updated profile info",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent =
                                            Intent(
                                                this@EditProfileActivity,
                                                MainActivity::class.java
                                            )
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
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }

    }

    private fun updateInfoWithoutImg() {
        val userName = binding.profileUserName.editText?.text.toString()
        if (userName == oldUserName) {
            val userRef = FirebaseDatabase.getInstance().reference.child("Users")

            val userMap = HashMap<String, Any>()
            userMap["uid"] = fireBaseUserID.toString()
            userMap["fullname"] = binding.profileFUllName.editText?.text.toString()
            userMap["username"] = userName
            userMap["bio"] = binding.profileBio.editText?.text.toString()

            userRef.child(fireBaseUserID.toString()).updateChildren(userMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Successfully updated profile info",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent =
                            Intent(this@EditProfileActivity, MainActivity::class.java)
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
                    }
                }
        } else {
            FirebaseDatabase.getInstance().reference
                .child("userNames").child(userName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("Snapshot", snapshot.toString())
                        if (snapshot.exists()) {
                            binding.profileUserName.error = "UserName already taken"
                        } else {
                            val userRef = FirebaseDatabase.getInstance().reference.child("Users")

                            val userMap = HashMap<String, Any>()
                            userMap["uid"] = fireBaseUserID.toString()
                            userMap["fullname"] = binding.profileFUllName.editText?.text.toString()
                            userMap["username"] = userName
                            userMap["bio"] = binding.profileBio.editText?.text.toString()

                            val userNameRef =
                                FirebaseDatabase.getInstance().reference.child("userNames")
                            val userNameMap = HashMap<String, Any>()
                            userNameMap[userName] = true
                            userNameRef.child(userName).updateChildren(userNameMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                    } else {
                                        val message = task.exception!!.message.toString()
                                        Toast.makeText(
                                            applicationContext,
                                            message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            userNameRef.child(oldUserName).removeValue()

                            userRef.child(fireBaseUserID.toString()).setValue(userMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Successfully updated profile info",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent =
                                            Intent(
                                                this@EditProfileActivity,
                                                MainActivity::class.java
                                            )
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
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }

    }

    private fun setUserInfoFromUID() {
        try {
            FirebaseDatabase.getInstance().reference
                .child("Users").child(fireBaseUserID.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("Snapshot", snapshot.toString())
                        if (snapshot.exists()) {
                            val user = snapshot.getValue((User::class.java))
                            Log.d("user", user.toString())
                            binding.profileUserName.editText?.setText(user?.username)
                            binding.profileFUllName.editText?.setText(user?.fullname)
                            binding.profileBio.editText?.setText(user?.bio)
                            oldUserName = user?.username.toString()
                            oldProfileImg = user?.profile_image.toString()
                            email = user?.email.toString()
                            Glide
                                .with(this@EditProfileActivity)
                                .load(user?.profile_image.toString().toUri())
                                .placeholder(R.drawable.profile)
                                .into(binding.profileImageChangeIV);
                        } else {

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                }
                )
        } catch (e: Exception) {

        }
    }

    private fun textWatcher() {
        binding.profileFUllName.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty()) {
                    binding.profileFUllName.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        binding.profileUserName.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty()) {
                    binding.profileUserName.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        binding.profileBio.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty()) {
                    binding.profileBio.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

}