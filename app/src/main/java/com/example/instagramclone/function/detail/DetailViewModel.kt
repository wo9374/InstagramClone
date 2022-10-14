package com.example.instagramclone.function.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.Firebase
import com.example.instagramclone.util.Firebase.auth
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

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

    //좋아요 버튼 클릭 처리
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
}