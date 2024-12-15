package com.example.retrievision;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthCredential;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SignIn extends AppCompatActivity {

    ImageView appName;
    AppCompatButton signin;
    ProgressBar progressBar;
    private NetworkDetection networkReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.signin);

        initIDs();

        logInPass();
        signInButton();
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        requestPermissions();
        networkReceiver = new NetworkDetection();

    }

    private void initIDs(){
        appName = findViewById(R.id.app_name);
        signin = findViewById(R.id.signin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void logInPass(){
        appName.setOnClickListener(v->startActivity(new Intent(this, HomeNavigation.class)));

    }

    private void signInButton(){
        signin.setOnClickListener(v->signInWithProvider());
    }

    private static final String TAG ="SignInActivity";
    private FirebaseAuth firebaseAuth;
    
    private FirebaseFirestore db;

    private void signInWithProvider(){
        showProgressBar();

        OAuthProvider.Builder provider = OAuthProvider.newBuilder("microsoft.com");
        provider.addCustomParameter("prompt","consent");

        firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    fetchProfilePicture(authResult, user);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Sign In Failed: "+e.getMessage());
                    hideProgressBar();
                });
    }

    private void fetchProfilePicture(AuthResult authResult, FirebaseUser firebaseUser){
        OAuthCredential credential = (OAuthCredential)authResult.getCredential();
        String accessToken = credential.getAccessToken();
        String graphApiUrl = "https://graph.microsoft.com/v1.0/me/photo/$value";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(graphApiUrl)
                .header("Authorization","Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error getting profile picture:" + e.getMessage());
                saveUser(firebaseUser, null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error fetching profile picture: " + response.code());
                    saveUser(firebaseUser, null);
                    return;
                }
                String contentType = response.header("Content-Type");
                if (contentType != null && contentType.startsWith("image")) {
                    saveProfilePicture(firebaseUser, response.body().bytes());
                } else {
                    Log.e(TAG, "Error: Response is not an image");
                    saveUser(firebaseUser, null);
                }
            }
        });
    }

    private void saveProfilePicture(FirebaseUser firebaseUser, byte[] imageData) {
        if (imageData == null) {
            saveUser(firebaseUser, null);
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profilePictureRef = storageRef.child("profilePictures/" + firebaseUser.getUid() + ".jpg");

        profilePictureRef.putBytes(imageData)
                .addOnSuccessListener(taskSnapshot -> profilePictureRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            saveUser(firebaseUser, uri.toString());
                            hideProgressBar();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error getting download URL: " + e.getMessage());
                            saveUser(firebaseUser, null);
                            hideProgressBar();
                        }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading profile picture: " + e.getMessage());
                    saveUser(firebaseUser, null);
                    hideProgressBar();
                });
    }

    private void saveUser(FirebaseUser firebaseUser, String profilePictureUrl) {
        String email = firebaseUser.getEmail();

        // check if the email domain is "@calamba.sti.edu.ph"
        if (email != null && email.endsWith("@calamba.sti.edu.ph")) {
            User newUser = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), profilePictureUrl, email);

            db.collection("User")
                    .document(firebaseUser.getUid())
                    .set(newUser)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Successfully written");
                        subscribeToLost();
                        subscribeToFound();
                        storeUserToken();
                        startActivity(new Intent(this, HomeNavigation.class));
                        hideProgressBar();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error writing: " + e.getMessage());
                        hideProgressBar();
                    });
        } else {
            //email domain does not match, don't proceed
            Toast.makeText(this, "You are not authenticated. Please use a valid STI Calamba email address.", Toast.LENGTH_LONG).show();
            hideProgressBar();
        }
    }


    private void subscribeToLost(){
        FirebaseMessaging.getInstance().subscribeToTopic("new_lost_object")
                .addOnCompleteListener(task-> {
                    String msg = task.isSuccessful() ? "Subscribed" : "Subscription failed";
                    Log.d("FCM", msg);
                });
    }
    private void subscribeToFound(){
        FirebaseMessaging.getInstance().subscribeToTopic("new_found_object")
                .addOnCompleteListener(task-> {
                    String msg = task.isSuccessful() ? "Subscribed" : "Subscription failed";
                    Log.d("FCM", msg);
                });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 101;
    private static final int REQUEST_CODE_CAMERA = 102;
    private static final int REQUEST_CODE_NOTIFICATION = 103;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 104;

    private void requestPermissions() {
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION);
            } else {
                requestCameraPermission();
            }
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA);
        } else {
            requestImagePermission();
        }
    }

    private void requestImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_READ_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CODE_NOTIFICATION:
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                    requestCameraPermission();
                    break;

                case REQUEST_CODE_CAMERA:
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                    requestImagePermission();
                    break;

                case REQUEST_CODE_READ_MEDIA_IMAGES:
                case REQUEST_CODE_READ_EXTERNAL_STORAGE:
                    Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {

            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void storeUserToken() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            firebaseMessaging.getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult();
                            firestore.collection("User").document(userId)
                                    .update("fcmToken", token)
                                    .addOnSuccessListener(aVoid -> {Log.d("FCM", "Token successfully stored" + token);
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        DocumentReference docRef = db.collection("User").document(userId);
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("fcmToken", token);
                                        docRef.update(updates)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Log.d("Firestore", "Field added/updated!");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w("Firestore", "Error adding field", e);
                                                });

                                    })
                                    .addOnFailureListener(e -> Log.e("FCM", "Error storing token: " + e.getMessage()));
                        } else {
                            Log.e("FCM", "Failed to retrieve token");
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }
}
