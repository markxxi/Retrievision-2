package com.example.retrievision;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Abstract_Confirmation extends AppCompatActivity {

    TextView getCategoryAsText, getTypeAsText, getColorAsText, getBrandAsText, getModelAsText, getTextAsText, getRemarksAsText, getSizeAsText, getShapeAsText, getWidthAsText, getHeightAsText, getLocationAsText, getDateAsText, getTimeAsText, getReporterName;
    LinearLayout categoryContainer, typeContainer, colorContainer, modelContainer, brandContainer, sizeContainer, shapeContainer, dimensionContainer, textContainer, remarksContainer, locationContainer, dateContainer, reporterContainer;
    FirebaseFirestore firestore;
    ProgressBar progressBar;

    protected void displayInformation() {
        Intent intent = getIntent();
        displayList(intent, "category", categoryContainer, getCategoryAsText);
        displayList(intent, "type", typeContainer, getTypeAsText);
        displayList(intent, "color", colorContainer, getColorAsText);
        displayList(intent, "model", modelContainer, getModelAsText);
        displayList(intent, "brand", brandContainer, getBrandAsText);
        displayList(intent, "texts", textContainer, getTextAsText);
        displaySingleValue(intent, "selectedShape", shapeContainer, getShapeAsText);
        displaySingleValue(intent, "selectedSize", sizeContainer, getSizeAsText);
        displaySingleValue(intent, "width", dimensionContainer, getWidthAsText);
        displaySingleValue(intent, "height", dimensionContainer, getHeightAsText);
        displaySingleValue(intent, "remarks", remarksContainer, getRemarksAsText);
        displaySingleValue(intent, "displayName", reporterContainer, getReporterName);
        displayList(intent, "selectedLocation", locationContainer, getLocationAsText);
        displaySingleValue(intent, "date", dateContainer, getDateAsText);
        displaySingleValue(intent, "time", dateContainer, getTimeAsText);
    }

    protected void displayList(Intent intent, String key, LinearLayout container, TextView textView) {
        ArrayList<String> dataList = intent.getStringArrayListExtra(key);
        if (dataList != null && !dataList.isEmpty()) {
            StringBuilder dataText = new StringBuilder();
            for (String item : dataList) {
                dataText.append(item).append(", ");
            }
            if (dataText.length() > 2) {
                dataText.delete(dataText.length() - 2, dataText.length());
            }
            textView.setText(dataText.toString());
            container.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.GONE);
        }
    }

    protected void displaySingleValue(Intent intent, String key, LinearLayout container, TextView textView) {
        String data = intent.getStringExtra(key);
        if (data != null && !data.isEmpty()) {
            textView.setText(data);
            container.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.GONE);
        }
    }


    protected void Image() {
        String photoPath = getIntent().getStringExtra("photoPath");
        CardView cardView = findViewById(R.id.cv);
        ImageView imageView = findViewById(R.id.imageView4);
        if(photoPath!=null){
            Glide.with(this).load(photoPath).into(imageView);
            Log.d("ImagePath", "Photo path: " + photoPath);
        }  else{
            cardView.setVisibility(View.GONE);
        }
    }


    protected void InitChipObject(String reportType){
        Intent intent = getIntent();
        ChipClass object = new ChipClass();

        object.setCategory(intent.getStringArrayListExtra("category"));
        object.setType(intent.getStringArrayListExtra("type"));
        object.setColors(intent.getStringArrayListExtra("color"));
        object.setModel(intent.getStringArrayListExtra("model"));
        object.setBrand(intent.getStringArrayListExtra("brand"));
        object.setText(intent.getStringArrayListExtra("texts"));
        object.setShape(intent.getStringExtra("selectedShape"));
        object.setSize(intent.getStringExtra("selectedSize"));
        object.setWidth(intent.getStringExtra("width"));
        object.setHeight(intent.getStringExtra("height"));
        object.setRemarks(intent.getStringExtra("remarks"));
        object.setLocation(intent.getStringArrayListExtra("selectedLocation"));
        object.setDate(intent.getStringExtra("date"));
        object.setTime(intent.getStringExtra("time"));
        object.setStatus("Pending");
        object.setStudentName(intent.getStringExtra("displayName"));
        object.setUID(intent.getStringExtra("userID"));
        object.setReportType(reportType);
        setReportDate(object);
        String photoPath = intent.getStringExtra("photoPath");

        checkForExistingReport(object, reportType, photoPath);
    }

    private void setReportDate(ChipClass object){
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateReportedFormatted = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String dateReported = localDateTime.format(dateReportedFormatted);
        object.setDateReported(dateReported);
    }

    protected void storeImage(String photoPath, ChipClass object, String collectionName){
        if (photoPath != null && !photoPath.isEmpty()) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            Uri file = Uri.fromFile(new File(photoPath));
            StorageReference imageRef = storageRef.child("images/" + file.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(file);
            uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                object.setImageUrl(imageUrl);
                setFireStore(object, collectionName);
            }).addOnFailureListener(exception -> Log.e("ImageUpload", "Failed to get image download URL: " + exception.getMessage()))).addOnFailureListener(exception -> Log.e("ImageUpload", "Image upload failed: " + exception.getMessage()));
        } else {
            setFireStore(object, collectionName);
        }

    }

    protected void setFireStore(ChipClass object, String collectionName) {
        String userId = getIntent().getStringExtra("userID");
        assert userId != null;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection(collectionName).document();
        String reportId = docRef.getId();
        object.setReportId(reportId);
        firestore.collection("User").document(userId).collection(collectionName)
                .document(reportId)
                .set(object)
                .addOnSuccessListener(aVoid -> {
                    firestore.collection(collectionName).document(reportId)
                            .set(object)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Document added with ID: " + reportId, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                if(object.getReportType().equals("LostObject")){notifyUsersOfNewLostReport();}
                                else {notifyUsersOfNewFoundReport();}
                                OpenSuccessfulActivity();

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to set document in collection", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add document to user collection", Toast.LENGTH_SHORT).show();
                });

    }
    private void checkForExistingReport(ChipClass object, String reportType, String photoPath) {
        String userId = getIntent().getStringExtra("userID");

        String category = object.getCategory() != null && !object.getCategory().isEmpty() ? object.getCategory().get(0) : null;
        String type = object.getType() != null && !object.getType().isEmpty() ? object.getType().get(0) : null;
        String color = object.getColors() != null && !object.getColors().isEmpty() ? object.getColors().get(0) : null;

        if (category == null || type == null || color == null) {
            Toast.makeText(this, "Missing report information", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        firestore.collection("User")
                .document(userId)
                .collection("FoundObject")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        boolean isDuplicate = false;

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            // fix this unchecked cast
                            List<String> existingCategory = (List<String>) document.get("category");
                            List<String> existingType = (List<String>) document.get("type");
                            List<String> existingColor = (List<String>) document.get("colors");
                            Log.d("checkForExistingReport", "Checking document: " + document.getId());
                            Log.d("checkForExistingReport", "Existing Category: " + existingCategory + ", Type: " + existingType + ", Color: " + existingColor);
                            Log.d("checkForExistingReport", "Input Category: " + category + ", Type: " + type + ", Color: " + color);


                            if (existingCategory != null && existingType != null && existingColor != null
                                    && existingCategory.contains(category)
                                    && existingType.contains(type)
                                    && existingColor.contains(color)) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (isDuplicate) {
                            Toast.makeText(this, "You already reported this object.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        } else {
                            storeImage(photoPath,object,reportType);

                        }
                    } else {
                        Log.e("checkForExistingReport", "Error getting documents: " + task.getException());
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("checkForExistingReport", "Firestore error: " + e.getMessage());
                    progressBar.setVisibility(View.GONE); // Ensure progress bar is hidden even if query fails
                });
    }



    protected void OpenSuccessfulActivity(){
        Intent intent = new Intent(this, flow_successful.class);
        startActivity(intent);
    }
    protected void notifyUsersOfNewFoundReport() {
        TokenFetcher tokenFetcher = new TokenFetcher();
        NotificationSender sender = new NotificationSender(tokenFetcher);
        sender.sendNotification("/topics/new_found_object", "Notice!", "A Found Object has been Reported");
    }
    protected void notifyUsersOfNewLostReport() {
        TokenFetcher tokenFetcher = new TokenFetcher();
        NotificationSender sender = new NotificationSender(tokenFetcher);
        sender.sendNotification("/topics/new_lost_object", "Notice!", "A Lost Object has been Reported");
    }

    protected  void backArrow(){
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v-> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getOnBackPressedDispatcher().onBackPressed();
            } else { // API 32 and below
                super.onBackPressed();
            }
        }
        );
    }

    protected void InitIDs(){
        getCategoryAsText = findViewById(R.id.getCategoryAsText);
        getTypeAsText = findViewById(R.id.getTypeAsText);
        getColorAsText = findViewById(R.id.getColorAsText);
        getBrandAsText = findViewById(R.id.getBrandAsText);
        getModelAsText = findViewById(R.id.getModelAsText);
        getTextAsText = findViewById(R.id.getTextAsText);
        getSizeAsText = findViewById(R.id.getSizeAsText);
        getShapeAsText = findViewById(R.id.getShapeAsText);
        getWidthAsText = findViewById(R.id.getWidthAsText);
        getHeightAsText = findViewById(R.id.getHeightAsText);
        getRemarksAsText = findViewById(R.id.getRemarksAsText);
        getLocationAsText = findViewById(R.id.getLocationAsText);
        getDateAsText = findViewById(R.id.getDateAsText);
        getTimeAsText = findViewById(R.id.getTimeAsText);
        getReporterName = findViewById(R.id.getNameAsText);
    }
    protected void InitContainers(){
        brandContainer = findViewById(R.id.brandContainer);
        modelContainer = findViewById(R.id.modelContainer);
        sizeContainer = findViewById(R.id.sizeContainer);
        shapeContainer = findViewById(R.id.shapeContainer);
        dimensionContainer = findViewById(R.id.dimensionContainer);
        textContainer = findViewById(R.id.textContainer);
        remarksContainer = findViewById(R.id.remarksContainer);
        categoryContainer = findViewById(R.id.categoryContainer);
        typeContainer = findViewById(R.id.typeContainer);
        colorContainer = findViewById(R.id.colorContainer);
        locationContainer = findViewById(R.id.locationContainer);
        dateContainer = findViewById(R.id.dateContainer);
        reporterContainer = findViewById(R.id.reporterContainer);
    }
}
