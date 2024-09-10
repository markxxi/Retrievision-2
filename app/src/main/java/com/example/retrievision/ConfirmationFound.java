package com.example.retrievision;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import com.google.firebase.firestore.FirebaseFirestore;

import soup.neumorphism.NeumorphButton;

public class ConfirmationFound extends Abstract_Confirmation {

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
        NeumorphButton submit = findViewById(R.id.submitToDB);
        submit.setOnClickListener(v-> InitChipObject("FoundObject"));
    }


}