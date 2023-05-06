package com.example.flyer.activity

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.flyer.R
import com.example.flyer.adapters.ChatAdapter
import com.example.flyer.databinding.ActivityChatBinding
import com.example.flyer.models.Chat
import com.example.flyer.models.ChatRooms
import com.example.flyer.models.User
import com.example.flyer.screenstate.ScreenState
import com.example.flyer.utils.Constants
import com.example.flyer.viewmodelfactory.ChatViewModelFactory
import com.example.flyer.viewmodels.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var userid: String
    private lateinit var senderid: String
    private lateinit var receiverid: String
    private lateinit var imageurl: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userid = FirebaseAuth.getInstance().uid!!
        val id1 = intent.getStringExtra(Constants.KEY_SENDER_ID)!!
        val id2 = intent.getStringExtra(Constants.KEY_RECEIVER_ID)!!
        if(userid == id1) {
            senderid = id1
            receiverid = id2
        } else {
            senderid = id2
            receiverid = id1
        }
        updateUnreadCount()
        val viewModel: ChatViewModel = ViewModelProvider(this, ChatViewModelFactory(senderid,receiverid))[ChatViewModel::class.java]
        viewModel.receiverLiveData.observe(this, Observer { state ->
            processReceiverDetails(state)
        })
        viewModel.chatLiveData.observe(this, Observer { state ->
            processChatList(state)
        })
        setListeners()
        activityListener()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setListeners() {
        binding.chatscreenBtnSend.setOnClickListener {
            sendMessage(receiverid)
            binding.chatsreenEtWritemessage.setText("")
        }
        binding.chatscreenIvBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun updateUnreadCount() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",senderid).whereEqualTo("receiver_id",receiverid).get().addOnSuccessListener { it1 ->
            if(it1.isEmpty) {
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",receiverid).whereEqualTo("receiver_id",senderid).get().addOnSuccessListener { it2 ->
                    val doc = it2.documents[0].toObject(ChatRooms::class.java)
                    if(doc?.last_text_from == receiverid) {
                        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(it2.documents[0].id).update("unread_count",0)
                    }
                }
            } else {
                val doc = it1.documents[0].toObject(ChatRooms::class.java)
                if(doc?.last_text_from == receiverid) {
                    database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(it1.documents[0].id).update("unread_count",0)
                }
            }
        }
    }

    private fun activityListener() {
        binding.chatsreenEtWritemessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                database = FirebaseFirestore.getInstance()
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",senderid).whereEqualTo("receiver_id",receiverid).get().addOnSuccessListener { it1 ->
                    if(it1.isEmpty) {
                        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",receiverid).whereEqualTo("receiver_id",senderid).get().addOnSuccessListener { it2 ->
                            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(it2.documents[0].id).update("receiver_activity","Typing...")
                        }
                    } else {
                        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(it1.documents[0].id).update("sender_activity","Typing...")
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                database = FirebaseFirestore.getInstance()
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",senderid).whereEqualTo("receiver_id",receiverid).get().addOnSuccessListener { it1 ->
                    if(it1.isEmpty) {
                        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",receiverid).whereEqualTo("receiver_id",senderid).get().addOnSuccessListener { it2 ->
                            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(it2.documents[0].id).update("receiver_activity","")
                        }
                    } else {
                        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(it1.documents[0].id).update("sender_activity","")
                    }
                }
            }

        })
    }

    private fun processReceiverDetails(state: ScreenState<ChatRooms?>) {
        when(state) {
            is ScreenState.Loading -> {

            }
            is ScreenState.Success -> {
                if (state.data != null) {
                    if(state.data.sender_id==userid) {
                        imageurl = state.data.receiver_image!!
                        Glide
                            .with(this)
                            .load(state.data.receiver_image)
                            .centerCrop()
                            .placeholder(R.drawable.ic_placeholder_1)
                            .into(binding.chatscreenIvProfile)
                        binding.chatscreenTvName.text = state.data.receiver_name
                        binding.chatscreenTvActivity.text = state.data.receiver_activity
                    } else {
                        imageurl = state.data.sender_image!!
                        Glide
                            .with(this)
                            .load(state.data.sender_image)
                            .centerCrop()
                            .placeholder(R.drawable.ic_placeholder_1)
                            .into(binding.chatscreenIvProfile)
                        binding.chatscreenTvName.text = state.data.sender_name
                        binding.chatscreenTvActivity.text = state.data.sender_activity
                    }
                }
            }
            is ScreenState.Error -> {

            }
        }
    }

    private fun processChatList(state: ScreenState<List<Chat>?>) {
        when(state) {
            is ScreenState.Loading -> {

            }
            is ScreenState.Success -> {
                if (state.data != null) {
                    Log.e("Size of Chat List", "${state.data.size}")
                    val adapter = ChatAdapter(this, state.data, imageurl)
                    adapter.setOnClickListener(object : ChatAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            //TODO
                        }
                    })
                    val rv = binding.chatscreenRvChat
                    val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    rv.layoutManager = lm
                    rv.adapter = adapter
                    adapter.notifyItemInserted(state.data.size)
                    if(state.data.isNotEmpty()) {
                        rv.smoothScrollToPosition(state.data.size - 1)
                    }
                }
            }
            is ScreenState.Error -> {

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessage(receiverid: String) {
        val text: String = binding.chatsreenEtWritemessage.text.toString().trim()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val dt: String = LocalDateTime.now().format(formatter)
        var message_number: Long = 0
        var unread_count: Long = 0
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",senderid).whereEqualTo("receiver_id",receiverid).get().addOnSuccessListener { document1 ->
            if(document1.isEmpty) {
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",receiverid).whereEqualTo("receiver_id",senderid).get().addOnSuccessListener { document2 ->
                    if(document2.isEmpty) {
                        //TODO
                    } else {
                        val room: ChatRooms = document2.documents[0].toObject(ChatRooms::class.java)!!
                        unread_count = room.unread_count + 1
                        message_number = room.message_number + 1
                        val chat: Chat = Chat(senderid,text,dt,message_number,"text","Sent")
                        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(document2.documents[0].id).collection(Constants.KEY_COLLECTION_CHAT).add(chat).addOnSuccessListener {
                            database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(document2.documents[0].id).update("last_text",text,"message_number",message_number,"timestamp",FieldValue.serverTimestamp(),"last_text_from",senderid,"unread_count",unread_count)
                        }
                    }
                }
            } else {
                val room: ChatRooms = document1.documents[0].toObject(ChatRooms::class.java)!!
                unread_count = room.unread_count + 1
                message_number = room.message_number + 1
                val chat: Chat = Chat(senderid,text,dt,message_number,"text","Sent")
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(document1.documents[0].id).collection(Constants.KEY_COLLECTION_CHAT).add(chat).addOnSuccessListener {
                    database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(document1.documents[0].id).update("last_text",text,"message_number",message_number,"timestamp",FieldValue.serverTimestamp(),"last_text_from",senderid,"unread_count",unread_count)
                }
            }
        }
    }

}