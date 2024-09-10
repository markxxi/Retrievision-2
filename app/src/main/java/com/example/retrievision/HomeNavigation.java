package com.example.retrievision;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;
public class HomeNavigation extends AppCompatActivity {

    BottomNavigationView navbar;
    View underlineView;

    ConstraintLayout constraintLayout, constraintLayout2;
    HomeFragment home = new HomeFragment();
    LeaderboardFragment reward = new LeaderboardFragment();
    NotificationFragment notification = new NotificationFragment();
    ProfileFragment profile = new ProfileFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_navigation);

        initIDs();
        replaceFragmentOnNavBar();
        navbarColorState();

        loadProfilePicture();
    }

    private void initIDs(){
        navbar = findViewById(R.id.bottom_navigation);
        constraintLayout = findViewById(R.id.constraint_layout);
        constraintLayout2 = findViewById(R.id.framelayout);
        underlineView = findViewById(R.id.underline);
        user_profile = findViewById(R.id.userprofile);
      //  initials = findViewById(R.id.initials);

    }

    private void replaceFragmentOnNavBar(){
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, home).commit();

        Fade enter = new Fade();
        enter.setDuration(600);

        navbar.setOnItemSelectedListener(item -> {

            underlineSelectedItem(item.getItemId());

            if (item.getItemId() == R.id.nav_home) {
                TransitionManager.beginDelayedTransition(constraintLayout, new Fade());
                constraintLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                home.setEnterTransition(enter);
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, home).commit();
                navbarColorState();
                return true;
            } else if (item.getItemId() == R.id.nav_rank) {
                TransitionManager.beginDelayedTransition(constraintLayout, new Fade());
                constraintLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, reward).commit();
                navbarColorState();
                return true;
            } else if (item.getItemId() == R.id.nav_notification) {
                TransitionManager.beginDelayedTransition(constraintLayout, new Fade());
                constraintLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, notification).commit();
                navbarColorState();
                return true;
            }
            return false;
        });

        ColorStateList color = ColorStateList.valueOf(Color.parseColor("#F1F5F9"));
        underlineSelectedItem(navbar.getMenu().getItem(0).getItemId());
        user_profile.setOnClickListener(v->{
            underlineSelectedItem(R.id.userprofile);
            TransitionManager.beginDelayedTransition(constraintLayout, new Fade());
            constraintLayout.setBackgroundColor(Color.parseColor("#242b45"));
            profile.setEnterTransition(enter);
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, profile).commit();
            user_profile.setBorderColor(Color.parseColor("#FBBC09"));
            new Handler().postDelayed(()->navbar.setItemIconTintList(color),300);
        });

    }

    public void underlineSelectedItem(int itemId) {
        TransitionManager.beginDelayedTransition(constraintLayout2, new ChangeBounds());
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout2);
        if (itemId == R.id.nav_home) {
            constraintSet.setHorizontalBias(R.id.underline, 0.06f);
        } else if (itemId == R.id.nav_rank) {
            constraintSet.setHorizontalBias(R.id.underline, 0.50f);
        } else if (itemId == R.id.nav_notification) {
            constraintSet.setHorizontalBias(R.id.underline, 0.94f);
        } else if (itemId == R.id.userprofile) {
            constraintSet.setHorizontalBias(R.id.underline, 1.432f);
        }
        constraintSet.applyTo(constraintLayout2);
    }

    private void navbarColorState(){
        ColorStateList iconColors = ContextCompat.getColorStateList(this, R.color.change_color_state);
        navbar.setItemIconTintList(iconColors);
        user_profile.setBorderColor(Color.parseColor("#143C6F"));
//        TransitionManager.beginDelayedTransition(constraintLayout, new Fade());
//        constraintLayout.setBackgroundColor(Color.parseColor("#242b45"));
    }

    CircleImageView user_profile;

    private void loadProfilePicture() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String initial = currentUser.getDisplayName();
        if (currentUser != null) {

            StorageReference profilePicRef = FirebaseStorage.getInstance().getReference()
                    .child("profilePictures")
                    .child(currentUser.getUid() + ".jpg");
            profilePicRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    if(profilePicRef != null){
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        user_profile.setImageBitmap(bitmap);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }



}