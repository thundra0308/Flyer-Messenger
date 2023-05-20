package com.example.flyer.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.flyer.R
import com.example.flyer.databinding.ActivityIntroBinding
import com.example.flyer.models.User
import com.example.flyer.utils.Constants
import com.example.flyer.utils.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var prefManager: PreferenceManager
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        prefManager = PreferenceManager(applicationContext)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        val lbtn = binding.introBtnLogin
        val rbtn = binding.introBtnRegister
        lbtn.setOnClickListener {
            val bottomSheet: BottomSheetDialog = BottomSheetDialog(this, R.style.bottomSheetStyle)
            bottomSheet.setContentView(R.layout.login_dialog)
            bottomSheet.show()
            val btn = bottomSheet.findViewById<MaterialButton>(R.id.btn_login)
            btn?.setOnClickListener {
                val email: String = bottomSheet.findViewById<TextInputEditText>(R.id.login_et_email)?.text.toString()
                val password: String = bottomSheet.findViewById<TextInputEditText>(R.id.login_et_pass)?.text.toString()
                if(isValidSignInDetails(email,password)) {
                    login(email,password)
                }
                bottomSheet.dismiss()
            }
        }
        rbtn.setOnClickListener {
            val bottomSheet: BottomSheetDialog = BottomSheetDialog(this, R.style.bottomSheetStyle)
            bottomSheet.setContentView(R.layout.register_dialog)
            bottomSheet.show()
            val btn = bottomSheet.findViewById<MaterialButton>(R.id.btn_register)
            btn?.setOnClickListener {
                val name: String = bottomSheet.findViewById<TextInputEditText>(R.id.register_et_name)?.text.toString()
                val email: String = bottomSheet.findViewById<TextInputEditText>(R.id.register_et_email)?.text.toString()
                val phone: String = bottomSheet.findViewById<TextInputEditText>(R.id.register_et_phone)?.text.toString()
                val pass: String = bottomSheet.findViewById<TextInputEditText>(R.id.register_et_pass)?.text.toString()
                val confpass: String = bottomSheet.findViewById<TextInputEditText>(R.id.register_et_confpass)?.text.toString()
                if(isValidSignUpDetails(name,email,phone,pass,confpass)) {
                    val user: User = User("",name,email,pass,phone,"","",false,"","","")
                    register(user)
                }
                bottomSheet.dismiss()
            }
        }
    }

    private fun login(email: String, password: String) {
        auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                if(it.isSuccessful) {
                    prefManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
                    prefManager.putString(Constants.KEY_USER_ID,it.result.user?.uid!!)
                    val intent: Intent = Intent(this@IntroActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("Unable to Sign In")
                }
            }
    }

    private fun register(user: User) {
        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(user.email!!, user.password!!).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    user.id = auth.currentUser?.uid!!

                    database = FirebaseFirestore.getInstance()
                    database.collection(Constants.KEY_COLLECTION_USER).document(user.id!!).set(user, SetOptions.merge()).addOnSuccessListener {
                        showToast("Registered Successfully")
                    }.addOnFailureListener {
                        showToast("Failed to Add Data!!")
                    }
                } else {
                    showToast("Registration Failed!! ${task.exception}")
                }
            }
    }

    private fun isValidSignInDetails(email: String, password: String): Boolean {
        return if(email.trim().isEmpty()) {
            showToast("Enter Email")
            false
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Enter Valid Email")
            false
        } else if(password.trim().isEmpty()) {
            showToast("Enter Password")
            false
        } else {
            true
        }
    }

    private fun isValidSignUpDetails(name: String, email: String, phone: String, password: String, confirPass: String): Boolean {
        if(name.trim().isEmpty()) {
            showToast("Enter Name")
            return false
        } else if(email.trim().isEmpty()) {
            showToast("Enter Email")
            return false
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Enter Valid Email")
            return false
        } else if(password.trim().isEmpty()) {
            showToast("Enter Password")
            return false
        } else if(confirPass.trim().isEmpty()) {
            showToast("Confirm Your Password")
            return false
        } else if(password != confirPass) {
            showToast("Password and Confirm Password Must be Same")
            return false
        } else {
            return true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
    }

}