package com.example.instagramclone.model

data class FollowDTO(
    val followerCount: Int = 0,
    val followers: MutableMap<String, Boolean> = mutableMapOf(),
    val followingCount: Int = 0,
    val followings: MutableMap<String, Boolean> = mutableMapOf()
)