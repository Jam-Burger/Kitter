package com.jamburger.kitter.utilities

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

object KeyboardManager {
    fun openKeyboard(activity: Activity) {
        val view = activity.currentFocus
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun closeKeyboard(activity: Activity) {
        val view = activity.currentFocus
        if (view != null) {
            val manager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun isKeyboardOpened(activity: Activity): Boolean {
        val manager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return manager.isAcceptingText
    }
}
