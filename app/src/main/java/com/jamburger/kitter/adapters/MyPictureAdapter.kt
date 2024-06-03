package com.jamburger.kitter.adapters

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.toObject
import com.jamburger.kitter.R
import com.jamburger.kitter.components.Post
import java.util.TreeSet

class MyPictureAdapter(private var mContext: Context) :
    RecyclerView.Adapter<MyPictureAdapter.ViewHolder>() {
    private var mPosts: TreeSet<DocumentReference>? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPosts = TreeSet(Comparator.comparing { obj: DocumentReference -> obj.id }
                .reversed())
        }
    }

    fun addPost(post: DocumentReference) {
        mPosts!!.add(post)
        notifyDataSetChanged()
    }

    fun clearPosts() {
        mPosts!!.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.adapter_my_picture, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postReference = mPosts!!.toTypedArray<DocumentReference>()[position]

        val dialog = Dialog(mContext)
        dialog.setContentView(R.layout.dialog_my_picture_preview)
        val previewImageView = dialog.findViewById<ImageView>(R.id.img_mypost)

        postReference.get().addOnSuccessListener { postSnapshot: DocumentSnapshot ->
            val post = postSnapshot.toObject<Post>()!!
            Glide.with(mContext).load(post.imageUrl).into(holder.myPostImage)
            Glide.with(dialog.context).load(post.imageUrl).into(previewImageView)
        }


        holder.myPostImage.setOnLongClickListener {
            dialog.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return mPosts!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var myPostImage: ImageView = itemView.findViewById(R.id.img_mypost)
    }
}
