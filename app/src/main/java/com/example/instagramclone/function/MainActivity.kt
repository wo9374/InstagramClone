package com.example.instagramclone.function

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ActivityMainBinding
import com.example.instagramclone.util.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        NavigationUI.setupWithNavController(
            binding.bottomNavigation,
            findNavController(R.id.main_content)
        )

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        registerPushToken()

        binding.toolbarBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun registerPushToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result ?: ""
                val uid = Firebase.auth.currentUser?.uid ?: return@addOnCompleteListener

                Firebase.firestore
                    .collection("pushtokens")
                    .document(uid)
                    .set(mapOf("pushToken" to token))
            }
        }
    }
}