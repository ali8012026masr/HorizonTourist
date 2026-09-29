package com.example.horizontourist.ui.guides;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.data.model.GuideAvailability;
import com.example.horizontourist.data.model.GuideBooking;
import com.example.horizontourist.data.model.GuideRating;
import com.example.horizontourist.databinding.ActivityGuideDetailBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.Formatters;
import com.example.horizontourist.util.SessionManager;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuideDetailActivity extends BaseActivity {

    public static final String EXTRA_GUIDE_ID = "extra_guide_id";

    public static Intent createIntent(Context context, long guideId) {
        Intent intent = new Intent(context, GuideDetailActivity.class);
        intent.putExtra(EXTRA_GUIDE_ID, guideId);
        return intent;
    }

    private ActivityGuideDetailBinding binding;
    private long guideId;
    private Guide loadedGuide;
    private GuideAvailabilityAdapter availabilityAdapter;
    private GuideRatingAdapter ratingAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuideDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        guideId = getIntent().getLongExtra(EXTRA_GUIDE_ID, -1L);
        if (guideId <= 0) {
            toast(R.string.error_unexpected);
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerViews();
        setupActions();
        loadAllData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        availabilityAdapter = new GuideAvailabilityAdapter(null);
        binding.rvAvailability.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAvailability.setAdapter(availabilityAdapter);

        ratingAdapter = new GuideRatingAdapter();
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(ratingAdapter);
    }

    private void setupActions() {
        binding.btnRetry.setOnClickListener(v -> loadAllData());

        binding.btnRequestGuide.setOnClickListener(v -> {
            if (loadedGuide == null) return;
            Intent intent = RequestGuideActivity.createIntent(this, guideId);
            startActivity(intent);
        });

        binding.btnViewLocation.setOnClickListener(v -> {
            String name = (loadedGuide != null && loadedGuide.fullName != null) ? loadedGuide.fullName : "";
            Intent intent = GuideLocationActivity.createIntent(this, guideId, name);
            startActivity(intent);
        });
    }

    private void loadAllData() {
        showLoading(true);
        binding.layoutError.setVisibility(View.GONE);
        binding.scrollView.setVisibility(View.GONE);
        binding.layoutBottomActions.setVisibility(View.GONE);

        loadGuideProfile();
        loadGuideAvailability();
        loadGuideRatings();
        checkAcceptedBookings();
    }

    private void loadGuideProfile() {
        ApiClient.getApiService().getGuideById(guideId).enqueue(new Callback<Guide>() {
            @Override
            public void onResponse(@NonNull Call<Guide> call, @NonNull Response<Guide> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    loadedGuide = response.body();
                    displayGuide(loadedGuide);
                    binding.scrollView.setVisibility(View.VISIBLE);
                    binding.layoutBottomActions.setVisibility(View.VISIBLE);
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    showError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Guide> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                showError(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void displayGuide(Guide guide) {
        // Name
        binding.tvGuideName.setText(guide.fullName != null ? guide.fullName : "");

        // Verified
        boolean isVerified = Constants.VERIFICATION_STATUS_VERIFIED.equalsIgnoreCase(guide.verificationStatus);
        binding.tvVerifiedBadge.setVisibility(isVerified ? View.VISIBLE : View.GONE);

        // Availability Badge
        if (guide.isAvailableNow()) {
            binding.tvAvailabilityBadge.setText(R.string.badge_available);
            binding.tvAvailabilityBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success)));
        } else {
            binding.tvAvailabilityBadge.setText(R.string.badge_busy);
            binding.tvAvailabilityBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.outline)));
        }

        // Rating
        double rating = guide.ratingAvg != null ? guide.ratingAvg : 0.0;
        int ratingCount = guide.ratingCount != null ? guide.ratingCount : 0;
        binding.tvRating.setText(String.format(Locale.US, "★ %.1f (%d reviews)", rating, ratingCount));

        // Experience
        int exp = guide.experienceYears != null ? guide.experienceYears : 0;
        binding.tvExperience.setText(getString(R.string.exp_years_format, exp));

        // Price
        binding.tvPrice.setText(Formatters.formatMoney(guide.defaultPrice));

        // Negotiable badge
        if (guide.isNegotiable()) {
            binding.tvNegotiableBadge.setText(R.string.badge_negotiable);
            binding.tvNegotiableBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary)));
        } else {
            binding.tvNegotiableBadge.setText(R.string.badge_fixed_price);
            binding.tvNegotiableBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.outline)));
        }

        // Location & Contact
        if (guide.location != null && !guide.location.trim().isEmpty()) {
            binding.tvLocation.setText(getString(R.string.guide_address_prefix, guide.location));
            binding.tvLocation.setVisibility(View.VISIBLE);
        } else {
            binding.tvLocation.setVisibility(View.GONE);
        }

        if (guide.phone != null && !guide.phone.trim().isEmpty()) {
            binding.tvPhone.setText(getString(R.string.guide_phone_prefix, guide.phone));
            binding.tvPhone.setVisibility(View.VISIBLE);
        } else {
            binding.tvPhone.setVisibility(View.GONE);
        }

        if (guide.email != null && !guide.email.trim().isEmpty()) {
            binding.tvEmail.setText(getString(R.string.guide_email_prefix, guide.email));
            binding.tvEmail.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmail.setVisibility(View.GONE);
        }

        // Bio
        if (guide.bio != null && !guide.bio.trim().isEmpty()) {
            binding.tvGuideBio.setText(guide.bio);
        } else {
            binding.tvGuideBio.setText(R.string.guide_bio_empty);
        }

        // Languages
        binding.cgLanguages.removeAllViews();
        if (guide.languages != null && !guide.languages.isEmpty()) {
            binding.cardLanguages.setVisibility(View.VISIBLE);
            for (String lang : guide.languages) {
                if (lang != null && !lang.trim().isEmpty()) {
                    Chip chip = new Chip(this);
                    chip.setText(lang.trim());
                    chip.setCheckable(false);
                    chip.setClickable(false);
                    chip.setEnsureMinTouchTargetSize(false);
                    binding.cgLanguages.addView(chip);
                }
            }
        } else {
            binding.cardLanguages.setVisibility(View.GONE);
        }
    }

    private void loadGuideAvailability() {
        ApiClient.getApiService().getGuideAvailabilityByGuideId(guideId).enqueue(new Callback<List<GuideAvailability>>() {
            @Override
            public void onResponse(@NonNull Call<List<GuideAvailability>> call, @NonNull Response<List<GuideAvailability>> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<GuideAvailability> openSlots = new ArrayList<>();
                    for (GuideAvailability slot : response.body()) {
                        if (slot.isOpen()) {
                            openSlots.add(slot);
                        }
                    }
                    availabilityAdapter.setItems(openSlots);
                    boolean isEmpty = openSlots.isEmpty();
                    binding.tvEmptyAvailability.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    binding.rvAvailability.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                } else {
                    binding.tvEmptyAvailability.setVisibility(View.VISIBLE);
                    binding.rvAvailability.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GuideAvailability>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.tvEmptyAvailability.setVisibility(View.VISIBLE);
                binding.rvAvailability.setVisibility(View.GONE);
            }
        });
    }

    private void loadGuideRatings() {
        ApiClient.getApiService().getGuideRatings(guideId).enqueue(new Callback<List<GuideRating>>() {
            @Override
            public void onResponse(@NonNull Call<List<GuideRating>> call, @NonNull Response<List<GuideRating>> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<GuideRating> ratings = response.body();
                    ratingAdapter.setItems(ratings);
                    boolean isEmpty = ratings.isEmpty();
                    binding.tvEmptyReviews.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    binding.rvReviews.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                } else {
                    binding.tvEmptyReviews.setVisibility(View.VISIBLE);
                    binding.rvReviews.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GuideRating>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.tvEmptyReviews.setVisibility(View.VISIBLE);
                binding.rvReviews.setVisibility(View.GONE);
            }
        });
    }

    private void checkAcceptedBookings() {
        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) {
            binding.btnViewLocation.setVisibility(View.GONE);
            return;
        }

        ApiClient.getApiService().getTouristGuideBookings(touristId).enqueue(new Callback<List<GuideBooking>>() {
            @Override
            public void onResponse(@NonNull Call<List<GuideBooking>> call, @NonNull Response<List<GuideBooking>> response) {
                if (isFinishing() || isDestroyed()) return;

                boolean hasAccepted = false;
                if (response.isSuccessful() && response.body() != null) {
                    for (GuideBooking b : response.body()) {
                        if (b != null && b.tourGuideId != null && b.tourGuideId.equals(guideId)
                                && Constants.GUIDE_BOOKING_STATUS_ACCEPTED.equalsIgnoreCase(b.status)) {
                            hasAccepted = true;
                            break;
                        }
                    }
                }
                binding.btnViewLocation.setVisibility(hasAccepted ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<List<GuideBooking>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnViewLocation.setVisibility(View.GONE);
            }
        });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message != null ? message : getString(R.string.error_unexpected));
        binding.scrollView.setVisibility(View.GONE);
        binding.layoutBottomActions.setVisibility(View.GONE);
    }
}
