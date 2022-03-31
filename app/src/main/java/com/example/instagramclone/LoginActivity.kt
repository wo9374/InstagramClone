package com.example.instagramclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.instagramclone.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding

    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        binding.emailLoginBtn.setOnClickListener {
            signInAndSignUp()
        }
    }

    fun signInAndSignUp(){
        auth.createUserWithEmailAndPassword(
            binding.editTextEmail.text.toString(),
            binding.editTextPassword.text.toString()
        ).addOnCompleteListener {
            if (it.isSuccessful){
                //Creating a user account
                moveMainPage(it.result?.user)
            }else if (it.exception?.message.isNullOrEmpty()){
                //Show the error message
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }else{
                //Login if you have account
                signInEmail()
            }
        }
    }

    fun signInEmail(){
        auth.signInWithEmailAndPassword(
            binding.editTextEmail.text.toString(),
            binding.editTextPassword.text.toString()
        ).addOnCompleteListener {
            if (it.isSuccessful){
                //Login
                moveMainPage(it.result?.user)
            }else{
                //Show the error message
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun moveMainPage(user: FirebaseUser?){
        if (user != null)
            startActivity(Intent(this, MainActivity::class.java))
    }
}