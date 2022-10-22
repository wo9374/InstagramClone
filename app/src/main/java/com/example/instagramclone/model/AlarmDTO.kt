package com.example.instagramclone.model

const val ALARM_LIKE = 0
const val ALARM_COMMENT = 1
const val ALARM_FOLLOW = 2

data class AlarmDTO(
    var destinationUid: String = "",
    var userId: String = "",
    var uid: String = "",
    var kind: Int = 0,
    var message: String = "",
    var timeStamp: Long = 0
)
