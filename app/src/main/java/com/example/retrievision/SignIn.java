package com.example.retrievision;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthCredential;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SignIn extends AppCompatActivity {

    ImageView appName;
    AppCompatButton signin;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signin);

        initIDs();
        logInPass();
        signInButton();
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
        showProgressBar(); // Show the progress bar

        OAuthProvider.Builder provider = OAuthProvider.newBuilder("microsoft.com");
        provider.addCustomParameter("prompt","consent");

        firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    fetchProfilePicture(authResult, user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Sign In Failed: "+e.getMessage());
                    hideProgressBar(); // Hide the progress bar on failure
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
            // Skip profile picture saving and just save the user data
            saveUser(firebaseUser, null);
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profilePictureRef = storageRef.child("profilePictures/" + firebaseUser.getUid() + ".jpg");

        profilePictureRef.putBytes(imageData)
                .addOnSuccessListener(taskSnapshot -> profilePictureRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            saveUser(firebaseUser, uri.toString());
                            hideProgressBar(); // Hide the progress bar after success
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error getting download URL: " + e.getMessage());
                            saveUser(firebaseUser, null);
                            hideProgressBar(); // Hide the progress bar after failure
                        }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading profile picture: " + e.getMessage());
                    saveUser(firebaseUser, null);
                    hideProgressBar(); // Hide the progress bar after failure
                });
    }

    private void saveUser(FirebaseUser firebaseUser, String profilePictureUrl) {
        User newUser = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), profilePictureUrl, firebaseUser.getEmail());

        db.collection("User")
                .document(firebaseUser.getUid())
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully written");
                    startActivity(new Intent(this, HomeNavigation.class));
                    hideProgressBar();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error writing: " + e.getMessage());
                    hideProgressBar();
                });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
