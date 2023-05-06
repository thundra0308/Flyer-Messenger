package com.example.flyer.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import java.util.*

data class ChatRooms(
    var receiver_id: String? = "",
    var receiver_name: String? = "",
    var receiver_image: String? = "",
    var receiver_activity: String? = "",
    var last_text: String? = "",
    var last_text_from: String? = "",
    var timestamp: Timestamp? = Timestamp(Date()),
    var date_added: Timestamp? = Timestamp(Date()),
    var message_number: Long = 0,
    var sender_id: String? = "",
    var sender_name: String? = "",
    var sender_image: String? = "",
    var sender_activity: String? = "",
    var unread_count: Long = 0
)