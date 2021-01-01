package org.techtown.stagram.navigation

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*
import org.techtown.stagram.R
import org.techtown.stagram.navigation.model.ContentDTO

class DetailViewFragment : Fragment(){
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        // 초기화
        firestore = FirebaseFirestore.getInstance()

        view.detailviewfragment_recyclerView.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerView.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init{
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                // 새로고침
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            //Id
            viewholder.detailviewitem_profile_textView.text = contentDTOs!![position].userId

            //Image (Glide)
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailviewitem_content_imageView)

            //Explain
            viewholder.detailviewitem_explain_textView.text = contentDTOs!![position].explain

            //Likes
            viewholder.detailviewitem_favoritecounter_textView.text = "Likes "+ contentDTOs!![position].favoriteCount

            //ProfileImage (X)
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailviewitem_profile_image)
        }
    }
}