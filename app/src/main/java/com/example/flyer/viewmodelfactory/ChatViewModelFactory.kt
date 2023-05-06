package com.example.flyer.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flyer.viewmodels.ChatViewModel

class ChatViewModelFactory(private val senderid: String, private val receiverid: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(senderid,receiverid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}