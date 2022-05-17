package com.ssquare.scrapbook.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ssquare.scrapbook.R
import com.ssquare.scrapbook.adapters.UserAdapter
import com.ssquare.scrapbook.models.User


class SearchFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null
    private var filteredUsers: MutableList<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        filteredUsers = ArrayList()
        mUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it, filteredUsers as ArrayList<User>, true) }
        recyclerView?.adapter = userAdapter

        retrieveUser()

        view.findViewById<EditText>(R.id.search_text_input)
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    if (p0!!.isNotEmpty()) {
                        recyclerView?.visibility = View.VISIBLE
                        searchUser(p0.toString().lowercase())
                    } else {
                        recyclerView?.visibility = View.GONE
                    }
                }

            })

        return view
    }

    private fun searchUser(input: String) {

        print(input)
        filteredUsers?.clear()
        if(input[0].equals("#")){
            for (item in mUser!!.iterator()) {
                if (item.getUserName()!!.lowercase().contains(input.substring(1).lowercase())) {
                    filteredUsers!!.add(item)
                }
            }
        }else{
            for (item in mUser!!.iterator()) {
                if (item.getFullName()!!.lowercase().contains(input.lowercase())) {
                    filteredUsers!!.add(item)
                }
            }
        }
        userAdapter?.notifyDataSetChanged()
    }

    private fun retrieveUser() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mUser?.clear()
                for (snapshot in snapshot.children) {
                    val user = snapshot.getValue((User::class.java))
                    if (user != null) {
                        mUser?.add(user)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}