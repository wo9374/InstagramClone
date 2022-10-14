package com.example.instagramclone.function.account

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.instagramclone.util.Firebase.auth
import com.example.instagramclone.util.PathString
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.mapLatest

class AccountViewModel : ViewModel(){
    val profileImageUrl = Firebase.firestore
        .collection(PathString.profileImages)
        .document(auth.uid ?: "")
        .snapshots()
        .mapLatest { value ->
            value.data?.get("image").toString()
        }

    /*private val followData = Firebase.firestore
        .collection(PathString.users)
        .document(uid ?: "")
        .snapshots()
        .mapLatest { value ->
            value.toObject(FollowDto::class.java) ?: throw IllegalArgumentException("null returned")
        }

    private val postList = Firebase.firestore
        .collection(PathString.images)
        .whereEqualTo("uid", uid ?: "")
        .snapshots()
        .mapLatest { value ->
            value.documents.map {
                it.toObject(ContentDto::class.java)?.copy(contentUid = it.id)
                    ?: throw IllegalArgumentException("null returned")
            }
        }*/

    fun uploadProfileImage(imageUri: Uri?, callback: (() -> Unit)? = null) = imageUri?.also { Firebase.auth.currentUser?.uid?.also { uid ->
        Firebase.storage.reference.child(PathString.userProfileImages).child(uid).also { ref ->
            ref.putFile(imageUri)
                .continueWithTask { ref.downloadUrl }
                .addOnSuccessListener { uri ->
                    val map = mapOf("image" to uri.toString())
                    Firebase.firestore.collection(PathString.profileImages).document(uid).set(map)

                    callback?.invoke()
                }
        }
    } }
}