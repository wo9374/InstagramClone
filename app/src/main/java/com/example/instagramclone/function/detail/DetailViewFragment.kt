package com.example.instagramclone.function.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.FragmentDetailViewBinding
import com.example.instagramclone.function.comment.CommentActivity
import com.example.instagramclone.model.ContentDTO
import com.example.instagramclone.util.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class DetailViewFragment : BaseFragment<FragmentDetailViewBinding>(R.layout.fragment_detail_view) {

    private val viewModel: DetailViewModel by viewModels()

    private var detailAdapter = DetailViewRecyclerAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        detailAdapter.setOnDetailClick(object : DetailViewRecyclerAdapter.OnDetailClickListener{
            override fun onLikeClick(v: View, position: Int) {
                v.isSelected = !v.isSelected
                viewModel.onLikeClicked(position)
            }

            override fun onProfileClick(v: View, item: ContentDTO) {
                if (!(item.uid.isBlank() || item.userId.isBlank())) {
                    if (item.uid != Firebase.auth.currentUser?.uid){ //포스팅의 프로필이 현재 로그인한 프로필과 다를때 (다른 사용자일때) 프로필뷰 이동
                        val action = DetailViewFragmentDirections.actionDetailFragmentToProfileFragment(item.uid, item.userId)
                        navController.navigate(action)
                    } else {
                        navController.navigate(R.id.action_account)
                    }
                }
            }

            override fun onCommentClick(v: View, item: ContentDTO) {
                val action = DetailViewFragmentDirections.actionDetailFragmentToCommentActivity(item.contentUid, item.userId)
                navController.navigate(action)
            }
        })

        binding.detailRecycler.apply {
            adapter = detailAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }

        lifecycleScope.launch{
            viewModel.itemsFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collectLatest {
                    detailAdapter.submitList(it)
                }
        }
    }
}