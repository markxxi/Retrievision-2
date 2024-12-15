package com.example.retrievision;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
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
import androidx.core.content.FileProvider;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Submit_Image_Found extends Abstract_Submit {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.submit_image);
        startPython();
        InitIDs();
        buttonFun();
        initActivityResultLauncher();

    }

    protected  void initActivityResultLauncher(){
        pictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        setImageView(openCamera);
                    } else {
                       clearImageView(openCamera);
                    }
                }
        );
    }

    private void cameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startTestCam(){
        Toast.makeText(this, "camera", Toast.LENGTH_SHORT).show();
    }
    @SuppressLint("QueryPermissionsNeeded")
    private void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Logger.getLogger(Submit_Image_Found.class.getName()).log(Level.SEVERE, "An error occurred", ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                pictureLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
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
            resultTextView.setText("Image cannot be empty!");
            cancelButton.setText("Take Photo");
        }

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            startCamera();
        });
        yesButton.setOnClickListener(v -> {
            Intent objectIntent = new Intent(this, className);
            objectIntent.putExtra("highestObject", classes[maxPos]);
            objectIntent.putExtra("dominant_colors", colorStringBuilder.toString());
            objectIntent.putExtra("photoPath", currentPhotoPath);
            startActivity(objectIntent);
        });
    }

    private void InitIDs(){
        openCamera = findViewById(R.id.openCamera);
        submitImage = findViewById(R.id.submitImage);
        progressBar = findViewById(R.id.progressbar);
        skip = findViewById(R.id.skip);
    }

    private void buttonFun(){
        openCamera.setOnClickListener(v -> {
        cameraPermission();
        });


        submitImage.setOnClickListener(v -> {
            if (currentPhotoPath == null || currentPhotoPath.isEmpty()) {
                nullDialog();
            } else {

                ImageClassification();

            }
        });
        withoutClassification();
    }



    private void withoutClassification(){
        skip.setVisibility(View.VISIBLE);
        skip.setText("SUBMIT WITHOUT CLASSIFICATION");
        skip.setOnClickListener(v->{
            if (currentPhotoPath == null || currentPhotoPath.isEmpty()) {
                nullDialog();
            } else {
                dialogWithoutClassification(Generated_Found.class);
            }
        });
    }

    private void dialogWithoutClassification(Class className) {
        if (currentPhotoPath == null || currentPhotoPath.isEmpty()) {
            Toast.makeText(this, "No photo captured!", Toast.LENGTH_SHORT).show();
        } else {
            Intent objectIntent = new Intent(this, className);
            objectIntent.putExtra("photoPath", currentPhotoPath); // Pass the photoPath
            startActivity(objectIntent);
        }
    }
}