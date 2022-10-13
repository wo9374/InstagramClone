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
import com.example.instagramclone.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoFragment : BaseFragment<FragmentAddPhotoBinding>(R.layout.fragment_add_photo) {
    lateinit var storage : FirebaseStorage
    lateinit var photoUri : Uri
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //권한 존재시 Album Pick On
        if (checkPermission()){
            storage = FirebaseStorage.getInstance()
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()

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
            Toast.makeText(requireContext(), "미디어 권한을 허용해 주십시오.", Toast.LENGTH_LONG).show()
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

        //업로드 Storage 방식 (구글 권장)
        storageRef.putFile(photoUri).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }.addOnSuccessListener { uri ->
            val contentDTO = ContentDTO(
                explain = binding.editExplain.text.toString(),
                imageUrl = uri.toString(),
                uid = auth.currentUser?.uid.toString(),
                userId = auth.currentUser?.email.toString(),
                timeStamp = System.currentTimeMillis()
            )
            firestore.collection("images").document().set(contentDTO)
            navController.popBackStack()
        }

        //업로드 Callback 방식
        /*storageRef.putFile(photoUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val contentDTO = ContentDTO(
                    explain = binding.editExplain.text.toString(),
                    imageUrl = uri.toString(),
                    uid = auth.currentUser?.uid.toString(),
                    userId = auth.currentUser?.email.toString(),
                    timeStamp = System.currentTimeMillis()
                )
                firestore.collection("images").document().set(contentDTO)
                navController.popBackStack()
            }
        }*/
    }
}