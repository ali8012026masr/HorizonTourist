package com.example.horizontourist.data.api;

import android.os.Handler;
import android.os.Looper;

import com.example.horizontourist.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public interface SessionExpiredListener {
        void onSessionExpired();
    }

    private static ApiService apiService;
    private static Retrofit retrofit;
    private static SessionExpiredListener sessionExpiredListener;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ApiClient() {
    }

    public static synchronized void init() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            if (BuildConfig.DEBUG) {
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            } else {
                logging.setLevel(HttpLoggingInterceptor.Level.NONE);
            }

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor())
                    .addInterceptor(logging)
                    .build();

            Gson gson = new GsonBuilder().create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            init();
        }
        return apiService;
    }

    public static synchronized Retrofit getRetrofit() {
        if (retrofit == null) {
            init();
        }
        return retrofit;
    }

    public static void setSessionExpiredListener(SessionExpiredListener listener) {
        sessionExpiredListener = listener;
    }

    public static void clearSessionExpiredListener() {
        sessionExpiredListener = null;
    }

    public static void notifySessionExpired() {
        mainHandler.post(() -> {
            if (sessionExpiredListener != null) {
                sessionExpiredListener.onSessionExpired();
            }
        });
    }
}
