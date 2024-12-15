package com.example.retrievision;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private LinearLayout layout;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        layout = view.findViewById(R.id.notification_view);
        progressBar = view.findViewById(R.id.progressBar);

        getNotificationList();

        return view;
    }

    private void InflateNotification(List<QueryDocumentSnapshot> notificationPayload) {
        for (QueryDocumentSnapshot np : notificationPayload) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.inf_notification, layout, false);
            TextView title = itemView.findViewById(R.id.notification_title);
            TextView body = itemView.findViewById(R.id.notification_body);

            title.setText(np.getData().get("title").toString());
            body.setText(np.getData().get("body").toString());
            layout.addView(itemView);
        }
    }

    private void getNotificationList() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            progressBar.setVisibility(View.GONE);
            return; // no user is logged in
        }

        String uid = firebaseUser.getUid();
        FirebaseFirestore.getInstance()
                .collection("User")
                .document(uid)
                .collection("Notification")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        List<QueryDocumentSnapshot> np_list = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Log.d("FirestoreData", document.getData().toString());
                            np_list.add(document);
                        }
                        InflateNotification(np_list);
                    } else {
                        Log.d("FirestoreData", "Error getting documents: ", task.getException());
                    }
                });
    }
}
