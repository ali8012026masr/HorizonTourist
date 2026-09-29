package com.example.horizontourist.ui.bookings;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Booking;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.Payment;
import com.example.horizontourist.data.model.PaymentRequest;
import com.example.horizontourist.data.model.ServiceSlot;
import com.example.horizontourist.data.model.StatusRequest;
import com.example.horizontourist.databinding.ActivityBookingDetailBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.Formatters;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingDetailActivity extends BaseActivity {

    public static final String EXTRA_BOOKING_ID = "extra_booking_id";
    public static final String EXTRA_SLOT_ID = "extra_slot_id";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_TOTAL_AMOUNT = "extra_total_amount";
    public static final String EXTRA_PAYMENT_STATUS = "extra_payment_status";
    public static final String EXTRA_BOOKING_DATE = "extra_booking_date";

    public static Intent createIntent(Context context, Long bookingId, Long slotId, String status,
                                      double totalAmount, String paymentStatus, String bookingDate) {
        Intent intent = new Intent(context, BookingDetailActivity.class);
        intent.putExtra(EXTRA_BOOKING_ID, bookingId);
        intent.putExtra(EXTRA_SLOT_ID, slotId);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_TOTAL_AMOUNT, totalAmount);
        intent.putExtra(EXTRA_PAYMENT_STATUS, paymentStatus);
        intent.putExtra(EXTRA_BOOKING_DATE, bookingDate);
        return intent;
    }

    private ActivityBookingDetailBinding binding;
    private Long bookingId;
    private Long slotId;
    private String status;
    private double totalAmount;
    private String paymentStatus;
    private String bookingDate;

    private ServiceSlot loadedSlot;
    private PaymentRecordAdapter paymentAdapter;

    private final ActivityResultLauncher<Intent> rateServiceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    binding.btnRateService.setEnabled(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1L);
        slotId = getIntent().getLongExtra(EXTRA_SLOT_ID, -1L);
        status = getIntent().getStringExtra(EXTRA_STATUS);
        totalAmount = getIntent().getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0.0);
        paymentStatus = getIntent().getStringExtra(EXTRA_PAYMENT_STATUS);
        bookingDate = getIntent().getStringExtra(EXTRA_BOOKING_DATE);

        if (status == null) status = Constants.BOOKING_STATUS_PENDING;
        if (paymentStatus == null) paymentStatus = Constants.PAYMENT_STATUS_UNPAID;

        setupToolbar();
        setupPaymentsRecycler();
        renderBookingInfo();
        setupActions();

        if (slotId != null && slotId > 0) {
            loadSlotDetails();
        }
        if (bookingId != null && bookingId > 0) {
            loadPayments();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_booking_detail);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupPaymentsRecycler() {
        paymentAdapter = new PaymentRecordAdapter();
        binding.rvPayments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPayments.setAdapter(paymentAdapter);
    }

    private void renderBookingInfo() {
        long id = bookingId != null ? bookingId : 0L;
        binding.tvBookingId.setText(getString(R.string.booking_id_prefix, id));
        binding.tvTotalAmount.setText(Formatters.formatMoney(totalAmount));

        if (bookingDate != null) {
            binding.tvBookingDate.setText(String.format(getString(R.string.booking_date_prefix),
                    Formatters.formatDateTime(bookingDate)));
        } else {
            binding.tvBookingDate.setText("");
        }

        // Status badge
        binding.tvBookingStatus.setText(status);
        if (Constants.BOOKING_STATUS_CONFIRMED.equalsIgnoreCase(status)) {
            binding.tvBookingStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success)));
        } else if (Constants.BOOKING_STATUS_PENDING.equalsIgnoreCase(status)) {
            binding.tvBookingStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.warning)));
        } else if (Constants.BOOKING_STATUS_COMPLETED.equalsIgnoreCase(status)) {
            binding.tvBookingStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
        } else {
            binding.tvBookingStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error)));
        }

        // Payment badge
        binding.tvPaymentStatus.setText(String.format(getString(R.string.status_payment_prefix), paymentStatus));
        if (Constants.PAYMENT_STATUS_PAID.equalsIgnoreCase(paymentStatus)) {
            binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success)));
        } else if (Constants.PAYMENT_STATUS_UNPAID.equalsIgnoreCase(paymentStatus)) {
            binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.warning)));
        } else {
            binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.outline)));
        }

        updateActionButtons();
    }

    private void updateActionButtons() {
        boolean isCancelled = Constants.BOOKING_STATUS_CANCELLED.equalsIgnoreCase(status);
        boolean isCompleted = Constants.BOOKING_STATUS_COMPLETED.equalsIgnoreCase(status);
        boolean isPaid = Constants.PAYMENT_STATUS_PAID.equalsIgnoreCase(paymentStatus);

        // Cancel action: only PENDING or CONFIRMED
        boolean canCancel = !isCancelled && !isCompleted &&
                (Constants.BOOKING_STATUS_PENDING.equalsIgnoreCase(status) || Constants.BOOKING_STATUS_CONFIRMED.equalsIgnoreCase(status));
        binding.btnCancelBooking.setVisibility(canCancel ? View.VISIBLE : View.GONE);

        // Pay now action: UNPAID and not CANCELLED
        boolean canPay = !isPaid && !isCancelled;
        binding.btnPayNow.setVisibility(canPay ? View.VISIBLE : View.GONE);

        // Rate service action: only COMPLETED
        binding.btnRateService.setVisibility(isCompleted ? View.VISIBLE : View.GONE);
    }

    private void setupActions() {
        binding.btnCancelBooking.setOnClickListener(v -> confirmCancelBooking());

        binding.btnPayNow.setOnClickListener(v -> showPaymentDialog());

        binding.btnRateService.setOnClickListener(v -> {
            Long providerId = (loadedSlot != null) ? loadedSlot.providerId : null;
            rateServiceLauncher.launch(RateServiceActivity.createIntent(this, bookingId, providerId));
        });
    }

    private void confirmCancelBooking() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cancel_booking_confirm_title)
                .setMessage(R.string.cancel_booking_confirm_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.btn_cancel_booking, (dialog, which) -> executeCancelBooking())
                .show();
    }

    private void executeCancelBooking() {
        showLoading(true);
        ApiClient.getApiService().updateBookingStatus(bookingId, new StatusRequest(Constants.BOOKING_STATUS_CANCELLED))
                .enqueue(new Callback<Booking>() {
                    @Override
                    public void onResponse(Call<Booking> call, Response<Booking> response) {
                        if (isFinishing() || isDestroyed()) return;
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            Booking updated = response.body();
                            status = updated.status != null ? updated.status : Constants.BOOKING_STATUS_CANCELLED;
                            paymentStatus = updated.paymentStatus != null ? updated.paymentStatus : paymentStatus;
                            renderBookingInfo();
                            Toast.makeText(BookingDetailActivity.this, R.string.cancel_booking_success, Toast.LENGTH_SHORT).show();
                        } else {
                            String msg = ApiError.extractMessage(response.errorBody());
                            Toast.makeText(BookingDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Booking> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        showLoading(false);
                        Toast.makeText(BookingDetailActivity.this, ApiError.networkErrorMessage(t), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showPaymentDialog() {
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
                .setTitle(R.string.payment_dialog_title)
                .setSingleChoiceItems(methods, 0, (dialog, which) -> selected[0] = which)
                .setPositiveButton(getString(R.string.btn_pay_now, Formatters.formatMoney(totalAmount)), (dialog, which) -> {
                    String chosenMethod = methodConstants[selected[0]];
                    executePayment(chosenMethod);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executePayment(String method) {
        showLoading(true);
        PaymentRequest request = PaymentRequest.forBooking(bookingId, totalAmount, method);
        ApiClient.getApiService().createPayment(request).enqueue(new Callback<Payment>() {
            @Override
            public void onResponse(Call<Payment> call, Response<Payment> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful()) {
                    paymentStatus = Constants.PAYMENT_STATUS_PAID;
                    renderBookingInfo();
                    loadPayments();
                    Toast.makeText(BookingDetailActivity.this, R.string.payment_success, Toast.LENGTH_SHORT).show();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    Toast.makeText(BookingDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Payment> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                Toast.makeText(BookingDetailActivity.this, ApiError.networkErrorMessage(t), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSlotDetails() {
        ApiClient.getApiService().getSlotById(slotId).enqueue(new Callback<ServiceSlot>() {
            @Override
            public void onResponse(Call<ServiceSlot> call, Response<ServiceSlot> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    loadedSlot = response.body();
                    displaySlot(loadedSlot);
                }
            }

            @Override
            public void onFailure(Call<ServiceSlot> call, Throwable t) {
                // Ignore silent slot failure
            }
        });
    }

    private void displaySlot(ServiceSlot slot) {
        binding.ivCategoryIcon.setImageResource(Formatters.getCategoryIconRes(slot.category));

        if (slot.origin != null && !slot.origin.trim().isEmpty() && slot.destination != null && !slot.destination.trim().isEmpty()) {
            binding.tvSlotTitle.setText(getString(R.string.route_format, slot.origin.trim(), slot.destination.trim()));
        } else if (slot.locationName != null && !slot.locationName.trim().isEmpty()) {
            binding.tvSlotTitle.setText(slot.locationName.trim());
        } else {
            binding.tvSlotTitle.setText(Formatters.capitalizeEnum(slot.category));
        }

        binding.tvProviderName.setText(slot.providerBusinessName != null ? slot.providerBusinessName : "");
        binding.tvStartTime.setText(Formatters.formatDateTime(slot.startDateTime));
        binding.tvEndTime.setText(Formatters.formatDateTime(slot.endDateTime));
    }

    private void loadPayments() {
        ApiClient.getApiService().getPaymentsByBookingId(bookingId).enqueue(new Callback<List<Payment>>() {
            @Override
            public void onResponse(Call<List<Payment>> call, Response<List<Payment>> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Payment> payments = response.body();
                    if (payments.isEmpty()) {
                        binding.tvEmptyPayments.setVisibility(View.VISIBLE);
                        binding.rvPayments.setVisibility(View.GONE);
                    } else {
                        binding.tvEmptyPayments.setVisibility(View.GONE);
                        binding.rvPayments.setVisibility(View.VISIBLE);
                        paymentAdapter.setItems(payments);
                    }
                } else {
                    binding.tvEmptyPayments.setVisibility(View.VISIBLE);
                    binding.rvPayments.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Payment>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.tvEmptyPayments.setVisibility(View.VISIBLE);
                binding.rvPayments.setVisibility(View.GONE);
            }
        });
    }

    private void showLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnCancelBooking.setEnabled(!loading);
        binding.btnPayNow.setEnabled(!loading);
        binding.btnRateService.setEnabled(!loading);
    }
}
