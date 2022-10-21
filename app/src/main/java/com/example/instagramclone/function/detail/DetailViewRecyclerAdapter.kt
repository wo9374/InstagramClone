package com.example.instagramclone.function.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ItemDetailBinding
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.Firebase.auth
import com.example.instagramclone.util.Firebase.firestore
import com.example.instagramclone.util.PathString


class DetailViewRecyclerAdapter : ListAdapter<ContentDTO, DetailViewHolder>(DetailDiffUtil()){

    lateinit var listener : OnDetailClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val detailItemBinding = ItemDetailBinding.inflate( LayoutInflater.from(parent.context), parent,false)
        return DetailViewHolder(detailItemBinding, listener)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        // currentList: 해당 Adapter 에 "submitList()"를 통해 삽입한 아이템 리스트
        holder.bind(currentList[position])
    }

    interface OnDetailClickListener {
        fun onLikeClick(v: View, position: Int)
        fun onProfileClick(v: View, item: ContentDTO)
        fun onCommentClick(v: View, item: ContentDTO)
    }

    fun setOnDetailClick(listener: OnDetailClickListener){
        this.listener = listener
    }
}

class DetailViewHolder(val binding: ItemDetailBinding, private val listener: DetailViewRecyclerAdapter.OnDetailClickListener): RecyclerView.ViewHolder(binding.root){
    fun bind(item: ContentDTO){
        val pos = adapterPosition
        if (pos != RecyclerView.NO_POSITION){
            binding.likeImg.setOnClickListener {
                listener.onLikeClick(binding.root, pos)
            }

            binding.profileImg.setOnClickListener {
                listener.onProfileClick(binding.root, item)
            }

            binding.detailChat.setOnClickListener {
                listener.onCommentClick(binding.root, item)
            }

            binding.item = item
        }
    }
}

class DetailDiffUtil : DiffUtil.ItemCallback<ContentDTO>() {
    override fun areItemsTheSame(oldItem: ContentDTO, newItem: ContentDTO): Boolean {
        return (oldItem.uid == newItem.uid) and (oldItem.timeStamp == newItem.timeStamp)
    }

    override fun areContentsTheSame(oldItem: ContentDTO, newItem: ContentDTO): Boolean {
        return (oldItem.likeCount == newItem.likeCount) or (oldItem.likedUsers == newItem.likedUsers)
    }
}


@BindingAdapter("binding:glide")
fun bindGlide(view: ImageView, imgUrl: String?){
    if(imgUrl != null)
        Glide
            .with(view.context)
            .load(imgUrl)
            .override(view.width)    //받아온 이미지 크기 조정
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE) //캐싱된 리소스와 로드할 리소스가 같은 리소스일때 캐싱된 리소스 사용, gif 느릴 경우 사용
            .into(view)
    else
        view.setImageResource(R.drawable.ic_launcher_background)
}

@BindingAdapter("binding:bind_profile")
fun bindProfile(view: ImageView, uid: String?){
    firestore.collection(PathString.profileImages)
        .document(uid ?: "")
        .get()
        .addOnCompleteListener { task ->
            val url = task.result.data?.get("image")

            Glide.with(view)
                .load(url)
                .circleCrop()
                .error(R.drawable.ic_account)
                .into(view)
        }
}

@BindingAdapter("binding:heart_click")
fun bindHeartClick(view: ImageView, item: ContentDTO){
    if (item.likedUsers.containsKey(auth.currentUser?.uid)){
        view.setImageResource(R.drawable.ic_favorite)
    } else {
        view.setImageResource(R.drawable.ic_favorite_border)
    }
}