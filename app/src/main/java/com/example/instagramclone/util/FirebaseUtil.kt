package com.example.instagramclone.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object Firebase {
    val auth get() = FirebaseAuth.getInstance()
    val firestore get() = FirebaseFirestore.getInstance()
    val storage get() = FirebaseStorage.getInstance()
}