package com.example.instagramclone.function

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.model.AlarmDTO
import com.example.instagramclone.util.Firebase
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class AlarmViewModel : ViewModel(){

    //알람 리스트를 가지는 Flow
    val alarmsFlow = Firebase.firestore
        .collection("alarms")
        .whereEqualTo("destinationUid", Firebase.auth.currentUser?.uid)
        .snapshots()
        .mapLatest { value ->
            value.documents.map {
                it.toObject(AlarmDTO::class.java)
                    ?: throw IllegalArgumentException("null returned")
            }
        }.catch {
            it.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}