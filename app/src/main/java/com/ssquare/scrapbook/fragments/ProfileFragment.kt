package com.ssquare.scrapbook.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.ssquare.scrapbook.EditProfileActivity
import com.ssquare.scrapbook.R
import com.ssquare.scrapbook.authentication.SignIn

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)
        view.findViewById<Button>(R.id.setting_profile_btn).setOnClickListener {
            startActivity(Intent(context,EditProfileActivity::class.java))
        }
        return view
    }
}