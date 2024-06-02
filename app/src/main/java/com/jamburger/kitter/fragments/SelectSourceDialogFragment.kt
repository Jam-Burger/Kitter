package com.jamburger.kitter.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.jamburger.kitter.activities.PostActivity

class SelectSourceDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select from")
            .setItems(arrayOf("Camera", "Gallery")) { _: DialogInterface?, which: Int ->
                when (which) {
                    0 -> (requireActivity() as PostActivity).selectFromCamera()
                    1 -> (requireActivity() as PostActivity).selectFromGallery()
                }
            }
        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        (activity as PostActivity?)!!.startMainActivity()
    }

    companion object {
        fun newInstance(): SelectSourceDialogFragment {
            val frag = SelectSourceDialogFragment()
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}