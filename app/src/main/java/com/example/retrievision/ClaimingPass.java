package com.example.retrievision;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaimingPass extends AppCompatActivity {
FirebaseFirestore firestore;
String matchedReportID, objectID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.claiming_pass);
        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();
        InitIDs();
        Intent intent = getIntent();
        matchedReportID = intent.getStringExtra("matchedReportId"); // the found id (from founder)
        objectID = intent.getStringExtra("objectID"); // the lost id (from user)
        retrieveObjectID();
        retrieveMatchedReportID();
        storeClaimPass();
        setCpass_home();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(ClaimingPass.this, HomeNavigation.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void storeClaimPass() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String userID = firebaseAuth.getUid();
        String displayName = currentUser.getDisplayName();

        Map<String, Object> claimData = new HashMap<>();
        claimData.put("foundedObjectMatched", objectID);
        claimData.put("lostObjectBeingMatched", matchedReportID);
        claimData.put("userName", displayName);
        claimData.put("dateClaimed", System.currentTimeMillis());
        firestore.collection("User").document(userID)
                .collection("ClaimedObjects")
                .add(claimData)
                .addOnSuccessListener(documentReference -> {
                    String reportId = documentReference.getId();
                    getReportIdAsText.setText(reportId);
                    NotifyFounder(objectID);
                    firestore.collection("Admin")
                            .document("Main")
                            .collection("Passes")
                            .add(claimData)
                            .addOnSuccessListener(task->{

                            }).addOnFailureListener(e->Toast.makeText(ClaimingPass.this, "Error adding document to admin", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(ClaimingPass.this, "Error adding document", Toast.LENGTH_SHORT).show());
    }
//
    private void NotifyFounder(String foundedObjectMatched){
        firestore.collection("FoundObject")
                .document(foundedObjectMatched)
                .get()
                .addOnSuccessListener(task ->{
                   Object uid = task.get("uid");
                    FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
                    firebaseMessaging.getToken().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()) {
                            firestore.collection("User")
                                    .document(uid.toString())
                                    .get()
                                    .addOnSuccessListener(task2->{
                                        Object tk = task2.get("fcmToken");
                                        String tkc = tk.toString();
                                        if(tkc == null){
                                            Toast.makeText(getApplicationContext(), "token is empty", Toast.LENGTH_SHORT).show();
                                        }
                                        Log.e("Token2: ", tkc);
                                        TokenFetcher tokenFetcher = new TokenFetcher();
                                        NotificationSender sender = new NotificationSender(tokenFetcher);
                                        sender.sendNotification(tkc, "Match Found", "The object you found " + task.get("type") + " has a match.");
                                    });
                            }
                    });
                });
    }

    private void NotifyUsersOnType(){
        firestore.collection("LostObject")
                .document()
                .get()
                .addOnSuccessListener(task -> {
                    
                });

    }


    TextView getReportIdAsText, getName, getTypeAsText, getColorAsText, getLocationAsText, getFoundDateAsText, getLostDate,
            getBrandAsText, getModelAsText, getSizeAsText, getShapeAsText, getWidthAsText, getLengthAsText, getTextAsText, getRemarksAsText;
    LinearLayout brandContainer, modelContainer, sizeContainer, shapeContainer, dimensionContainer, textContainer, remarksContainer,
            reportIDContainer, typeContainer, colorContainer, locationContainer;
    ImageView imageView;
    CardView cardView;
MaterialButton cpass_home;
    private void InitIDs(){
        getReportIdAsText = findViewById(R.id.getReportIdAsText);
        getTypeAsText = findViewById(R.id.getTypeAsText);
        getColorAsText = findViewById(R.id.getColorAsText);
        getBrandAsText = findViewById(R.id.getBrandAsText);
        getModelAsText = findViewById(R.id.getModelAsText);
        getSizeAsText = findViewById(R.id.getSizeAsText);
        getShapeAsText = findViewById(R.id.getShapeAsText);
        getWidthAsText = findViewById(R.id.getWidthAsText);
        getLengthAsText = findViewById(R.id.getHeightAsText);
        getTextAsText = findViewById(R.id.getTextAsText);
        getRemarksAsText = findViewById(R.id.getRemarksAsText);
        getLocationAsText = findViewById(R.id.getLocationAsText);
        getFoundDateAsText = findViewById(R.id.getDateFound);
        getLostDate = findViewById(R.id.getDateLost);
        getName = findViewById(R.id.getNameAsText);

        brandContainer = findViewById(R.id.brandContainer);
        modelContainer = findViewById(R.id.modelContainer);
        sizeContainer = findViewById(R.id.sizeContainer);
        shapeContainer = findViewById(R.id.shapeContainer);
        dimensionContainer = findViewById(R.id.dimensionContainer);
        textContainer = findViewById(R.id.textContainer);
        remarksContainer = findViewById(R.id.remarksContainer);

        reportIDContainer = findViewById(R.id.reportIdContainer);
        typeContainer = findViewById(R.id.typeContainer);
        colorContainer = findViewById(R.id.colorContainer);
        locationContainer = findViewById(R.id.locationContainer);

        imageView = findViewById(R.id.imageView4);
        cardView = findViewById(R.id.cv);
        cpass_home = findViewById(R.id.cpass_home);
    }
    private void setCpass_home(){
        cpass_home.setOnClickListener(v-> {
            Intent intent = new Intent(this, HomeNavigation.class);
            startActivity(intent);
            finish();
        });
    }
    private void retrieveMatchedReportID(){

        firestore.collection("LostObject")
                .document(matchedReportID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        ChipClass object = document.toObject(ChipClass.class);
                        getLostDate.setText(object.getDate() + " " + object.getTime());
                    }
                });

    }

    private void retrieveObjectID(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String displayName = currentUser.getDisplayName();

        getName.setText(displayName);

        firestore.collection("FoundObject")
                .document(objectID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        ChipClass object = document.toObject(ChipClass.class);
                        setAttributeVisibility(typeContainer,  getTypeAsText, object.getType());
                        setAttributeVisibility(colorContainer, getColorAsText, object.getColors());
                        setAttributeVisibility(locationContainer, getLocationAsText, object.getLocation());
                        setAttributeVisibility(brandContainer, getBrandAsText, object.getBrand());
                        setAttributeVisibility(modelContainer, getModelAsText, object.getModel());
                        setAttributeVisibility(textContainer, getTextAsText, object.getText());
                        setAttributeVisibility(sizeContainer, getSizeAsText, object.getSize());
                        setAttributeVisibility(shapeContainer, getShapeAsText, object.getShape());
                        setAttributeVisibility(dimensionContainer, getWidthAsText, object.getWidth());
                        setAttributeVisibility(dimensionContainer, getLengthAsText, object.getHeight());
                        setAttributeVisibility(remarksContainer, getRemarksAsText, object.getRemarks());
                        getFoundDateAsText.setText(object.getDate() + " " + object.getTime());

                        String image = object.getImageUrl();
                        if(image !=null){
                            Glide.with(this).load(image).into(imageView);
                        } else {
                            cardView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void setAttributeVisibility(LinearLayout container, TextView description, List<String> attribute){
        if (attribute != null && !attribute.isEmpty()){
            String text = String.join(", ", attribute);
            container.setVisibility(View.VISIBLE);
            description.setText(text);
        } else container.setVisibility(View.GONE);
    }

    private void setAttributeVisibility(LinearLayout container, TextView description, String attribute){
        if (attribute != null && !attribute.isEmpty()){
            String text = String.join(", ", attribute);
            container.setVisibility(View.VISIBLE);
            description.setText(text);
        } else container.setVisibility(View.GONE);
    }

}