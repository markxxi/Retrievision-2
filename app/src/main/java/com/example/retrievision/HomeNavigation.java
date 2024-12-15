package com.example.retrievision;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;
public class HomeNavigation extends AppCompatActivity {

    BottomNavigationView navbar;
    View underlineView;
LinearLayout btl;
    ConstraintLayout constraintLayout;
    HomeFragment home = new HomeFragment();
    LeaderboardFragment reward = new LeaderboardFragment();
    NotificationFragment notification = new NotificationFragment();
    ProfileFragment profile = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.home_navigation);

        initIDs();
        replaceFragmentOnNavBar();
        navbarColorState();
        UnderlineHomeInit(R.id.nav_home, 0);
        loadProfilePicture();
        networkDetection = new NetworkDetection();
    }

    private void initIDs(){
        navbar = findViewById(R.id.bottom_navigation);
        constraintLayout = findViewById(R.id.constraint_layout);
        btl = findViewById(R.id.bottom_navigation_layout);
        underlineView = findViewById(R.id.underline);
        user_profile = findViewById(R.id.userprofile);

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

        ColorStateList color = ColorStateList.valueOf(Color.parseColor("#143C6F"));
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

        if (itemId == R.id.nav_home) {
            moveUnderlineTo(0, false);
        } else if (itemId == R.id.nav_rank) {
            moveUnderlineTo(1, false);
        } else if (itemId == R.id.nav_notification) {
            moveUnderlineTo(2, false);
        } else if (itemId == R.id.userprofile) {
            moveUnderlineTo(-1, true);
        }
            }

private void UnderlineHomeInit(int item, int index){
    if (item == R.id.nav_home) {
        int totalWidth = navbar.getWidth() + (int) getResources().getDimension(com.intuit.sdp.R.dimen._23sdp);
        int itemCount = navbar.getMenu().size();
        int itemWidth = totalWidth / itemCount;
        int newTranslationX = itemWidth + (itemWidth / 2) - (underlineView.getWidth() / 2);
        underlineView.animate().translationX(newTranslationX).setDuration(300).start();
    }
}
    private void moveUnderlineTo(int index, boolean isProfile) {
        int totalWidth = navbar.getWidth() - (int) getResources().getDimension(com.intuit.sdp.R.dimen._20sdp);
        int itemCount = navbar.getMenu().size();

        if (isProfile) {
            int[] profileLocation = new int[2];
            user_profile.getLocationOnScreen(profileLocation);

            int profileImageCenterX = profileLocation[0] + (user_profile.getWidth() / 2);
            underlineView.animate().translationX(profileImageCenterX - ((float) underlineView.getWidth() / 2) - (int) getResources().getDimension(com.intuit.sdp.R.dimen._27sdp)).setDuration(300).start();
        } else {
            int itemWidth = totalWidth / itemCount;
            int newTranslationX = itemWidth * index + (itemWidth / 2) - (underlineView.getWidth() / 2);
            underlineView.animate().translationX(newTranslationX).setDuration(300).start();
        }
    }

    private void navbarColorState(){
        ColorStateList iconColors = ContextCompat.getColorStateList(this, R.color.change_color_state);
        navbar.setItemIconTintList(iconColors);
        user_profile.setBorderColor(Color.parseColor("#143C6F"));
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

    NetworkDetection networkDetection;
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkDetection, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkDetection);
    }
}