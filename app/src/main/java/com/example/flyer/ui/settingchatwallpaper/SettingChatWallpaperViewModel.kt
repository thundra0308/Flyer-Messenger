package com.example.flyer.ui.settingchatwallpaper

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flyer.models.User
import com.example.flyer.screenstate.ScreenState
import com.example.flyer.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SettingChatWallpaperViewModel(): ViewModel() {

    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var wallpapersLiveData: MutableLiveData<ScreenState<String?>> = MutableLiveData()
    val wallpaperLiveData: LiveData<ScreenState<String?>>
        get() = wallpapersLiveData

    init {
        fetchWallpaperData()
    }

    private fun fetchWallpaperData() {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(FirebaseAuth.getInstance().uid!!).addSnapshotListener { value, error ->
            if(value!=null) {
                val user = value.toObject(User::class.java)
                wallpapersLiveData.postValue(ScreenState.Success(user?.global_chat_wallpaper))
            }
        }
    }

    fun uploadWallpaper(image: Uri?, path: String) {
        if(image!=null) {
            storage = FirebaseStorage.getInstance()
            val storageReference: StorageReference = storage.reference.child(path)
            storageReference.putFile(image).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    updateUserWallpaper(uri.toString())
                }
            }.addOnFailureListener { exception ->
                //TODO
            }
        } else {
            updateUserWallpaper("")
        }
    }

    private fun updateUserWallpaper(image: String) {
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(FirebaseAuth.getInstance().uid!!).update("global_chat_wallpaper",image)
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("sender_id",FirebaseAuth.getInstance().uid).addSnapshotListener { value, error ->
            if(value!=null && !value.isEmpty) {
                for(doc in value.documents) {
                    database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(doc.id).update("sender_local_chat_wallpaper",image)
                }
            }
        }
        database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).whereEqualTo("receiver_id",FirebaseAuth.getInstance().uid).addSnapshotListener { value, error ->
            if(value!=null && !value.isEmpty) {
                for(doc in value.documents) {
                    database.collection(Constants.KEY_COLLECTION_CHAT_ROOMS).document(doc.id).update("receiver_local_chat_wallpaper",image)
                }
            }
        }
    }

}