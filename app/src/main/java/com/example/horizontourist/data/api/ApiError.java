package com.example.horizontourist.data.api;

import com.example.horizontourist.data.model.ErrorResponse;
import com.google.gson.Gson;

import okhttp3.ResponseBody;

public final class ApiError {

    private ApiError() {
    }

    public static ErrorResponse parse(ResponseBody responseBody) {
        if (responseBody == null) {
            return null;
        }
        try {
            String json = responseBody.string();
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return new Gson().fromJson(json, ErrorResponse.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String getMessage(ResponseBody responseBody, int statusCode) {
        if (statusCode == 423) {
            return "Account locked";
        }
        ErrorResponse err = parse(responseBody);
        if (err != null && err.message != null && !err.message.trim().isEmpty()) {
            return err.message;
        }
        return "Error code " + statusCode;
    }

    public static String extractMessage(ResponseBody responseBody) {
        ErrorResponse err = parse(responseBody);
        if (err != null && err.message != null && !err.message.trim().isEmpty()) {
            return err.message;
        }
        return "An error occurred";
    }

    public static String networkErrorMessage(Throwable t) {
        if (t instanceof java.net.SocketTimeoutException) {
            return "Request timed out. Please try again.";
        } else if (t instanceof java.io.IOException) {
            return "No connection to server";
        } else {
            String msg = t != null ? t.getMessage() : null;
            return msg != null && !msg.trim().isEmpty() ? msg : "An unexpected error occurred";
        }
    }
}
