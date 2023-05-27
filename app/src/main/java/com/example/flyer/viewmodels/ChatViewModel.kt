package com.example.flyer.viewmodels

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flyer.api.ApiClient
import com.example.flyer.api.ApiService
import com.example.flyer.models.Chat
import com.example.flyer.models.ChatRooms
import com.example.flyer.models.User
import com.example.flyer.screenstate.ScreenState
import com.example.flyer.utils.Constants
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatViewModel(private val sender_id: String, private val receiver_id: String, private var chat_room_id: String): ViewModel() {
    private lateinit var database: FirebaseFirestore
    private var chatsLiveData: MutableLiveData<ScreenState<List<Chat>?>> = MutableLiveData()
    val chatLiveData: LiveData<ScreenState<List<Chat>?>>
        get() = chatsLiveData

    private var receiversLiveData: MutableLiveData<ScreenState<ChatRooms?>> = MutableLiveData()
    val receiverLiveData: LiveData<ScreenState<ChatRooms?>>
        get() = receiversLiveData

    private var senderDetailsLiveData: MutableLiveData<ScreenState<User?>> = MutableLiveData()
    val senderDetailLiveData: LiveData<ScreenState<User?>>
        get() = senderDetailsLiveData

    private var receiverDetailsLiveData: MutableLiveData<ScreenState<User?>> = MutableLiveData()
    val receiverDetailLiveData: LiveData<ScreenState<User?>>
        get() = receiverDetailsLiveData

    private var wallpapersLiveData: MutableLiveData<ScreenState<String?>> = MutableLiveData()
    val wallpaperLiveData: LiveData<ScreenState<String?>>
        get() = wallpapersLiveData

    init {
        fetchSenderDetail()
        fetchReceiverDetail()
        fetchPreviousChats()
        fetchReceiverDetails()
        fetchWallpaper()
    }

    private fun fetchPreviousChats() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).get().addOnSuccessListener { document_room ->
            val room = document_room.toObject(ChatRooms::class.java)
            var lastMessageNumber: Long = 0
            if(sender_id==room?.sender_id) {
                lastMessageNumber = room.sender_last_message_number
            } else {
                lastMessageNumber = room?.receiver_last_message_number!!
            }
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).collection(Constants.KEY_COLLECTION_CHAT).addSnapshotListener { document_chats, error ->
                if(error==null && document_chats!=null) {
                    val chats = mutableListOf<Chat>()
                    for (document in document_chats) {
                        val chat = document.toObject(Chat::class.java)
                        if (chat.timestamp > lastMessageNumber && chat.datetime != null) {
                            if (chat.del_for == "Me") {
                                if (chat.del_by?.contains(sender_id)!!) chat.text =
                                    "This Message has been Deleted."
                            } else if (chat.del_for == "Everyone") {
                                chat.text = "This Message has been Deleted."
                            }
                            chat.id = document.id
                            if (chat.status == "Delivered") {
                                document.reference.update("status", "Read")
                            }
                            chats.add(chat)
                        }
                    }
                    val sortedChats = chats.sortedBy { it.timestamp }
                    chatsLiveData.postValue(ScreenState.Success(sortedChats))
                }
            }
        }
    }

    private fun fetchReceiverDetails() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).addSnapshotListener { document1, error1 ->
            val room = document1?.toObject(ChatRooms::class.java)
            receiversLiveData.postValue(ScreenState.Success(room))
        }
    }

    private fun fetchSenderDetail() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(sender_id).addSnapshotListener { value, error ->
            val sender = value?.toObject(User::class.java)
            senderDetailsLiveData.postValue(ScreenState.Success(sender))
        }
    }

    private fun fetchReceiverDetail() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(receiver_id).addSnapshotListener { value, error ->
            val receiver = value?.toObject(User::class.java)
            receiverDetailsLiveData.postValue(ScreenState.Success(receiver))
        }
    }

    fun sendMessage(chat: Chat) {
        var unread_count: Long = 0
        var message_number: Long = 0
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).get().addOnSuccessListener {
            val room: ChatRooms = it.toObject(ChatRooms::class.java)!!
            unread_count = room.unread_count + 1
            message_number = room.message_number + 1
            chat.timestamp = message_number
            val map = HashMap<String,Any>()
            val properties = chat.javaClass.declaredFields
            for (property in properties) {
                property.isAccessible = true
                map[property.name] = property.get(chat)!!
            }
            map["datetime"] = FieldValue.serverTimestamp()
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).collection(Constants.KEY_COLLECTION_CHAT).add(map).addOnSuccessListener { doc ->
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).update("last_text",chat.text,"message_number",message_number,"timestamp", FieldValue.serverTimestamp(),"last_text_from",sender_id,"unread_count",unread_count,"last_msg_del_status",ArrayList<String>(),"last_msg_id",it.id)
            }
        }
    }

    fun sendNotification(message: String) {
        ApiClient.getInstance().create(ApiService::class.java).sendMessage(Constants.getRemoteMsgHeaders(),message).enqueue(object :
            Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful) {
                    try {
                        if(response.body()!=null) {
                            val responseJson: JSONObject = JSONObject(response.body()!!)
                            val results: JSONArray = responseJson.getJSONArray("results")
                            if(responseJson.getInt("failure")==1) {
                                val error: JSONObject = results.get(0) as JSONObject
                                return
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    //TODO
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    fun deleteMessage(id: String, flag: String) {
        if(flag=="Everyone") {
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).get().addOnSuccessListener {
                val room = it.toObject(ChatRooms::class.java)
                if(room?.last_msg_id==id) {
                    val map: HashMap<String,Any> = HashMap()
                    room.last_msg_del_status?.add(sender_id)
                    room.last_msg_del_status?.add(receiver_id)
                    map["last_msg_del_status"] = room.last_msg_del_status!!
                    database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).update(map)
                }
            }
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).collection(Constants.KEY_COLLECTION_CHAT).document(id).get().addOnSuccessListener {
                val chat = it.toObject(Chat::class.java)
                val map: HashMap<String, Any> = HashMap()
                chat?.del_by?.add(sender_id)
                chat?.del_by?.add(receiver_id)
                map["del_for"] = flag
                map["del_by"] = chat?.del_by!!
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).collection(Constants.KEY_COLLECTION_CHAT).document(id).update(map)
            }
        } else {
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).get().addOnSuccessListener {
                val room = it.toObject(ChatRooms::class.java)
                if(room?.last_msg_id==id) {
                    val map: HashMap<String,Any> = HashMap()
                    room.last_msg_del_status?.add(sender_id)
                    map["last_msg_del_status"] = room.last_msg_del_status!!
                    database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).update(map)
                }
            }
            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).collection(Constants.KEY_COLLECTION_CHAT).document(id).get().addOnSuccessListener {
                val chat = it.toObject(Chat::class.java)
                val  map: HashMap<String,Any> = HashMap()
                chat?.del_by?.add(sender_id)
                map["del_for"] = flag
                map["del_by"] = chat?.del_by!!
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).collection(Constants.KEY_COLLECTION_CHAT).document(id).update(map)
            }
        }
    }

    fun updateTypingStatus(status: String) {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).get().addOnSuccessListener { it ->
            val room = it.toObject(ChatRooms::class.java)
            if(sender_id==room?.sender_id)
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).update("sender_activity",status)
            else if(sender_id==room?.receiver_id)
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).update("receiver_activity",status)
        }
    }

    fun updateReadCount() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).get().addOnSuccessListener {
            val doc = it.toObject(ChatRooms::class.java)
            if(doc?.last_text_from == receiver_id) {
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(it.id).update("unread_count",0)
            }
        }
    }

    private fun fetchWallpaper() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(chat_room_id).addSnapshotListener { value, error ->
            if(value!=null) {
                val room = value.toObject(ChatRooms::class.java)
                if(sender_id==room?.sender_id) {
                    wallpapersLiveData.postValue(ScreenState.Success(room.sender_local_chat_wallpaper))
                } else {
                    wallpapersLiveData.postValue(ScreenState.Success(room?.receiver_local_chat_wallpaper))
                }
            }
        }
    }

}