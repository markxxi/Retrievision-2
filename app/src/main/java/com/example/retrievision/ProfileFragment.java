package com.example.retrievision;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();

        InitIDs();
        ProfileInfo();
        InitRecent();
        clickedFoundIcon();
        clickedLostIcon();
        clickedTicketIcon();
        clickedHandIcon();
        clickSettingsIcon();

        return view;
    }
    ImageView found, lost, ticket, surrender, settings;
    CircleImageView profilePic;
    TextView displayName, displayEmail;

    private void InitIDs(){
        found = view.findViewById(R.id.found_objects);
        lost = view.findViewById(R.id.lost_objects);
        ticket = view.findViewById(R.id.ticket);
        surrender = view.findViewById(R.id.surrendered);
        settings = view.findViewById(R.id.settings);

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
    int defaultColor = Color.parseColor("#143C6F");
    ProgressBar progressBar;

    @SuppressLint("SetTextI18n")
    private void clickedFoundIcon(){

        found.setOnClickListener(v-> {
            found.setColorFilter(changeColor);
            dialog = new Dialog(requireContext(),R.style.AlertDialogTheme);

            dialog.setContentView(R.layout.inf_dialog_scan);
            ScrollView scroll_cont = dialog.findViewById(R.id.scroll_container);
            scroll_cont.setVisibility(View.GONE);
            dialog_title = dialog.findViewById(R.id.dialog_title);
            dialog_title.setText("Found Objects");
            dialog_footer = dialog.findViewById(R.id.dialog_footer);
            dialog_footer.setVisibility(View.VISIBLE);

            progressBar = dialog.findViewById(R.id.progressBar);
            showProgressBar();
            ViewPager2 viewPager = dialog.findViewById(R.id.viewPager);
            database_found(() -> {
                ObjectAdapter adapter = new ObjectAdapter(objects, dialog);
                sort(objects);
                viewPager.setAdapter(adapter);
                hideProgressBar();
            });
            dialog.show();
            new Handler().postDelayed(() -> found.setColorFilter(defaultColor), 50);
        });
    }

    private void sort(List<ChipClass> objects){
        Comparator<ChipClass> comparator = (object1, object2)->{
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            LocalDateTime d1 = LocalDateTime.parse(object1.getDateReported(), dateTimeFormatter);
            LocalDateTime d2 = LocalDateTime.parse(object2.getDateReported(), dateTimeFormatter);

            return d2.compareTo(d1);
        };

        Collections.sort(objects, comparator);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
    TextView dialog_footer, dialog_title;


    @SuppressLint("SetTextI18n")
    private void clickedLostIcon(){
        lost.setOnClickListener(v-> {
            lost.setColorFilter(changeColor);
            dialog = new Dialog(requireContext(),R.style.AlertDialogTheme);
            dialog.setContentView(R.layout.inf_dialog_scan);
            ScrollView scroll_cont = dialog.findViewById(R.id.scroll_container);
            scroll_cont.setVisibility(View.GONE);
            dialog_title = dialog.findViewById(R.id.dialog_title);
            dialog_title.setText("Lost Objects");
            dialog_footer = dialog.findViewById(R.id.dialog_footer);
            dialog_footer.setVisibility(View.VISIBLE);
            progressBar = dialog.findViewById(R.id.progressBar);
            showProgressBar();

            ViewPager2 viewPager = dialog.findViewById(R.id.viewPager);
            database_lost(() -> {
                ObjectAdapter adapter = new ObjectAdapter(objects, dialog);
                viewPager.setAdapter(adapter);
                if (objects.isEmpty()) {
                    dialog_footer.setText("No Reported Objects");
                }
                hideProgressBar();
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

    @SuppressLint("SetTextI18n")
    private void clickedHandIcon(){
        surrender.setOnClickListener(v-> {
            surrender.setColorFilter(changeColor);
            dialog = new Dialog(requireContext(),R.style.AlertDialogTheme);
            dialog.setContentView(R.layout.inf_dialog_scan);
            ScrollView scroll_cont = dialog.findViewById(R.id.scroll_container);
            scroll_cont.setVisibility(View.GONE);
            dialog_title = dialog.findViewById(R.id.dialog_title);
            dialog_title.setText("Surrendered Objects");
            dialog_footer = dialog.findViewById(R.id.dialog_footer);
            dialog_footer.setVisibility(View.VISIBLE);
            progressBar = dialog.findViewById(R.id.progressBar);
            showProgressBar();
            ViewPager2 viewPager = dialog.findViewById(R.id.viewPager);
            database_lost_where_surrendered(() -> {
                ObjectAdapter adapter = new ObjectAdapter(objects, dialog);
                viewPager.setAdapter(adapter);
                if (objects.isEmpty()) {
                    dialog_footer.setText("No Surrendered Objects");
                    Toast.makeText(getContext(), "Nothing to show", Toast.LENGTH_SHORT).show();
                }
                hideProgressBar();
            });

            dialog.show();
            new Handler().postDelayed(() -> surrender.setColorFilter(defaultColor), 50);
        });
    }

    private void clickSettingsIcon() {

        SettingsFragment settingsFragment = new SettingsFragment();
        settings.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, settingsFragment).commit();
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
                    }  if (objects.isEmpty()) {
                    }
                    onComplete.run();
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
                    }

                    onComplete.run();
                });
    }

    private void database_lost_where_surrendered(Runnable onComplete){
        firestore.collection("User").document(userID)
                .collection("FoundObject")
                .whereIn("status", Arrays.asList("Surrendered", "Matched and Surrendered", "Retrieved"))
                .get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot querySnapshot = task.getResult();
                    objects = new ArrayList<>();
                    for(QueryDocumentSnapshot document : querySnapshot){
                        ChipClass lostObject = document.toObject(ChipClass.class);
                        objects.add(lostObject);
                    }

                    onComplete.run();
                });
    }

    // RECENT OBJECT VIEW
    ImageView imageForFoundObject;
    TextView cv_text, recent_type, recent_date, recent_time, recent_location, recent_text, no_recent;
    Chip recent_status, recent_chip_report, recent_chip_report_lost;
    ProgressBar recent_progress_bar;

    CardView cv;
    private void InitRecent(){
        imageForFoundObject = view.findViewById(R.id.imageView4);
        cv_text = view.findViewById(R.id.cvtext);
        recent_type = view.findViewById(R.id.recent_type);
        recent_date = view.findViewById(R.id.recent_date);
        recent_time = view.findViewById(R.id.recent_time);
        recent_location = view.findViewById(R.id.recent_location);
        recent_status = view.findViewById(R.id.recent_status);
        recent_chip_report = view.findViewById(R.id.recent_chip_report);
        recent_chip_report_lost = view.findViewById(R.id.recent_chip_report_lost);
        recent_progress_bar = view.findViewById(R.id.recent_progressbar);
        no_recent = view.findViewById(R.id.no_recent);
        cv = view.findViewById(R.id.cv);
        recent_text = view.findViewById(R.id.recent_text);
        showRecent();
    }

    @SuppressLint("SetTextI18n")
    private void displayRecent(ChipClass recentObject){
        recent_text.setVisibility(View.VISIBLE);

        recent_type.setText("Type: ".concat(recentObject.getType().get(0)));
        recent_date.setText("Date: ".concat(recentObject.getDate()));
        recent_time.setText("Time: ".concat(recentObject.getTime()));
        recent_location.setText("Location: ".concat(recentObject.getLocation().get(0)));
        String photoPath = recentObject.getImageUrl();
        if (getActivity() != null) {
            if (photoPath != null && !photoPath.isEmpty()) {
                cv.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(photoPath).into(imageForFoundObject);
            } else {
                cv.setVisibility(View.VISIBLE);
                cv_text.setVisibility(View.VISIBLE);
            }
        }
        if(recentObject.getReportType().equals("FoundObject")){
            recent_chip_report.setVisibility(View.VISIBLE);
            recent_chip_report.setText("Found");
        } else {
            recent_chip_report_lost.setVisibility(View.VISIBLE);
            recent_chip_report_lost.setText("Lost");
        }
        statusState(recentObject);
    }
    //status state for recent object
    private void statusState(ChipClass recentObject){
        recent_status.setVisibility(View.VISIBLE);
        String status = recentObject.getStatus();

                switch (status) {
                    case "Pending": //kapag kakareport pa lang ng object
                        recent_status.setText(recentObject.getStatus());
                        recent_status.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#404244")));
                        break;
                    case "Matched and Surrendered": //kapag surrendered na pero wala pang nagsscan for matching
                        recent_status.setText(recentObject.getStatus());
                        recent_status.setChipIcon(ContextCompat.getDrawable(dialog.getContext(), R.drawable.match_and_surrendered));
                        break;
                    case "Match Found": //kapag hindi pa nasusurrender pero may nakapagscan na
                        recent_status.setText(recentObject.getStatus());
                        recent_status.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#1FA745")));
                        break;
                    case "Surrendered": //kapag ibinigay agad ni finder sa SA yung bagay na wala pang match
                        recent_status.setText(recentObject.getStatus());
                        recent_status.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#FBBC09")));
                        break;
                    case "Retrieved": //[lost object event] kapag nakuha na ni Owner yung bagay kay admin; marked by admin
                        recent_status.setText(recentObject.getStatus());
                        recent_status.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#0071BB")));
                        break;

                }
    }

    private void showRecentProgressBar(){
        recent_progress_bar.setVisibility(View.VISIBLE);
    }
    private void hideRecentProgressBar(){
        recent_progress_bar.setVisibility(View.GONE);
    }

    private void showRecent(){
        showRecentProgressBar();
        List<ChipClass> AllReports = new ArrayList<>();
        database_found(()->{
            AllReports.addAll(objects);
            database_lost(()->{
                AllReports.addAll(objects);
                database_lost_where_surrendered(()->{
                    AllReports.addAll(objects);
                    if(!AllReports.isEmpty()){
                        sort(AllReports);
                        ChipClass recentObject = AllReports.get(0);
                        displayRecent(recentObject);
                        hideRecentProgressBar();
                    } else {
                        hideRecentProgressBar();
                        no_recent.setVisibility(View.VISIBLE);
                    }
                });
            });
        });
    }

}

class ObjectAdapter extends RecyclerView.Adapter<ObjectAdapter.ObjectViewHolder>{
    private final List<ChipClass> object;
    private final Dialog dialog;

    public ObjectAdapter(List<ChipClass> object, Dialog dialog) {
        this.object = object;
        this.dialog = dialog;
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

        holder.getDateAsText.setText(objects.getDateReported());
        String photoPath = objects.getImageUrl();
        if (photoPath != null && !photoPath.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(photoPath).into(holder.setImage);
        } else {
            holder.textView.setVisibility(View.VISIBLE);

        }

        HideDefaultButton(holder);
        InflateObjectTypeStatus(holder, objects);
        CloseDialog(holder);
        ScanButtonProfile(holder);

        String reportID = objects.getReportId();
        deleteReport(holder, reportID, dialog);
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
                categoryContainer, typeContainer, colorContainer, locationContainer;
        ImageView setImage, close_dialog;
        TextView textView;
        Chip status_tag, type_chip;
        LinearLayout hide_def_btn, type_status, setClaimed, scan_layout_profile;
        MaterialButton setClaimedBtn, scanNowButton, deleteReportButton;

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
            scanNowButton = itemView.findViewById(R.id.scan_button_profile);
            setClaimedBtn = itemView.findViewById(R.id.setClaimed);
            deleteReportButton = itemView.findViewById(R.id.deleteReportButton);

            close_dialog = itemView.findViewById(R.id.close_dialog);
        }
    }
    private void HideDefaultButton(ObjectViewHolder holder){
        holder.hide_def_btn.setVisibility(View.GONE);
    }
    private void InflateObjectTypeStatus(ObjectViewHolder holder, ChipClass object){
        holder.type_status.setVisibility(View.VISIBLE);
        setTextTypeStatus(holder, object);
    }
    private  void CloseDialog(ObjectViewHolder holder){
        holder.close_dialog.setOnClickListener(v-> dialog.dismiss() );
    }
    private void ScanButtonProfile(ObjectViewHolder holder){
        holder.scanNowButton.setOnClickListener(v-> {
            dialog.dismiss();
                Intent intent = new Intent(dialog.getContext(), HomeNavigation.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                dialog.getContext().startActivity(intent);
        });
    }

    private void setTextTypeStatus(ObjectViewHolder holder, ChipClass object) {
        String reportType = object.getReportType();
        String status = object.getStatus();
        //status state for adapter
        switch (reportType) {
            case "FoundObject":
                switch (status) {
                    case "Pending":
                        setChipDetails(holder, object, "Found", "#1D4054", "#404244", "Pending");
                        holder.scan_layout_profile.setVisibility(View.VISIBLE);
                        holder.deleteReportButton.setVisibility(View.GONE);
                        break;
                    case "Matched and Surrendered":
                        status(object, holder, "Found", null, "#404244", "NOT YET CLAIMED BY OWNER");
                        holder.status_tag.setChipIcon(ContextCompat.getDrawable(dialog.getContext(), R.drawable.match_and_surrendered));
                        break;
                    case "Match Found":
                        status(object, holder, "Found", "#1FA745", "#404244", "NOT SURRENDERED");
                        break;
                    case "Surrendered":
                        status(object, holder, "Found", "#FBBC09", "#FBBC09", "SURRENDERED");
                        break;
                    case "Retrieved":
                        status(object, holder, "Found", "#0071BB", "#0071BB", "RETRIEVED SUCCESSFULLY BY THE OWNER");
                        break;
                }
                break;

            case "LostObject":
                switch (status) {
                    case "Pending":
                        setChipDetails(holder, object, "Lost", "#F7B94A", "#404244", "Pending");
                        holder.type_chip.setChipIcon(ResourcesCompat.getDrawable(holder.itemView.getContext().getResources(), R.drawable.icon_lost, holder.itemView.getContext().getTheme()));
                        holder.scan_layout_profile.setVisibility(View.VISIBLE);
                        break;
                    case "Match Found":
                        status(object, holder, "Lost", "#1FA745", "#F7B94A", "NOT CLAIMED");
                        break;
                    case "Retrieved":
                        status(object, holder, "Lost", "#0071BB", "#F7B94A", "CLAIMED");
                        break;
                }
                break;
        }
    }

        private void setChipDetails(ObjectViewHolder holder, ChipClass object, String objectType, String bgColor, String iconColor, String status) {
            holder.status_tag.setText(status);
            holder.status_tag.setChipIconTint(ColorStateList.valueOf(Color.parseColor(iconColor)));
            holder.status_tag.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.type_chip.setText(objectType);
            holder.type_chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(bgColor)));
            holder.type_chip.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
        }

        private void status(ChipClass object, ObjectViewHolder holder, String objectType, String colorIcon, String colorBG, String status) {
            holder.status_tag.setText(object.getStatus());
            if (colorIcon != null && !colorIcon.isEmpty()) {
                holder.status_tag.setChipIconTint(ColorStateList.valueOf(Color.parseColor(colorIcon)));
            }
            holder.status_tag.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.type_chip.setText(objectType);
            if(holder.type_chip.equals("LostObject")){
                holder.type_chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FBBC09")));
            } else {
                holder.type_chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#1D4054")));

            }
            holder.type_chip.setTypeface(ResourcesCompat.getFont(holder.itemView.getContext(), R.font.poppinsmedium));
            holder.setClaimed.setVisibility(View.VISIBLE);
            holder.setClaimedBtn.setText(status);
            holder.setClaimedBtn.setBackgroundColor(Color.parseColor(colorBG));
        }

        private void deleteReport(ObjectViewHolder holder, String reportID, Dialog ObjectDialog){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String UID = currentUser.getUid();
            DocumentReference reportToDelete = db.collection("User").document(UID).collection("LostObject").document(reportID);
            DocumentReference deleteInFoundCollection = db.collection("LostObject").document(reportID);

            holder.deleteReportButton.setOnClickListener(v -> {
                AlertDialog dialog = new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Delete Report")
                        .setMessage("Are you sure you want to delete this report?")
                        .setPositiveButton("Yes", (dialog1, which) -> {
                            reportToDelete.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        deleteInFoundCollection.delete()
                                                        .addOnSuccessListener(aVoid1->{
                                                            Toast.makeText(holder.itemView.getContext(), "Report deleted.", Toast.LENGTH_SHORT).show();
                                                            ObjectDialog.dismiss();
                                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(holder.itemView.getContext(), "Failed to delete report.", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("No", null)
                        .show();
                new Handler().post(() -> {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF4081"));
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#1FA745"));
                });
            });

        }
    }
