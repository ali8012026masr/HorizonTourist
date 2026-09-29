package com.example.horizontourist.data.api;

import androidx.annotation.NonNull;

import com.example.horizontourist.util.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String path = original.url().encodedPath();

        Request.Builder builder = original.newBuilder();

        if (!isPublicAuthPath(path)) {
            try {
                SessionManager sessionManager = SessionManager.getInstance();
                String accessToken = sessionManager.getAccessToken();
                String refreshToken = sessionManager.getRefreshToken();

                if (accessToken != null && !accessToken.trim().isEmpty()) {
                    builder.header("Authorization", "Bearer " + accessToken);
                }
                if (refreshToken != null && !refreshToken.trim().isEmpty()) {
                    builder.header("x-refresh-token", refreshToken);
                }
            } catch (IllegalStateException ignored) {
                // SessionManager may not be initialized yet in test environments
            }
        }

        Response response = chain.proceed(builder.build());

        // Check for refreshed access token in response header
        String refreshedToken = response.header("x-access-token");
        if (refreshedToken != null && !refreshedToken.trim().isEmpty()) {
            try {
                SessionManager.getInstance().updateAccessToken(refreshedToken.trim());
            } catch (IllegalStateException ignored) {
            }
        }

        // Handle 401 on non-auth path
        if (response.code() == 401 && !isPublicAuthPath(path)) {
            try {
                SessionManager.getInstance().clear();
            } catch (IllegalStateException ignored) {
            }
            ApiClient.notifySessionExpired();
        }

        return response;
    }

    private boolean isPublicAuthPath(String path) {
        if (path == null) {
            return false;
        }
        return path.contains("/auth/login")
                || path.contains("/auth/register/tourist")
                || path.contains("/auth/forgot-password")
                || path.contains("/auth/reset-password");
    }
}
