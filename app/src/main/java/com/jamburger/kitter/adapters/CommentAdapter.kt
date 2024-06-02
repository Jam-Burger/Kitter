package com.jamburger.kitter.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jamburger.kitter.R
import com.jamburger.kitter.activities.OtherProfileActivity
import com.jamburger.kitter.components.Comment
import com.jamburger.kitter.components.User
import com.jamburger.kitter.utilities.DateTimeFormatter

class CommentAdapter(private var mContext: Context, private var mComments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.adapter_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = mComments[position]
        val publisherReference =
            FirebaseFirestore.getInstance().collection("Users").document(comment.publisherId)
        publisherReference.get().addOnSuccessListener { snapshot: DocumentSnapshot ->
            val user = snapshot.toObject(
                User::class.java
            )
            Glide.with(mContext).load(user!!.profileImageUrl).into(holder.profileImage)
            holder.username.text = user.username
            holder.time.text = DateTimeFormatter.getTimeDifference(comment.commentId, true)
            holder.comment.text = comment.text
            holder.container.setOnClickListener {
                val intent = Intent(mContext, OtherProfileActivity::class.java)
                intent.putExtra("userid", user.id)
                mContext.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return mComments.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: ImageView = itemView.findViewById(R.id.img_profile)
        var username: TextView = itemView.findViewById(R.id.txt_username)
        var comment: TextView = itemView.findViewById(R.id.txt_comment)
        var time: TextView = itemView.findViewById(R.id.txt_time)
        var container: View = itemView.findViewById(R.id.container)
    }
}
