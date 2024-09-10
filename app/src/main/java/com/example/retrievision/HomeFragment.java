package com.example.retrievision;

import android.app.Dialog;
import android.content.Intent;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import soup.neumorphism.NeumorphButton;

public class HomeFragment extends Fragment {
    View view;
    Button lostObject, foundObject;
    Dialog dialog;
    LinearLayout container;
    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;
    String userID;
    NeumorphButton scanNowButton;
    ProgressBar progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        InitIDs();

        clickListeners();
        scanNowButton = view.findViewById(R.id.scanbutton);
        scanNowButton.setOnClickListener(v -> {
            simulateButtonClick();
            fetchObjects(this::showFoundObjectsDialog);
            scanNowButton.setEnabled(false); });

        return view;
    }

private void pressedState(){
        scanNowButton.setShadowColorLight(Color.parseColor("#F7B94A"));
        scanNowButton.setShadowColorDark(Color.parseColor("#F7B94A"));
}
    private void InitIDs(){
        lostObject = view.findViewById(R.id.lostObject);
        foundObject = view.findViewById(R.id.foundObject);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void clickListeners(){
        lostObject.setOnClickListener(v->startActivity(new Intent(getActivity(), Start_Lost.class)));
        foundObject.setOnClickListener(v->startActivity(new Intent(getActivity(), Start_Found.class)));
    }

    private void newDialog(){
        dialog = new Dialog(requireContext(),R.style.AlertDialogTheme);
        dialog.setContentView(R.layout.inf_dialog_scan);
        container = dialog.findViewById(R.id.container);
        progressBar2 = dialog.findViewById(R.id.progressBar);
        dialog.show();
    }

    private void showFoundObjectsDialog(List<ChipClass> foundObjects, List<ChipClass> lostObjects) {
        newDialog();
        populateObjects(foundObjects, "FoundObject", R.color.blue);
        populateObjects(lostObjects, "LostObject", R.color.yellow);
    }

    //Show the user's found and lost reported lost object in firestore
    private void populateObjects(List<ChipClass> objects, String objectType, int chipColor) {
        for (ChipClass object : objects) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.inf_objects_scan, container, false);
            Chip chip = itemView.findViewById(R.id.chip_reporttype);
            TextView objectName = itemView.findViewById(R.id.object_name);
            TextView objectDate = itemView.findViewById(R.id.object_date);
            Button scanButton = itemView.findViewById(R.id.scan_button);

            chip.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsmedium));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), chipColor)));
            if (objectType.equals("FoundObject")) chip.setText("Found");
            else chip.setText("Lost");

            objectName.setText(object.getType().toString().replace("[", "").replace("]", ""));
            objectDate.setText(object.getDate());

            container.addView(itemView);

            scanButton.setOnClickListener(v -> {
                showProgressBar1();
                if (objectType.equals("FoundObject")) { searchForFoundMatchObjects(object); hideProgressBar1(); }
                else { searchForLostMatchObjects(object); hideProgressBar1(); }
            });
        }
    }

    private interface FireStoreCallback {
        void onCallback(List<ChipClass> foundObjects, List<ChipClass> lostObjects);
    }

    //Get the user's found and lost reported lost object in firestore
    private void fetchObjects(FireStoreCallback firestoreCallback){
        userID = firebaseAuth.getUid();
        showProgressBar();
        Task<QuerySnapshot> foundObjectTask = firestore.collection("User").document(userID).collection("FoundObject").get();
        Task<QuerySnapshot> lostObjectTask = firestore.collection("User").document(userID).collection("LostObject").get();

        Tasks.whenAllSuccess(foundObjectTask, lostObjectTask)
                .addOnCompleteListener(task->{
                    hideProgressBar();
                    if(task.isSuccessful()){
                        List<ChipClass> foundObjects = new ArrayList<>();
                        List<ChipClass> lostObjects = new ArrayList<>();
                            for(QueryDocumentSnapshot document: foundObjectTask.getResult()){
                                ChipClass foundObject = document.toObject(ChipClass.class);
                                foundObjects.add(foundObject);
                            }
                            for(QueryDocumentSnapshot document: lostObjectTask.getResult()){
                                ChipClass lostObject = document.toObject(ChipClass.class);
                                lostObjects.add(lostObject);
                            }
                            firestoreCallback.onCallback(foundObjects, lostObjects);
                            scanNowButton.setEnabled(true);
                    }
                    else {
                        Log.w("Error getting documents.", task.getException());
                    }
                });
    }

    //Get the matching objects in FireStore
   List<ChipClass> matchedObjects;
    List<String> foundCategories, foundTypes;
    List<String> foundCategory;
    String reportId;
    private void searchForMatchObjects(ChipClass reportType, String collectionPath, boolean isLost) {
       reportId = reportType.getReportId();
        firestore.collection(collectionPath)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        matchedObjects = new ArrayList<>();
                        boolean matchFound = false;
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Object category = document.get("category");
                            Object type = document.get("type");
                            if(category instanceof List<?>){
                                //noinspection unchecked
                                foundCategories = (List<String>) category;
                            }
                            if (type instanceof List<?>){
                                //noinspection unchecked
                                foundTypes = (List<String>) type;
                            }

                            if (arraysMatch(reportType.getCategory(), foundCategories) &&
                                    arraysMatch(reportType.getType(), foundTypes)) {
                                    matchFound = true;
                                       ChipClass matchedObject = document.toObject(ChipClass.class);
                                       // matchedObject.getReportId();
                                       // Toast.makeText(getContext(), "Report Id: " + matchedObject.getReportId(), Toast.LENGTH_SHORT).show();
                                        matchedObjects.add(matchedObject);
                                    }
                        }
                        InitViewsResult();
                        handleMatchResult(matchFound, isLost);
                    }
                });
    }

    private void handleMatchResult(boolean matchFound, boolean isLost)
    {
        if (isLost) {
            if (matchFound) {
                int matchCount = matchedObjects.size();
                textView.setText(MessageFormat.format("{0}{1}", matchCount, getString(R.string.possible_matches_found)));
                showMatchesForLost(matchedObjects);
            } else {
                scrollView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);
            }
        } else {
            scrollView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
            if (matchFound) {
                headline.setText(R.string.a_match_has_been_found);
                subheadline.setText(R.string.surrender_the_object_to_the_student_affairs);
            }
        }
    }

    private void searchForLostMatchObjects(ChipClass reportType) {
        searchForMatchObjects(reportType, "FoundObject", true);
    }

    private void searchForFoundMatchObjects(ChipClass reportType) {
        searchForMatchObjects(reportType, "LostObject", false);
    }

    ScrollView scrollView;
    RelativeLayout relativeLayout;
    TextView textView, headline, subheadline;
    ProgressBar progressBar2;

    private void InitViewsResult(){
            scrollView = dialog.findViewById(R.id.scroll_container);
            relativeLayout = dialog.findViewById(R.id.layout_no_match);
            textView = dialog.findViewById(R.id.dialog_title);
            headline = dialog.findViewById(R.id.matchfound_headline);
            subheadline = dialog.findViewById(R.id.subheadline);
    }

    //Inflate the matched objects in a card
    private void showMatchesForLost(List<ChipClass> matchedObjects) {
        ScrollView scroll_cont = dialog.findViewById(R.id.scroll_container);
        scroll_cont.setVisibility(View.GONE);
        ViewPager2 viewPager = dialog.findViewById(R.id.viewPager);
        Adapter_MatchedObjects adapter = new Adapter_MatchedObjects(matchedObjects, viewPager, reportId);
        viewPager.setAdapter(adapter);
        TextView dialog_footer = dialog.findViewById(R.id.dialog_footer);
        dialog_footer.setVisibility(View.VISIBLE);
    }

    //Matching
    private boolean arraysMatch(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null) {
            return false;
        }
        if (list1.size() != list2.size()) {
            return true;
        }
        for (String item : list1) {
            boolean found = false;
            for (String listItem : list2) {
                if (item.equalsIgnoreCase(listItem)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private void simulateButtonClick() {
        scanNowButton.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        scanNowButton.setStrokeWidth(2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scanNowButton.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F7B94A")));
                scanNowButton.setStrokeWidth(2);
            }
        }, 100);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }
    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
    private void showProgressBar1() {
        progressBar2.setVisibility(View.VISIBLE);
    }
    private void hideProgressBar1() {
        progressBar2.setVisibility(View.GONE);
    }


}