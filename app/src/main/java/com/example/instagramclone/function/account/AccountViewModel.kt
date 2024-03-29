package com.example.instagramclone.function.account

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.model.ALARM_FOLLOW
import com.example.instagramclone.model.AlarmDTO
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.model.FollowDTO
import com.example.instagramclone.util.FcmPush
import com.example.instagramclone.util.Firebase
import com.example.instagramclone.util.PathString
import com.example.instagramclone.util.SystemString
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import java.lang.String.format

class AccountViewModel(private val uid: String?) : ViewModel(){
    val profileImageUrl = Firebase.firestore
        .collection(PathString.profileImages)
        .document(uid ?: "")
        .snapshots()
        .mapLatest { value ->
            value.data?.get("image").toString()
        }.catch {
            it.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val followData = Firebase.firestore
        .collection(PathString.users)
        .document(uid ?: requireNotNull(uid))
        .snapshots()
        .mapLatest { value ->
            value.toObject(FollowDTO::class.java) ?: throw IllegalArgumentException("null returned")
        }.catch {
            it.printStackTrace()
        }

    val postList = Firebase.firestore
        .collection(PathString.images)
        .whereEqualTo("uid", uid ?: "")
        .snapshots()
        .mapLatest { value ->
            value.documents.map {
                it.toObject(ContentDTO::class.java)?.copy(contentUid = it.id)
                    ?: throw IllegalArgumentException("null returned")
            }
        }.catch {
            it.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    /**
     * 팔로우/언팔로우 요청 처리
     */
    fun requestFollow(followDto: FollowDTO) = kotlin.runCatching {
        val tsDocMyFollowing = Firebase.firestore.collection("users").document(Firebase.auth.currentUser?.uid!!)
        Firebase.firestore.runTransaction {
            val myFollow = it.get(tsDocMyFollowing).toObject(FollowDTO::class.java) ?: FollowDTO()
            if (myFollow.followings.containsKey(uid!!)) {
                it.set(
                    tsDocMyFollowing,
                    myFollow.copy(followingCount = myFollow.followingCount - 1).apply { followings.remove(uid!!) }
                )
            } else {
                it.set(
                    tsDocMyFollowing,
                    myFollow.copy(followingCount = myFollow.followingCount + 1).apply { followings[uid!!] = true }
                )
            }
        }

        val tsDocOthersFollower = Firebase.firestore.collection("users").document(uid!!)
        Firebase.firestore.runTransaction {
            if (followDto.followers.containsKey(Firebase.auth.currentUser?.uid)) {
                it.set(
                    tsDocOthersFollower,
                    followDto.copy(followerCount = followDto.followerCount - 1).apply { followers.remove(Firebase.auth.currentUser?.uid) }
                )
            } else {
                it.set(
                    tsDocOthersFollower,
                    followDto.copy(followerCount = followDto.followerCount + 1).apply { followers[Firebase.auth.currentUser?.uid!!] = true }
                )

                registerFollowAlarm()
            }
        }
    }

    // 팔로우 시 상대방에게 푸시알람 전송
    private fun registerFollowAlarm() = uid?.let {
        val alarmDto = AlarmDTO(
            destinationUid = it,
            userId = Firebase.auth.currentUser?.email ?: "",
            uid = Firebase.auth.currentUser?.uid ?: "",
            kind = ALARM_FOLLOW,
            timeStamp = System.currentTimeMillis()
        )

        Firebase.firestore
            .collection("alarms")
            .document()
            .set(alarmDto)

        val msg = format(SystemString.ALARM_FOLLOW, Firebase.auth.currentUser?.email ?: "")
        FcmPush.sendMessage(it, "Instagram-clone", msg)
    }
}

class AccountViewModelFactory(private val uid: String?) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AccountViewModel(uid) as T
}