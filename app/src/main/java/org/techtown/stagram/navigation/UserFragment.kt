package org.techtown.stagram.navigation

import android.app.Application
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_grid.view.*
import org.techtown.stagram.R
import org.techtown.stagram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.fragment_user.view.*
import org.techtown.stagram.LoginActivity
import org.techtown.stagram.MainActivity
import org.techtown.stagram.navigation.model.AlarmDTO
import org.techtown.stagram.navigation.model.FollowDTO
import org.techtown.stagram.navigation.util.FcmPush

class UserFragment : Fragment(){
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid : String? = null
    var loadfollow : ListenerRegistration? = null
    var loadprofileimage : ListenerRegistration? = null

    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        // 넘어온 Uid 받아오기
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if(uid == currentUserUid){
            // 나의 유저 페이지
            fragmentView?.account_profile_button?.text = getString(R.string.signout)
            fragmentView?.account_profile_button?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut()
            }

            // 프로필 사진 클릭
            fragmentView?.account_profile_imageView?.setOnClickListener {
                var photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
            }
        }
        else{
            // 다른 유저 페이지
            fragmentView?.account_profile_button?.text = getString(R.string.follow)
            var mainActivity = (activity as MainActivity)
            mainActivity?.toolbar_username?.text = arguments?.getString("userId")
            mainActivity?.toolbar_back_Button?.setOnClickListener{
                mainActivity.bottom_navigation.selectedItemId = R.id.home
            } // 디테일 화면으로 돌아가기

            mainActivity?.top_image.visibility = View.GONE
            mainActivity?.toolbar_back_Button.visibility = View.VISIBLE
            mainActivity?.toolbar_username.visibility = View.VISIBLE

            fragmentView?.account_profile_button?.setOnClickListener {
                requestFollow()
            }
        }

        // 어댑터 연결
        fragmentView?.account_recyclerView?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerView?.layoutManager = GridLayoutManager(activity!!,3)

        return fragmentView
    }

    override fun onStart() {
        super.onStart()

        // 팔로우, 팔로워 수 불러오기
        loadfollow = firestore?.collection("users")?.document(uid!!)?.addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followerCount != null){
                fragmentView?.account_follower_count?.text = followDTO?.followerCount?.toString()
            }
            if(followDTO?.followingCount != null){
                fragmentView?.account_following_count?.text = followDTO?.followingCount?.toString()
                if(followDTO?.followers?.containsKey(currentUserUid!!)){
                    fragmentView?.account_profile_button?.text = getString(R.string.follow_cancel)
                    fragmentView?.account_profile_button?.background?.setColorFilter(ContextCompat.getColor(activity!!,R.color.colorLightGray),PorterDuff.Mode.MULTIPLY)
                }
                else{
                    if(uid!= currentUserUid){
                        fragmentView?.account_profile_button?.text = getString(R.string.follow)
                        fragmentView?.account_profile_button?.background?.colorFilter = null
                    }
                }
            }
        }

        // 프로필 사진 이미지 불러오기
        loadprofileimage = firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_profile_imageView!!)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // 스냅샷 제거(오류 방지)
        loadfollow?.remove()
        loadprofileimage?.remove()
    }


    // 팔로워 알림
    fun followerAlarm(destinationUid : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        // 팔로워 푸시 이벤트
        var message = auth?.currentUser?.email + " "+getString(R.string.alarm_follow)
        FcmPush.instance.sendMessage(destinationUid,"Stagram",message)
    }

    // 팔로우
    fun requestFollow(){
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction{ transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if (followDTO == null){
                // 팔로우 정보가 없을 때 생성
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followings[uid!!] = true

                transaction.set(tsDocFollowing,followDTO)
                return@runTransaction
            }

            if(followDTO.followings.containsKey(uid)){
                // 팔로우 하고 있는 경우 (취소)
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followings?.remove(uid)
            }
            else{
                // 팔로우 하고 있지 않은 경우 (팔로우)
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followings[uid!!] = true
            }
            transaction.set(tsDocFollowing,followDTO)
            return@runTransaction
        }

        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)

                transaction.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }
            if(followDTO!!.followers.containsKey(currentUserUid)){
                // 팔로우 하고 있는 경우
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid)
            }
            else{
                // 팔로우 하고 있지 않은 경우
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init{
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(querySnapshot == null) return@addSnapshotListener

                // 데이터 받아오기
                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                fragmentView?.account_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged()

            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels /3

            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)

        }
    }
}