package com.essam.chatapp.firebase.fcm;

import android.util.Log;

import androidx.annotation.NonNull;

import com.essam.chatapp.firebase.data.FirebaseManager;
import com.essam.chatapp.retrofit.ApiClient;
import com.essam.chatapp.utils.Consts;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FcmUtils {
    private static final String TAG = "FcmUtils";

    /**
     * This method gets the firebase FCM token and then uses FirebaseManager to store the
     * token to the firebase database
     */
    public static void initFcm() {
        Task<String> token = FirebaseMessaging.getInstance().getToken();
        token.addOnSuccessListener(s -> FirebaseManager.getInstance().updateFirebaseToken(s));
    }

    public static void pushNewMessageNotification(FirebaseCloudMessage message) {
        // get server key
        FirebaseManager.getInstance().getServerKey(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // here starts the actual work
                    String serverKey = snapshot.child(Consts.SERVER_KEY).getValue().toString();

                    // headers
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("content-type", "application/json");
                    headers.put("authorization", "key=" + serverKey);

                    // retrofit call
                    Call<ResponseBody> call = ApiClient.getINSTANCE().send(headers, message);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                            Log.i(TAG, "onResponse: server response" + response.toString());
                        }

                        @Override
                        public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                            Log.i(TAG, "onFailure: server response" + t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
