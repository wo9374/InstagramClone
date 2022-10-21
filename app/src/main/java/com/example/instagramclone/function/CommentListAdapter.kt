package com.example.instagramclone.function

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ItemCommentBinding
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.Firebase

class CommentListAdapter : ListAdapter<ContentDTO.Comment, CommentListAdapter.CommentViewHolder>(CommentDiffUtil()) {

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        internal fun bind(item: ContentDTO.Comment) {
            binding.apply {
                userIdTxt.text = item.userId
                commentTxt.text = item.comment
            }

            Firebase.firestore
                .collection("profileImages")
                .document(item.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result["image"]

                        Glide.with(binding.profileImg)
                            .load(url)
                            .error(R.drawable.ic_account)
                            .circleCrop()
                            .into(binding.profileImg)
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CommentDiffUtil : DiffUtil.ItemCallback<ContentDTO.Comment>() {
    override fun areItemsTheSame(oldItem: ContentDTO.Comment, newItem: ContentDTO.Comment, ): Boolean {
        return (oldItem.userId == newItem.userId) and (oldItem.timeStamp == newItem.timeStamp)
    }

    override fun areContentsTheSame(oldItem: ContentDTO.Comment, newItem: ContentDTO.Comment, ): Boolean {
        return oldItem.comment == newItem.comment
    }
}