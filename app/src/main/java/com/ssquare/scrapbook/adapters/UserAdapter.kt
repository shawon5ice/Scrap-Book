package com.ssquare.scrapbook.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.ssquare.scrapbook.R
import com.ssquare.scrapbook.fragments.ProfileFragment
import com.ssquare.scrapbook.models.User
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private var mContext: Context,
    private var mUser: List<User>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUser[position]
        print(user.toString())
        holder.userName.text = "#" + user.getUserName()
        holder.fullName.text = user.getFullName()
        if (user.getProfileImage()!!.isEmpty()) {
            holder.profileImage.setImageResource(R.drawable.profile)
        } else {
            Picasso.get().load(user.getProfileImage()).into(holder.profileImage)
        }

        holder.userViewItem.setOnClickListener(View.OnClickListener{

            val pref = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()

            pref.putString("profileUserName",user.getUserName())
            pref.putString("profileUID",user.getUid())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.container, ProfileFragment()).commit()

        })


        if(!user.getUid().equals(firebaseUser?.uid)){
            checkFollowingStatus(user.getUid().toString(), holder.followBtn)

            holder.followBtn.setOnClickListener {
                if (holder.followBtn.text.equals("Follow")) {

                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(itl.toString())
                            .child("Following").child(user.getUid().toString())
                            .setValue(true).addOnCompleteListener { task ->
                                firebaseUser?.uid.let { itl ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUid().toString())
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
                            .child("Following").child(user.getUid().toString())
                            .removeValue().addOnCompleteListener { task ->
                                firebaseUser?.uid.let { itl ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUid().toString())
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
        }else{
            holder.followBtn.text = "View Profile"
        }

    }

    private fun checkFollowingStatus(uid: String, followBtn: Button) {
        val followingRef = firebaseUser?.uid.let { itl ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(itl.toString())
                .child("Following")
        }
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(uid).exists()) {
                    followBtn.text = "Following"
                } else {
                    followBtn.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val profileImage: CircleImageView =
            ItemView.findViewById(R.id.user_profile_image_search)
        val userName: TextView = ItemView.findViewById(R.id.user_name_TV_search)
        val fullName: TextView = ItemView.findViewById(R.id.user_full_name_TV_search)
        val followBtn: Button = ItemView.findViewById(R.id.user_follow_btn_search)
        val userViewItem: CardView = ItemView.findViewById(R.id.user_item_view)
    }
}