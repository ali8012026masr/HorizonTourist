package com.example.horizontourist.ui.bookings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.ServiceRating;
import com.example.horizontourist.data.model.ServiceRatingRequest;
import com.example.horizontourist.databinding.ActivityRateServiceBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RateServiceActivity extends BaseActivity {

    public static final String EXTRA_BOOKING_ID = "extra_booking_id";
    public static final String EXTRA_PROVIDER_ID = "extra_provider_id";

    public static Intent createIntent(Context context, Long bookingId, Long providerId) {
        Intent intent = new Intent(context, RateServiceActivity.class);
        intent.putExtra(EXTRA_BOOKING_ID, bookingId);
        intent.putExtra(EXTRA_PROVIDER_ID, providerId);
        return intent;
    }

    private ActivityRateServiceBinding binding;
    private Long bookingId;
    private Long providerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRateServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1L);
        providerId = getIntent().getLongExtra(EXTRA_PROVIDER_ID, -1L);

        if (bookingId <= 0) {
            Toast.makeText(this, R.string.error_unexpected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupActions();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_rate_service);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupActions() {
        binding.btnSubmit.setOnClickListener(v -> submitRating());
    }

    private void submitRating() {
        int score = Math.round(binding.ratingBar.getRating());
        if (score < 1 || score > 5) {
            Toast.makeText(this, R.string.rating_score_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = binding.etComment.getText() != null ?
                binding.etComment.getText().toString().trim() : null;

        Long touristId = SessionManager.getInstance().getTouristId();
        if (touristId == null || touristId <= 0) {
            Toast.makeText(this, R.string.error_session_expired, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);

        ServiceRatingRequest request = new ServiceRatingRequest(bookingId, touristId, providerId, score, comment);
        ApiClient.getApiService().rateService(request).enqueue(new Callback<ServiceRating>() {
            @Override
            public void onResponse(Call<ServiceRating> call, Response<ServiceRating> response) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(RateServiceActivity.this, R.string.rating_success, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else if (response.code() == 409 || response.code() == 400) {
                    Toast.makeText(RateServiceActivity.this, R.string.rating_already_exists, Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    Toast.makeText(RateServiceActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ServiceRating> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);
                Toast.makeText(RateServiceActivity.this, ApiError.networkErrorMessage(t), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(!loading);
        binding.ratingBar.setEnabled(!loading);
        binding.etComment.setEnabled(!loading);
    }
}
