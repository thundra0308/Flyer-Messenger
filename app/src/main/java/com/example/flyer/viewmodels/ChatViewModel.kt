package com.example.flyer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flyer.models.Chat
import com.example.flyer.models.ChatRooms
import com.example.flyer.models.User
import com.example.flyer.screenstate.ScreenState
import com.example.flyer.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(private val senderid: String, private val receiverid: String): ViewModel() {
    private lateinit var database: FirebaseFirestore
    private var chatsLiveData: MutableLiveData<ScreenState<List<Chat>?>> = MutableLiveData()
    val chatLiveData: LiveData<ScreenState<List<Chat>?>>
        get() = chatsLiveData

    private var receiversLiveData: MutableLiveData<ScreenState<ChatRooms?>> = MutableLiveData()
    val receiverLiveData: LiveData<ScreenState<ChatRooms?>>
        get() = receiversLiveData


    init {
        fetchPreviousChats()
        fetchReceiverDetails()
    }

    private fun fetchPreviousChats() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo(Constants.KEY_SENDER_ID,senderid).whereEqualTo(Constants.KEY_RECEIVER_ID,receiverid).get().addOnSuccessListener { document1 ->
            if(document1.isEmpty) {
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo(Constants.KEY_SENDER_ID,receiverid).whereEqualTo(Constants.KEY_RECEIVER_ID,senderid).get().addOnSuccessListener { document2 ->
                    if(document2.isEmpty) {
                        //TODO
                    } else {
                        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(document2.documents[0].id).collection(Constants.KEY_COLLECTION_CHAT).addSnapshotListener { docs1, error ->
                            val chats = mutableListOf<Chat>()
                            for(document in docs1!!) {
                                val chat = document.toObject(Chat::class.java)
                                if(chat.status=="Delivered") {
                                    document.reference.update("status","Read")
                                }
                                chats.add(chat)
                            }
                            val sortedChats = chats.sortedBy { it.timestamp }
                            chatsLiveData.postValue(ScreenState.Success(sortedChats))
                        }
                    }
                }
            } else {
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(document1.documents[0].id).collection(Constants.KEY_COLLECTION_CHAT).addSnapshotListener { docs2, error ->
                    val chats = mutableListOf<Chat>()
                    for(document in docs2!!) {
                        val chat = document.toObject(Chat::class.java)
                        if(chat.status=="Delivered") {
                            document.reference.update("status","Read")
                        }
                        chats.add(chat)
                    }
                    val sortedChats = chats.sortedBy { it.timestamp }
                    chatsLiveData.postValue(ScreenState.Success(sortedChats))
                }
            }
        }
    }

    private fun fetchReceiverDetails() {
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id", senderid).whereEqualTo("receiver_id", receiverid).addSnapshotListener { document1, error1 ->
            if (document1?.isEmpty!!) {
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id", receiverid).whereEqualTo("receiver_id", senderid).addSnapshotListener { document2, error2 ->
                    if (!document2?.isEmpty!!) {
                        val room = document2.documents[0].toObject(ChatRooms::class.java)
                        receiversLiveData.postValue(ScreenState.Success(room))
                    }
                }
            } else {
                val room = document1.documents[0].toObject(ChatRooms::class.java)
                receiversLiveData.postValue(ScreenState.Success(room))
            }
        }
    }
}