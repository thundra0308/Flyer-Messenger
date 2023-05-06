package com.example.flyer.ui.chats

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flyer.models.ChatRooms
import com.example.flyer.screenstate.ScreenState
import com.example.flyer.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatsViewModel(private val senderid: String, private val receiverid: String) : ViewModel() {
    private var chatsLiveData: MutableLiveData<ScreenState<List<ChatRooms>?>> = MutableLiveData()
    val chatLiveData: LiveData<ScreenState<List<ChatRooms>?>>
        get() = chatsLiveData

    init {
        fetchChatList()
    }

    private fun fetchChatList() {
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",senderid).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { documents1, error ->
            if(error!=null) {
                chatsLiveData.postValue(ScreenState.Error(null,error.toString()))
                Log.e(ContentValues.TAG, "Error Getting the Users", error)
            } else {
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("receiver_id",senderid).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { documents2, error ->
                    if(error!=null) {
                        chatsLiveData.postValue(ScreenState.Error(null,error.toString()))
                        Log.e(ContentValues.TAG, "Error Getting the Users", error)
                    } else {
                        val documents = documents1?.documents?.union(documents2?.documents!!)
                        Log.d("size",documents?.size.toString())
                        val users = mutableListOf<ChatRooms>()
                        for (document in documents!!) {
                            val user = document.toObject(ChatRooms::class.java)
                            if(user?.message_number!! > 0) users.add(user)
                        }
                        chatsLiveData.postValue(ScreenState.Success(users))
                    }
                }
            }
        }
    }

}