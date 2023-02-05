package com.jamburger.kitter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.ProfileAdapter;
import com.jamburger.kitter.components.User;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    SearchView searchbar;
    RecyclerView recyclerViewProfiles;
    TextView messageText;
    ProfileAdapter profileAdapter;
    List<User> profiles, allProfiles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerViewProfiles = view.findViewById(R.id.recyclerview_profiles);
        searchbar = view.findViewById(R.id.et_search);
        messageText = view.findViewById(R.id.txt_message);

        profiles = new ArrayList<>();
        allProfiles = new ArrayList<>();
        profileAdapter = new ProfileAdapter(requireContext(), profiles, "PROFILE");
        recyclerViewProfiles.setHasFixedSize(true);
        recyclerViewProfiles.setAdapter(profileAdapter);

        readProfiles();

        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
        return view;
    }

    private void filter(String text) {
        profiles.clear();
        messageText.setVisibility(View.GONE);
        if (text.length() == 0) return;
        for (User user : allProfiles) {
            if (user.getName().toLowerCase().contains(text.toLowerCase()) || user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                profiles.add(user);
            }
        }
        if (profiles.isEmpty()) {
            messageText.setVisibility(View.VISIBLE);
        }
        profileAdapter.filterList(profiles);
    }

    private void readProfiles() {
        CollectionReference userReference = FirebaseFirestore.getInstance().collection("Users");
        userReference.get().addOnSuccessListener(usersSnapshots -> {
            allProfiles.clear();
            for (DocumentSnapshot userSnapshot : usersSnapshots) {
                User user = userSnapshot.toObject(User.class);
                allProfiles.add(user);
            }
        });
    }
}