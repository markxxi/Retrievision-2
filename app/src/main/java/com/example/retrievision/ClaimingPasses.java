package com.example.retrievision;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class ClaimingPasses extends AppCompatActivity {
FirebaseFirestore firestore;
FirebaseAuth firebaseAuth;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.claiming_passes);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();

        database_claimingPass();

    }

    String reportId;
    List<String> claimedObjects = new ArrayList<>();
    private void database_claimingPass(){

        firestore.collection("User").document(userID)
                .collection("ClaimedObjects")
                .get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot querySnapshot = task.getResult();
                    for (QueryDocumentSnapshot document : querySnapshot){
                        reportId = document.getId();
                        claimedObjects.add(reportId);
                    }
                    pass_to_pager();
                });

    }


    private void pass_to_pager(){
        ViewPager2 viewPager2 = findViewById(R.id.claiming_pass_viewpager);
        ClaimingPassAdapter adapter = new ClaimingPassAdapter(claimedObjects);
        viewPager2.setAdapter(adapter);
    }

    }

    class ClaimingPassAdapter extends RecyclerView.Adapter<ClaimingPassAdapter.ObjectViewHolder>{
    List<String> claimedObjects;


    public ClaimingPassAdapter(List<String> claimedObjects){
        this.claimedObjects = claimedObjects;
    }

    @NonNull
    @Override
    public ClaimingPassAdapter.ObjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inf_claiming_pass, parent, false);
        return new ObjectViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ClaimingPassAdapter.ObjectViewHolder holder, int position) {
        String claimedObject = claimedObjects.get(position);
        holder.getReportIdAsText.setText(claimedObject);

        claimed_object_details(holder, claimedObject);

    }

    @Override
    public int getItemCount() {
        return claimedObjects.size();
    }

    public static class ObjectViewHolder extends RecyclerView.ViewHolder {

        TextView getReportIdAsText, getName, getTypeAsText, getColorAsText, getLocationAsText, getFoundDateAsText, getLostDate,
                getBrandAsText, getModelAsText, getSizeAsText, getShapeAsText, getWidthAsText, getLengthAsText, getTextAsText, getRemarksAsText;
        LinearLayout brandContainer, modelContainer, sizeContainer, shapeContainer, dimensionContainer, textContainer, remarksContainer,
                 typeContainer, colorContainer, locationContainer;
        ImageView imageView;
TextView cv_text;
        public ObjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cv_text = itemView.findViewById(R.id.cvtext);
            getReportIdAsText = itemView.findViewById(R.id.getReportIdAsText);
            getTypeAsText = itemView.findViewById(R.id.getTypeAsText);
            getColorAsText = itemView.findViewById(R.id.getColorAsText);
            getBrandAsText = itemView.findViewById(R.id.getBrandAsText);
            getModelAsText = itemView.findViewById(R.id.getModelAsText);
            getSizeAsText = itemView.findViewById(R.id.getSizeAsText);
            getShapeAsText = itemView.findViewById(R.id.getShapeAsText);
            getWidthAsText = itemView.findViewById(R.id.getWidthAsText);
            getLengthAsText = itemView.findViewById(R.id.getHeightAsText);
            getTextAsText = itemView.findViewById(R.id.getTextAsText);
            getRemarksAsText = itemView.findViewById(R.id.getRemarksAsText);
            getLocationAsText = itemView.findViewById(R.id.getLocationAsText);
            getFoundDateAsText = itemView.findViewById(R.id.getDateFound);
            getLostDate = itemView.findViewById(R.id.getDateLost);
            getName = itemView.findViewById(R.id.getNameAsText);

            brandContainer = itemView.findViewById(R.id.brandContainer);
            modelContainer = itemView.findViewById(R.id.modelContainer);
            sizeContainer = itemView.findViewById(R.id.sizeContainer);
            shapeContainer = itemView.findViewById(R.id.shapeContainer);
            dimensionContainer = itemView.findViewById(R.id.dimensionContainer);
            textContainer = itemView.findViewById(R.id.textContainer);
            remarksContainer = itemView.findViewById(R.id.remarksContainer);

            typeContainer = itemView.findViewById(R.id.typeContainer);
            colorContainer = itemView.findViewById(R.id.colorContainer);
            locationContainer = itemView.findViewById(R.id.locationContainer);

            imageView = itemView.findViewById(R.id.imageView4);
        }
    }

    FirebaseFirestore firestore;
    String lost, found;
    FirebaseUser username;

    private void claimed_object_details(ObjectViewHolder holder, String claimedObject){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userID = firebaseAuth.getUid();
        username = firebaseAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        assert userID != null;
        firestore.collection("User").document(userID)
                .collection("ClaimedObjects")
                .document(claimedObject)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                                found = document.getString("foundedObjectMatched");
                                lost = document.getString("lostObjectBeingMatched");
                                retrieveMatchedReportID(holder);
                                objectID(holder);
                        }
                    }
                });
    }

        private void retrieveMatchedReportID(ObjectViewHolder holder){
            firestore.collection("LostObject")
                    .document(lost)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            ChipClass object = document.toObject(ChipClass.class);
                            assert object != null;
                            holder.getLostDate.setText(String.format("%s %s", object.getDate(), object.getTime()));
                            holder.getName.setText(username.getDisplayName());
                        }
                    });
        }

        private void objectID(ObjectViewHolder holder) {
            firestore.collection("FoundObject")
                    .document(found)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            ChipClass object = document.toObject(ChipClass.class);

                            if (object != null) {
                                if (object.getType() != null) {
                                    setAttributeVisibility(holder.typeContainer, holder.getTypeAsText, object.getType());
                                }
                                if (object.getColors() != null) {
                                    setAttributeVisibility(holder.colorContainer, holder.getColorAsText, object.getColors());
                                }
                                if (object.getLocation() != null) {
                                    setAttributeVisibility(holder.locationContainer, holder.getLocationAsText, object.getLocation());
                                }
                                if (object.getBrand() != null) {
                                    setAttributeVisibility(holder.brandContainer, holder.getBrandAsText, object.getBrand());
                                }
                                if (object.getModel() != null) {
                                    setAttributeVisibility(holder.modelContainer, holder.getModelAsText, object.getModel());
                                }
                                if (object.getText() != null) {
                                    setAttributeVisibility(holder.textContainer, holder.getTextAsText, object.getText());
                                }
                                if (object.getSize() != null) {
                                    setAttributeVisibility(holder.sizeContainer, holder.getSizeAsText, object.getSize());
                                }
                                if (object.getShape() != null) {
                                    setAttributeVisibility(holder.shapeContainer, holder.getShapeAsText, object.getShape());
                                }
                                if (object.getWidth() != null) {
                                    setAttributeVisibility(holder.dimensionContainer, holder.getWidthAsText, object.getWidth());
                                }
                                if (object.getHeight() != null) {
                                    setAttributeVisibility(holder.dimensionContainer, holder.getLengthAsText, object.getHeight());
                                }
                                if (object.getRemarks() != null) {
                                    setAttributeVisibility(holder.remarksContainer, holder.getRemarksAsText, object.getRemarks());
                                }

                                // Display date and time only if both are available
                                if (object.getDate() != null && object.getTime() != null) {
                                    holder.getFoundDateAsText.setText(String.format("%s %s", object.getDate(), object.getTime()));
                                }

                                String image = object.getImageUrl();
                                if (image != null) {
                                    Glide.with(holder.itemView.getContext()).load(image).into(holder.imageView);
                                } else {
                                    holder.cv_text.setText("Reported By Admin");
                                    holder.cv_text.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Log.e("objectID", "Failed to map document to ChipClass");
                            }
                        } else {
                            Log.e("objectID", "Failed to get document: " + task.getException());
                        }
                    });
        }

        private void setAttributeVisibility(LinearLayout container, TextView description, List < String > attribute) {
            if (attribute != null && !attribute.isEmpty()) {
                String text = String.join(", ", attribute);
                container.setVisibility(View.VISIBLE);
                description.setText(text);
            } else container.setVisibility(View.GONE);
        }

        private void setAttributeVisibility(LinearLayout container, TextView description, String attribute) {
            if (attribute != null && !attribute.isEmpty()) {
                String text = String.join(", ", attribute);
                container.setVisibility(View.VISIBLE);
                description.setText(text);
            } else container.setVisibility(View.GONE);
        }
}