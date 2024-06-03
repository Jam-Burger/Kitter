package com.jamburger.kitter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jamburger.kitter.R
import com.jamburger.kitter.components.Message
import com.jamburger.kitter.services.AuthService
import com.jamburger.kitter.utilities.DateTimeFormatter

class MessageAdapter(private var mContext: Context, private var fellowProfileImageUrl: String) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private var myUID: String? = AuthService.auth.uid
    private var messages: MutableList<Message> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == MESSAGE_LAYOUT) LayoutInflater.from(mContext)
            .inflate(R.layout.adapter_message, parent, false)
        else LayoutInflater.from(mContext).inflate(R.layout.adapter_timestamp, parent, false)

        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].messageId == "@") TIMESTAMP_LAYOUT else MESSAGE_LAYOUT
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        val nextMessage = if (position + 1 < messages.size) messages[position + 1] else null

        if (holder.itemViewType == MESSAGE_LAYOUT) {
            holder.message.text = message.text
            holder.time.text =
                DateTimeFormatter.getHoursMinutes(message.messageId)

            if (myUID == message.senderId) {
                holder.container.layoutDirection = View.LAYOUT_DIRECTION_RTL
                holder.profileImage.visibility = View.GONE
            } else {
                holder.container.layoutDirection = View.LAYOUT_DIRECTION_LTR
                if (nextMessage == null || nextMessage.senderId == myUID) {
                    holder.profileImage.visibility = View.VISIBLE
                    Glide.with(mContext).load(fellowProfileImageUrl).into(holder.profileImage)
                } else {
                    holder.profileImage.visibility = View.INVISIBLE
                }
            }
        } else {
            holder.timestamp.text = message.text
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var message: TextView = itemView.findViewById(R.id.txt_message)
        var time: TextView = itemView.findViewById(R.id.txt_time)
        var profileImage: ImageView = itemView.findViewById(R.id.img_profile)
        var timestamp: TextView = itemView.findViewById(R.id.txt_timestamp)
        var container: View = itemView.findViewById(R.id.container)
    }

    companion object {
        private const val MESSAGE_LAYOUT = 0
        private const val TIMESTAMP_LAYOUT = 1
    }
}
