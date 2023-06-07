package com.jamburger.kitter.utilities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class ForceUpdateChecker {
    public static final String KEY_UPDATE_REQUIRED = "force_update_required";
    public static final String KEY_CURRENT_VERSION = "force_update_current_version";
    public static final String KEY_PROJECT_URL = "project_github_url";
    private static final String TAG = ForceUpdateChecker.class.getSimpleName();
    private final OnUpdateNeededListener onUpdateNeededListener;
    private final Context context;

    public ForceUpdateChecker(@NonNull Context context,
                              OnUpdateNeededListener onUpdateNeededListener) {
        this.context = context;
        this.onUpdateNeededListener = onUpdateNeededListener;
    }

    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    static int versionCompare(String v1, String v2) {
        int ver1 = 0, ver2 = 0;
        for (int i = 0, j = 0; (i < v1.length() || j < v2.length()); ) {
            while (i < v1.length() && v1.charAt(i) != '.') {
                ver1 = ver1 * 10 + (v1.charAt(i) - '0');
                i++;
            }

            while (j < v2.length() && v2.charAt(j) != '.') {
                ver2 = ver2 * 10 + (v2.charAt(j) - '0');
                j++;
            }

            if (ver1 > ver2) return 1;
            if (ver2 > ver1) return -1;
            ver1 = ver2 = 0;
            i++;
            j++;
        }
        return 0;
    }

    private String getAppVersion(Context context) {
        String result = "";
        try {
            result = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public interface OnUpdateNeededListener {
        void onUpdateNeeded(String updateUrl);
    }

    public static class Builder {

        private final Context context;
        private OnUpdateNeededListener onUpdateNeededListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateNeeded(OnUpdateNeededListener onUpdateNeededListener) {
            this.onUpdateNeededListener = onUpdateNeededListener;
            return this;
        }

        public ForceUpdateChecker build() {
            return new ForceUpdateChecker(context, onUpdateNeededListener);
        }

        public boolean check() {
            ForceUpdateChecker forceUpdateChecker = build();
            return forceUpdateChecker.check();
        }
    }

    public boolean check() {
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        if (remoteConfig.getBoolean(KEY_UPDATE_REQUIRED)) {
            String currentValidVersion = remoteConfig.getString(KEY_CURRENT_VERSION);
            String appVersion = getAppVersion(context);
            String updateUrl = remoteConfig.getString(KEY_PROJECT_URL);
            if (versionCompare(appVersion, currentValidVersion) < 0 && onUpdateNeededListener != null) {
                onUpdateNeededListener.onUpdateNeeded(updateUrl);
                return true;
            }
        }
        return false;
    }
}
