package com.example.retrievision;

import android.app.Dialog;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;
import java.util.Objects;

import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import soup.neumorphism.NeumorphButton;

public class HomeFragment extends Fragment {

    View view;
    Button lostObject, foundObject;
    Dialog dialog;
    LinearLayout container;
    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;
    String userID;
    NeumorphButton scanNowButton, scan_another, home_button;
    ProgressBar progressBar;

    //get the matching objects in FireStore
    List<ChipClass> matchedObjects;
    List<String>foundTypes;

    String reportId;
    boolean matchFound;

    ScrollView scrollView;
    RelativeLayout relativeLayout;
    TextView textView, headline, subheadline;
    ProgressBar progressBar2;

    TextView floor, count;
    AppCompatButton gfloor, mfloor, floor2, floor3, floor4, rfloor, court, parking;
    ProgressBar textPB;
    int docCount;
    private AppCompatButton selectedButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        view = inflater.inflate(R.layout.fragment_home, container, false);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        InitIDs();

        initHint();

        InitFloorButtons();
        clickListeners();

        scanNowButton = view.findViewById(R.id.scanbutton);
        scanNowButton.setOnClickListener(v -> {
            simulateButtonClick(scanNowButton, "#FFFFFF", "#F7B94A");
            fetchObjects(this::showFoundObjectsDialog);
            scanNowButton.setEnabled(false); });

        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        });
    }


    private void InitIDs(){
        lostObject = view.findViewById(R.id.lostObject);
        foundObject = view.findViewById(R.id.foundObject);
        progressBar = view.findViewById(R.id.progressBar);
    }
    private void initHint(){
        floor = view.findViewById(R.id.floor);
        count = view.findViewById(R.id.count);

        gfloor = view.findViewById(R.id.gfloor);
        mfloor = view.findViewById(R.id.mfloor);
        floor2 = view.findViewById(R.id.floor2);
        floor3 = view.findViewById(R.id.floor3);
        floor4 = view.findViewById(R.id.floor4);
        rfloor = view.findViewById(R.id.rfloor);
        court = view.findViewById(R.id.court);
        parking = view.findViewById(R.id.parking);

        textPB = view.findViewById(R.id.textProgressBar);
    }
    private void InitFloorButtons(){
        InitGroundFloor();
        OnClickFloorButton(gfloor, "Ground Floor","Ground Floor", null);
        OnClickFloorButton(mfloor, "Mezzanine","1st Floor (Faculty and Conference Room)", "1st Floor (Mezzanine)");
        OnClickFloorButton(floor2, "2nd Floor", "2nd Floor", null);
        OnClickFloorButton(floor3, "3rd Floor", "3rd Floor", null);
        OnClickFloorButton(floor4, "4th Floor","4th Floor (Multipurpose Hall)", "4th Floor (Library)");
        OnClickFloorButton(rfloor, "Roof Deck", "Roof Deck", null);
        OnClickFloorButton(court, "Court", "Court", null);
        OnClickFloorButton(parking, "Parking", "Parking", null);
    }
    private void InitGroundFloor(){
        floor.setText(R.string.ground_floor);
        selectButton(gfloor);
        getCount("Ground Floor", null);
    }
    private void clickListeners(){
        lostObject.setOnClickListener(v->startActivity(new Intent(getActivity(), Start_Lost.class)));
        foundObject.setOnClickListener(v->startActivity(new Intent(getActivity(), Start_Found.class)));
    }
    private void OnClickFloorButton(AppCompatButton button, String floorName, String floor_val, String floor_val_2){
        button.setOnClickListener(v->{
            count.setVisibility(View.GONE);
            selectButton(button);
            floor.setText(floorName);
            getCount(floor_val, floor_val_2);

        });
    }
    private void selectButton(AppCompatButton button) {
        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.button_selector_default);
        }
        selectedButton = button;
        selectedButton.setBackgroundResource(R.drawable.button_selector_clicked);
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

        List<ChipClass> compiledObjects = new ArrayList<>();
        compiledObjects.addAll(foundObjects);
        compiledObjects.addAll(lostObjects);

        Comparator<ChipClass> comparator = (object1, object2) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            LocalDateTime date1 = LocalDateTime.parse(object1.getDateReported(), formatter);
            LocalDateTime date2 = LocalDateTime.parse(object2.getDateReported(), formatter);
            return date2.compareTo(date1);
        };
        Collections.sort(compiledObjects, comparator);
        TextView noObjectsTextView = dialog.findViewById(R.id.dialog_title);
        if (foundObjects.isEmpty() && lostObjects.isEmpty()) {
            noObjectsTextView.setText(R.string.no_reported_objects);
        } else {
            noObjectsTextView.setVisibility(View.GONE);
            for (ChipClass object : compiledObjects) {
                String objectType = foundObjects.contains(object) ? "FoundObject" : "LostObject";
                int chipColor = objectType.equals("FoundObject") ? R.color.blue : R.color.yellow;
                populateObjects(Collections.singletonList(object), objectType, chipColor);
            }
        }

    }
    private void populateObjects(List<ChipClass> objects, String objectType, int chipColor) {
        for (ChipClass object : objects) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.inf_objects_scan, container, false);
            Chip chip = itemView.findViewById(R.id.chip_reporttype);
            TextView objectName = itemView.findViewById(R.id.object_name);
            TextView objectDate = itemView.findViewById(R.id.object_date);
            NeumorphButton scanButton = itemView.findViewById(R.id.scan_button);
            chip.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppinsmedium));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), chipColor)));
            if (objectType.equals("FoundObject")) chip.setText("Found");
            else chip.setText("Lost");

            objectName.setText(object.getType().toString().replace("[", "").replace("]", ""));
            objectDate.setText(object.getDate());

            container.addView(itemView);

            scanButton.setOnClickListener(v -> {
                if (objectType.equals("FoundObject")) { searchForFoundMatchObjects(object); }
                else { searchForLostMatchObjects(object); hideProgressBar1(); }
            });
        }

    }
    private void showMatchesForLost(List<ChipClass> matchedObjects) {
        ScrollView scroll_cont = dialog.findViewById(R.id.scroll_container);
        scroll_cont.setVisibility(View.GONE);
        ViewPager2 viewPager = dialog.findViewById(R.id.viewPager);
        Adapter_MatchedObjects adapter = new Adapter_MatchedObjects(getContext(), matchedObjects, viewPager, reportId);
        viewPager.setAdapter(adapter);
        TextView dialog_footer = dialog.findViewById(R.id.dialog_footer);
        dialog_footer.setVisibility(View.VISIBLE);
    }
    private void InitViewsResult(){
        scrollView = dialog.findViewById(R.id.scroll_container);
        relativeLayout = dialog.findViewById(R.id.layout_no_match);
        textView = dialog.findViewById(R.id.dialog_title);
        headline = dialog.findViewById(R.id.matchfound_headline);
        subheadline = dialog.findViewById(R.id.subheadline);

        function();
    }

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
                            if("Pending".equals(foundObject.getStatus())){
                                foundObjects.add(foundObject);
                            }
                        }
                        for(QueryDocumentSnapshot document: lostObjectTask.getResult()){
                            ChipClass lostObject = document.toObject(ChipClass.class);
                            if("Pending".equals(lostObject.getStatus())){
                                lostObjects.add(lostObject);
                            }
                        }
                        firestoreCallback.onCallback(foundObjects, lostObjects);
                        scanNowButton.setEnabled(true);
                    }
                    else {
                        Log.w("Error getting documents.", task.getException());
                    }
                });
    }
    private void getCount(String floor_val, String floor_val_2){
        textPB.setVisibility(View.VISIBLE);
        firestore.collection("FoundObject")
                .whereArrayContains("location", floor_val)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        docCount = queryDocumentSnapshots.size();
                        firestore.collection("FoundObject")
                                .whereArrayContains("location", floor_val_2)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        int docCount2 = queryDocumentSnapshots.size() + docCount;
                                        String holder = " Objects Found";
                                        count.setText(String.valueOf(docCount2).concat(holder));
                                        Log.e("Size: ", String.valueOf(docCount2));
                                        textPB.setVisibility(View.GONE);
                                        count.setVisibility(View.VISIBLE);
                                    }
                                });

                    }

                });

    }


    private void searchForLostMatchObjects(ChipClass reportType) {
        searchForMatchObjects(reportType, "FoundObject", true);
    }
    private void searchForFoundMatchObjects(ChipClass reportType) {
        searchForMatchObjects(reportType, "LostObject", false);
    }
    private void searchForMatchObjects(final ChipClass reportType, String collectionPath, boolean isLost) {
        reportId = reportType.getReportId();
        firestore.collection(collectionPath)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        matchedObjects = new ArrayList<>();
                        matchFound = false;
                        Syn synonymService = new Syn();
                        List<String> reportTypesList = reportType.getType();
                        AtomicInteger pendingRequests = new AtomicInteger(querySnapshot.size());
                        ScrollView scroll_cont = dialog.findViewById(R.id.scroll_container);
                        scroll_cont.setVisibility(View.GONE);
                        showProgressBar1();

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String status = document.getString("status");

                            if ("Returned".equals(status)) {
                                pendingRequests.decrementAndGet();
                                continue;
                            }

                            Object typeObj = document.get("type");
                            foundTypes = new ArrayList<>();
                            if (typeObj instanceof List<?>) {
                                List<?> typeList = (List<?>) typeObj;
                                if (typeList.isEmpty() || typeList.get(0) instanceof String) {
                                    //noinspection unchecked
                                    foundTypes = (List<String>) typeObj;
                                }
                            }

                            if (arraysMatch(reportTypesList, foundTypes)) {
                                matchFound = true;
                                matchedObjects.add(document.toObject(ChipClass.class));
                                pendingRequests.decrementAndGet();
                            } else {
                                synonymService.getSynonyms(String.join(" ", reportTypesList), new Callback<List<Synonym>>() {
                                    @Override
                                    public void onResponse(Call<List<Synonym>> call, Response<List<Synonym>> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            for (Synonym synonym : response.body()) {
                                                if (foundTypes.stream().anyMatch(type -> type.equalsIgnoreCase(synonym.word))) {
                                                    matchFound = true;
                                                    matchedObjects.add(document.toObject(ChipClass.class));
                                                    Log.i("SynonymMatch", "Detected synonym: " + synonym.word + " for type: " + reportTypesList);
                                                    break;
                                                }
                                            }
                                        }
                                        checkAndHandleResults(pendingRequests.decrementAndGet(), isLost, reportType);
                                    }

                                    @Override
                                    public void onFailure(Call<List<Synonym>> call, Throwable t) {
                                        Log.e("SynonymService", "Error fetching synonyms", t);
                                        checkAndHandleResults(pendingRequests.decrementAndGet(), isLost, reportType);
                                    }
                                });
                            }
                        }

                        checkAndHandleResults(pendingRequests.get(), isLost, reportType);
                    }
                });
    }
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
    private int threshold(ChipClass attribute1, ChipClass attribute2 ){
        int threshold = 0;
        if(arraysMatch(attribute1.getType(), attribute2.getType())) {
            threshold+=4;
        }
        if(arraysMatch(attribute1.getCategory(), attribute2.getCategory())){
            threshold += 3;
        }
        if (arraysMatch(attribute1.getColors(), attribute2.getCategory())){
            threshold += 3;
        }
        if (arraysMatch(attribute1.getLocation(), attribute2.getLocation())){
            threshold+=3;
        }
        if (arraysMatch(attribute1.getModel(), attribute2.getModel())){
            threshold+=3;
        }
        if (Objects.equals(attribute1.getSize(), attribute2.getSize())) {
            threshold += 2;
        }
        if (Objects.equals(attribute1.getBrand(), attribute2.getBrand())){
            threshold += 2;
        }

        threshold+=MatchDateScore(attribute1.getDateReported(), attribute2.getDateReported());

        return threshold;
    }
    private int MatchDateScore(String date1, String date2) {
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        DateTimeFormatter formatter2 = DateTimeFormatter.ISO_DATE_TIME;  // This handles the 'T' and 'Z'

        ZonedDateTime d1 = parseDate(date1, formatter1, formatter2);
        ZonedDateTime d2 = parseDate(date2, formatter1, formatter2);

        long daysBetween = ChronoUnit.DAYS.between(d1, d2);
        if (daysBetween == 0) {
            return 4;
        } else if (daysBetween <= 1) {
            return 3;
        } else if (daysBetween <= 3) {
            return 2;
        } else if (daysBetween <= 7) {
            return 1;
        } else {
            return 0;
        }
    }

    private ZonedDateTime parseDate(String date, DateTimeFormatter formatter1, DateTimeFormatter formatter2) {
        try {
            // Try parsing with the first format (dd-MM-yyyy HH:mm:ss)
            return LocalDateTime.parse(date, formatter1).atZone(ZoneId.systemDefault());
        } catch (Exception e) {
            // If parsing fails, try the second format (ISO 8601 with 'T' and 'Z')
            return ZonedDateTime.parse(date, formatter2);
        }
    }

    private void checkAndHandleResults(int remainingRequests, boolean isLost, ChipClass reportType) {
        if (remainingRequests == 0) {
            hideProgressBar1();
            InitViewsResult();

            if (matchFound) {
                Collections.sort(matchedObjects, (attribute1, attribute2) -> {
                    int score1 = threshold(attribute1, reportType);
                    int score2 = threshold(attribute2, reportType);
                    String status1 = attribute1.getStatus();
                    String status2 = attribute2.getStatus();

                    if ("Match Found".equals(status1) && !"Match Found".equals(status2)) {
                        return 1;
                    } else if (!"Match Found".equals(status1) && "Match Found".equals(status2)) {
                        return -1;
                    } else {
                        return Integer.compare(score2, score1); // higher scores first
                    }
                });
                handleMatchResult(true, isLost);
            } else {
                handleMatchResult(false, isLost);
            }
        }
    }
    private void handleMatchResult(boolean matchFound, boolean isLost) {
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
                NotifyOwner();
            }
        }
    }

    private void function(){
        home_button = dialog.findViewById(R.id.home_button);
        scan_another = dialog.findViewById(R.id.scan_another);
        home_button.setOnClickListener(v->{simulateButtonClick(home_button, "#FFFFFF", "#143C6F");dialog.dismiss();});
        scan_another.setOnClickListener(v->{ simulateButtonClick(scan_another, "#143C6F", "#FFFFFF"); fetchObjects(this::showFoundObjectsDialog); dialog.dismiss();});
    }
    private void simulateButtonClick(NeumorphButton button, String ColorInit, String ColorRun) {
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor(ColorInit)));
        button.setStrokeWidth(2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setStrokeColor(ColorStateList.valueOf(Color.parseColor(ColorRun)));
                button.setStrokeWidth(2);
            }
        }, 100);
    }
    private void NotifyOwner(){
        for (ChipClass obj: matchedObjects){
            String OwnerUID = obj.getUID();

            Toast.makeText(getContext(), "OwnerUID: " + OwnerUID, Toast.LENGTH_SHORT).show();
            FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
            firebaseMessaging.getToken().addOnCompleteListener(task1 -> {
                if(task1.isSuccessful()) {
                    String token = task1.getResult();
                    firestore.collection("User")
                            .document(OwnerUID)
                            .update("fcmToken", token)
                            .addOnSuccessListener(task2->{
                                Toast.makeText(getContext(), "FCM: " + token, Toast.LENGTH_SHORT).show();
                                TokenFetcher tokenFetcher = new TokenFetcher();
                                NotificationSender sender = new NotificationSender(tokenFetcher);
                                sender.sendNotification(token, "Match Found", "The object you lost " + obj.getType() + " has a match.");
                            });
                } //documents that has a status of match found already should not receive any notification
            });
        }
    }



    private interface FireStoreCallback {
        void onCallback(List<ChipClass> foundObjects, List<ChipClass> lostObjects);
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