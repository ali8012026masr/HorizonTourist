package com.example.horizontourist;

import android.app.Application;

import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.util.CartManager;
import com.example.horizontourist.util.SessionManager;

public class HorizonApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(this);
        CartManager.init(this);
        ApiClient.init();
    }
}
