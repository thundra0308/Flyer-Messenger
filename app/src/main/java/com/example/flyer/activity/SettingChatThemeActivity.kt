package com.example.flyer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.flyer.R
import com.example.flyer.databinding.ActivitySettingChatThemeBinding
import com.example.flyer.viewmodelfactory.ChatViewModelFactory
import com.example.flyer.viewmodels.ChatViewModel
import com.example.flyer.viewmodels.SettingChatThemeViewModel

class SettingChatThemeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingChatThemeBinding
    private lateinit var viewModel: SettingChatThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingChatThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[SettingChatThemeViewModel::class.java]
    }
}