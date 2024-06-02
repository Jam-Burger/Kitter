package com.jamburger.kitter.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.R;
import com.jamburger.kitter.activities.AddInfoActivity;
import com.jamburger.kitter.components.User;

import java.util.ArrayList;
import java.util.List;

public class UsernameFragment extends Fragment {

    EditText username;
    AddInfoActivity parent;
    List<String> userNames;
    boolean valid;

    public UsernameFragment(AddInfoActivity parent) {
        this.parent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_username, container, false);
        username = view.findViewById(R.id.et_username);
        parent.headerText.setText("Choose a username");
        userNames = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(userSnapshots -> {
            for (DocumentSnapshot userSnapshot : userSnapshots) {
                User user = userSnapshot.toObject(User.class);
                userNames.add(user.getUsername());
            }
        });
        parent.nextButton.setClickable(false);
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                valid = true;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                parent.data.put("username", s.toString());
                if (!s.toString().matches("^\\w+$") || s.length() < 3 || s.length() > 15) {
                    valid = false;
                }
                for (String name : userNames) {
                    if (name.equals(s.toString())) {
                        valid = false;
                        break;
                    }
                }
                if (!valid) {
                    username.setTextColor(getResources().getColor(R.color.like));
                    parent.nextButton.setClickable(false);
                } else {
                    username.setTextColor(getResources().getColor(R.color.inverted));
                    parent.nextButton.setClickable(true);
                }
            }
        });
        return view;
    }
}