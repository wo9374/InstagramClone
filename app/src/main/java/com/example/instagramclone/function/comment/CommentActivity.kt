package com.example.instagramclone.function.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ActivityCommentBinding
import com.example.instagramclone.model.ALARM_COMMENT
import com.example.instagramclone.model.AlarmDTO
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.FcmPush
import com.example.instagramclone.util.Firebase
import com.example.instagramclone.util.Firebase.auth
import com.example.instagramclone.util.Firebase.firestore

class CommentActivity : AppCompatActivity() {
    lateinit var binding : ActivityCommentBinding

    private val args: CommentActivityArgs by navArgs()
    private val contentUid: String by lazy { args.contentUid }
    private val destinationUid: String by lazy { args.destinationUid }

    private val listAdapter = CommentListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)

        binding.apply {
            sendBtn.setOnClickListener {
                val text = commentEditTxt.text.toString()
                sendComment(text)
                commentEditTxt.text.clear()
                registerCommentAlarm(destinationUid, text)
            }

            commentRecycler.adapter = listAdapter
            commentRecycler.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

            getCommentsFromFirestore()
        }
    }

    private fun sendComment(text: String){
        val comment = ContentDTO.Comment().apply {
            userId = auth.currentUser?.email.toString()
            uid = auth.currentUser?.uid.toString()
            comment = text
            timeStamp = System.currentTimeMillis()
        }

        firestore
            .collection("images")
            .document(contentUid)
            .collection("comments")
            .document()
            .set(comment)
    }

    private fun registerCommentAlarm(destinationUid: String?, message: String) {
        if (!destinationUid.isNullOrBlank()) {
            val alarmDto = AlarmDTO(
                destinationUid = destinationUid,
                userId = auth.currentUser?.email ?: "",
                uid = auth.currentUser?.uid ?: "",
                message = message,
                kind = ALARM_COMMENT,
                timeStamp = System.currentTimeMillis()
            )

            firestore
                .collection("alarms")
                .document()
                .set(alarmDto)

            val msg = "${auth.currentUser?.email} ${getString(R.string.alarm_comment)} of $message"
            FcmPush.sendMessage(destinationUid, "Instagram-clone", msg)
        }
    }

    private fun getCommentsFromFirestore() {
        firestore
            .collection("images")
            .document(contentUid)
            .collection("comments")
            .orderBy("timeStamp")
            .addSnapshotListener { value, error ->
                kotlin.runCatching {
                    value?.documents?.map {
                        it.toObject(ContentDTO.Comment::class.java)
                    } ?: throw IllegalArgumentException("Failed to get list(null returned)")
                }.onSuccess {
                    Log.d("TAG", "getCommentsFromFirestore: list = ${it.toString()}")
                    listAdapter.submitList(it)
                }.onFailure { e ->
                    e.printStackTrace()
                    error?.printStackTrace()
                }
            }
    }
}