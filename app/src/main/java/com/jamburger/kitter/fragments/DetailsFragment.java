package com.jamburger.kitter.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.jamburger.kitter.R;
import com.jamburger.kitter.activities.AddInfoActivity;

public class DetailsFragment extends Fragment {
    EditText name, bio;
    AddInfoActivity parent;

    public DetailsFragment(AddInfoActivity parent) {
        this.parent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        name = view.findViewById(R.id.et_name);
        bio = view.findViewById(R.id.et_bio);
        parent.headerText.setText("Add details");

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                parent.data.put("name", s.toString());
            }
        });
        bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                parent.data.put("bio", s.toString());
            }
        });
        return view;
    }
}