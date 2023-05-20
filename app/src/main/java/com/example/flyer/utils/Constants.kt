package com.example.flyer.utils

object Constants {
    const val KEY_COLLECTION_USER: String = "users"
    const val KEY_COLLECTION_CHAT_ROOMS: String = "chat_rooms"
    const val KEY_COLLECTION_CHAT: String = "chat"
    const val KEY_SENDER_ID: String = "sender_id"
    const val KEY_RECEIVER_ID: String = "receiver_id"
    const val KEY_CHAT_ROOM_ID: String = "chat_room_id"
    const val KEY_AVAILABILITY: String = "online_status"
    const val KEY_FCM_TOKEN: String = "fcm_token"
    const val KEY_MESSAGE: String = "message"
    const val KEY_USER: String = "user"
    const val KEY_CHAT_ID: String = "chat_id"
    const val KEY_NAME: String = "name"
    const val KEY_EMAIL: String = "email"
    const val KEY_PASSWORD: String = "password"
    const val KEY_PHONE_NUMBER: String = "phone_number"
    const val KEY_REFERENCE_NAME: String = "chat_app_preference"
    const val KEY_IS_SIGNED_IN = "is_logged_in"
    const val KEY_USER_ID: String = "user_id"
    const val REMOTE_MSG_AUTHORIZATION: String = "Authorization"
    const val REMOTE_MSG_CONTENT_TYPE: String = "Content-Type"
    const val REMOTE_MSG_DATA: String = "data"
    const val REMOTE_MSG_REGISTRATION_IDS: String = "registration_ids"
    private var remoteMsgHeaders: HashMap<String,String>? = null
    fun getRemoteMsgHeaders(): HashMap<String,String> {
        if(remoteMsgHeaders==null) {
            remoteMsgHeaders = HashMap<String,String>()
            remoteMsgHeaders!!.put(REMOTE_MSG_AUTHORIZATION,"key=AAAAcJpDihM:APA91bGPSPUM398Z0Gk92G20EEihWUyvBp00SqWctx8vkcXm9TDsPWQQh9HIyrh7Br9Gw8HToLHvjhd1X-aNAEjtH5ewaG0cij46dvPfkmkZkRuGDN-gdUFAY_i1t0ZOOA97szp1FJPE")
            remoteMsgHeaders!!.put(REMOTE_MSG_CONTENT_TYPE,"application/json")
        }
        return remoteMsgHeaders!!
    }
}