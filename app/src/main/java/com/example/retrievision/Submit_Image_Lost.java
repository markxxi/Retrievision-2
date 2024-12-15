package com.example.retrievision;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import soup.neumorphism.NeumorphButton;

public class Submit_Image_Lost extends Abstract_Submit {
    TextView rfo, opengallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.submit_image);
        startPython();
        InitIDs();
        textViewFun();
        buttonVisibilityFun();
        buttonFun();
        initActivityResultLauncher();
    }

    protected void initActivityResultLauncher(){
        pictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImage = data.getData();
                            currentPhotoPath = getRealPathFromURI(selectedImage);
                            setImageView(openGallery);
                        }
                    } else {
                        clearImageView(openGallery);
                    }
                }
        );
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }
    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 100;

    private void storagePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_CODE_READ_MEDIA_IMAGES);
        }else {

            startGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // granted
                startGallery();
            } else {
                // denied
                Toast.makeText(this, "Permission denied to read your images",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pictureLauncher.launch(pickPhotoIntent);
    }

    private void InitIDs(){
        openGallery =findViewById(R.id.openCamera);
        submitImage = findViewById(R.id.submitImage);
        skip = findViewById(R.id.skip);
        rfo = findViewById(R.id.rfo_text);
        opengallery = findViewById(R.id.textView3);
        progressBar = findViewById(R.id.progressbar);
    }

    private void buttonFun(){
        openGallery.setOnClickListener(v -> storagePermission());
        submitImage.setOnClickListener(v -> {
            if (currentPhotoPath == null || currentPhotoPath.isEmpty()) {
                nullDialog();
            } else {
                ImageClassificationLost();

            }
        });
        setSkip();
    }
    protected void showDialog(String message, boolean isError, Class className) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.inf_dialog, null);
        TextView resultTextView = dialogView.findViewById(R.id.objectresult);
        MaterialCardView materialCardView = dialogView.findViewById(R.id.RectangleView);
        TextView headerTextView = dialogView.findViewById(R.id.resultheader);
        Button cancelButton = dialogView.findViewById(R.id.noButton);
        Button yesButton = dialogView.findViewById(R.id.yesButton);

        resultTextView.setText(message);
        if (isError) {
            materialCardView.setStrokeColor(Color.parseColor("#D6293E"));
            headerTextView.setText("Photo required.");
            yesButton.setVisibility(View.GONE);
            cancelButton.setText("Take Photo");
            resultTextView.setText("Image cannot be empty!");
        }

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        cancelButton.setText("Re-select");
        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            startGallery();
        });
        yesButton.setOnClickListener(v -> {
            Intent objectIntent = new Intent(this, className);
            objectIntent.putExtra("highestObject", classes[maxPos]);
            objectIntent.putExtra("dominant_colors", colorStringBuilder.toString());
            objectIntent.putExtra("photoPath", currentPhotoPath);
            startActivity(objectIntent);
        });
    }

    private void textViewFun(){
        rfo.setText(R.string.reportLostObject);
        opengallery.setText(R.string.opengallery);
    }

    private void buttonVisibilityFun(){
        skip.setVisibility(View.VISIBLE);
    }

    private void setSkip(){ skip.setOnClickListener(v->startActivity(new Intent(this, Skip.class)));}

}