package com.jamburger.kitter.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.MessageAdapter;
import com.jamburger.kitter.components.Message;
import com.jamburger.kitter.components.User;
import com.jamburger.kitter.utilities.DateFormatter;

public class ChatActivity extends AppCompatActivity {
    User fellow;
    String myUID, fellowUID;
    TextView username;
    EditText message;
    MessageAdapter messageAdapter;
    RecyclerView recyclerViewMessages;
    DatabaseReference chatReference;
    ImageView profileImage, sendButton;
    String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myUID = FirebaseAuth.getInstance().getUid();
        fellowUID = getIntent().getStringExtra("userid");

        username = findViewById(R.id.txt_username);
        profileImage = findViewById(R.id.img_profile);
        message = findViewById(R.id.et_message);
        sendButton = findViewById(R.id.btn_send_message);
        recyclerViewMessages = findViewById(R.id.recyclerview_messages);

        CollectionReference users = FirebaseFirestore.getInstance().collection("Users");
        users.document(fellowUID).get().addOnSuccessListener(documentSnapshot -> {
            fellow = documentSnapshot.toObject(User.class);
            assert fellow != null;
            username.setText(fellow.getUsername());
            Glide.with(this).load(fellow.getProfileImageUrl()).into(profileImage);

            messageAdapter = new MessageAdapter(this, fellow.getProfileImageUrl());
            recyclerViewMessages.setHasFixedSize(true);
            recyclerViewMessages.setAdapter(messageAdapter);

            getChatData();
            readMessages();
        });
        sendButton.setOnClickListener(v -> {
            String messageString = message.getText().toString();
            if (!messageString.isEmpty()) {
                String messageId = DateFormatter.getCurrentTime();
                message.setText("");
                Message newMessage = new Message(messageId, messageString, myUID);
                chatReference.child(messageId).setValue(newMessage);
            }
        });
    }

    private void readMessages() {
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot chatSnapshot) {
                messageAdapter.clearMessages();
                Message lastMessage = null;
                for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                    Message nextMessage = messageSnapshot.getValue(Message.class);
                    String nextDateMonth = DateFormatter.getDateMonth(nextMessage.getMessageId());

                    if (lastMessage == null) {
                        String today = DateFormatter.getDateMonth(DateFormatter.getCurrentTime());
                        if (nextDateMonth.equals(today)) nextDateMonth = "Today";
                        Message timestamp = new Message("@", nextDateMonth, "");
                        messageAdapter.addMessage(timestamp);
                    } else {
                        String lastDateMonth = DateFormatter.getDateMonth(lastMessage.getMessageId());
                        if (!lastDateMonth.equals(nextDateMonth)) {
                            String today = DateFormatter.getDateMonth(DateFormatter.getCurrentTime());
                            if (nextDateMonth.equals(today)) nextDateMonth = "Today";
                            Message timestamp = new Message("@", nextDateMonth, "");
                            messageAdapter.addMessage(timestamp);
                        }
                    }

                    messageAdapter.addMessage(nextMessage);
                    lastMessage = nextMessage;
                }
                if (messageAdapter.getItemCount() > 0)
                    recyclerViewMessages.getLayoutManager().smoothScrollToPosition(recyclerViewMessages, new RecyclerView.State(), messageAdapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getChatData() {
        boolean less = myUID.compareTo(fellowUID) < 0;
        chatId = less ? myUID + '&' + fellowUID : fellowUID + '&' + myUID;
        chatReference = FirebaseDatabase.getInstance().getReference().child("chats").child(chatId);
    }

    // TODO: finish issue of close keyboard
}