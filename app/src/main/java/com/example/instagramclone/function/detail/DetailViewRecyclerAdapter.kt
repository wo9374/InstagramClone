package com.example.instagramclone.function.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ItemDetailBinding
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.Firebase.auth


class DetailViewRecyclerAdapter(private val dataList:ArrayList<ContentDTO>) : RecyclerView.Adapter<DetailViewHolder>(){

    lateinit var listener : OnFavoriteClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val detailItemBinding = ItemDetailBinding.inflate( LayoutInflater.from(parent.context), parent,false)
        return DetailViewHolder(detailItemBinding, listener)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    interface OnFavoriteClickListener {
        fun onFavoriteClick(v: View, data: ContentDTO, position: Int)
    }

    fun setOnFavoriteClick(listener: OnFavoriteClickListener){
        this.listener = listener
    }
}

class DetailViewHolder(val binding: ItemDetailBinding, val listener: DetailViewRecyclerAdapter.OnFavoriteClickListener): RecyclerView.ViewHolder(binding.root){
    fun bind(item: ContentDTO){
        val position = adapterPosition
        if (position != RecyclerView.NO_POSITION){
            binding.favorite.setOnClickListener {
                listener.onFavoriteClick(binding.root, item, position)
            }
            binding.item = item
        }
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

@BindingAdapter("binding:heart_click")
fun bindHeartClick(view: ImageView, item: ContentDTO){
    if (item.favorites.containsKey(auth.currentUser?.uid)){
        view.setImageResource(R.drawable.ic_favorite)
    } else {
        view.setImageResource(R.drawable.ic_favorite_border)
    }
}