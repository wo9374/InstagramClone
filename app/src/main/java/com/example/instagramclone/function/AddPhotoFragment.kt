package com.example.instagramclone.function

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.FragmentAddPhotoBinding
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoFragment : BaseFragment<FragmentAddPhotoBinding>(R.layout.fragment_add_photo) {
    lateinit var storage : FirebaseStorage
    lateinit var photoUri : Uri

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //권한 존재시 Album Pick On
        if (checkPermission()){
            storage = FirebaseStorage.getInstance()

            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"

            val pickImageAlbumLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                if (it.resultCode == AppCompatActivity.RESULT_OK){
                    //선택한 사진의 이미지 경로가 넘어옴
                    photoUri = it.data?.data ?: Uri.EMPTY
                    binding.addPhotoImg.setImageURI(photoUri)
                }else{
                    navController.popBackStack() //취소 선택시 이전 Fragment 로 돌아감
                }
            }
            pickImageAlbumLauncher.launch(photoPickerIntent)
        } else {
            navController.popBackStack() //권한 없을시 이전 Fragment 로 돌아감
        }

        binding.addPhotoUploadBtn.setOnClickListener {
            contentUpload()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun contentUpload(){
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_$timeStamp.png"

        val storageRef = storage.reference.child("images").child(imageFileName)

        storageRef.putFile(photoUri).addOnSuccessListener {
            Toast.makeText(requireContext(), getString(R.string.upload_success), Toast.LENGTH_LONG).show()
        }
    }
}