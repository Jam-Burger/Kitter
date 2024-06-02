package com.jamburger.kitter.utilities;

import static com.jamburger.kitter.utilities.Constants.TAG;

import android.app.Application;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        Map<String, Object> remoteConfigDefaults = new HashMap<>();
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_REQUIRED, false);
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_CURRENT_VERSION, "4.4.0");
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_PROJECT_URL,
                "https://github.com/Jam-Burger/Kitter");

        firebaseRemoteConfig.setDefaultsAsync(remoteConfigDefaults);
        firebaseRemoteConfig.fetch(60) // fetch every minutes
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "remote config is fetched.");
                        firebaseRemoteConfig.activate();
                    }
                });
    }
}
