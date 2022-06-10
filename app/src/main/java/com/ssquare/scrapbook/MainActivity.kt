package com.ssquare.scrapbook

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.ssquare.scrapbook.databinding.ActivityMainBinding
import com.ssquare.scrapbook.fragments.HomeFragment
import com.ssquare.scrapbook.fragments.NotificationFragment
import com.ssquare.scrapbook.fragments.ProfileFragment
import com.ssquare.scrapbook.fragments.SearchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    internal var selectedFragment: Fragment? = HomeFragment()


    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

        pref.putString("profileUID",FirebaseAuth.getInstance().currentUser?.uid)
        pref.apply()
        val navView: BottomNavigationView = binding.navView

        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.nav_search -> {
                    loadFragment(SearchFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.nav_add_post -> {
                }
                R.id.nav_notifications -> {
                    loadFragment(NotificationFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.nav_profile -> {

                    loadFragment(ProfileFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        loadFragment(HomeFragment())
    }
}

