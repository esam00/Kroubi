package com.essam.chatapp.network;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.essam.chatapp.chat.model.Message;
import com.essam.chatapp.chat.activity.ChatActivity;
import com.google.gson.Gson;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SocketListener extends WebSocketListener {
    private ChatActivity activity ;

    public SocketListener(ChatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Connection established!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMessage(WebSocket webSocket, final String receivedMessage) {
        super.onMessage(webSocket, receivedMessage);

        Log.i("tag","received message"+receivedMessage);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                Message message = gson.fromJson(receivedMessage,Message.class);
                activity.updateUi(message);

            }
        });

    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @NonNull Response response) {
        super.onFailure(webSocket, t, response);
    }
}
