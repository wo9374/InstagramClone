package com.example.instagramclone.function

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ItemCommentBinding
import com.example.instagramclone.model.ALARM_COMMENT
import com.example.instagramclone.model.ALARM_FOLLOW
import com.example.instagramclone.model.ALARM_LIKE
import com.example.instagramclone.model.AlarmDTO
import com.example.instagramclone.util.Firebase

class AlarmListAdapter : ListAdapter<AlarmDTO, AlarmListAdapter.AlarmViewHolder>(AlarmDiffUtil()) {

    inner class AlarmViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.commentTxt.visibility = View.INVISIBLE
        }

        internal fun bind(item: AlarmDTO) {
            Firebase.firestore
                .collection("profileImages")
                .document(item.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result["image"]

                        Glide.with(binding.profileImg)
                            .load(url)
                            .circleCrop()
                            .error(R.drawable.ic_account)
                            .into(binding.profileImg)
                    }
                }

            val text = when (item.kind) {
                ALARM_LIKE -> binding.root.context.getString(R.string.alarm_favorite)
                ALARM_COMMENT -> "${binding.root.context.getString(R.string.alarm_comment)} of ${item.message}"
                ALARM_FOLLOW -> binding.root.context.getString(R.string.alarm_follow)
                else -> ""
            }
            binding.userIdTxt.text = "${item.userId} $text"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

internal class AlarmDiffUtil : DiffUtil.ItemCallback<AlarmDTO>() {
    override fun areItemsTheSame(oldItem: AlarmDTO, newItem: AlarmDTO): Boolean {
        return (oldItem.uid == newItem.uid) and (oldItem.timeStamp == newItem.timeStamp)
    }

    override fun areContentsTheSame(oldItem: AlarmDTO, newItem: AlarmDTO): Boolean {
        return oldItem.kind == newItem.kind
    }
}