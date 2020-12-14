package org.techtown.stagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener { signinAndSignup() }

    }
//
    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(
            email_editText.text.toString(), password_editText.text.toString())
            ?.addOnCompleteListener{
                task ->
                    if(task.isSuccessful){
                        // 아이디 생성 되었을 때
                        moveMainPage(task.result?.user)
                    }else if(task.exception?.message.isNullOrEmpty()){
                        // 로그인 에러 발생
                        Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                    }else{
                        // 둘다 아님
                        signinEmail()
                    }
            }
    }

    fun signinEmail(){
        auth?.createUserWithEmailAndPassword(
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