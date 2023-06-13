package com.jamburger.kitter.utilities;

import static com.jamburger.kitter.utilities.Constants.TAG;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.components.User;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationManager {
    public static void sendNotification(String uid, String message) {
        FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnCompleteListener(task -> {
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
        });

    }
}
