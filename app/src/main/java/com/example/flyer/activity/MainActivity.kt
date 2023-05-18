package com.example.flyer.activity

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.flyer.R
import com.example.flyer.databinding.ActivityMainBinding
import com.example.flyer.models.User
import com.example.flyer.ui.accountdetails.AccountDetailFragment
import com.example.flyer.utils.Constants
import com.example.flyer.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setUpBottomNavigation()
        setUpUi()
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
        binding.mainscreenIvProfileIcon.setOnClickListener {
            val bottomSheetFragment = AccountDetailFragment()
            bottomSheetFragment.show(this@MainActivity.supportFragmentManager, "bottomSheetTag")
        }
    }

    private fun updateToken(token: String) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(FirebaseAuth.getInstance().uid!!).update("fcmtoken",token).addOnSuccessListener {
            Log.d("TOKEN UPDATE STATUS", "Token Updated Successfully")
        }. addOnFailureListener {
            Log.d("TOKEN UPDATE STATUS", "Failed to Update The Token")
        }
    }

    private fun setUpUi() {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USER).document(FirebaseAuth.getInstance().uid!!).addSnapshotListener { value, error ->
            val user = value?.toObject(User::class.java)
            if(!isFinishing) {
                Glide
                    .with(this)
                    .load(user?.image)
                    .centerCrop()
                    .placeholder(R.drawable.profile_icon_placeholder_1)
                    .into(binding.mainscreenIvProfileIcon)
            }

        }
    }

    private fun setUpBottomNavigation() {
        val navView: SmoothBottomBar = binding.navView
        val popUpMenu = PopupMenu(applicationContext,null)
        popUpMenu.inflate(R.menu.bottom_nav_menu)
        val menu: Menu = popUpMenu.menu
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(menu,navController)
    }
    private fun setThatFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_activity_main, fragment)
            commit()
        }
    }

}