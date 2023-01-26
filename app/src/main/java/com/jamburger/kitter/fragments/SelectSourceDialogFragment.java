package com.jamburger.kitter.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jamburger.kitter.PostActivity;

public class SelectSourceDialogFragment extends DialogFragment {
    public static SelectSourceDialogFragment newInstance() {
        SelectSourceDialogFragment frag = new SelectSourceDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select from")
                .setItems(new String[]{"Text only", "Camera", "Gallery"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            ((PostActivity) requireActivity()).sendTextOnly();
                            break;
                        case 1:
                            ((PostActivity) requireActivity()).selectFromCamera();
                            break;
                        case 2:
                            ((PostActivity) requireActivity()).selectFromGallery();
                            break;
                    }
                });
        return builder.create();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        ((PostActivity) getActivity()).startHomeFragment();
    }

}