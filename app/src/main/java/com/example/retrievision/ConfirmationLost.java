package com.example.retrievision;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import soup.neumorphism.NeumorphButton;


public class ConfirmationLost extends Abstract_Confirmation{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.confirmation);
        firestore = FirebaseFirestore.getInstance();
        InitIDs();
        InitContainers();
        displayInformation();
        Image();
        reporterLabel();
        NeumorphButton submit = findViewById(R.id.submitToDB);
        backArrow();
        progressBar = findViewById(R.id.progress_bar);
        submit.setOnClickListener(v-> {
            submit.setEnabled(false);
            simulateButtonClick(submit);
            submit.setText("");
            submit.setBackgroundColor(Color.parseColor("#D9D9D9"));
            progressBar.setVisibility(View.VISIBLE);
            InitChipObject("LostObject");
        });
    }



    private void reporterLabel(){
        TextView reporterLabel = findViewById(R.id.reporterlabel);
        reporterLabel.setText(R.string.owner);
    }


    private void simulateButtonClick(NeumorphButton button) {
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        button.setStrokeWidth(2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F7B94A")));
                button.setStrokeWidth(2);
            }
        }, 100);
    }

}
