package com.jamburger.kitter;

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
import com.jamburger.kitter.adapters.MessageAdapter;
import com.jamburger.kitter.components.Message;
import com.jamburger.kitter.components.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    User me, fellow;
    TextView username;
    EditText message;
    List<Message> messages;
    MessageAdapter messageAdapter;
    RecyclerView recyclerViewMessages;
    DatabaseReference chatsReference, chatReference;
    ImageView profileImage, sendButton;
    String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getChatData();

        username = findViewById(R.id.txt_username);
        profileImage = findViewById(R.id.img_profile);
        message = findViewById(R.id.et_message);
        sendButton = findViewById(R.id.btn_send_message);
        recyclerViewMessages = findViewById(R.id.recyclerview_messages);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages);
        recyclerViewMessages.setHasFixedSize(true);
        recyclerViewMessages.setAdapter(messageAdapter);

        sendButton.setOnClickListener(v -> {
            String messageString = message.getText().toString();
            if (!messageString.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.post_time_format), Locale.getDefault());
                String messageId = sdf.format(new Date());
                message.setText("");
                Message newMessage = new Message(messageId, messageString, me.getId());
                messages.add(newMessage);
                messageAdapter.notifyItemInserted(messages.size() - 1);
                recyclerViewMessages.scrollToPosition(messages.size() - 1);
                chatReference.child(messageId).setValue(newMessage);
            }
        });
    }

    private void readMessages() {
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot chatSnapshot) {
                messages.clear();
                for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    messages.add(message);
                }
                messageAdapter.notifyDataSetChanged();
                if (messages.size() > 0)
                    recyclerViewMessages.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getChatData() {
        String myUID = FirebaseAuth.getInstance().getUid();
        assert myUID != null;
        String fellowUID = getIntent().getStringExtra("userid");

        chatsReference = FirebaseDatabase.getInstance().getReference().child("chats");
        chatsReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatId = myUID + "&" + fellowUID;
                for (DataSnapshot data : task.getResult().getChildren()) {
                    String key = data.getKey();
                    assert key != null;
                    String[] ids = key.split("&");
                    if ((ids[0].equals(myUID) && ids[1].equals(fellowUID)) || (ids[1].equals(myUID) && ids[0].equals(fellowUID))) {
                        chatId = key;
                        break;
                    }
                }
                chatReference = chatsReference.child(chatId);
                readMessages();
            }
        });

        CollectionReference users = FirebaseFirestore.getInstance().collection("Users");
        users.document(myUID).get().addOnSuccessListener(documentSnapshot -> me = documentSnapshot.toObject(User.class));
        users.document(fellowUID).get().addOnSuccessListener(documentSnapshot -> {
            fellow = documentSnapshot.toObject(User.class);
            assert fellow != null;
            username.setText(fellow.getUsername());
            Glide.with(this).load(fellow.getProfileImageUrl()).into(profileImage);
        });
    }
}