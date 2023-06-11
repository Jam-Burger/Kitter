package com.jamburger.kitter.backend;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseManager {
    private static final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    public static void deleteAllComments() {
        ref.child("comments").removeValue((error, ref) -> System.out.println("all comments deleted\n" + error.getMessage()));
    }

    public static void deleteAllChat() {
        ref.child("chats").removeValue((error, ref) -> System.out.println("all chat deleted\n" + error.getMessage()));
    }
}
