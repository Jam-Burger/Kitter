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
import com.jamburger.kitter.R
import com.jamburger.kitter.activities.ChatActivity
import com.jamburger.kitter.activities.OtherProfileActivity
import com.jamburger.kitter.components.User

class ProfileAdapter(
    private var mContext: Context,
    private var mUsers: List<User>,
    private var use: String
) :
    RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.adapter_profile, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUsers[position]
        Glide.with(mContext).load(user.profileImageUrl).into(holder.profileImage)
        holder.username.text = user.username
        holder.name.text = user.name
        holder.container.setOnClickListener {
            val intent = if (use == "PROFILE") {
                Intent(mContext, OtherProfileActivity::class.java)
            } else {
                Intent(mContext, ChatActivity::class.java)
            }
            intent.putExtra("userid", user.id)
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    fun filterList(filteredList: List<User>) {
        mUsers = filteredList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: ImageView = itemView.findViewById(R.id.img_profile)
        var username: TextView = itemView.findViewById(R.id.txt_username)
        var name: TextView = itemView.findViewById(R.id.txt_name)
        var container: View = itemView.findViewById(R.id.container)
    }
}
