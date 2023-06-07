package com.jamburger.kitter.utilities;

import static com.jamburger.kitter.utilities.Constants.TAG;

import android.app.Application;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Map;

public class ApplicationClass extends Application {
    // Replace the below with your own ONESIGNAL_APP_ID
    private static final String ONESIGNAL_APP_ID = "5e126269-04f4-496e-b0aa-70eb66b062e9";

    @Override
    public void onCreate() {
        super.onCreate();

        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        Map<String, Object> remoteConfigDefaults = new HashMap<>();
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_REQUIRED, false);
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_CURRENT_VERSION, "3.4.0");
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

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
        OneSignal.promptForPushNotifications();
    }
}
