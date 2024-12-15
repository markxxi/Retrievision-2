package com.example.retrievision;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FCM_NOTIF extends AppCompatActivity {
FirebaseMessaging firebaseMessaging;
Button fcmButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fcm_notif);
        FirebaseApp.initializeApp(this);
        firebaseMessaging = FirebaseMessaging.getInstance();
        InitButton();


    }
    private void InitButton(){
        fcmButton = findViewById(R.id.FCM_Button);
        fcmButton.setOnClickListener(v->{
            //GetToken();

        });
    }
    private void GetToken(){
        firebaseMessaging.getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()){
                            return;
                        }
                        String token = task.getResult();
                        Toast.makeText(getApplicationContext(), "FCM: " + token, Toast.LENGTH_SHORT).show();
                        System.err.println(token);
                        TokenFetcher tokenFetcher = new TokenFetcher();
                        NotificationSender sender = new NotificationSender(tokenFetcher);
                        sender.sendNotification(token, "title", "body");
                    }
                });

    }




}