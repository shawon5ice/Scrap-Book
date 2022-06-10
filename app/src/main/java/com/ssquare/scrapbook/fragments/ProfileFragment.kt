package com.ssquare.scrapbook.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.with
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.ssquare.scrapbook.EditProfileActivity
import com.ssquare.scrapbook.R
import com.ssquare.scrapbook.models.User
import de.hdodenhof.circleimageview.CircleImageView
import java.net.URLEncoder


class ProfileFragment : Fragment() {

    private lateinit var profileUserName: String
    private lateinit var profileUID: String
    private var firebaseUser = FirebaseAuth.getInstance().currentUser
    private lateinit var user: User
    private lateinit var settingButton: Button
    private lateinit var userNameTextView: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var profileImage2: CircleImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var totalFollowers: TextView
    private lateinit var totalFollowing: TextView
    private lateinit var totalPosts: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        settingButton = view.findViewById(R.id.setting_profile_btn)

        userNameTextView = view.findViewById(R.id.profile_fragment_username)
        progressBar = view.findViewById(R.id.progressBar)
        fullNameTextView = view.findViewById(R.id.profile_full_name)
        bioTextView = view.findViewById(R.id.profile_fragment_bio)
        profileImage = view.findViewById(R.id.profile_image_id)
        profileImage2 = view.findViewById(R.id.profile2)
        totalPosts = view.findViewById(R.id.total_posts)
        totalFollowing = view.findViewById(R.id.total_following)
        totalFollowers = view.findViewById(R.id.total_followers)
        if (prefs != null) {
            this.profileUID = prefs.getString("profileUID", "none").toString()
        }
        val uid = firebaseUser?.uid.toString()
        if (profileUID == uid) {
            settingButton.text = "Edit Profile"
            setUserInfoFromUID(uid)
            getFollowers(uid)
            getFollowings(uid)
        }
        else{
            checkFollowAndFollowing()
            setUserInfoFromUID(profileUID)
        }

        val url = "https://firebasestorage.googleapis.com/v0/b/scrap-book-163ee.appspot.com/o/Profile%20Pictures%2FYcdTTusUyIWSWM8iqZjyKqYBMWo2.jpg?alt=media&token=bdba3921-d281-4da5-8114-762b2e3241cc"
        Picasso.get()
            .load(url)
            .placeholder(R.drawable.profile)
            .into(profileImage2)


        settingButton.setOnClickListener {
            val getButtonText = settingButton.text.toString()

            when{
                getButtonText == "Edit Profile" -> startActivity(Intent(context, EditProfileActivity::class.java))
                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(itl.toString())
                            .child("Following").child(profileUID)
                            .setValue(true)
                    }
                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileUID)
                            .child("Followers").child(itl.toString())
                            .setValue(true)
                    }
                    getFollowers(profileUID)
                }
                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(itl.toString())
                            .child("Following").child(profileUID)
                            .removeValue()
                    }
                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileUID)
                            .child("Followers").child(itl.toString())
                            .removeValue()
                    }
//                    getFollowers(user?.getUid().toString())
//                    getFollowings(user?.getUid().toString())
                    getFollowers(profileUID)
                }
            }
        }
    }

    private fun checkFollowAndFollowing() {
        val followingRef = firebaseUser?.uid.let { itl ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(itl.toString())
                .child("Following")
        }
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(profileUID).exists()) {
                    settingButton.text = "Following"
                } else {
                    settingButton.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    private fun setUserInfoFromUID(uid: String) {
        try {
            progressBar.visibility = View.VISIBLE
            Log.d("uid",uid)
            FirebaseDatabase.getInstance().reference
                .child("Users").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("Snapshot", snapshot.toString())
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)
                            Log.d("user", user.toString())
                            userNameTextView.text = user?.username
                            fullNameTextView.text = user?.fullname
                            bioTextView.text = user?.bio
                            getFollowers(user?.uid.toString())
                            getFollowings(user?.uid.toString())

                            val requestOptions = RequestOptions()
                            requestOptions.placeholder(R.drawable.profile)
                            requestOptions.error(R.drawable.profile)

//                            Log.d("user", "onDataChange: username: ${user?.getUserName()}")
//                            Log.d("user", "onDataChange: profile_image: ${user?.getProfileImage()}")
//                            Log.d("user", "onDataChange: fullname: ${user?.getFullName()}")
                            Log.d("user_db", "onDataChange: user:${snapshot.child("profile_image")}")
                            Log.d("user_db", "onDataChange: imgUrl: ${user?.profile_image}")


                            with(requireContext())
                                .load(user?.profile_image)
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.camera)
                                .into(profileImage)
                            progressBar.visibility = View.INVISIBLE
                            profileImage.setImageURI(Uri.parse(snapshot.child("username").toString()))
                            Log.d("pi",snapshot.child("profile_image").toString())

                        } else {
                            progressBar.visibility = View.INVISIBLE
                            Toast.makeText(context,"User doesn't exist",Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(context,"Failed to load user data",Toast.LENGTH_SHORT).show()
                    }
                }
                )
        } catch (e: Exception) {

        }
    }


    private fun getFollowers(uid: String) {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(uid)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    totalFollowers.text = snapshot.childrenCount.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun getFollowings( uid: String) {
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("fff",snapshot.toString())
                if (snapshot.exists()) {
                    totalFollowing.text = snapshot.childrenCount.toString()
                    Log.d("fff",snapshot.childrenCount.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()

        pref?.putString("profileUID",FirebaseAuth.getInstance().currentUser?.uid)
        pref?.apply()
    }

    override fun onResume() {
        super.onResume()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()

        pref?.putString("profileUID",FirebaseAuth.getInstance().currentUser?.uid)
        pref?.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()

        pref?.putString("profileUID",FirebaseAuth.getInstance().currentUser?.uid)
        pref?.apply()
    }
}