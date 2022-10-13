package com.example.instagramclone.function.detail

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.FragmentDetailViewBinding
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.Firebase.auth
import com.example.instagramclone.util.Firebase.firestore

class DetailViewFragment : BaseFragment<FragmentDetailViewBinding>(R.layout.fragment_detail_view) {

    private val contentDto: ArrayList<ContentDTO> = arrayListOf()
    private val contentUriList: ArrayList<String> = arrayListOf()

    private lateinit var detailAdapter : DetailViewRecyclerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore.collection("images").orderBy("timeStamp").addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            contentDto.clear()
            contentUriList.clear()

            for (snapshot in querySnapshot?.documents!!){
                val item = snapshot.toObject(ContentDTO::class.java)
                contentDto.add(item!!)
                contentUriList.add(item.uid)
            }

            detailAdapter = DetailViewRecyclerAdapter(contentDto).apply {
                setOnFavoriteClick(object : DetailViewRecyclerAdapter.OnFavoriteClickListener{
                    override fun onFavoriteClick(v: View, data: ContentDTO, position: Int) {
                        val tsDoc = firestore.collection("images").document(contentUriList[position])
                        firestore.runTransaction { transaction ->
                            val uid = auth.currentUser?.uid
                            val contentDTO = transaction.get(tsDoc).toObject(ContentDTO::class.java)

                            contentDTO?.let {
                                if (it.favorites.containsKey(uid)){
                                    it.favoriteCount = it.favoriteCount - 1
                                    it.favorites.remove(uid)
                                } else {
                                    it.favoriteCount = it.favoriteCount + 1
                                    it.favorites[uid!!] = true
                                }
                                transaction.set(tsDoc, contentDTO)
                            }
                        }
                    }
                })
            }

            binding.detailRecycler.apply {
                adapter = detailAdapter
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            }
        }

    }
}