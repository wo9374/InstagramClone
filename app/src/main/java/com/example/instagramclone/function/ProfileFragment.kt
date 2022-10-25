package com.example.instagramclone.function

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.FragmentAccountBinding
import com.example.instagramclone.function.account.AccountViewModel
import com.example.instagramclone.function.account.AccountViewModelFactory
import com.example.instagramclone.function.account.PostListAdapter
import com.example.instagramclone.model.FollowDTO
import com.example.instagramclone.util.Firebase
import com.example.instagramclone.util.Firebase.auth
import com.example.instagramclone.util.LoadingDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class ProfileFragment : BaseFragment<FragmentAccountBinding>(R.layout.fragment_account) {

    private val args: ProfileFragmentArgs by navArgs()
    private val uid: String? by lazy { args.uid.ifBlank { auth.currentUser?.uid } }
    private val userId: String? by lazy { args.userId.ifBlank { auth.currentUser?.email } }

    private val viewModel : AccountViewModel by viewModels { AccountViewModelFactory(uid) }

    private val listAdapter = PostListAdapter()

    private var followDto: FollowDTO by Delegates.observable(FollowDTO()) { _, _, newValue ->
        kotlin.runCatching {
            binding.apply {
                followingCount.text = newValue.followingCount.toString()
                followerCount.text = newValue.followerCount.toString()
                if (newValue.followers.contains(auth.currentUser?.uid)) {
                    btnLogout.setText(R.string.follow_cancel)
                } else {
                    btnLogout.setText(R.string.follow)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity() as MainActivity).binding.apply {
            bottomNavigation.visibility = View.GONE
            mainLogo.visibility = View.GONE
            toolbarBack.visibility = View.VISIBLE
            profileUserTxt.apply {
                visibility = View.VISIBLE
                text = userId
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).supportActionBar?.subtitle = userId

        setupProfileButton()

        lifecycleScope.launch {
            viewModel.profileImageUrl
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collectLatest {
                    Glide.with(binding.profileImg)
                        .load(it)
                        .circleCrop()
                        .error(R.drawable.ic_account)
                        .into(binding.profileImg)
                }
        }

        lifecycleScope.launch {
            viewModel.followData
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collectLatest {
                    followDto = it
                }
        }

        lifecycleScope.launch {
            viewModel.postList
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collectLatest {
                    listAdapter.submitList(it)
                    binding.postCount.text = it.size.toString()
                }
        }

        binding.accountRecycler.apply {
            adapter = listAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }


    private fun setupProfileButton() = uid?.also {
        binding.btnLogout.apply {
            setText(R.string.follow)
            setOnClickListener { viewModel.requestFollow(followDto) }
            isEnabled = it != auth.currentUser?.uid
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as MainActivity).binding.apply {
            bottomNavigation.visibility = View.VISIBLE
            mainLogo.visibility = View.VISIBLE
            toolbarBack.visibility = View.GONE
            profileUserTxt.visibility = View.GONE
        }
    }
}