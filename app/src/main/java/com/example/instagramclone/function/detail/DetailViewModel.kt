package com.example.instagramclone.function.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.Firebase
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class DetailViewModel : ViewModel() {

    val itemsFlow = Firebase.firestore
        .collection("images")
        .orderBy("timeStamp")
        .snapshots()
        .mapLatest { value ->
            value.documents.map {
                it.toObject(ContentDTO::class.java)
                    ?:throw  IllegalArgumentException("null returned")
            }
        }.catch {
            it.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}