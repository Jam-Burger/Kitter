package com.jamburger.kitter.adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.jamburger.kitter.R
import com.jamburger.kitter.activities.CommentsActivity
import com.jamburger.kitter.activities.OtherProfileActivity
import com.jamburger.kitter.components.Post
import com.jamburger.kitter.components.User
import com.jamburger.kitter.services.AuthService
import com.jamburger.kitter.utilities.DateTimeFormatter
import java.util.TreeSet

class PostAdapter(private var mContext: Context) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private lateinit var mPosts: TreeSet<Post>
    private lateinit var db: FirebaseFirestore
    private lateinit var userReference: DocumentReference
    private var lastClickTime: Long = 0

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPosts = TreeSet(Comparator.comparing { obj: Post -> obj.postid })
        }
    }

    fun addPost(post: Post) {
        mPosts.add(post)
        notifyDataSetChanged()
    }

    fun clearPosts() {
        mPosts.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.adapter_post, parent, false)
        db = FirebaseFirestore.getInstance()
        userReference = db.collection("Users").document(AuthService.auth.uid!!)
        ViewHolder.userReference = userReference
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = mPosts.toTypedArray<Post>()[position]

        db.collection("Users").document(post.creator).get()
            .addOnSuccessListener { snapshot: DocumentSnapshot ->
                val creator = snapshot.toObject<User>()
                Glide.with(mContext).load(creator!!.profileImageUrl).into(holder.profileImage)
                holder.username.text = creator.username
            }

        holder.post = post
        holder.time.text = DateTimeFormatter.getTimeDifference(post.postid, false)
        Glide.with(mContext).load(post.imageUrl).into(holder.postImage)
        holder.caption.text = post.caption
        holder.kitt.text = post.kitt

        if (post.kitt.isEmpty()) {
            holder.kitt.visibility = View.GONE
            holder.caption.visibility = View.VISIBLE
        } else {
            holder.kitt.visibility = View.VISIBLE
            holder.caption.visibility = View.GONE
        }

        holder.checkIfLiked()
        holder.checkIfSaved()

        val commentsReference =
            FirebaseDatabase.getInstance().reference.child("comments").child(post.postid)
        commentsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentCount = snapshot.childrenCount
                if (commentCount >= 2) {
                    val commentsCountText = "View all $commentCount comments"
                    holder.commentCount.text = commentsCountText
                    holder.commentCount.visibility = View.VISIBLE
                } else {
                    holder.commentCount.text = ""
                    holder.commentCount.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


        holder.postImage.setOnClickListener {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                holder.toggleLike()
            }
            lastClickTime = clickTime
        }
        holder.like.setOnClickListener { holder.toggleLike() }
        holder.save.setOnClickListener { holder.toggleSave() }

        holder.commentCount.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postid", post.postid)
            intent.putExtra("openKeyboard", false)
            mContext.startActivity(intent)
        }

        holder.comment.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postid", post.postid)
            intent.putExtra("openKeyboard", true)
            mContext.startActivity(intent)
        }

        holder.header.setOnClickListener {
            val intent = Intent(mContext, OtherProfileActivity::class.java)
            intent.putExtra("userid", post.creator)
            mContext.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return mPosts.size
    }

    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: ImageView = itemView.findViewById(R.id.img_profile)
        var like: ImageView = itemView.findViewById(R.id.btn_like)
        var comment: ImageView = itemView.findViewById(R.id.btn_comment)
        var save: ImageView = itemView.findViewById(R.id.btn_save)
        var postImage: ImageView = itemView.findViewById(R.id.img_post)
        var header: View = itemView.findViewById(R.id.header)
        var likeAnimation: LottieAnimationView = itemView.findViewById(R.id.animation_like)
        var username: TextView = itemView.findViewById(R.id.txt_username)
        private var noOfLikes: TextView = itemView.findViewById(R.id.txt_likes)
        var caption: TextView = itemView.findViewById(R.id.caption)
        var kitt: TextView = itemView.findViewById(R.id.txt_kitt)
        var commentCount: TextView = itemView.findViewById(R.id.txt_comment_count)
        var time: TextView = itemView.findViewById(R.id.txt_time)
        private var isLiked: Boolean = false
        private var isSaved: Boolean = false
        var post: Post? = null

        init {
            likeAnimation.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
                }

                override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                    likeAnimation.visibility = View.INVISIBLE
                }
            })
        }

        private fun update() {
            val n = post!!.likes.size
            if (n > 0) {
                noOfLikes.visibility = View.VISIBLE
                val str = n.toString() + (if (n > 1) " likes" else " like")
                noOfLikes.text = str
            } else {
                noOfLikes.visibility = View.GONE
            }

            if (isLiked) like.setImageResource(R.drawable.ic_heart)
            else like.setImageResource(R.drawable.ic_heart_outlined)

            if (isSaved) save.setImageResource(R.drawable.ic_save)
            else save.setImageResource(R.drawable.ic_save_outlined)
        }

        fun toggleLike() {
            isLiked = !isLiked
            if (isLiked) {
                likeAnimation.playAnimation()
                likeAnimation.visibility = View.VISIBLE
            } else {
                likeAnimation.visibility = View.INVISIBLE
            }
            updateLikesData()
        }

        fun toggleSave() {
            isSaved = !isSaved
            updateSavedData()
        }


        fun checkIfLiked() {
            isLiked = post!!.likes.contains(userReference)
            update()
        }

        private fun updateLikesData() {
            val postReference = FirebaseFirestore.getInstance().collection("Posts").document(
                post!!.postid
            )
            if (isLiked) {
                postReference.update("likes", FieldValue.arrayUnion(userReference))
                post!!.likes.add(userReference)
            } else {
                postReference.update("likes", FieldValue.arrayRemove(userReference))
                post!!.likes.remove(userReference)
            }
            update()
        }

        fun checkIfSaved() {
            val postReference = FirebaseFirestore.getInstance().collection("Posts").document(
                post!!.postid
            )
            userReference.get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                val user = documentSnapshot.toObject<User>()
                isSaved = user?.saved?.contains(postReference) == true
                update()
            }
        }

        private fun updateSavedData() {
            val postReference = FirebaseFirestore.getInstance().collection("Posts").document(
                post!!.postid
            )
            if (isSaved) {
                userReference.update("saved", FieldValue.arrayUnion(postReference))
            } else {
                userReference.update("saved", FieldValue.arrayRemove(postReference))
            }
            update()
        }

        companion object {
            lateinit var userReference: DocumentReference
        }
    }

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}
