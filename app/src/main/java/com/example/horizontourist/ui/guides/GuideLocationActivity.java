package com.example.horizontourist.ui.guides;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.data.model.Location;
import com.example.horizontourist.databinding.ActivityGuideLocationBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.Formatters;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuideLocationActivity extends BaseActivity {

    public static final String EXTRA_GUIDE_ID = "extra_guide_id";
    public static final String EXTRA_GUIDE_NAME = "extra_guide_name";
    private static final long REFRESH_INTERVAL_MS = 15000;

    public static Intent createIntent(Context context, long guideId, String guideName) {
        Intent intent = new Intent(context, GuideLocationActivity.class);
        intent.putExtra(EXTRA_GUIDE_ID, guideId);
        intent.putExtra(EXTRA_GUIDE_NAME, guideName);
        return intent;
    }

    private ActivityGuideLocationBinding binding;
    private long guideId;
    private String guideName;
    private Location currentLocation;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadLocation(false);
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuideLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        guideId = getIntent().getLongExtra(EXTRA_GUIDE_ID, -1L);
        guideName = getIntent().getStringExtra(EXTRA_GUIDE_NAME);

        if (guideId <= 0) {
            toast(R.string.error_unexpected);
            finish();
            return;
        }

        setupToolbar();
        setupViews();
        setupActions();

        if (guideName == null || guideName.trim().isEmpty()) {
            loadGuideInfo();
        } else {
            binding.tvGuideName.setText(guideName);
        }

        loadLocation(true);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        if (guideName != null && !guideName.trim().isEmpty()) {
            binding.tvGuideName.setText(guideName);
        } else {
            binding.tvGuideName.setText(R.string.loading_guide_name);
        }
    }

    private void setupActions() {
        binding.btnRefreshLocation.setOnClickListener(v -> loadLocation(true));
        binding.btnRetryFriendly.setOnClickListener(v -> loadLocation(true));

        binding.btnOpenMaps.setOnClickListener(v -> {
            if (currentLocation == null) {
                toast(R.string.live_location_unavailable);
                return;
            }
            openInMaps(currentLocation.latitude, currentLocation.longitude);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void loadGuideInfo() {
        ApiClient.getApiService().getGuideById(guideId).enqueue(new Callback<Guide>() {
            @Override
            public void onResponse(@NonNull Call<Guide> call, @NonNull Response<Guide> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    guideName = response.body().fullName;
                    binding.tvGuideName.setText(guideName != null ? guideName : "");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Guide> call, @NonNull Throwable t) {
                // Ignore, optional fallback
            }
        });
    }

    private void loadLocation(boolean showProgress) {
        if (showProgress) {
            showLoading(true);
        }

        ApiClient.getApiService().getGuideLocation(guideId).enqueue(new Callback<Location>() {
            @Override
            public void onResponse(@NonNull Call<Location> call, @NonNull Response<Location> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentLocation = response.body();
                    displayLocation(currentLocation);
                } else if (response.code() == 403 || response.code() == 404) {
                    showFriendlyError(getString(R.string.location_not_accepted));
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    showFriendlyError(msg != null ? msg : getString(R.string.location_fetch_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Location> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                showFriendlyError(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void displayLocation(Location location) {
        binding.cardLocationDetails.setVisibility(View.VISIBLE);
        binding.layoutFriendlyError.setVisibility(View.GONE);

        binding.tvCoordinates.setText(String.format(Locale.US,
                getString(R.string.coordinates_format), location.latitude, location.longitude));

        if (location.updatedAt != null && !location.updatedAt.trim().isEmpty()) {
            binding.tvLastUpdated.setText(String.format(
                    getString(R.string.last_updated_prefix), Formatters.formatDateTime(location.updatedAt)));
            binding.tvLastUpdated.setVisibility(View.VISIBLE);
        } else {
            binding.tvLastUpdated.setVisibility(View.GONE);
        }
    }

    private void showFriendlyError(String message) {
        binding.cardLocationDetails.setVisibility(View.GONE);
        binding.layoutFriendlyError.setVisibility(View.VISIBLE);
        binding.tvFriendlyError.setText(message != null ? message : getString(R.string.location_fetch_error));
    }

    private void openInMaps(double lat, double lng) {
        String label = (guideName != null && !guideName.trim().isEmpty()) ? guideName : "Guide";
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + Uri.encode(label) + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, webUri);
            try {
                startActivity(browserIntent);
            } catch (Exception ex) {
                toast(R.string.location_fetch_error);
            }
        }
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
