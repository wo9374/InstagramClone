package com.example.instagramclone.function.detail

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class DetailViewFragment : BaseFragment<FragmentDetailViewBinding>(R.layout.fragment_detail_view) {

    private val viewModel: DetailViewModel by viewModels()

    private var detailAdapter = DetailViewRecyclerAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        detailAdapter.setOnFavoriteClick(object : DetailViewRecyclerAdapter.OnFavoriteClickListener{
            override fun onFavoriteClick(v: View, position: Int) {
                viewModel.onLikeClicked(position)
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