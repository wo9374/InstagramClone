package com.example.instagramclone.function.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.model.ALARM_LIKE
import com.example.instagramclone.model.AlarmDTO
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.FcmPush
import com.example.instagramclone.util.Firebase
import com.example.instagramclone.util.Firebase.auth
import com.example.instagramclone.util.SystemString
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.lang.String.format

class DetailViewModel : ViewModel() {

    //Firebase 글 리스트 Flow
    var itemsFlow = Firebase.firestore
        .collection("images")
        .orderBy("timeStamp")
        .snapshots()
        .mapLatest { value ->
            value.documents.map {
                it.toObject(ContentDTO::class.java)?.copy(contentUid = it.id)
                    ?:throw  IllegalArgumentException("null returned")
            }
        }.catch {
            it.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    //좋아요 버튼 클릭 처리 및 알림 전송
    fun onLikeClicked(clickPosition: Int){

        val contentUid = itemsFlow.value[clickPosition].contentUid

        val tsDoc = Firebase.firestore.collection("images").document(contentUid)

        Firebase.firestore.runTransaction { transaction ->
            val item = transaction.get(tsDoc).toObject(ContentDTO::class.java) ?: return@runTransaction

            if (item.likedUsers.containsKey(auth.uid)){
                item.likeCount = item.likeCount - 1
                item.likedUsers.remove(auth.uid)
            }else{
                item.likeCount = item.likeCount + 1
                item.likedUsers[auth.uid!!] = true
            }

            transaction.set(tsDoc, item)
        }
    }

    //좋아요 버튼 눌렸을 때 상대방에게 알람이 가도록 처리
    fun registerLikeAlarm(clickPosition: Int) {

        val destinationUid = itemsFlow.value[clickPosition].userId

        if (destinationUid.isBlank()) return

        // 알람 내용을 firestore에 저장
        val alarmDto = AlarmDTO(
            destinationUid = destinationUid,
            userId = auth.currentUser?.email ?: "",
            uid = auth.currentUser?.uid ?: "",
            kind = ALARM_LIKE,
            timeStamp = System.currentTimeMillis()
        )

        Firebase.firestore
            .collection("alarms")
            .document()
            .set(alarmDto)

        // 푸시 알람 전송
        val msg = format(SystemString.ALARM_FAVORITE, auth.currentUser?.email ?: "")
        FcmPush.sendMessage(destinationUid, "Instagram-clone", msg)
    }
}