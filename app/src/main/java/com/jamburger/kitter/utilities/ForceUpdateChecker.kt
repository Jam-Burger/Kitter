package com.jamburger.kitter.utilities

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class ForceUpdateChecker(
    private val context: Context,
    private val onUpdateNeededListener: OnUpdateNeededListener?
) {
    private fun getAppVersion(context: Context): String {
        var result = ""
        try {
            result = context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
            result = result.replace("[a-zA-Z]|-".toRegex(), "")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, e.message!!)
        }

        return result
    }

    interface OnUpdateNeededListener {
        fun onUpdateNeeded(updateUrl: String)
    }

    class Builder(private val context: Context) {
        private var onUpdateNeededListener: OnUpdateNeededListener? = null

        fun onUpdateNeeded(onUpdateNeededListener: OnUpdateNeededListener?): Builder {
            this.onUpdateNeededListener = onUpdateNeededListener
            return this
        }

        private fun build(): ForceUpdateChecker {
            return ForceUpdateChecker(context, onUpdateNeededListener)
        }

        fun check(): Boolean {
            val forceUpdateChecker = build()
            return forceUpdateChecker.check()
        }
    }

    fun check(): Boolean {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        if (remoteConfig.getBoolean(KEY_UPDATE_REQUIRED)) {
            val currentValidVersion = remoteConfig.getString(KEY_CURRENT_VERSION)
            val appVersion = getAppVersion(context)
            val updateUrl = remoteConfig.getString(KEY_PROJECT_URL)
            if (versionCompare(
                    appVersion,
                    currentValidVersion
                ) < 0 && onUpdateNeededListener != null
            ) {
                onUpdateNeededListener.onUpdateNeeded(updateUrl)
                return true
            }
        }
        return false
    }

    companion object {
        const val KEY_UPDATE_REQUIRED: String = "force_update_required"
        const val KEY_CURRENT_VERSION: String = "force_update_current_version"
        const val KEY_PROJECT_URL: String = "project_github_url"
        private val TAG: String = ForceUpdateChecker::class.java.simpleName
        fun with(context: Context): Builder {
            return Builder(context)
        }

        fun versionCompare(v1: String, v2: String): Int {
            var ver1 = 0
            var ver2 = 0
            var i = 0
            var j = 0
            while ((i < v1.length || j < v2.length)) {
                while (i < v1.length && v1[i] != '.') {
                    ver1 = ver1 * 10 + (v1[i].code - '0'.code)
                    i++
                }

                while (j < v2.length && v2[j] != '.') {
                    ver2 = ver2 * 10 + (v2[j].code - '0'.code)
                    j++
                }

                if (ver1 > ver2) return 1
                if (ver2 > ver1) return -1
                ver1 = 0
                ver2 = 0
                i++
                j++
            }
            return 0
        }
    }
}
