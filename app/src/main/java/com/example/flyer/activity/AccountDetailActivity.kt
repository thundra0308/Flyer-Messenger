package com.example.flyer.activity

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.flyer.R
import com.example.flyer.databinding.ActivityAccountDetailBinding
import com.example.flyer.models.User
import com.example.flyer.screenstate.ScreenState
import com.example.flyer.utils.Constants
import com.example.flyer.viewmodels.AccountDetailViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException

class AccountDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountDetailBinding
    private lateinit var database: FirebaseFirestore
    private var saveImageToInternalStorage: Uri? = null
    private var mProfileImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel: AccountDetailViewModel = ViewModelProvider(this).get(AccountDetailViewModel::class.java)
        viewModel.userLiveData.observe(this, Observer { state ->
            setUpUi(state)
            setUpListeners()
        })
    }

    private fun setUpListeners() {
        binding.accountsetailIvProfile.setOnClickListener {
            choosePhotoFromGallery()
        }
        binding.accountdetailBtnSave.setOnClickListener {
            if(saveImageToInternalStorage!=null) {
                uploadUserImage()
            } else {
                updateUserProfileData()
            }
        }
    }

    private fun setUpUi(state: ScreenState<User?>) {
        binding.accountdetailEtName.setText(state.data?.name)
        binding.accountdetailEtEmail.setText(state.data?.email)
        binding.accountdetailEtPhonenumber.setText(state.data?.phone)
        binding.accountdetailEtThoughts.setText(state.data?.text_status)
        Glide
            .with(this)
            .load(state.data?.image)
            .centerCrop()
            .placeholder(R.drawable.profile_icon_placeholder_1)
            .into(binding.accountsetailIvProfile)
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String,Any>()
        var anyChangesMade = false
        if(mProfileImageUrl!=null) {
            userHashMap["image"] = mProfileImageUrl
        }
        userHashMap["name"] = binding.accountdetailEtName.text.toString()
        userHashMap["text_status"] = binding.accountdetailEtThoughts.text.toString()
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(FirebaseAuth.getInstance().uid!!).update(userHashMap).addOnSuccessListener {
            Toast.makeText(this@AccountDetailActivity,"Profile Updated Successfully",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this@AccountDetailActivity,"Failed to Update the Data",Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        @Suppress("DEPRECATION")
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage = data.data
                        try {
                            Glide
                                .with(this)
                                .load(saveImageToInternalStorage)
                                .centerCrop()
                                .placeholder(R.drawable.profile_icon_placeholder_1)
                                .into(binding.accountsetailIvProfile)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }
    }

    private fun uploadUserImage() {
        if(saveImageToInternalStorage!=null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child("USER_IMAGE"+System.currentTimeMillis()+"."+getFileExtension(saveImageToInternalStorage!!))
            sRef.putFile(saveImageToInternalStorage!!).addOnSuccessListener {
                    taskSnapshot ->
                Log.i("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri->
                    Log.i("Downloadable Image URL", uri.toString())
                    mProfileImageUrl = uri.toString()
                    updateUserProfileData()
                }
            }.addOnFailureListener {
                    exception ->
                Toast.makeText(this@AccountDetailActivity,exception.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                showRationalDialog("Permission Denied","It Looks Like You have Turned Off Permission for Storage.. Please Enable it From Settings to Continue..")
            }
        }).onSameThread().check()
    }

    fun showRationalDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message).setPositiveButton("Go To Settings"){
                _, _->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package",packageName,null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel") {
                dialog,_->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun getFileExtension(uri: Uri): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object{
        private const val GALLERY = 1
    }

}