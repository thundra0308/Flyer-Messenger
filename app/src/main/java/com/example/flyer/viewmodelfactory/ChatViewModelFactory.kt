package com.example.flyer.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flyer.viewmodels.ChatViewModel

class ChatViewModelFactory(private val sender_id: String, private val receiver_id: String, private val chat_room_id: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(sender_id,receiver_id,chat_room_id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}