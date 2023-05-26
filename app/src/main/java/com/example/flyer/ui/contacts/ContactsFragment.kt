package com.example.flyer.ui.contacts

import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flyer.R
import com.example.flyer.activity.BaseFragments
import com.example.flyer.activity.ChatActivity
import com.example.flyer.adapters.ContactsAdapter
import com.example.flyer.databinding.FragmentContactsBinding
import com.example.flyer.models.ChatRooms
import com.example.flyer.models.User
import com.example.flyer.screenstate.ScreenState
import com.example.flyer.utils.Constants
import com.example.flyer.viewmodelfactory.ChatViewModelFactory
import com.example.flyer.viewmodelfactory.ContactViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ContactsFragment : BaseFragments() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseFirestore
    private val senderid: String = FirebaseAuth.getInstance().uid!!
    private lateinit var sender: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val viewModel: ContactsViewModel = ViewModelProvider(this, ContactViewModelFactory(senderid,""))[ContactsViewModel::class.java]
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(senderid).addSnapshotListener { value, error ->
            sender = value?.toObject(User::class.java)!!
        }
        viewModel.contactLiveData.observe(viewLifecycleOwner) { state ->
            processContactList(state)
        }
        setListeners()
        return root
    }

    private fun setListeners() {
        binding.contactscreenCvAdd.setOnClickListener {
            val bottomSheet: BottomSheetDialog = BottomSheetDialog(requireContext(), R.style.bottomSheetStyleaddcontact)
            bottomSheet.setContentView(R.layout.add_contact)
            bottomSheet.show()
            val btn = bottomSheet.findViewById<CardView>(R.id.chatscreen_btn_add)
            btn?.setOnClickListener {
                val contact: String = bottomSheet.findViewById<EditText>(R.id.chatscreen_et_emailphone)?.text.toString()
                createChatRoom(contact)
                bottomSheet.dismiss()
            }
        }
    }

    private fun processContactList(state: ScreenState<List<ChatRooms>?>) {
        when(state) {
            is ScreenState.Loading -> {

            }
            is ScreenState.Success -> {

                if(state.data!=null) {
                    Log.e("Size of Contacts List", "${state.data.size}")
                    val adapter = ContactsAdapter(requireContext(),state.data)
                    adapter.setOnClickListener(object : ContactsAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val senderId = state.data[position].sender_id
                            val receiverId = state.data[position].receiver_id
                            val chat_room_id = state.data[position].id
                            val intent = Intent(context, ChatActivity::class.java)
                            intent.putExtra(Constants.KEY_SENDER_ID,senderId)
                            intent.putExtra(Constants.KEY_RECEIVER_ID,receiverId)
                            intent.putExtra(Constants.KEY_CHAT_ROOM_ID,chat_room_id)
                            startActivity(intent)
                        }
                    })
                    val rv = binding.contactscreenRvContacts
                    rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
                    rv.setHasFixedSize(true)
                    rv.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }
            is ScreenState.Error -> {

            }
        }
    }

    private fun createChatRoom(contact: String) {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).whereEqualTo("phone",contact).get().addOnSuccessListener {
            if(it.isEmpty) {
                //TODO
            } else {
                val receiver = it.documents[0].toObject(User::class.java)
                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",senderid).whereEqualTo("receiver_id",receiver?.id).get().addOnSuccessListener { it1 ->
                    if(it1.isEmpty) {
                        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",receiver?.id).whereEqualTo("receiver_id",senderid).get().addOnSuccessListener { it2 ->
                            if(it2.isEmpty) {
                                val chatRoom: ChatRooms = ChatRooms("",receiver?.id,receiver?.name,receiver?.image,"",receiver?.text_status,"", "",Timestamp(Date()),Timestamp(Date()),0,sender.id,sender.name,sender.image,"",sender.text_status,0, ArrayList(),"",sender.global_chat_wallpaper,receiver?.global_chat_wallpaper)
                                database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).add(chatRoom).addOnSuccessListener { it3 ->
                                    val hashmap: HashMap<String,Any> = HashMap()
                                    hashmap["timestamp"] = FieldValue.serverTimestamp()
                                    hashmap["date_added"] = FieldValue.serverTimestamp()
                                    hashmap["id"] = it3.id
                                    it3.update(hashmap)
                                    Toast.makeText(context,"Contact Added Successfully",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}