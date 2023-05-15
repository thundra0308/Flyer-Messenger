package com.example.flyer.models

import android.os.Parcel
import android.os.Parcelable

data class Chat(
    var id: String? = "",
    var from_id: String? = "",
    var text: String? = "",
    var datetime: String? = "",
    var timestamp: Long = 0,
    var type: String? = "",
    var status: String? = "",
    var is_selected: Boolean = false,
    var del_for: String? = "",
    var del_by: ArrayList<String>? = ArrayList(),
    var reply_attached: Boolean = false,
    var reply_to: String? = "",
    var reply_id: String? = "",
    var reply_pos: Long = 0,
    var reply_text: String? = ""
)