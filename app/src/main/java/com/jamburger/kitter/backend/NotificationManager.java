package com.jamburger.kitter.backend;

import static com.jamburger.kitter.utilities.Constants.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.components.User;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationManager {
    public static void sendNotification(String uid, String message) {
        FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    User user = task.getResult().toObject(User.class);
                    String playerID = user != null ? user.getOnesignalPlayerId() : null;
                    JSONObject json = new JSONObject();
                    try {
                        json.put("contents", new JSONObject().put("en", message));
                        json.put("include_player_ids", new JSONArray().put(playerID));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    OneSignal.postNotification(json, new OneSignal.PostNotificationResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Log.i(TAG, "notification sent successfully!");
                        }

                        @Override
                        public void onFailure(JSONObject response) {
                            Log.i(TAG, response.toString());
                        }
                    });

                    // Handle notification opening
                    OneSignal.setNotificationOpenedHandler(result -> {
                        // Handle notification opening event
                    });
                } else {
                    Log.i(TAG, "couldn't send notification.\nUser does not exist");
                }
            }
        });

    }
}
