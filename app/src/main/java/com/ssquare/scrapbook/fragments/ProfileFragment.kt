package com.ssquare.scrapbook.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.ssquare.scrapbook.EditProfileActivity
import com.ssquare.scrapbook.R
import com.ssquare.scrapbook.models.User
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var profileUserName: String
    private lateinit var profileUID: String
    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private lateinit var user: User
    private lateinit var settingButton: Button
    private lateinit var userNameTextView: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var totalFollowers: TextView
    private lateinit var totalFollowing: TextView
    private lateinit var totalPosts: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val prefs = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        settingButton = view.findViewById(R.id.setting_profile_btn)

        userNameTextView = view.findViewById(R.id.profile_fragment_username)
        fullNameTextView = view.findViewById(R.id.profile_full_name)
        bioTextView = view.findViewById(R.id.profile_fragment_bio)
        profileImage = view.findViewById(R.id.profile_image_id)
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
            settingButton.setOnClickListener {
                startActivity(Intent(context, EditProfileActivity::class.java))
            }

        } else if (profileUID != uid) {
            checkFollowAndFollowing()
            setUserInfoFromUID(profileUID)
            settingButton.setOnClickListener {
                if (settingButton.text.equals("Follow")) {

                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(itl.toString())
                            .child("Following").child(uid)
                            .setValue(true).addOnCompleteListener { task ->
                                firebaseUser?.uid.let { itl ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(uid)
                                        .child("Followers").child(itl.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                    }

                } else {

                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(itl.toString())
                            .child("Following").child(uid)
                            .removeValue().addOnCompleteListener { task ->
                                firebaseUser?.uid.let { itl ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(uid)
                                        .child("Followers").child(itl.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                    }
                }
            }
        }
        return view
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
            Log.d("uid",uid)
            FirebaseDatabase.getInstance().reference
                .child("Users").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("Snapshot", snapshot.toString())
                        if (snapshot.exists()) {
                            val user = snapshot.getValue((User::class.java))
                            Log.d("user", user.toString())
                            userNameTextView.text = user?.getUserName()
                            fullNameTextView.text = user?.getFullName()
                            bioTextView.text = user?.getBio()
                            getFollowers(user?.getUid().toString())
                            getFollowings(user?.getUid().toString())
                            if (user?.getProfileImage()!!.isEmpty()) {
                                profileImage.setImageResource(R.drawable.profile)
                            } else {
                                Picasso.get().load(user.getProfileImage()).into(profileImage)
                            }
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