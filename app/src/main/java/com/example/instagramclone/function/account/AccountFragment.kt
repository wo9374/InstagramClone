package com.example.instagramclone.function.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.FragmentAccountBinding
import com.example.instagramclone.function.LoginActivity
import com.example.instagramclone.util.Firebase.auth
import com.example.instagramclone.util.LoadingDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class AccountFragment : BaseFragment<FragmentAccountBinding>(R.layout.fragment_account) {

    private val viewModel : AccountViewModel by viewModels { AccountViewModelFactory(auth.currentUser?.uid) }

    private val listAdapter = PostListAdapter()

    private val albumLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사진을 선택했을 때
            LoadingDialog.show(requireContext())
            viewModel.uploadProfileImage(result.data?.data) {
                LoadingDialog.dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            viewModel.postList
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collectLatest {
                    listAdapter.submitList(it)
                    binding.postCount.text = it.size.toString()
                }
        }

        binding.btnLogout.setOnClickListener {
            requireActivity().run {
                auth.signOut()
                startActivity(Intent(requireActivity(), LoginActivity::class.java))
                finish()
            }
        }
        binding.profileImg.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            albumLauncher.launch(imagePickerIntent)
        }

        binding.accountRecycler.apply {
            adapter = listAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }
}