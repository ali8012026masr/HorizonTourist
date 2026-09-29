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
import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.data.model.GuideAvailability;
import com.example.horizontourist.data.model.GuideBooking;
import com.example.horizontourist.data.model.GuideBookingRequest;
import com.example.horizontourist.databinding.ActivityRequestGuideBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.Formatters;
import com.example.horizontourist.util.SessionManager;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestGuideActivity extends BaseActivity {

    public static final String EXTRA_GUIDE_ID = "extra_guide_id";

    public static Intent createIntent(Context context, long guideId) {
        Intent intent = new Intent(context, RequestGuideActivity.class);
        intent.putExtra(EXTRA_GUIDE_ID, guideId);
        return intent;
    }

    private ActivityRequestGuideBinding binding;
    private long guideId;
    private Guide loadedGuide;
    private GuideAvailabilityAdapter slotsAdapter;
    private GuideAvailability selectedAvailabilitySlot;
    private String selectedCustomDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        guideId = getIntent().getLongExtra(EXTRA_GUIDE_ID, -1L);
        if (guideId <= 0) {
            toast(R.string.error_unexpected);
            finish();
            return;
        }

        setupToolbar();
        setupSlotsRecyclerView();
        setupDateOptions();
        setupActions();
        loadData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSlotsRecyclerView() {
        slotsAdapter = new GuideAvailabilityAdapter(availability -> {
            selectedAvailabilitySlot = availability;
        });
        binding.rvSlots.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSlots.setAdapter(slotsAdapter);
    }

    private void setupDateOptions() {
        binding.rgDateOption.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSlotOption) {
                binding.layoutSlotsContainer.setVisibility(View.VISIBLE);
                binding.layoutCustomDateContainer.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbCustomDateOption) {
                binding.layoutSlotsContainer.setVisibility(View.GONE);
                binding.layoutCustomDateContainer.setVisibility(View.VISIBLE);
            }
        });

        binding.btnPickCustomDate.setOnClickListener(v -> openDatePicker());
    }

    private void openDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(R.string.select_date_prompt);

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointForward.now());
        builder.setCalendarConstraints(constraintsBuilder.build());

        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                selectedCustomDate = sdf.format(new Date(selection));
                binding.tvSelectedCustomDate.setText(getString(R.string.custom_date_prefix, selectedCustomDate));
            }
        });
        picker.show(getSupportFragmentManager(), "PICK_CUSTOM_DATE");
    }

    private void setupActions() {
        binding.btnSubmitRequest.setOnClickListener(v -> submitBookingRequest());
    }

    private void loadData() {
        showLoading(true);

        ApiClient.getApiService().getGuideById(guideId).enqueue(new Callback<Guide>() {
            @Override
            public void onResponse(@NonNull Call<Guide> call, @NonNull Response<Guide> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    loadedGuide = response.body();
                    displayGuideInfo(loadedGuide);
                    loadAvailability();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    toast(msg);
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Guide> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                toast(ApiError.networkErrorMessage(t));
                finish();
            }
        });
    }

    private void displayGuideInfo(Guide guide) {
        binding.tvGuideName.setText(guide.fullName != null ? guide.fullName : "");
        binding.tvDefaultPrice.setText(Formatters.formatMoney(guide.defaultPrice));

        if (guide.isNegotiable()) {
            binding.tvNegotiableBadge.setText(R.string.badge_negotiable);
            binding.tvNegotiableBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary)));
            binding.etPrice.setEnabled(true);
            binding.tilPrice.setHelperText("Price is negotiable (Default: " + Formatters.formatMoney(guide.defaultPrice) + ")");
        } else {
            binding.tvNegotiableBadge.setText(R.string.badge_fixed_price);
            binding.tvNegotiableBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.outline)));
            binding.etPrice.setEnabled(false);
            binding.tilPrice.setHelperText(getString(R.string.fixed_price_hint));
        }

        binding.etPrice.setText(String.format(Locale.US, "%.2f", guide.defaultPrice));
    }

    private void loadAvailability() {
        ApiClient.getApiService().getGuideAvailabilityByGuideId(guideId).enqueue(new Callback<List<GuideAvailability>>() {
            @Override
            public void onResponse(@NonNull Call<List<GuideAvailability>> call, @NonNull Response<List<GuideAvailability>> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<GuideAvailability> openSlots = new ArrayList<>();
                    for (GuideAvailability a : response.body()) {
                        if (a.isOpen()) {
                            openSlots.add(a);
                        }
                    }
                    slotsAdapter.setItems(openSlots);
                    if (openSlots.isEmpty()) {
                        binding.tvNoSlotsPrompt.setVisibility(View.VISIBLE);
                        binding.rbCustomDateOption.setChecked(true);
                    } else {
                        binding.tvNoSlotsPrompt.setVisibility(View.GONE);
                        selectedAvailabilitySlot = openSlots.get(0);
                        slotsAdapter.setSelectedId(selectedAvailabilitySlot.id);
                    }
                } else {
                    binding.tvNoSlotsPrompt.setVisibility(View.VISIBLE);
                    binding.rbCustomDateOption.setChecked(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GuideAvailability>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.tvNoSlotsPrompt.setVisibility(View.VISIBLE);
                binding.rbCustomDateOption.setChecked(true);
            }
        });
    }

    private void submitBookingRequest() {
        if (loadedGuide == null) return;

        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) {
            toast(R.string.error_unexpected);
            return;
        }

        Long availabilityId = null;
        String scheduleDate = null;

        if (binding.rbSlotOption.isChecked()) {
            if (selectedAvailabilitySlot == null || selectedAvailabilitySlot.date == null) {
                toast(R.string.error_select_date);
                return;
            }
            availabilityId = selectedAvailabilitySlot.id;
            scheduleDate = selectedAvailabilitySlot.date;
        } else {
            if (selectedCustomDate == null || selectedCustomDate.trim().isEmpty()) {
                toast(R.string.error_select_date);
                return;
            }
            scheduleDate = selectedCustomDate;
        }

        // Validate price
        String priceText = (binding.etPrice.getText() != null) ? binding.etPrice.getText().toString().trim() : "";
        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) {
                toast(R.string.error_invalid_price);
                return;
            }
        } catch (NumberFormatException e) {
            toast(R.string.error_invalid_price);
            return;
        }

        boolean isNegotiated = loadedGuide.isNegotiable() && (Math.abs(price - loadedGuide.defaultPrice) > 0.01);

        GuideBookingRequest request = new GuideBookingRequest(
                touristId,
                loadedGuide.id,
                availabilityId,
                scheduleDate,
                price,
                isNegotiated
        );

        showLoading(true);
        binding.btnSubmitRequest.setEnabled(false);

        ApiClient.getApiService().createGuideBooking(request).enqueue(new Callback<GuideBooking>() {
            @Override
            public void onResponse(@NonNull Call<GuideBooking> call, @NonNull Response<GuideBooking> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                binding.btnSubmitRequest.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    toast(R.string.guide_request_success);
                    Intent intent = new Intent(RequestGuideActivity.this, MyGuideBookingsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    toast(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GuideBooking> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                binding.btnSubmitRequest.setEnabled(true);
                toast(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
