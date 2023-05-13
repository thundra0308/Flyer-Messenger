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
    var reply_pos: Long = 0
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        TODO("del_by"),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(from_id)
        parcel.writeString(text)
        parcel.writeString(datetime)
        parcel.writeLong(timestamp)
        parcel.writeString(type)
        parcel.writeString(status)
        parcel.writeByte(if (is_selected) 1 else 0)
        parcel.writeString(del_for)
        parcel.writeByte(if (reply_attached) 1 else 0)
        parcel.writeString(reply_to)
        parcel.writeString(reply_id)
        parcel.writeLong(reply_pos)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Chat> {
        override fun createFromParcel(parcel: Parcel): Chat {
            return Chat(parcel)
        }

        override fun newArray(size: Int): Array<Chat?> {
            return arrayOfNulls(size)
        }
    }
}