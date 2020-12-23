package com.essam.chatapp.retrofit;

import com.essam.chatapp.firebase.fcm.FirebaseCloudMessage;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiService {

    @POST("send")
    Call<ResponseBody> send(
            @HeaderMap Map<String,String> header,
            @Body FirebaseCloudMessage message
            );
}