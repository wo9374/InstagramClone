package com.example.instagramclone.function

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.FragmentDetailViewBinding
import com.example.instagramclone.databinding.FragmentGridBinding
import com.example.instagramclone.function.account.PostListAdapter
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.Firebase
import com.google.firebase.firestore.ListenerRegistration

class GridFragment : BaseFragment<FragmentGridBinding>(R.layout.fragment_grid) {

    private val listAdapter = PostListAdapter()

    private var postListLis: ListenerRegistration? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gridRecycler.apply {
            adapter = listAdapter
            layoutManager = GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
        }

        getPostListFromFireStore()
    }

    private fun getPostListFromFireStore(){
        postListLis = Firebase.firestore
            .collection("images")
            .addSnapshotListener { value, error ->
                kotlin.runCatching {
                    value?.documents?.map {
                        it.toObject(ContentDTO::class.java)?.copy(contentUid = it.id)
                    } ?: throw IllegalArgumentException("Failed to get list(null returned)")
                }.onSuccess { list ->
                    listAdapter.submitList(list)
                }.onFailure { e ->
                    e.printStackTrace()
                    error?.printStackTrace()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        postListLis?.remove()
    }
}