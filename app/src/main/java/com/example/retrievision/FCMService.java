package com.example.retrievision;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FCMService {
    @Headers("Content-Type: application/json")
    @POST("v1/projects/retrievision/messages:send")
    Call<ResponseBody> sendNotification(
            @Header("Authorization") String authHeader,
            @Body NotificationPayload payload
    );
}
