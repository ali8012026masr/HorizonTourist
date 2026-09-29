package com.example.horizontourist.ui.guides;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.GuideRating;
import com.example.horizontourist.data.model.GuideRatingRequest;
import com.example.horizontourist.databinding.ActivityRateGuideBinding;
import com.example.horizontourist.ui.BaseActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RateGuideActivity extends BaseActivity {

    public static final String EXTRA_GUIDE_BOOKING_ID = "extra_guide_booking_id";

    public static Intent createIntent(Context context, long guideBookingId) {
        Intent intent = new Intent(context, RateGuideActivity.class);
        intent.putExtra(EXTRA_GUIDE_BOOKING_ID, guideBookingId);
        return intent;
    }

    private ActivityRateGuideBinding binding;
    private long guideBookingId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRateGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        guideBookingId = getIntent().getLongExtra(EXTRA_GUIDE_BOOKING_ID, -1L);
        if (guideBookingId <= 0) {
            toast(R.string.error_unexpected);
            finish();
            return;
        }

        setupToolbar();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        binding.btnSubmit.setOnClickListener(v -> submitRating());
    }

    private void submitRating() {
        int score = Math.round(binding.ratingBar.getRating());
        if (score < 1 || score > 5) {
            toast(R.string.rating_score_required);
            return;
        }

        String comment = (binding.etComment.getText() != null)
                ? binding.etComment.getText().toString().trim()
                : "";

        showLoading(true);
        GuideRatingRequest request = new GuideRatingRequest(guideBookingId, score, comment);

        ApiClient.getApiService().rateGuide(request).enqueue(new Callback<GuideRating>() {
            @Override
            public void onResponse(@NonNull Call<GuideRating> call, @NonNull Response<GuideRating> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RateGuideActivity.this, R.string.rating_guide_success, Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 409 || response.code() == 400) {
                    Toast.makeText(RateGuideActivity.this, R.string.rating_guide_already_exists, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    Toast.makeText(RateGuideActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GuideRating> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                Toast.makeText(RateGuideActivity.this, ApiError.networkErrorMessage(t), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(!show);
        binding.ratingBar.setEnabled(!show);
        binding.etComment.setEnabled(!show);
    }
}
