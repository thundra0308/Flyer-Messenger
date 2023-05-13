package com.example.flyer.ui.accountdetails

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flyer.models.User
import com.example.flyer.screenstate.ScreenState
import com.example.flyer.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountDetailViewModel(private val senderid: String, private val receiverid: String) : ViewModel() {
    private var usersLiveData: MutableLiveData<ScreenState<User?>> = MutableLiveData()
    val userLiveData: LiveData<ScreenState<User?>>
        get() = usersLiveData

    init {
        fetchUserDetails()
    }

    private fun fetchUserDetails() {
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(FirebaseAuth.getInstance().uid!!).get().addOnSuccessListener { document ->
            val user = document.toObject(User::class.java)
            usersLiveData.postValue(ScreenState.Success(user))
        }.addOnFailureListener { exception ->
            usersLiveData.postValue(ScreenState.Error(null,exception.toString()))
            Log.e(ContentValues.TAG, "Error Getting the User Details", exception)
        }
    }
}