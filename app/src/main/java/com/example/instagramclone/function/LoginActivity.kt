package com.example.instagramclone.function

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ActivityLoginBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding

    lateinit var auth : FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var getResultLauncher: ActivityResultLauncher<Intent>
    /*var getResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK){
            it.data
        }
    }*/

    lateinit var callbackManager: CallbackManager

    interface ClickCallBack{
        fun onClick(view: View)
    }

    private val clickCallBack = object: ClickCallBack{
        override fun onClick(view : View){
            when(view.id){
                binding.emailLoginBtn.id -> signInAndSignUp()
                binding.signInFacebook.id -> facebookLogin()
                binding.signInGoogle.id -> googleLogin()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.clickCallBack = clickCallBack

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("856064662288-vehejte6lhc891ok48dmrfpmjh9sb50u.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        callbackManager = CallbackManager.Factory.create()
        //printHashKey()

        getResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == RESULT_OK){
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    // Google ???????????? ??????????????????. Firebase ??? ???????????????.
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    // Google ????????? ??????, UI??? ???????????? ????????????
                    Log.d(LoginActivity::class.java.toString(), e.message.toString())
                }
            }else{
                Log.e("TAG", "RESULT_FAil ?????????????????? ???????????? ????????? SHA ????????? ?????? ?????? ??????")
            }
        }
    }

    fun googleLogin(){
        val signInIntent = googleSignInClient.signInIntent
        getResultLauncher.launch(signInIntent)
        //startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE) //deprecated
    }

    fun facebookLogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, callbackManager, listOf("email", "public_profile"))

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult) {
                    firebaseAuthWithFacebook(result.accessToken) //????????? ????????? Facebook Data ??? Firebase ??? ??????
                }
                override fun onCancel() {}
                override fun onError(error: FacebookException) {}
            })
    }

    fun firebaseAuthWithFacebook(token: AccessToken?) {
        // AccessToken ?????? Facebook ??????
        val credential = FacebookAuthProvider.getCredential(token?.token!!)

        // ?????? ??? Firebase ??? ?????? ?????? ????????? (?????????)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    moveMainPage(it.result?.user)
                else
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    moveMainPage(it.result?.user)
                else
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
    }

    fun signInAndSignUp(){
        auth.createUserWithEmailAndPassword(
            binding.editTextEmail.text.toString(),
            binding.editTextPassword.text.toString()
        ).addOnCompleteListener {
            when {
                it.isSuccessful ->  //Creating a user account
                    moveMainPage(it.result?.user)
                it.exception?.message.isNullOrEmpty() ->    //Show the error message
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                else -> //Login if you have account
                    signInEmail()
            }
        }
    }

    //Facebook ???????????? HashKey get ??????
    fun printHashKey() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }

    fun signInEmail(){
        auth.signInWithEmailAndPassword(
            binding.editTextEmail.text.toString(),
            binding.editTextPassword.text.toString()
        ).addOnCompleteListener {
            if (it.isSuccessful)    //Login
                moveMainPage(it.result?.user)
            else                    //Show the error message
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun moveMainPage(user: FirebaseUser?){
        if (user != null)
            startActivity(Intent(this, MainActivity::class.java))
    }
}