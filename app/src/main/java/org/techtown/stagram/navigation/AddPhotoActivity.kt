package org.techtown.stagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import org.techtown.stagram.R
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // 스토리지 초기화
        storage = FirebaseStorage.getInstance()

        // 앨범 열기
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        add_photo_button.setOnClickListener { contentUpload() }
        // 업로드 버튼에 contentUpload 연결
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if (resultCode == Activity.RESULT_OK){
                // 이미지를 선택 했을 때
                photoUri = data?.data
                add_photo_image.setImageURI(photoUri)
                // 이미지 화면에 선택된 이미지 불러오기
            }
            else{
                // 취소 되었을 때
                finish()
            }
        }
    }

    fun contentUpload(){
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        // 이미지 이름을 현재시간으로 정해줘서 중복 방지

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 이미지 업로드
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            Toast.makeText(this,getString(R.string.upload_success),Toast.LENGTH_SHORT).show()
        }
    }
}