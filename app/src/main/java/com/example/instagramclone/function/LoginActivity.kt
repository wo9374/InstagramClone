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
import com.example.instagramclone.BuildConfig
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ActivityLoginBinding
import com.example.instagramclone.util.Firebase.auth
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.SIGNIN_TOKEN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        callbackManager = CallbackManager.Factory.create()
        //printHashKey()

        getResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == RESULT_OK){
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    // Google 로그인에 성공했습니다. Firebase 로 인증합니다.
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    // Google 로그인 실패, UI를 적절하게 업데이트
                    Log.d(LoginActivity::class.java.toString(), e.message.toString())
                }
            }else{
                Log.e("TAG", "RESULT_FAil 파이어베이스 프로젝트 설정에 SHA 인증서 지문 추가 추천")
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
                    firebaseAuthWithFacebook(result.accessToken) //로그인 성공시 Facebook Data 를 Firebase 로 넘김
                }
                override fun onCancel() {}
                override fun onError(error: FacebookException) {}
            })
    }

    fun firebaseAuthWithFacebook(token: AccessToken?) {
        // AccessToken 으로 Facebook 인증
        val credential = FacebookAuthProvider.getCredential(token?.token!!)

        // 성공 시 Firebase 에 유저 정보 보내기 (로그인)
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
                it.exception is FirebaseAuthUserCollisionException ->
                    signInEmail()
                it.exception is Exception -> { //Show the error message
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //Facebook 로그인용 HashKey get 함수
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