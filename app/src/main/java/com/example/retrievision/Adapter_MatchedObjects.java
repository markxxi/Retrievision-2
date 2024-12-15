package com.example.retrievision;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Adapter_MatchedObjects extends RecyclerView.Adapter < Adapter_MatchedObjects.MatchViewHolder > {
    private final List < ChipClass > matchedObjects;
    private final ViewPager2 viewPager2;
    private final String matchedReportId;
    private Context context;

    public Adapter_MatchedObjects(Context context,List < ChipClass > matchedObjects, ViewPager2 viewPager2, String matchedReportId) {
        this.context = context;
        this.matchedObjects = matchedObjects;
        this.viewPager2 = viewPager2;
        this.matchedReportId = matchedReportId;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inf_match_found, parent, false);
        return new MatchViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        ChipClass matchedObject = matchedObjects.get(position);

        holder.getDateAsText.setText(matchedObject.getDateReported());


        setAttributeVisibility(holder.categoryContainer, holder.getCategoryAsText, matchedObject.getCategory());
        setAttributeVisibility(holder.typeContainer, holder.getTypeAsText, matchedObject.getType());
        setAttributeVisibility(holder.colorContainer, holder.getColorAsText, matchedObject.getColors());
        setAttributeVisibility(holder.locationContainer, holder.getLocationAsText, matchedObject.getLocation());
        setAttributeVisibility(holder.brandContainer, holder.getBrandAsText, matchedObject.getBrand());
        setAttributeVisibility(holder.modelContainer, holder.getModelAsText, matchedObject.getModel());
        setAttributeVisibility(holder.textContainer, holder.getTextAsText, matchedObject.getText());
        setAttributeVisibility(holder.sizeContainer, holder.getSizeAsText, matchedObject.getSize());
        setAttributeVisibility(holder.shapeContainer, holder.getShapeAsText, matchedObject.getShape());
        setAttributeVisibility(holder.dimensionContainer, holder.getWidthAsText, matchedObject.getWidth());
        setAttributeVisibility(holder.dimensionContainer, holder.getLengthAsText, matchedObject.getHeight());
        setAttributeVisibility(holder.remarksContainer, holder.getRemarksAsText, matchedObject.getRemarks());
        if (matchedObject.getImageUrl() != null && !matchedObject.getImageUrl().isEmpty()) {
            String photoPath = matchedObject.getImageUrl();
            Glide.with(holder.itemView.getContext()).load(photoPath).into(holder.setImage);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.logo_ret_rev)
                    .into(holder.setImage);
        }


        buttonClaim(holder.claim, matchedObject, holder.itemView.getContext());
        buttonNotMine(holder.notMine, position);

        ForObjectWithStatusMatchFound(matchedObject, position, holder.nc_btn, holder.setClaimedLayout, holder.setClaimed);
    }

    private void ForObjectWithStatusMatchFound(ChipClass matchedObject, int position, LinearLayout layout1, LinearLayout layout2, MaterialButton button) {
        if (matchedObject.getStatus().equals("Match Found")) {
            int index = matchedObjects.size();
            int newPos = matchedObjects.lastIndexOf(index);
            position = newPos;
            viewPager2.setCurrentItem(position);

            layout1.setVisibility(View.GONE);
            layout2.setVisibility(View.VISIBLE);

            button.setOnClickListener(v -> {
                Toast.makeText(context, "For instances of objects being exchanged, please visit the SAO.", Toast.LENGTH_SHORT+8).show();
            });
        }
    }


    private void buttonNotMine(MaterialButton button, int position) {
        button.setOnClickListener(v -> {
            viewPager2.setCurrentItem(position + 1, true);
        });
    }

    private void buttonClaim(MaterialButton button, ChipClass object, Context context) {
        button.setOnClickListener(v -> {
            showDialog(context, object);
        });
    }

    private void showDialog(Context context, ChipClass object) {
        Dialog dialog = new Dialog(context, R.style.AlertDialogTheme);
        dialog.setContentView(R.layout.inf_dialog);
        TextView header = dialog.findViewById(R.id.resultheader);
        header.setText(R.string.confirmation);
        TextView textView = dialog.findViewById(R.id.objectresult);
        textView.setText(R.string.are_you_certain_that_the_object_belongs_to_you);
        textView.setTextSize(11);
        AppCompatButton yes = dialog.findViewById(R.id.yesButton);
        AppCompatButton no = dialog.findViewById(R.id.noButton);
        no.setText("No");
        no.setOnClickListener(v -> dialog.dismiss());
        yes.setOnClickListener(v -> {

            String currentStatus = object.getStatus();
            String newStatus;

            if ("Surrendered".equals(currentStatus)) {
                newStatus = "Match Found and Surrendered";
            } else if ("Pending".equals(currentStatus)) {
                newStatus = "Match Found";
            } else {
                newStatus = "Match Found";
            }

            updateObjectStatus(object, newStatus, context); // Update with the determined status
            Intent intent = new Intent(context, ClaimingPass.class);
            intent.putExtra("objectID", objectID);
            intent.putExtra("matchedReportId", matchedReportId);
            context.startActivity(intent);
        });

        dialog.show();
    }

    String objectID;

    private void updateObjectStatus(ChipClass object, String newStatus, Context context) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userID = firebaseAuth.getUid();
        objectID = object.getReportId();

        firestore.collection("User").document(userID)
                .collection("FoundObject").document(objectID)
                .update("status", newStatus)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firestore.collection("FoundObject")
                                .document(objectID)
                                .update("status", newStatus)
                                .addOnCompleteListener(task1 -> {

                                });
                    } else {
                    }
                });

        firestore.collection("User").document(userID)
                .collection("LostObject").document(matchedReportId)
                .update("status", "Match Found")
                .addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        firestore.collection("LostObject")
                                .document(matchedReportId)
                                .update("status", newStatus)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                    } else {
                                        Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                                    }
                                });
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

    @Override
    public int getItemCount() {
        return matchedObjects.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView getCategoryAsText, getTypeAsText, getColorAsText, getLocationAsText, getDateAsText,
                getBrandAsText, getModelAsText, getSizeAsText, getShapeAsText, getWidthAsText, getLengthAsText, getTextAsText, getRemarksAsText;
        LinearLayout brandContainer, modelContainer, sizeContainer, shapeContainer, dimensionContainer, textContainer, remarksContainer,
                categoryContainer, typeContainer, colorContainer, locationContainer, nc_btn, setClaimedLayout;
        ImageView setImage;
        MaterialButton notMine, claim, setClaimed;
        TextView cv_text;
        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            getCategoryAsText = itemView.findViewById(R.id.getCategoryAsText);
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
            getDateAsText = itemView.findViewById(R.id.getDateAsText);

            cv_text = itemView.findViewById(R.id.cvtext);
            setImage = itemView.findViewById(R.id.imageView4);
            brandContainer = itemView.findViewById(R.id.brandContainer);
            modelContainer = itemView.findViewById(R.id.modelContainer);
            sizeContainer = itemView.findViewById(R.id.sizeContainer);
            shapeContainer = itemView.findViewById(R.id.shapeContainer);
            dimensionContainer = itemView.findViewById(R.id.dimensionContainer);
            textContainer = itemView.findViewById(R.id.textContainer);
            remarksContainer = itemView.findViewById(R.id.remarksContainer);

            categoryContainer = itemView.findViewById(R.id.categoryContainer);
            typeContainer = itemView.findViewById(R.id.typeContainer);
            colorContainer = itemView.findViewById(R.id.colorContainer);
            locationContainer = itemView.findViewById(R.id.locationContainer);

            notMine = itemView.findViewById(R.id.notMine);
            claim = itemView.findViewById(R.id.claim);

            nc_btn = itemView.findViewById(R.id.ncbtn_layout);
            setClaimedLayout = itemView.findViewById(R.id.setClaimedLayout);
            setClaimed = itemView.findViewById(R.id.setClaimed);
        }

    }
}