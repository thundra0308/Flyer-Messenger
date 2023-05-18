package com.example.flyer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import com.bumptech.glide.Glide
import com.example.flyer.R
import com.example.flyer.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {
   private lateinit var docRef: DocumentReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        docRef = database.collection(Constants.KEY_COLLECTION_USER).document(FirebaseAuth.getInstance().uid!!)
    }

    override fun onPause() {
        super.onPause()
        Glide.with(this).pauseRequests()
        docRef.update("online_status",false)
    }

    override fun onResume() {
        super.onResume()
        Glide.with(this).resumeRequests()
        docRef.update("online_status",true)
    }
}