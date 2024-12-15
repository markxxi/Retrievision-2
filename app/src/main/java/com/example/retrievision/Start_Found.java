package com.example.retrievision;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import soup.neumorphism.NeumorphButton;

public class Start_Found extends AppCompatActivity {
    NeumorphButton start;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.start);

        InitIDs();
        buttonFun();
    }
    private void InitIDs(){
        start = findViewById(R.id.start_button);
    }
    private void buttonFun(){
        start.setOnClickListener(v->{simulateButtonClick();
            startActivity(new Intent(this, Submit_Image_Found.class));
        });
    }
    private void simulateButtonClick() {
        start.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        start.setStrokeWidth(2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                start.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FBBC09")));
                start.setStrokeWidth(2);
            }
        }, 100);
    }
}