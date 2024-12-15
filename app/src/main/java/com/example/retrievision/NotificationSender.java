package com.example.retrievision;

import android.app.Notification;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationSender {
    private final ExecutorService executor;
    private final TokenFetcher tokenFetcher;

    public NotificationSender(TokenFetcher tokenFetcher){
        this.tokenFetcher = tokenFetcher;
        this.executor = Executors.newSingleThreadExecutor();
    }
    public void sendNotification (String token, String title, String body){
    executor.submit(() -> {
        try {
            String oauthToken = tokenFetcher.getAccessToken();
            NotificationPayload payload = new NotificationPayload(token, title, body);
            Retrofit retrofit = new  Retrofit.Builder()
                    .baseUrl("https://fcm.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            FCMService fcmService = retrofit.create(FCMService.class);
            Call<ResponseBody> call = fcmService.sendNotification("Bearer " + oauthToken, payload);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()){
                        System.out.println("Notification sent successfully.");
                        storeUserNotification(payload);
                    }
                    else {
                        System.err.println("Failed to send notification. Response code: " + response.code());
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                System.err.println("Error response: " + errorBody);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    throwable.printStackTrace();
                    System.err.println("Failed to send notification.");
                }
            });

        } catch (Exception e){
            e.printStackTrace();
        }
});
    }
    public void shutdown() {
        // shutdown executor if no longer needed
        executor.shutdown();
    }
    private void storeUserNotification(NotificationPayload notificationPayload){
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("token", notificationPayload.getMessage().getToken());
        notificationData.put("title", notificationPayload.getMessage().getNotification().getTitle());
        notificationData.put("body", notificationPayload.getMessage().getNotification().getBody());

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        String uid = firebaseUser.getUid();
        FirebaseFirestore.getInstance()
                .collection("User")
                .document(uid)
                .collection("Notification")
                .add(notificationData);
    }
}
