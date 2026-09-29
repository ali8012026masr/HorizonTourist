package com.example.horizontourist.data.api;

import androidx.annotation.NonNull;

import com.example.horizontourist.data.model.ErrorResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ApiCallback<T> implements Callback<T> {

    public abstract void onSuccess(T response);

    public abstract void onError(String message, int code);

    public void onError(String message, int code, ErrorResponse errorResponse) {
        onError(message, code);
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (response.isSuccessful()) {
            onSuccess(response.body());
        } else {
            int code = response.code();
            String message = null;
            ErrorResponse errorObj = null;

            if (code == 423) {
                message = "Account locked";
            }

            ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                try {
                    String json = errorBody.string();
                    if (json != null && !json.trim().isEmpty()) {
                        errorObj = new Gson().fromJson(json, ErrorResponse.class);
                        if (message == null && errorObj != null && errorObj.message != null && !errorObj.message.trim().isEmpty()) {
                            message = errorObj.message;
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            if (message == null || message.trim().isEmpty()) {
                message = "Server error (" + code + ")";
            }

            onError(message, code, errorObj);
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        if (t instanceof SocketTimeoutException) {
            onError("Request timed out. Please try again.", -2, null);
        } else if (t instanceof IOException) {
            onError("No connection to server", -1, null);
        } else {
            String msg = t.getMessage();
            onError(msg != null && !msg.trim().isEmpty() ? msg : "An unexpected error occurred", -3, null);
        }
    }
}
