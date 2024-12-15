package com.example.retrievision;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import java.util.Collections;
import java.util.LinkedHashMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderboardFragment extends Fragment {
    RecyclerView recyclerView;

    LeaderboardAdapter lbadapter;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        return inflater.inflate(R.layout.fragment_first, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("FoundObject")
                .orderBy("dateReported", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Integer> arrangedWordsCount = new LinkedHashMap<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userId = document.getString("userId");
                                String studentName = document.getString("studentName");
                                if (userId != null && userId.equals("e3VBk0DQh2U9AHfdgTMUb8zWQv12")) {
                                    continue;
                                }
                                if (studentName != null) {
                                    arrangedWordsCount.put(studentName, arrangedWordsCount.getOrDefault(studentName, 0) + 1);
                                }
                            }
                            List<String> sortedNames = new ArrayList<>(arrangedWordsCount.keySet());
                            sortedNames.sort((w1, w2) -> arrangedWordsCount.get(w2) - arrangedWordsCount.get(w1));

                            ArrayList<String> uniqueName = new ArrayList<>(sortedNames);
                            ArrayList<String> countName = new ArrayList<>();
                            for (String name : uniqueName) {
                                countName.add(String.valueOf(arrangedWordsCount.get(name)));
                            }

                            Log.d(TAG, "Retrieved documents: " + task.getResult().size());

                            loadProfilePictures(uniqueName, countName);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void loadProfilePictures(ArrayList<String> uniqueName, ArrayList<String> countName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<String> profilePictures = new ArrayList<>(Collections.nCopies(uniqueName.size(), "default_placeholder_url"));
        final AtomicInteger requestCount = new AtomicInteger(0);

        for (int i = 0; i < uniqueName.size(); i++) {
            final int finalI = i;

            String name = uniqueName.get(i);

            db.collection("User")
                    .whereEqualTo("name", name)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String profileUrl = task.getResult().getDocuments().get(0).getString("profilePictureUrl");
                            profilePictures.set(finalI, profileUrl != null ? profileUrl : "default_placeholder_url");
                        } else {
                            profilePictures.set(finalI, "default_placeholder_url");
                        }
                        if (requestCount.incrementAndGet() == uniqueName.size()) {
                            setupRecyclerView(profilePictures, uniqueName, countName);
                        }
                    });
        }

    }


    private void setupRecyclerView(ArrayList<String> profilePictures, ArrayList<String> uniqueName, ArrayList<String> countName) {
        recyclerView = getView().findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        lbadapter = new LeaderboardAdapter(profilePictures, uniqueName, countName);
        recyclerView.setAdapter(lbadapter);
    }
}