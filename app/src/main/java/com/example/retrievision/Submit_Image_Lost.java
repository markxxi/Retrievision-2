package com.example.retrievision;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import soup.neumorphism.NeumorphButton;

public class Submit_Image_Lost extends Abstract_Submit {
    TextView rfo, opengallery;
    NeumorphButton skip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.submit_image);

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

    private void storagePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            startGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGallery();
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
    }

    private void buttonFun(){
        openGallery.setOnClickListener(v -> storagePermission());
        submitImage.setOnClickListener(v -> {
            if (currentPhotoPath == null || currentPhotoPath.isEmpty()) {
                nullDialog();
            } else {
                ImageClassification();
                dialog();
            }
        });
        setSkip();
    }

    private void dialog(){
        String result = classes[maxPos];
        showDialog(result, false, Skip.class);
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