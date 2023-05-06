package com.example.flyer.ui.contacts

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flyer.models.ChatRooms
import com.example.flyer.models.User
import com.example.flyer.screenstate.ScreenState
import com.example.flyer.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ContactsViewModel(private val senderid: String, private val receiverid: String) : ViewModel() {
    private var contactsLiveData: MutableLiveData<ScreenState<List<ChatRooms>?>> = MutableLiveData()
    val contactLiveData: LiveData<ScreenState<List<ChatRooms>?>>
        get() = contactsLiveData

    init {
        fetchContactsList()
    }

    private fun fetchContactsList() {
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",senderid).orderBy("date_added", Query.Direction.DESCENDING).addSnapshotListener { documents1, error ->
            if(error!=null) {
                contactsLiveData.postValue(ScreenState.Error(null,error.toString()))
                Log.e(TAG, "Error Getting the Users", error)
            } else {
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("receiver_id",senderid).orderBy("date_added", Query.Direction.DESCENDING).addSnapshotListener { documents2, error ->
                    if(error!=null) {
                        contactsLiveData.postValue(ScreenState.Error(null,error.toString()))
                        Log.e(TAG, "Error Getting the Users", error)
                    } else {
                        val documents = documents1?.documents?.union(documents2?.documents!!)
                        Log.d("size",documents?.size.toString())
                        val users = mutableListOf<ChatRooms>()
                        for (document in documents!!) {
                            val user = document.toObject(ChatRooms::class.java)
                            users.add(user!!)
                        }
                        contactsLiveData.postValue(ScreenState.Success(users))
                    }
                }
            }
        }
    }

}