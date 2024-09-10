package com.example.retrievision;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import com.google.firebase.firestore.FirebaseFirestore;

import soup.neumorphism.NeumorphButton;


public class ConfirmationLost extends Abstract_Confirmation{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.confirmation);
        firestore = FirebaseFirestore.getInstance();
        InitIDs();
        InitContainers();
        displayInformation();
        Image();
        reporterLabel();
        NeumorphButton submit = findViewById(R.id.submitToDB);
        submit.setOnClickListener(v-> {InitChipObject("LostObject");
            Toast.makeText(this, "Button working", Toast.LENGTH_SHORT).show();});
    }

    private void reporterLabel(){
        TextView reporterLabel = findViewById(R.id.reporterlabel);
        reporterLabel.setText(R.string.owner);
    }

}
