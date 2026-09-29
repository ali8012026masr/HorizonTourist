package com.example.horizontourist.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.horizontourist.data.model.LoginResponse;

public class SessionManager {
    private static final String PREF_NAME = "horizon_tourist_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOURIST_ID = "tourist_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager must be initialized in HorizonApp.onCreate()");
        }
        return instance;
    }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        long id = getTouristId();
        return token != null && !token.trim().isEmpty() && id > 0;
    }

    public void save(LoginResponse response) {
        if (response == null) {
            return;
        }
        Long touristId = response.getTouristId();
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, response.accessToken)
                .putString(KEY_REFRESH_TOKEN, response.refreshToken)
                .putLong(KEY_TOURIST_ID, touristId != null ? touristId : -1L)
                .putString(KEY_FULL_NAME, response.fullName)
                .putString(KEY_EMAIL, response.email)
                .apply();
    }

    public void updateAccessToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
        }
    }

    public void updateProfile(String fullName) {
        if (fullName != null) {
            prefs.edit().putString(KEY_FULL_NAME, fullName).apply();
        }
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public long getTouristId() {
        return prefs.getLong(KEY_TOURIST_ID, -1L);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
