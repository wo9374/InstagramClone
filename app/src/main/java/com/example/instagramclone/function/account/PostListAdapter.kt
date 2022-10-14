package com.example.instagramclone.function.account

import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramclone.model.ContentDTO

class PostListAdapter : ListAdapter<ContentDTO, PostListAdapter.PostViewHolder>(PostDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val size = parent.resources.displayMetrics.widthPixels / 3

        val imageView = ImageView(parent.context).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(size, size)
        }
        return PostViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val view: ImageView) : RecyclerView.ViewHolder(view) {
        internal fun bind(item: ContentDTO) {
            Glide.with(view)
                .load(item.imageUrl)
                .centerCrop()
                .into(view)
        }
    }
}

class PostDiffUtil : DiffUtil.ItemCallback<ContentDTO>() {
    override fun areItemsTheSame(oldItem: ContentDTO, newItem: ContentDTO): Boolean {
        return (oldItem.timeStamp == newItem.timeStamp) and (oldItem.uid == newItem.uid)
    }

    override fun areContentsTheSame(oldItem: ContentDTO, newItem: ContentDTO): Boolean {
        return oldItem.imageUrl == newItem.imageUrl
    }
}