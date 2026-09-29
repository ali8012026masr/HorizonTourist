package com.example.horizontourist.ui.guides;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.data.model.GuideBooking;
import com.example.horizontourist.data.model.Payment;
import com.example.horizontourist.data.model.PaymentRequest;
import com.example.horizontourist.databinding.ActivityMyGuideBookingsBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.ui.bookings.GuideBookingAdapter;
import com.example.horizontourist.util.Formatters;
import com.example.horizontourist.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyGuideBookingsActivity extends BaseActivity implements GuideBookingAdapter.OnGuideBookingListener {

    private ActivityMyGuideBookingsBinding binding;
    private GuideBookingAdapter adapter;
    private final Set<Long> pendingGuideRequests = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyGuideBookingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadGuideBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGuideBookings();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new GuideBookingAdapter(this);
        binding.rvGuideBookings.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGuideBookings.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary);
        binding.swipeRefresh.setOnRefreshListener(this::loadGuideBookings);
        binding.btnRetry.setOnClickListener(v -> loadGuideBookings());
    }

    private void loadGuideBookings() {
        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) {
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        showLoading(true);
        ApiClient.getApiService().getTouristGuideBookings(touristId).enqueue(new Callback<List<GuideBooking>>() {
            @Override
            public void onResponse(@NonNull Call<List<GuideBooking>> call, @NonNull Response<List<GuideBooking>> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<GuideBooking> list = response.body();
                    adapter.setItems(list);
                    boolean isEmpty = list.isEmpty();
                    binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    binding.rvGuideBookings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                    binding.layoutError.setVisibility(View.GONE);
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    showError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GuideBooking>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                showError(ApiError.networkErrorMessage(t));
            }
        });
    }

    @Override
    public void onGuideBookingClick(GuideBooking booking) {
        // Tapping card can open guide location if accepted
        if (booking == null) return;
        if (booking.isAccepted()) {
            Intent intent = GuideLocationActivity.createIntent(this, booking.tourGuideId, "");
            startActivity(intent);
        } else if (booking.isCompleted()) {
            Intent intent = RateGuideActivity.createIntent(this, booking.id);
            startActivity(intent);
        }
    }

    @Override
    public void onNeedGuide(Long guideId) {
        if (guideId == null || pendingGuideRequests.contains(guideId)) return;
        pendingGuideRequests.add(guideId);

        ApiClient.getApiService().getGuideById(guideId).enqueue(new Callback<Guide>() {
            @Override
            public void onResponse(@NonNull Call<Guide> call, @NonNull Response<Guide> response) {
                pendingGuideRequests.remove(guideId);
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    adapter.putGuide(guideId, response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Guide> call, @NonNull Throwable t) {
                pendingGuideRequests.remove(guideId);
            }
        });
    }

    @Override
    public void onPayClick(GuideBooking booking) {
        if (booking == null || booking.id == null) return;

        String[] methods = new String[]{
                getString(R.string.method_card),
                getString(R.string.method_mobile),
                getString(R.string.method_cash)
        };
        final String[] methodConstants = new String[]{
                Constants.PAYMENT_METHOD_CARD,
                Constants.PAYMENT_METHOD_MOBILE_BANKING,
                Constants.PAYMENT_METHOD_CASH
        };
        final int[] selected = new int[]{0};

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.payment_method_guide_title)
                .setSingleChoiceItems(methods, 0, (dialog, which) -> selected[0] = which)
                .setPositiveButton(getString(R.string.btn_pay_now, Formatters.formatMoney(booking.agreedPrice)), (dialog, which) -> {
                    String chosenMethod = methodConstants[selected[0]];
                    executePayment(booking, chosenMethod);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executePayment(GuideBooking booking, String method) {
        showLoading(true);
        PaymentRequest request = PaymentRequest.forGuideBooking(booking.id, booking.agreedPrice, method);

        ApiClient.getApiService().createPayment(request).enqueue(new Callback<Payment>() {
            @Override
            public void onResponse(@NonNull Call<Payment> call, @NonNull Response<Payment> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    toast(R.string.payment_success);
                    loadGuideBookings();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    toast(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Payment> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                toast(ApiError.networkErrorMessage(t));
            }
        });
    }

    @Override
    public void onViewLocationClick(GuideBooking booking, Guide guide) {
        if (booking == null || booking.tourGuideId == null) return;
        String guideName = (guide != null && guide.fullName != null) ? guide.fullName : "";
        Intent intent = GuideLocationActivity.createIntent(this, booking.tourGuideId, guideName);
        startActivity(intent);
    }

    @Override
    public void onRateGuideClick(GuideBooking booking) {
        if (booking == null || booking.id == null) return;
        Intent intent = RateGuideActivity.createIntent(this, booking.id);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        binding.swipeRefresh.setRefreshing(show);
        if (show) {
            binding.layoutError.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message != null ? message : getString(R.string.error_unexpected));
        binding.rvGuideBookings.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
    }
}
