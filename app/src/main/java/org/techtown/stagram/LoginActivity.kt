package org.techtown.stagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener { signinAndSignup() }
        // 이메일 로그인 버튼에 signinAndSignup 연결

        google_sign_in_button.setOnClickListener { googleLogin() }
        // 구글 로그인 버튼에 googleLogin 연결

       var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
    }

    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)!!
            // 구글API가 넘겨주는 값 받아옴

            if(result.isSuccess) {
                var accout = result.signInAccount
                firebaseAuthWithGoogle(accout)
            }
        }
    }

    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    // 아이디, 비밀번호 맞을 때
                    moveMainPage(task.result?.user)
                    Toast.makeText(this,"로그인 성공",Toast.LENGTH_SHORT).show()
                }else{
                    // 틀렸을 때
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(
                email_editText.text.toString(), password_editText.text.toString())
        ?.addOnCompleteListener{
                task ->
            if(task.isSuccessful){
                // 아이디 생성 되었을 때
                moveMainPage(task.result?.user)
            }else if(task.exception?.message.isNullOrEmpty()){
                // 아이디 생성 에러 발생
                Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
            }else{
                // 둘다 아님
                signinEmail()
            }
        }
    }

    fun signinEmail(){
        auth?.signInWithEmailAndPassword(
            email_editText.text.toString(), password_editText.text.toString())
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    // 아이디, 비밀번호 맞을 때
                    moveMainPage(task.result?.user)
                }else{
                    // 틀렸을 때
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun moveMainPage(user:FirebaseUser?){
        if( user!= null){
            startActivity(Intent(this,MainActivity::class.java))
        } // 유저정보 넘겨주고 메인 액티비티 호출
    }
}