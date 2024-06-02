package com.jamburger.kitter.adapters

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.jamburger.kitter.R
import com.jamburger.kitter.components.Post
import com.jamburger.kitter.utilities.DateTimeFormatter
import java.util.TreeSet

class MyKittAdapter(private var mContext: Context) :
    RecyclerView.Adapter<MyKittAdapter.ViewHolder>() {
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
        val view = LayoutInflater.from(mContext).inflate(R.layout.adapter_my_kitt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postReference = mPosts!!.toTypedArray<DocumentReference>()[position]

        val dialog = Dialog(mContext)
        dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        dialog.setContentView(R.layout.dialog_my_kitt_preview)
        val previewTextView = dialog.findViewById<TextView>(R.id.kitt_mypost)

        postReference.get().addOnSuccessListener { postSnapshot: DocumentSnapshot ->
            val post = postSnapshot.toObject(Post::class.java)!!
            holder.kitt.text = post.kitt
            holder.time.text = DateTimeFormatter.getTimeDifference(post.postid, false)
            holder.container.visibility = View.VISIBLE
            previewTextView.text = holder.kitt.text.toString()
        }

        holder.container.setOnLongClickListener {
            dialog.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return mPosts!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var kitt: TextView = itemView.findViewById(R.id.txt_kitt)
        var time: TextView = itemView.findViewById(R.id.txt_time)
        var container: View = itemView.findViewById(R.id.container)
    }
}
