package com.example.retrievision;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
FirebaseFirestore firestore;
FirebaseAuth firebaseAuth;
String userID;
View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();

        InitIDs();
        ProfileInfo();
        clickedFoundIcon();
        clickedLostIcon();
        clickedTicketIcon();
        clickedHandIcon();


        return view;
    }

    ImageView found, lost, ticket, surrender;
    CircleImageView profilePic;
    TextView displayName, displayEmail;

    private void InitIDs(){
        found = view.findViewById(R.id.found_objects);
        lost = view.findViewById(R.id.lost_objects);
        ticket = view.findViewById(R.id.ticket);
        surrender = view.findViewById(R.id.surrendered);

        profilePic = view.findViewById(R.id.profile_image);
        displayName = view.findViewById(R.id.display_name);
        displayEmail = view.findViewById(R.id.user_email);

    }

    private void ProfileInfo(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        StorageReference profilePicRef = FirebaseStorage.getInstance().getReference()
                .child("profilePictures")
                .child(currentUser.getUid() + ".jpg");
        profilePicRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            profilePic.setImageBitmap(bitmap);

        });
        String username = currentUser.getDisplayName();
        displayName.setText(username);
        String email = currentUser.getEmail();
        displayEmail.setText(email);

    }

    List<ChipClass> objects;
    Dialog dialog;

    int changeColor = Color.parseColor("#FBBC09");
    int defaultColor = Color.parseColor("#F1F5F9");
ProgressBar progressBar;
    private void clickedFoundIcon(){

        found.setOnClickListener(v-> {
            found.setColorFilter(changeColor);
            dialog = new Dialog(requireContext(),R.style.AlertDialogTheme);


            dialog.setContentView(R.layout.inf_dialog_scan);
            ScrollView scroll_cont = dialog.findViewById(R.id.scroll_container);
            scroll_cont.setVisibility(View.GONE);
            TextView dialog_title = dialog.findViewById(R.id.dialog_title);
            dialog_title.setVisibility(View.GONE);
            TextView dialog_footer = dialog.findViewById(R.id.dialog_footer);
            dialog_footer.setVisibility(View.VISIBLE);

            progressBar = dialog.findViewById(R.id.progressBar);
            showProgressBar();
            ViewPager2 viewPager = dialog.findViewById(R.id.viewPager);
            database_found(() -> {
                ObjectAdapter adapter = new ObjectAdapter(objects);
                viewPager.setAdapter(adapter);
                hideProgressBar();
            });
            dialog.show();
            new Handler().postDelayed(() -> found.setColorFilter(defaultColor), 50);
        });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void clickedLostIcon(){
        lost.setOnClickListener(v-> {
            lost.setColorFilter(changeColor);
            dialog = new Dialog(requireContext(),R.style.AlertDialogTheme);
            dialog.setContentView(R.layout.inf_dialog_scan);
            ScrollView scroll_cont = dialog.findViewById(R.id.scroll_container);
            scroll_cont.setVisibility(View.GONE);
            TextView dialog_title = dialog.findViewById(R.id.dialog_title);
            dialog_title.setVisibility(View.GONE);
            TextView dialog_footer = dialog.findViewById(R.id.dialog_footer);
            dialog_footer.setVisibility(View.VISIBLE);
            ViewPager2 viewPager = dialog.findViewById(R.id.viewPager);
            database_lost(() -> {
                ObjectAdapter adapter = new ObjectAdapter(objects);
                viewPager.setAdapter(adapter);
            });
            dialog.show();
            new Handler().postDelayed(() -> lost.setColorFilter(defaultColor), 50);
        });
    }

    private void clickedTicketIcon(){
        ticket.setOnClickListener(v-> {
            ticket.setColorFilter(changeColor);
            startActivity(new Intent(getContext(), ClaimingPasses.class));
            new Handler().postDelayed(() -> ticket.setColorFilter(defaultColor), 50);
        });
    }

    private void clickedHandIcon(){
        surrender.setOnClickListener(v-> {
            surrender.setColorFilter(changeColor);
            dialog = new Dialog(requireContext(),R.style.AlertDialogTheme);
            dialog.setContentView(R.layout.inf_dialog_scan);
            ScrollView scroll_cont = dialog.findViewById(R.id.scroll_container);
            scroll_cont.setVisibility(View.GONE);
            TextView dialog_footer = dialog.findViewById(R.id.dialog_footer);
            dialog_footer.setVisibility(View.VISIBLE);

            ViewPager2 viewPager = dialog.findViewById(R.id.viewPager);
            database_lost_where_surrendered(() -> {
                ObjectAdapter adapter = new ObjectAdapter(objects);
                viewPager.setAdapter(adapter);
            });

            dialog.show();
            new Handler().postDelayed(() -> surrender.setColorFilter(defaultColor), 50);
        });
    }

    private void database_found(Runnable onComplete){
        firestore.collection("User").document(userID)
                .collection("FoundObject")
                .get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot querySnapshot = task.getResult();
                    objects = new ArrayList<>();
                    for(QueryDocumentSnapshot document : querySnapshot){
                        ChipClass foundObject = document.toObject(ChipClass.class);
                        objects.add(foundObject);
                    } onComplete.run();
                });
    }

    private void database_lost(Runnable onComplete){
        firestore.collection("User").document(userID)
                .collection("LostObject")
                .get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot querySnapshot = task.getResult();
                    objects = new ArrayList<>();
                    for(QueryDocumentSnapshot document : querySnapshot){
                        ChipClass lostObject = document.toObject(ChipClass.class);
                        objects.add(lostObject);
                    } onComplete.run();
                });
    }


    private void database_lost_where_surrendered(Runnable onComplete){
        firestore.collection("User").document(userID)
                .collection("FoundObject")
                .whereEqualTo("status", "Surrendered")
                .get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot querySnapshot = task.getResult();
                    objects = new ArrayList<>();
                    for(QueryDocumentSnapshot document : querySnapshot){
                        ChipClass lostObject = document.toObject(ChipClass.class);
                        objects.add(lostObject);
                    }
                    if (objects.isEmpty()) {
                        Toast.makeText(getContext(), "Nothing to show", Toast.LENGTH_SHORT).show();
                    }
                    onComplete.run();
                });
    }

}

    class ObjectAdapter extends RecyclerView.Adapter<ObjectAdapter.ObjectViewHolder>{
    private final List<ChipClass> object;

    public ObjectAdapter(List<ChipClass> object) {
        this.object = object;
    }

    @NonNull
    @Override
    public ObjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inf_match_found, parent, false);
        return new ObjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObjectViewHolder holder, int position) {
        ChipClass objects = object.get(position);
        setAttributeVisibility(holder.categoryContainer, holder.getCategoryAsText, objects.getCategory());
        setAttributeVisibility(holder.typeContainer, holder.getTypeAsText, objects.getType());
        setAttributeVisibility(holder.colorContainer, holder.getColorAsText, objects.getColors());
        setAttributeVisibility(holder.locationContainer, holder.getLocationAsText, objects.getLocation());
        setAttributeVisibility(holder.brandContainer, holder.getBrandAsText, objects.getBrand());
        setAttributeVisibility(holder.modelContainer, holder.getModelAsText, objects.getModel());
        setAttributeVisibility(holder.textContainer, holder.getTextAsText, objects.getText());
        setAttributeVisibility(holder.sizeContainer, holder.getSizeAsText, objects.getSize());
        setAttributeVisibility(holder.shapeContainer, holder.getShapeAsText, objects.getShape());
        setAttributeVisibility(holder.dimensionContainer, holder.getWidthAsText, objects.getWidth());
        setAttributeVisibility(holder.dimensionContainer, holder.getLengthAsText, objects.getHeight());
        setAttributeVisibility(holder.remarksContainer, holder.getRemarksAsText, objects.getRemarks());
        String photoPath = objects.getImageUrl();
        if (photoPath != null && !photoPath.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(photoPath).into(holder.setImage);
        } else {
            holder.textView.setVisibility(View.VISIBLE);
        }

        HideDefaultButton(holder);
        InflateObjectTypeStatus(holder, objects);
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
        return object.size();
    }

    public static class ObjectViewHolder extends RecyclerView.ViewHolder{
        TextView getCategoryAsText, getTypeAsText, getColorAsText, getLocationAsText, getDateAsText, getTimeAsText,
                getBrandAsText, getModelAsText, getSizeAsText, getShapeAsText, getWidthAsText, getLengthAsText, getTextAsText, getRemarksAsText;
        LinearLayout brandContainer, modelContainer, sizeContainer, shapeContainer, dimensionContainer, textContainer, remarksContainer,
                categoryContainer, typeContainer, colorContainer, locationContainer, nc_btn, setClaimedLayout;
        ImageView setImage;
        TextView textView;
        Chip status_tag, type_chip;
        LinearLayout hide_def_btn, type_status, setClaimed, scan_layout_profile;
        MaterialButton setClaimedBtn;

        public ObjectViewHolder(@NonNull View itemView) {
            super(itemView);
            getCategoryAsText = itemView.findViewById(R.id.getCategoryAsText);
            getTypeAsText = itemView.findViewById(R.id.getTypeAsText);
            getColorAsText = itemView.findViewById(R.id.getColorAsText);
            getLocationAsText = itemView.findViewById(R.id.getLocationAsText);
            getDateAsText = itemView.findViewById(R.id.getDateAsText);
            getTimeAsText = itemView.findViewById(R.id.getTimeAsText);

            getBrandAsText = itemView.findViewById(R.id.getBrandAsText);
            getModelAsText = itemView.findViewById(R.id.getModelAsText);
            getSizeAsText = itemView.findViewById(R.id.getSizeAsText);
            getShapeAsText = itemView.findViewById(R.id.getShapeAsText);
            getWidthAsText = itemView.findViewById(R.id.getWidthAsText);
            getLengthAsText = itemView.findViewById(R.id.getHeightAsText);
            getTextAsText = itemView.findViewById(R.id.getTextAsText);
            getRemarksAsText = itemView.findViewById(R.id.getRemarksAsText);

            //image
            setImage = itemView.findViewById(R.id.imageView4);
            textView = itemView.findViewById(R.id.cvtext);
            //inflate set visibility
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

            hide_def_btn = itemView.findViewById(R.id.ncbtn_layout);
            type_status = itemView.findViewById(R.id.dialog_header);
            type_chip = itemView.findViewById(R.id.report_type);
            status_tag = itemView.findViewById(R.id.status_tag);
            setClaimed = itemView.findViewById(R.id.setClaimedLayout);
            scan_layout_profile = itemView.findViewById(R.id.scan_layout_profile);
            setClaimedBtn = itemView.findViewById(R.id.setClaimed);
        }
    }

    private void HideDefaultButton(ObjectViewHolder holder){
        holder.hide_def_btn.setVisibility(View.GONE);
    }
    private void InflateObjectTypeStatus(ObjectViewHolder holder, ChipClass object){
        holder.type_status.setVisibility(View.VISIBLE);
        setTextTypeStatus(holder, object);
    }
    private void setTextTypeStatus(ObjectViewHolder holder, ChipClass object){
        if (object.getReportType().equals("FoundObject") && object.getStatus().equals("Pending")){
            holder.status_tag.setText(object.getStatus());
            holder.status_tag.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#404244")));
            holder.status_tag.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.type_chip.setText("Found");
            holder.type_chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#1D4054")));
            holder.type_chip.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.scan_layout_profile.setVisibility(View.VISIBLE);

        }
        else if (object.getReportType().equals("FoundObject") && object.getStatus().equals("Match Found")){
            holder.status_tag.setText(object.getStatus());
            holder.status_tag.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#1FA745")));
            holder.status_tag.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.type_chip.setText("Found");
            holder.type_chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#1D4054")));
            holder.type_chip.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.setClaimed.setVisibility(View.VISIBLE);
            holder.setClaimedBtn.setText("NOT SURRENDERED");
        }
        else if (object.getReportType().equals("FoundObject") && object.getStatus().equals("Surrendered")){
            holder.status_tag.setText(object.getStatus());
            holder.status_tag.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#FBBC09")));
            holder.status_tag.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.type_chip.setText("Found");
            holder.type_chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#1D4054")));
            holder.type_chip.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.setClaimed.setVisibility(View.VISIBLE);
            holder.setClaimedBtn.setText("SURRENDERED");
        }
        else if (object.getReportType().equals("LostObject") && object.getStatus().equals("Pending")){
            holder.status_tag.setText(object.getStatus());
            holder.status_tag.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#404244")));
            holder.status_tag.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.type_chip.setText("Lost");
            holder.type_chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F7B94A")));
            holder.type_chip.setChipIcon(ResourcesCompat.getDrawable(holder.itemView.getContext().getResources(), R.drawable.icon_lost, holder.itemView.getContext().getTheme()));
            holder.type_chip.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.scan_layout_profile.setVisibility(View.VISIBLE);
        }
        else if (object.getReportType().equals("LostObject") && object.getStatus().equals("Match Found")){
            holder.status_tag.setText(object.getStatus());
            holder.status_tag.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#1FA745")));
            holder.status_tag.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.type_chip.setText("Lost");
            holder.type_chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F7B94A")));
            holder.type_chip.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.setClaimed.setVisibility(View.VISIBLE);
            holder.setClaimedBtn.setText("NOT CLAIMED");
        }
        else if (object.getReportType().equals("LostObject") && object.getStatus().equals("Claimed")){
            holder.status_tag.setText(object.getStatus());
            holder.status_tag.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#1FA745")));
            holder.status_tag.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.type_chip.setText("Lost");
            holder.type_chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#0071BB")));
            holder.type_chip.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.setClaimed.setVisibility(View.VISIBLE);
            holder.setClaimedBtn.setText("CLAIMED");
        }
    }

}
