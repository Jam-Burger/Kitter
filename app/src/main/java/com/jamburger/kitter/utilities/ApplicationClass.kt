package com.jamburger.kitter.utilities

import android.app.Application
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

        val remoteConfigDefaults: MutableMap<String, Any> = HashMap()
        remoteConfigDefaults[ForceUpdateChecker.KEY_UPDATE_REQUIRED] = false
        remoteConfigDefaults[ForceUpdateChecker.KEY_CURRENT_VERSION] = "4.4.0"
        remoteConfigDefaults[ForceUpdateChecker.KEY_PROJECT_URL] =
            "https://github.com/Jam-Burger/Kitter"

        firebaseRemoteConfig.setDefaultsAsync(remoteConfigDefaults)
        firebaseRemoteConfig.fetch(60) // fetch every minutes
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    Log.d(Constants.TAG, "remote config is fetched.")
                    firebaseRemoteConfig.activate()
                }
            }
    }
}
