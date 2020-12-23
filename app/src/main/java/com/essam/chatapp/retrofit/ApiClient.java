package com.essam.chatapp.retrofit;
import com.essam.chatapp.firebase.fcm.FirebaseCloudMessage;
import com.essam.chatapp.utils.Consts;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;

public class ApiClient {
    private ApiService mApiService;
    private static ApiClient INSTANCE;

    public ApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Consts.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mApiService = retrofit.create(ApiService.class);
    }

    public static ApiClient getINSTANCE() {
        if (INSTANCE == null){
            INSTANCE = new ApiClient();
        }
        return INSTANCE;
    }

    public Call<ResponseBody> send (Map<String,String> header, FirebaseCloudMessage message){
        return mApiService.send(header, message );
    }
}
