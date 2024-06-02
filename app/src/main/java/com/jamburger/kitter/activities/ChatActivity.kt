package com.jamburger.kitter.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jamburger.kitter.R
import com.jamburger.kitter.adapters.MessageAdapter
import com.jamburger.kitter.components.Message
import com.jamburger.kitter.components.User
import com.jamburger.kitter.utilities.DateTimeFormatter

class ChatActivity : AppCompatActivity() {
    private lateinit var fellow: User
    private lateinit var myUID: String
    private lateinit var fellowUID: String
    private lateinit var username: TextView
    private lateinit var message: EditText
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var chatReference: DatabaseReference
    private lateinit var profileImage: ImageView
    private lateinit var sendButton: ImageView
    private lateinit var chatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        myUID = FirebaseAuth.getInstance().uid!!
        fellowUID = intent.getStringExtra("userid")!!

        username = findViewById(R.id.txt_username)
        profileImage = findViewById(R.id.img_profile)
        message = findViewById(R.id.et_message)
        sendButton = findViewById(R.id.btn_send_message)
        recyclerViewMessages = findViewById(R.id.recyclerview_messages)

        val users = FirebaseFirestore.getInstance().collection("Users")
        users.document(fellowUID).get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                fellow = documentSnapshot.toObject(
                    User::class.java
                )!!
                username.text = fellow.username
                Glide.with(this).load(fellow.profileImageUrl).into(profileImage)

                messageAdapter = MessageAdapter(this, fellow.profileImageUrl)
                recyclerViewMessages.setHasFixedSize(true)
                recyclerViewMessages.setAdapter(messageAdapter)

                chatData
                readMessages()
            }
        sendButton.setOnClickListener {
            val messageString = message.getText().toString()
            if (messageString.isNotEmpty()) {
                val messageId = DateTimeFormatter.getCurrentTime()
                message.setText("")
                val newMessage = Message(messageId, messageString, myUID)
                chatReference.child(messageId).setValue(newMessage)
            }
        }
    }

    private fun readMessages() {
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(chatSnapshot: DataSnapshot) {
                messageAdapter.clearMessages()
                var lastMessage: Message? = null
                for (messageSnapshot in chatSnapshot.children) {
                    val nextMessage = messageSnapshot.getValue(
                        Message::class.java
                    )
                    var nextDateMonth = DateTimeFormatter.getDateMonth(
                        nextMessage!!.messageId
                    )

                    if (lastMessage == null) {
                        val today =
                            DateTimeFormatter.getDateMonth(DateTimeFormatter.getCurrentTime())
                        if (nextDateMonth == today) nextDateMonth = "Today"
                        val timestamp = Message("@", nextDateMonth, "")
                        messageAdapter.addMessage(timestamp)
                    } else {
                        val lastDateMonth = DateTimeFormatter.getDateMonth(lastMessage.messageId)
                        if (lastDateMonth != nextDateMonth) {
                            val today =
                                DateTimeFormatter.getDateMonth(DateTimeFormatter.getCurrentTime())
                            if (nextDateMonth == today) nextDateMonth = "Today"
                            val timestamp = Message("@", nextDateMonth, "")
                            messageAdapter.addMessage(timestamp)
                        }
                    }

                    messageAdapter.addMessage(nextMessage)
                    lastMessage = nextMessage
                }
                if (messageAdapter.itemCount > 0) recyclerViewMessages.layoutManager!!
                    .smoothScrollToPosition(
                        recyclerViewMessages,
                        RecyclerView.State(),
                        messageAdapter.itemCount - 1
                    )
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private val chatData: Unit
        get() {
            val less = myUID < fellowUID
            chatId = if (less) "$myUID&$fellowUID" else "$fellowUID&$myUID"
            chatReference = FirebaseDatabase.getInstance().reference.child("chats").child(chatId)
        } // TODO: finish issue of close keyboard
}