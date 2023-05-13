package com.example.flyer.ui.chats

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flyer.R
import com.example.flyer.activity.ChatActivity
import com.example.flyer.adapters.ChatsAdapter
import com.example.flyer.adapters.ContactsAdapter
import com.example.flyer.databinding.FragmentChatsBinding
import com.example.flyer.databinding.FragmentContactsBinding
import com.example.flyer.models.ChatRooms
import com.example.flyer.models.User
import com.example.flyer.screenstate.ScreenState
import com.example.flyer.ui.contacts.ContactsViewModel
import com.example.flyer.utils.Constants
import com.example.flyer.viewmodelfactory.ChatsViewModelFactory
import com.example.flyer.viewmodelfactory.ContactViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseFirestore
    private val senderid: String = FirebaseAuth.getInstance().uid!!
    private lateinit var sender: User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val viewModel: ChatsViewModel = ViewModelProvider(this, ChatsViewModelFactory(senderid,""))[ChatsViewModel::class.java]
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(senderid).addSnapshotListener { value, error ->
            sender = value?.toObject(User::class.java)!!
        }
        viewModel.chatLiveData.observe(viewLifecycleOwner) { state ->
            processChatList(state)
        }
        return root
    }

    private fun processChatList(state: ScreenState<List<ChatRooms>?>) {
        when(state) {
            is ScreenState.Loading -> {

            }
            is ScreenState.Success -> {

                if(state.data!=null) {
                    Log.e("Size of Contacts List", "${state.data.size}")
                    val adapter = ChatsAdapter(requireContext(),state.data)
                    adapter.setOnClickListener(object : ChatsAdapter.onItemClickListener{
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
                    val rv = binding.chatscreenRvChats
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

}