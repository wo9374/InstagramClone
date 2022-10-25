package com.example.instagramclone.model

data class PushDTO(
    val to: String = "",
    val notification: Notification = Notification()
) {
    data class Notification(
        val title: String = "",
        val body: String = ""
    )
}
