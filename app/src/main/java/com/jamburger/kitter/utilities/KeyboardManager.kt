package com.jamburger.kitter.utilities;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardManager {
    public static void openKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static boolean isKeyboardOpened(Activity activity) {
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        return manager.isAcceptingText();
    }
}
