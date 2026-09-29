package com.example.horizontourist.ui.slots;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.CartItem;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.ServiceRating;
import com.example.horizontourist.data.model.ServiceSlot;
import com.example.horizontourist.databinding.ActivitySlotDetailBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.CartManager;
import com.example.horizontourist.util.Formatters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SlotDetailActivity extends BaseActivity {

    public static final String EXTRA_SLOT_ID = "extra_slot_id";

    public static Intent createIntent(Context context, Long slotId) {
        Intent intent = new Intent(context, SlotDetailActivity.class);
        intent.putExtra(EXTRA_SLOT_ID, slotId);
        return intent;
    }

    private ActivitySlotDetailBinding binding;
    private Long slotId;
    private ServiceSlot currentSlot;
    private ProviderRatingAdapter ratingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySlotDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        slotId = getIntent().getLongExtra(EXTRA_SLOT_ID, -1L);
        if (slotId <= 0) {
            Toast.makeText(this, R.string.error_unexpected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        setupActions();
        loadSlotDetail();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_slot_detail);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        ratingAdapter = new ProviderRatingAdapter();
        binding.rvProviderRatings.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProviderRatings.setAdapter(ratingAdapter);
    }

    private void setupActions() {
        binding.btnAddToCart.setOnClickListener(v -> {
            if (currentSlot == null) return;
            if (CartManager.getInstance().contains(currentSlot.id)) {
                Toast.makeText(this, R.string.already_in_cart, Toast.LENGTH_SHORT).show();
                return;
            }

            String title = buildTitle(currentSlot);
            String nowIso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date());
            CartItem item = new CartItem(
                    currentSlot.id,
                    title,
                    currentSlot.price,
                    currentSlot.category,
                    currentSlot.startDateTime,
                    currentSlot.availableSeats,
                    currentSlot.status,
                    nowIso
            );
            CartManager.getInstance().add(item);
            Toast.makeText(this, R.string.added_to_cart_msg, Toast.LENGTH_SHORT).show();
            updateButtonStates();
        });

        binding.btnBookNow.setOnClickListener(v -> {
            if (currentSlot == null) return;
            startActivity(CheckoutActivity.createSingleItemIntent(
                    this,
                    currentSlot.id,
                    buildTitle(currentSlot),
                    currentSlot.price
            ));
        });
    }

    private void loadSlotDetail() {
        showLoading(true);
        ApiClient.getApiService().getSlotById(slotId).enqueue(new Callback<ServiceSlot>() {
            @Override
            public void onResponse(Call<ServiceSlot> call, Response<ServiceSlot> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentSlot = response.body();
                    displaySlot(currentSlot);
                    if (currentSlot.providerId != null) {
                        loadProviderRatings(currentSlot.providerId);
                    }
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    Toast.makeText(SlotDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ServiceSlot> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                Toast.makeText(SlotDetailActivity.this, ApiError.networkErrorMessage(t), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadProviderRatings(Long providerId) {
        ApiClient.getApiService().getProviderRatings(providerId).enqueue(new Callback<List<ServiceRating>>() {
            @Override
            public void onResponse(Call<List<ServiceRating>> call, Response<List<ServiceRating>> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<ServiceRating> ratings = response.body();
                    if (ratings.isEmpty()) {
                        binding.tvEmptyRatings.setVisibility(View.VISIBLE);
                        binding.rvProviderRatings.setVisibility(View.GONE);
                    } else {
                        binding.tvEmptyRatings.setVisibility(View.GONE);
                        binding.rvProviderRatings.setVisibility(View.VISIBLE);
                        ratingAdapter.setItems(ratings);
                    }
                } else {
                    binding.tvEmptyRatings.setVisibility(View.VISIBLE);
                    binding.rvProviderRatings.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<ServiceRating>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.tvEmptyRatings.setVisibility(View.VISIBLE);
                binding.rvProviderRatings.setVisibility(View.GONE);
            }
        });
    }

    private void displaySlot(ServiceSlot slot) {
        binding.ivCategoryIcon.setImageResource(Formatters.getCategoryIconRes(slot.category));
        binding.tvCategoryName.setText(Formatters.capitalizeEnum(slot.category));

        binding.tvTitle.setText(buildTitle(slot));
        binding.tvPrice.setText(Formatters.formatMoney(slot.price));

        binding.tvStartTime.setText(Formatters.formatDateTime(slot.startDateTime));
        binding.tvEndTime.setText(Formatters.formatDateTime(slot.endDateTime));

        int capacity = slot.capacity != null ? slot.capacity : 0;
        binding.tvCapacity.setText(getString(R.string.capacity_format, capacity));

        // Status badge
        String status = slot.status != null ? slot.status : Constants.SLOT_STATUS_OPEN;
        binding.tvStatus.setText(status);
        if (Constants.SLOT_STATUS_OPEN.equalsIgnoreCase(status)) {
            binding.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success)));
        } else if (Constants.SLOT_STATUS_FULL.equalsIgnoreCase(status)) {
            binding.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.warning)));
        } else {
            binding.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error)));
        }

        // Available seats
        int seats = slot.availableSeats != null ? slot.availableSeats : 0;
        if (seats > 0) {
            binding.tvSeatsLeft.setText(getString(R.string.seats_left, seats));
            binding.tvSeatsLeft.setTextColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            binding.tvSeatsLeft.setText(R.string.no_seats);
            binding.tvSeatsLeft.setTextColor(ContextCompat.getColor(this, R.color.error));
        }

        // Provider info
        String providerName = slot.providerBusinessName != null ? slot.providerBusinessName : getString(R.string.provider_label);
        binding.tvProviderName.setText(providerName);

        if (slot.providerRatingAvg != null && slot.providerRatingCount != null && slot.providerRatingCount > 0) {
            binding.tvProviderRatingAvg.setText(String.format(Locale.US, getString(R.string.rating_score_format),
                    slot.providerRatingAvg, slot.providerRatingCount));
            binding.tvProviderRatingAvg.setVisibility(View.VISIBLE);
        } else {
            binding.tvProviderRatingAvg.setVisibility(View.GONE);
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        if (currentSlot == null) return;

        boolean isOpen = currentSlot.isOpen();
        boolean hasSeats = currentSlot.availableSeats != null && currentSlot.availableSeats > 0;
        boolean inCart = CartManager.getInstance().contains(currentSlot.id);

        boolean canBook = isOpen && hasSeats;

        binding.btnBookNow.setEnabled(canBook);

        if (inCart) {
            binding.btnAddToCart.setText(R.string.btn_added_to_cart);
            binding.btnAddToCart.setEnabled(false);
        } else {
            binding.btnAddToCart.setText(R.string.btn_add_to_cart);
            binding.btnAddToCart.setEnabled(canBook);
        }
    }

    private String buildTitle(ServiceSlot slot) {
        if (slot.origin != null && !slot.origin.trim().isEmpty()
                && slot.destination != null && !slot.destination.trim().isEmpty()) {
            return getString(R.string.route_format, slot.origin, slot.destination);
        } else if (slot.locationName != null && !slot.locationName.trim().isEmpty()) {
            return slot.locationName;
        } else if (slot.destination != null && !slot.destination.trim().isEmpty()) {
            return slot.destination;
        } else if (slot.providerBusinessName != null) {
            return slot.providerBusinessName;
        } else {
            return Formatters.capitalizeEnum(slot.category);
        }
    }

    private void showLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.scrollView.setVisibility(loading ? View.GONE : View.VISIBLE);
        binding.bottomBar.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonStates();
    }
}
