package com.example.flyer.activity

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatRoomId: String
    private lateinit var userId: String
    private lateinit var senderId: String
    private lateinit var receiverId: String
    private lateinit var imageUrl: String
    private lateinit var adapter: ChatAdapter
    private var senderSet = HashSet<Int>()
    private var receiverSet = HashSet<Int>()
    private var replyPos: Long = 0
    private lateinit var senderDetails: User
    private lateinit var receiverDetails: User


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = FirebaseAuth.getInstance().uid!!
        val id1 = intent.getStringExtra(Constants.KEY_SENDER_ID)!!
        val id2 = intent.getStringExtra(Constants.KEY_RECEIVER_ID)!!
        chatRoomId = intent.getStringExtra(Constants.KEY_CHAT_ROOM_ID)!!
        if(userId == id1) {
            senderId = id1
            receiverId = id2
        } else {
            senderId = id2
            receiverId = id1
        }
        viewModel = ViewModelProvider(this, ChatViewModelFactory(senderId,receiverId,chatRoomId))[ChatViewModel::class.java]
        updateUnreadCount()
        viewModel.senderDetailLiveData.observe(this, Observer { state ->
            senderDetails = state.data!!
        })
        viewModel.receiverDetailLiveData.observe(this, Observer { state ->
            receiverDetails = state.data!!
            processReceiverAvailabilityStatus()
        })
        viewModel.receiverLiveData.observe(this, Observer { state ->
            processReceiverDetails(state)
        })
        viewModel.wallpaperLiveData.observe(this , Observer { state ->
            if(state.data!="") {
                processWallpaper(state)
            }
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
            sendMessage()
            showHeaderDefault()
            binding.replyCv.visibility = View.GONE
            if(!receiverDetails.online_status!!) {
                try {
                    val tokens: JSONArray = JSONArray()
                    tokens.put(receiverDetails.fcmtoken)
                    val data: JSONObject = JSONObject()
                    data.put(Constants.KEY_USER_ID,userId)
                    data.put(Constants.KEY_CHAT_ROOM_ID,chatRoomId)
                    data.put(Constants.KEY_NAME,senderDetails.name)
                    data.put(Constants.KEY_FCM_TOKEN,senderDetails.fcmtoken)
                    data.put(Constants.KEY_MESSAGE,binding.chatsreenEtWritemessage.text.toString())
                    val body: JSONObject = JSONObject()
                    body.put(Constants.REMOTE_MSG_DATA,data)
                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens)
                    viewModel.sendNotification(body.toString())
                } catch (e: Exception) {
                    showToast(e.message!!)
                }
            }
            binding.chatsreenEtWritemessage.setText("")
        }
        binding.chatscreenIvBack.setOnClickListener {
            onBackPressed()
        }
        binding.chatscreenIvDel.setOnClickListener {
            showDeleteDialog()
        }
        binding.chatscreenIvReply.setOnClickListener {
            reply()
        }
        binding.replyCancel.setOnClickListener {
            replyCancel()
        }
        binding.chatscreenIvPin.setOnClickListener {
            showAttachmentDialog()
        }
    }

    private fun updateUnreadCount() {
        viewModel.updateReadCount()
    }

    private fun processReceiverAvailabilityStatus() {
        if(receiverDetails.online_status==true) {
            binding.chatscreenCvStatus.setCardBackgroundColor(ContextCompat.getColor(this@ChatActivity,R.color.schatscreen_color_online))
        } else {
            binding.chatscreenCvStatus.setCardBackgroundColor(ContextCompat.getColor(this@ChatActivity,R.color.schatscreen_color_offline))
        }
    }

    private fun activityListener() {
        binding.chatsreenEtWritemessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.updateTypingStatus("Typing...")
            }

            override fun afterTextChanged(p0: Editable?) {
                binding.chatsreenEtWritemessage.post {
                    binding.chatsreenEtWritemessage.height = binding.chatsreenEtWritemessage.lineHeight * binding.chatsreenEtWritemessage.lineCount
                }
                viewModel.updateTypingStatus("")
            }

        })
    }

    private fun processWallpaper(state: ScreenState<String?>) {
        when(state) {
            is ScreenState.Success -> {
                Glide.with(this)
                    .load(state.data)
                    .centerCrop()
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            binding.chatactivityClMain.background = resource
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Called when the image is no longer needed to be displayed
                        }
                    })
            }
            else -> {
                //TODO
            }
        }
    }

    private fun processReceiverDetails(state: ScreenState<ChatRooms?>) {
        when(state) {
            is ScreenState.Loading -> {

            }
            is ScreenState.Success -> {
                if (state.data != null) {
                    if(state.data.sender_id==userId) {
                        imageUrl = state.data.receiver_image!!
                        Glide
                            .with(this)
                            .load(state.data.receiver_image)
                            .centerCrop()
                            .placeholder(R.drawable.ic_placeholder_1)
                            .into(binding.chatscreenIvProfile)
                        binding.chatscreenTvName.text = state.data.receiver_name
                        binding.chatscreenTvActivity.text = state.data.receiver_activity
                    } else {
                        imageUrl = state.data.sender_image!!
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
                    val rv = binding.chatscreenRvChat
                    adapter = ChatAdapter(this, state.data, imageUrl, binding.chatscreenTvName.text.toString(),rv,senderId)
                    val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    rv.layoutManager = lm
                    rv.adapter = adapter
                    adapter.liveSenderSet.observe(this, Observer { sender_state ->
                        senderSet = sender_state
                        processUi()
                    })
                    adapter.liveReceiverSet.observe(this, Observer { receiver_state ->
                        receiverSet = receiver_state
                        processUi()
                    })
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
    private fun sendMessage() {
        val text: String = binding.chatsreenEtWritemessage.text.toString().trim()
        var message_number: Long = 0
        var unread_count: Long = 0
        var replyAttached: Boolean = false
        var replyTo: String = ""
        var replyBy: String = ""
        var replyText:String = ""
        if(binding.replyCv.visibility==View.VISIBLE) {
            replyAttached = true
            replyTo = if(binding.replyName.text.toString()=="You") userId
            else receiverId
            replyBy = userId
            replyText = binding.replyMsg.text.toString()
        }
        val chat = Chat("",senderId,text, Timestamp(Date()),message_number,"text","Sent", false,"",ArrayList(),replyAttached,replyTo,replyBy,replyPos,replyText)
        viewModel.sendMessage(chat)
    }

    private fun showDeleteDialog() {
        val bottomSheet: BottomSheetDialog = BottomSheetDialog(this, R.style.bottomSheetStyle_longpressdelchat)
        bottomSheet.setContentView(R.layout.longpress_chat_dialog)
        bottomSheet.show()
        val ll_first = bottomSheet.findViewById<LinearLayout>(R.id.chatscree_del_chat_ll_first)
        val ll_second = bottomSheet.findViewById<LinearLayout>(R.id.chatscree_del_chat_ll_second)
        val delForEve = bottomSheet.findViewById<LinearLayout>(R.id.chatscree_ll_delForEve)
        val delForMe = bottomSheet.findViewById<LinearLayout>(R.id.chatscree_ll_delForMe)
        val no = bottomSheet.findViewById<LinearLayout>(R.id.longpress_del_chat_no)
        val yes = bottomSheet.findViewById<LinearLayout>(R.id.longpress_del_chat_yes)
        val text = bottomSheet.findViewById<TextView>(R.id.longpress_del_chat_tvtop)
        var flag: String = ""
        if(receiverSet.size>0) {
            ll_first?.visibility = View.VISIBLE
            ll_second?.visibility = View.GONE
            delForEve?.visibility = View.GONE
            delForMe?.visibility = View.VISIBLE
            no?.visibility = View.GONE
            yes?.visibility = View.GONE
            text?.visibility = View.GONE
        } else if(senderSet.size>0) {
            ll_first?.visibility = View.VISIBLE
            ll_second?.visibility = View.GONE
            delForEve?.visibility = View.VISIBLE
            delForMe?.visibility = View.VISIBLE
            no?.visibility = View.GONE
            yes?.visibility = View.GONE
            text?.visibility = View.GONE
        }
        delForEve?.setOnClickListener {
            flag = "Everyone"
            ll_first?.visibility = View.GONE
            ll_second?.visibility = View.VISIBLE
            delForEve.visibility = View.GONE
            delForMe?.visibility = View.GONE
            no?.visibility = View.VISIBLE
            yes?.visibility = View.VISIBLE
            text?.visibility = View.VISIBLE
            yes?.setOnClickListener {
                val list = adapter.getSelectedItems()
                for(chat in list) {
                    viewModel.deleteMessage(chat.id!!,flag)
                }
                showHeaderDefault()
                bottomSheet.dismiss()
            }
        }
        delForMe?.setOnClickListener {
            flag = "Me"
            ll_first?.visibility = View.GONE
            ll_second?.visibility = View.VISIBLE
            delForEve?.visibility = View.GONE
            delForMe.visibility = View.GONE
            no?.visibility = View.VISIBLE
            yes?.visibility = View.VISIBLE
            text?.visibility = View.VISIBLE
            yes?.setOnClickListener {
                val list = adapter.getSelectedItems()
                for(chat in list) {
                    viewModel.deleteMessage(chat.id!!,flag)
                }
                showHeaderDefault()
                bottomSheet.dismiss()
            }
        }
        no?.setOnClickListener {
            bottomSheet.dismiss()
        }
    }

    private fun reply() {
        binding.replyCv.visibility = View.VISIBLE
        val selectedItems = adapter.getSelectedItems()
        if(selectedItems[0].from_id==senderId) {
            binding.replyBar.setBackgroundColor(ContextCompat.getColor(this@ChatActivity,R.color.reply_sender_color))
            binding.replyName.setTextColor(ContextCompat.getColor(this@ChatActivity,R.color.reply_sender_color))
            binding.replyMsg.text = selectedItems[0]?.text?.toString()
            binding.replyName.text = "You"
            replyPos = selectedItems[0]?.timestamp!!
        } else {
            binding.replyBar.setBackgroundColor(ContextCompat.getColor(this@ChatActivity,R.color.reply_receiver_color))
            binding.replyName.setTextColor(ContextCompat.getColor(this@ChatActivity,R.color.reply_receiver_color))
            binding.replyMsg.text = selectedItems[0]?.text
            binding.replyName.text = binding.chatscreenTvName.text
            replyPos = selectedItems[0]?.timestamp!!
        }
        showHeaderDefault()
    }

    private fun replyCancel() {
        binding.replyCv.visibility = View.GONE
        adapter.getSelectedItems()
    }

    private fun processUi() {
        if(senderSet.size>0 || receiverSet.size>0) {
            showHeaderAlternate()
            if(senderSet.size+receiverSet.size>1) binding.chatscreenIvReply.visibility = View.GONE
        } else {
            showHeaderDefault()
        }
    }
    private fun showHeaderDefault() {
        binding.chatscreenIvDots.visibility = View.VISIBLE
        binding.chatscreenIvProfile.visibility = View.VISIBLE
        binding.chatscreenTvActivity.visibility = View.VISIBLE
        binding.chatscreenTvName.visibility = View.VISIBLE
        binding.chatscreenCvStatus.visibility = View.VISIBLE
        binding.chatscreenIvReply.visibility = View.GONE
        binding.chatscreenIvDel.visibility = View.GONE
        binding.chatscreenIvForward.visibility = View.GONE
        binding.chatscreenIvVerticaldots.visibility = View.GONE
    }

    private fun showHeaderAlternate() {
        binding.chatscreenIvDots.visibility = View.GONE
        binding.chatscreenIvProfile.visibility = View.GONE
        binding.chatscreenTvActivity.visibility = View.GONE
        binding.chatscreenTvName.visibility = View.GONE
        binding.chatscreenCvStatus.visibility = View.GONE
        binding.chatscreenIvReply.visibility = View.VISIBLE
        binding.chatscreenIvDel.visibility = View.VISIBLE
        binding.chatscreenIvForward.visibility = View.VISIBLE
        binding.chatscreenIvVerticaldots.visibility = View.VISIBLE
    }

    private fun showAttachmentDialog() {
        val bottomSheet: BottomSheetDialog = BottomSheetDialog(this, R.style.bottomSheetStyle_attachmentpopup)
        bottomSheet.setContentView(R.layout.attachmentbottomsheet_dialog)
        bottomSheet.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this@ChatActivity,message,Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if(senderSet.size+receiverSet.size>0) {
            adapter.getSelectedItems()
        } else {
            super.onBackPressed()
        }
    }

}