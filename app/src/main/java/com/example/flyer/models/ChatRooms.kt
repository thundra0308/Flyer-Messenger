package com.example.flyer.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import java.util.*
import kotlin.collections.ArrayList

data class ChatRooms(
    var id: String? = "",
    var receiver_id: String? = "",
    var receiver_name: String? = "",
    var receiver_image: String? = "",
    var receiver_activity: String? = "",
    var receiver_thoughts: String? = "",
    var last_text: String? = "",
    var last_text_from: String? = "",
    var timestamp: Timestamp? = Timestamp(Date()),
    var date_added: Timestamp? = Timestamp(Date()),
    var message_number: Long = 0,
    var sender_id: String? = "",
    var sender_name: String? = "",
    var sender_image: String? = "",
    var sender_activity: String? = "",
    var sender_thoughts: String? = "",
    var unread_count: Long = 0,
    var last_msg_del_status: ArrayList<String>? = ArrayList(),
    var last_msg_id: String? = "",
    var sender_local_chat_wallpaper: String? = "",
    var receiver_local_chat_wallpaper: String? = "",
    var sender_last_message_number: Long = 0,
    var receiver_last_message_number: Long = 0
)