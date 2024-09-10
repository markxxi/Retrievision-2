package com.example.retrievision;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import soup.neumorphism.NeumorphButton;

public class Start_Lost extends AppCompatActivity {
TextView rfotext, rfo;
NeumorphButton start;
ImageView replaceBoxImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.start);
        InitIDs();
        textViewFun();
        buttonFun();
        setReplaceBoxImage();
    }

    private void InitIDs(){
        rfotext = findViewById(R.id.rfotext);
        rfo = findViewById(R.id.rfo);
        start = findViewById(R.id.start_button);
        replaceBoxImage = findViewById(R.id.boximage);
    }

    private void setReplaceBoxImage(){
        replaceBoxImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_lost));
    }

    private void textViewFun(){
        rfotext.setText(R.string.reportLostObject);
        rfo.setText(R.string.lost_description);
    }

    private void buttonFun(){
        start.setOnClickListener(v->{simulateButtonClick();
        startActivity(new Intent(this, Submit_Image_Lost.class));
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