package com.example.horizontourist.ui;

import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.ui.auth.LoginActivity;
import com.example.horizontourist.util.SessionManager;

public abstract class BaseActivity extends AppCompatActivity {

    private long lastClickTime = 0;
    private static final long CLICK_THRESHOLD_MS = 600;

    private final ApiClient.SessionExpiredListener sessionExpiredListener = this::handleSessionExpired;

    @Override
    protected void onStart() {
        super.onStart();
        if (!(this instanceof LoginActivity)) {
            ApiClient.setSessionExpiredListener(sessionExpiredListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!(this instanceof LoginActivity)) {
            ApiClient.clearSessionExpiredListener();
        }
    }

    protected void handleSessionExpired() {
        if (!isSafe()) {
            return;
        }
        Toast.makeText(this, R.string.error_session_expired, Toast.LENGTH_LONG).show();
        logout();
    }

    public void logout() {
        SessionManager.getInstance().clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected boolean isSafe() {
        return !isFinishing() && !isDestroyed();
    }

    protected boolean isFastDoubleClick() {
        long current = SystemClock.elapsedRealtime();
        if (current - lastClickTime < CLICK_THRESHOLD_MS) {
            return true;
        }
        lastClickTime = current;
        return false;
    }

    public void toast(String message) {
        if (isSafe() && message != null && !message.trim().isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void toast(@androidx.annotation.StringRes int resId) {
        if (isSafe()) {
            Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        }
    }
}
