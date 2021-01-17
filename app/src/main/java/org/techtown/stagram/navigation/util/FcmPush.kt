package org.techtown.stagram.navigation.util

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.techtown.stagram.BuildConfig
import org.techtown.stagram.R
import org.techtown.stagram.navigation.model.PushDTO

class FcmPush {

    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = ""
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null

    companion object{
        var instance = FcmPush()
    }

    init{
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

//    fun sendMessage(destinationUid : String,  title : String, message : String){
//        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener {
//            task ->
//            if(task.isSuccessful){
//                var token = task?.result?.get("pushToken").toString()
//
//                var pushDTO = PushDTO()
//                pushDTO.to = token
//                pushDTO.notification.title = title
//                pushDTO.notification.body = message
//
//                var body = RequestBody.create(JSON,gson?.toJson(pushDTO))
//                var request = Request.Builder()
//                    .addHeader()
//            }
//        }
//    }

}