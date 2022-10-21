package com.example.instagramclone.function

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ActivityCommentBinding
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.Firebase
import com.example.instagramclone.util.Firebase.auth
import com.example.instagramclone.util.Firebase.firestore

class CommentActivity : AppCompatActivity() {
    lateinit var binding : ActivityCommentBinding

    lateinit var contentUid : String

    private val listAdapter = CommentListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)

        contentUid = intent.getStringExtra("contentUid") ?: ""

        binding.apply {
            sendBtn.setOnClickListener {
                sendComment( commentEditTxt.text.toString() )
                commentEditTxt.text.clear()
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