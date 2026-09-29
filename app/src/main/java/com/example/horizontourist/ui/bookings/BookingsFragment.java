package com.example.horizontourist.ui.bookings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Booking;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.data.model.GuideBooking;
import com.example.horizontourist.data.model.ServiceSlot;
import com.example.horizontourist.data.model.Payment;
import com.example.horizontourist.data.model.PaymentRequest;
import com.example.horizontourist.databinding.FragmentBookingsBinding;
import com.example.horizontourist.ui.BaseFragment;
import com.example.horizontourist.ui.guides.GuideLocationActivity;
import com.example.horizontourist.ui.guides.MyGuideBookingsActivity;
import com.example.horizontourist.ui.guides.RateGuideActivity;
import com.example.horizontourist.util.Formatters;
import com.example.horizontourist.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingsFragment extends BaseFragment implements ServiceBookingAdapter.OnServiceBookingListener, GuideBookingAdapter.OnGuideBookingListener {

    private FragmentBookingsBinding binding;
    private ServiceBookingAdapter serviceAdapter;
    private GuideBookingAdapter guideAdapter;

    private final List<Booking> allServiceBookings = new ArrayList<>();
    private final List<GuideBooking> allGuideBookings = new ArrayList<>();
    private final Set<Long> pendingSlotRequests = new HashSet<>();
    private final Set<Long> pendingGuideRequests = new HashSet<>();

    private boolean isServicesTab = true;
    private String currentStatusFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAdapters();
        setupToggleAndFilters();
        setupRefreshAndRetry();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCurrentTab();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupAdapters() {
        serviceAdapter = new ServiceBookingAdapter(this);
        guideAdapter = new GuideBookingAdapter(this);

        binding.rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBookings.setAdapter(serviceAdapter);
    }

    private void setupToggleAndFilters() {
        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            isServicesTab = (checkedId == R.id.btnTabServices);
            updateTabMode();
        });

        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int chipId = checkedIds.get(0);
            if (chipId == R.id.chipPending) {
                currentStatusFilter = Constants.BOOKING_STATUS_PENDING;
            } else if (chipId == R.id.chipConfirmed) {
                currentStatusFilter = Constants.BOOKING_STATUS_CONFIRMED;
            } else if (chipId == R.id.chipCompleted) {
                currentStatusFilter = Constants.BOOKING_STATUS_COMPLETED;
            } else if (chipId == R.id.chipCancelled) {
                currentStatusFilter = Constants.BOOKING_STATUS_CANCELLED;
            } else {
                currentStatusFilter = "ALL";
            }
            applyServiceFilter();
        });
    }

    private void setupRefreshAndRetry() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary);
        binding.swipeRefresh.setOnRefreshListener(this::loadCurrentTab);
        binding.btnRetry.setOnClickListener(v -> loadCurrentTab());
    }

    private void updateTabMode() {
        if (isServicesTab) {
            binding.scrollFilters.setVisibility(View.VISIBLE);
            binding.rvBookings.setAdapter(serviceAdapter);
            applyServiceFilter();
            if (allServiceBookings.isEmpty()) {
                loadServiceBookings();
            }
        } else {
            binding.scrollFilters.setVisibility(View.GONE);
            binding.rvBookings.setAdapter(guideAdapter);
            updateGuideEmptyState();
            if (allGuideBookings.isEmpty()) {
                loadGuideBookings();
            }
        }
    }

    private void loadCurrentTab() {
        if (isServicesTab) {
            loadServiceBookings();
        } else {
            loadGuideBookings();
        }
    }

    private void loadServiceBookings() {
        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) return;

        showLoading(true);
        hideError();

        ApiClient.getApiService().getTouristBookings(touristId).enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (!isSafe()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    allServiceBookings.clear();
                    allServiceBookings.addAll(response.body());
                    applyServiceFilter();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    showError(msg);
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                if (!isSafe()) return;
                showLoading(false);
                showError(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void loadGuideBookings() {
        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) return;

        showLoading(true);
        hideError();

        ApiClient.getApiService().getTouristGuideBookings(touristId).enqueue(new Callback<List<GuideBooking>>() {
            @Override
            public void onResponse(Call<List<GuideBooking>> call, Response<List<GuideBooking>> response) {
                if (!isSafe()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    allGuideBookings.clear();
                    allGuideBookings.addAll(response.body());
                    guideAdapter.setItems(allGuideBookings);
                    updateGuideEmptyState();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    showError(msg);
                }
            }

            @Override
            public void onFailure(Call<List<GuideBooking>> call, Throwable t) {
                if (!isSafe()) return;
                showLoading(false);
                showError(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void applyServiceFilter() {
        List<Booking> filtered = new ArrayList<>();
        if ("ALL".equalsIgnoreCase(currentStatusFilter)) {
            filtered.addAll(allServiceBookings);
        } else {
            for (Booking b : allServiceBookings) {
                if (currentStatusFilter.equalsIgnoreCase(b.status)) {
                    filtered.add(b);
                }
            }
        }

        serviceAdapter.setItems(filtered);

        boolean isEmpty = filtered.isEmpty();
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.tvEmptyMessage.setText(R.string.empty_service_bookings);
        binding.rvBookings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updateGuideEmptyState() {
        boolean isEmpty = allGuideBookings.isEmpty();
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.tvEmptyMessage.setText(R.string.empty_guide_bookings);
        binding.rvBookings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onBookingClick(Booking booking) {
        if (!isSafe() || booking == null || booking.id == null) return;
        Intent intent = BookingDetailActivity.createIntent(
                requireContext(),
                booking.id,
                booking.serviceSlotId,
                booking.status,
                booking.totalAmount,
                booking.paymentStatus,
                booking.bookingDate
        );
        startActivity(intent);
    }

    @Override
    public void onNeedSlot(Long slotId) {
        if (slotId == null || pendingSlotRequests.contains(slotId)) return;
        pendingSlotRequests.add(slotId);

        ApiClient.getApiService().getSlotById(slotId).enqueue(new Callback<ServiceSlot>() {
            @Override
            public void onResponse(Call<ServiceSlot> call, Response<ServiceSlot> response) {
                pendingSlotRequests.remove(slotId);
                if (!isSafe()) return;
                if (response.isSuccessful() && response.body() != null) {
                    serviceAdapter.putSlot(slotId, response.body());
                }
            }

            @Override
            public void onFailure(Call<ServiceSlot> call, Throwable t) {
                pendingSlotRequests.remove(slotId);
            }
        });
    }

    @Override
    public void onGuideBookingClick(GuideBooking booking) {
        if (!isSafe()) return;
        Intent intent = new Intent(requireContext(), MyGuideBookingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPayClick(GuideBooking booking) {
        if (!isSafe() || booking == null || booking.id == null) return;
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

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.payment_method_guide_title)
                .setSingleChoiceItems(methods, 0, (dialog, which) -> selected[0] = which)
                .setPositiveButton(getString(R.string.btn_pay_now, Formatters.formatMoney(booking.agreedPrice)), (dialog, which) -> {
                    String chosenMethod = methodConstants[selected[0]];
                    executeGuidePayment(booking, chosenMethod);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executeGuidePayment(GuideBooking booking, String method) {
        showLoading(true);
        PaymentRequest request = PaymentRequest.forGuideBooking(booking.id, booking.agreedPrice, method);
        ApiClient.getApiService().createPayment(request).enqueue(new Callback<Payment>() {
            @Override
            public void onResponse(@NonNull Call<Payment> call, @NonNull Response<Payment> response) {
                if (!isSafe()) return;
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
                if (!isSafe()) return;
                showLoading(false);
                toast(ApiError.networkErrorMessage(t));
            }
        });
    }

    @Override
    public void onViewLocationClick(GuideBooking booking, Guide guide) {
        if (!isSafe() || booking == null || booking.tourGuideId == null) return;
        String guideName = (guide != null && guide.fullName != null) ? guide.fullName : "";
        Intent intent = GuideLocationActivity.createIntent(requireContext(), booking.tourGuideId, guideName);
        startActivity(intent);
    }

    @Override
    public void onRateGuideClick(GuideBooking booking) {
        if (!isSafe() || booking == null || booking.id == null) return;
        Intent intent = RateGuideActivity.createIntent(requireContext(), booking.id);
        startActivity(intent);
    }

    @Override
    public void onNeedGuide(Long guideId) {
        if (guideId == null || pendingGuideRequests.contains(guideId)) return;
        pendingGuideRequests.add(guideId);

        ApiClient.getApiService().getGuideById(guideId).enqueue(new Callback<Guide>() {
            @Override
            public void onResponse(Call<Guide> call, Response<Guide> response) {
                pendingGuideRequests.remove(guideId);
                if (!isSafe()) return;
                if (response.isSuccessful() && response.body() != null) {
                    guideAdapter.putGuide(guideId, response.body());
                }
            }

            @Override
            public void onFailure(Call<Guide> call, Throwable t) {
                pendingGuideRequests.remove(guideId);
            }
        });
    }

    @Override
    public void showLoading(boolean loading) {
        if (binding == null) return;
        binding.swipeRefresh.setRefreshing(loading);
        if (loading) {
            binding.layoutError.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError(String message) {
        if (binding == null) return;
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message);
        binding.rvBookings.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
    }

    private void hideError() {
        if (binding == null) return;
        binding.layoutError.setVisibility(View.GONE);
    }
}
