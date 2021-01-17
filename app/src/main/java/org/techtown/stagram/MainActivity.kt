package org.techtown.stagram

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import org.techtown.stagram.navigation.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var detailViewFragment = DetailViewFragment()
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setToolbarDefault()

        when(item.itemId){
            R.id.home ->{
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true
            }
            R.id.search ->{
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
                return true
            }
            R.id.add_photo ->{
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    // 권한 체크해서 권한이 있을 때
                    startActivity(Intent(this,AddPhotoActivity::class.java))
                }
                return true
            }
            R.id.favorite_alarm ->{
                var alarmFragment = AlarmViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,alarmFragment).commit()
                return true
            }
            R.id.account ->{
                var userFragment = UserFragment()
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid",uid)
                userFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
        registerPushToken()
    }

    // 툴바 기본 이미지 설정
    fun setToolbarDefault(){
        toolbar_username.visibility = View.GONE
        toolbar_back_Button.visibility = View.GONE
        top_image.visibility = View.VISIBLE
    }

    // 푸시 토큰 생성
    fun registerPushToken(){
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            task ->
            val token = task.result?.token
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val map = mutableMapOf<String,Any>()
            map["pushToken"] = token!!

            FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 프로필 사진 이미지 파이어베이스에 올리기
        if(requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask { task : Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                var map = HashMap<String,Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }
        }
    }
}