package com.example.horizontourist.ui.slots;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Booking;
import com.example.horizontourist.data.model.BookingRequest;
import com.example.horizontourist.data.model.CartItem;
import com.example.horizontourist.data.model.ConfirmedBookingItem;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.Payment;
import com.example.horizontourist.data.model.PaymentRequest;
import com.example.horizontourist.databinding.ActivityCheckoutBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.CartManager;
import com.example.horizontourist.util.Formatters;
import com.example.horizontourist.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends BaseActivity {

    public static final String EXTRA_DIRECT_BOOKING = "extra_direct_booking";
    public static final String EXTRA_SLOT_ID = "extra_slot_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_PRICE = "extra_price";

    public static Intent createCartIntent(Context context) {
        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra(EXTRA_DIRECT_BOOKING, false);
        return intent;
    }

    public static Intent createSingleItemIntent(Context context, Long slotId, String title, double price) {
        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra(EXTRA_DIRECT_BOOKING, true);
        intent.putExtra(EXTRA_SLOT_ID, slotId);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_PRICE, price);
        return intent;
    }

    private ActivityCheckoutBinding binding;
    private CheckoutItemAdapter adapter;
    private final List<CheckoutItemAdapter.ItemState> items = new ArrayList<>();
    private boolean isDirectBooking = false;
    private boolean isProcessing = false;
    private double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isDirectBooking = getIntent().getBooleanExtra(EXTRA_DIRECT_BOOKING, false);

        setupToolbar();
        setupRecyclerView();
        loadItems();
        setupActions();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isProcessing) {
                    Toast.makeText(CheckoutActivity.this, R.string.processing_checkout, Toast.LENGTH_SHORT).show();
                    return;
                }
                finish();
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isDirectBooking ? R.string.single_booking_title : R.string.title_checkout);
        }
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (!isProcessing) {
                finish();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CheckoutItemAdapter();
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrderItems.setAdapter(adapter);
    }

    private void loadItems() {
        items.clear();
        totalAmount = 0.0;

        if (isDirectBooking) {
            long slotId = getIntent().getLongExtra(EXTRA_SLOT_ID, -1L);
            String title = getIntent().getStringExtra(EXTRA_TITLE);
            double price = getIntent().getDoubleExtra(EXTRA_PRICE, 0.0);
            if (slotId > 0) {
                items.add(new CheckoutItemAdapter.ItemState(slotId, title, price));
                totalAmount = price;
            }
        } else {
            List<CartItem> cartList = CartManager.getInstance().getItems();
            for (CartItem cartItem : cartList) {
                if (cartItem.slotId != null) {
                    items.add(new CheckoutItemAdapter.ItemState(cartItem.slotId, cartItem.title, cartItem.price));
                    totalAmount += cartItem.price;
                }
            }
        }

        adapter.setItems(items);
        binding.tvOrderTotal.setText(Formatters.formatMoney(totalAmount));
        binding.btnPay.setText(getString(R.string.btn_pay_now, Formatters.formatMoney(totalAmount)));

        if (items.isEmpty()) {
            Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupActions() {
        binding.btnPay.setOnClickListener(v -> startCheckoutProcess());
    }

    private String getSelectedPaymentMethod() {
        if (binding.rbMobile.isChecked()) {
            return Constants.PAYMENT_METHOD_MOBILE_BANKING;
        } else if (binding.rbCash.isChecked()) {
            return Constants.PAYMENT_METHOD_CASH;
        } else {
            return Constants.PAYMENT_METHOD_CARD;
        }
    }

    private void startCheckoutProcess() {
        Long touristId = SessionManager.getInstance().getTouristId();
        if (touristId == null || touristId <= 0) {
            Toast.makeText(this, R.string.error_unexpected, Toast.LENGTH_SHORT).show();
            return;
        }

        setProcessing(true);
        binding.tvCheckoutError.setVisibility(View.GONE);

        String paymentMethod = getSelectedPaymentMethod();
        processItemAt(0, touristId, paymentMethod);
    }

    private void processItemAt(int index, Long touristId, String paymentMethod) {
        if (isFinishing() || isDestroyed()) return;

        if (index >= items.size()) {
            onAllItemsProcessed();
            return;
        }

        CheckoutItemAdapter.ItemState item = items.get(index);

        if (item.paymentDone) {
            // Already completed earlier, move to next
            processItemAt(index + 1, touristId, paymentMethod);
            return;
        }

        if (!item.bookingDone) {
            // Step 1: Create booking
            item.statusText = getString(R.string.processing_checkout);
            item.isError = false;
            adapter.notifyItemChanged(index);

            ApiClient.getApiService().createBooking(new BookingRequest(touristId, item.slotId))
                    .enqueue(new Callback<Booking>() {
                        @Override
                        public void onResponse(Call<Booking> call, Response<Booking> response) {
                            if (isFinishing() || isDestroyed()) return;

                            if (response.isSuccessful() && response.body() != null) {
                                Booking booking = response.body();
                                item.createdBookingId = booking.id;
                                item.bookingDone = true;
                                adapter.notifyItemChanged(index);

                                // Proceed to step 2: Payment
                                executePaymentForItem(index, item, paymentMethod, touristId);
                            } else {
                                if (response.code() == 409) {
                                    item.statusText = getString(R.string.slot_unavailable_error);
                                } else {
                                    String msg = ApiError.extractMessage(response.errorBody());
                                    item.statusText = getString(R.string.booking_failed_format, msg);
                                }
                                item.isError = true;
                                adapter.notifyItemChanged(index);

                                // Move to next item
                                processItemAt(index + 1, touristId, paymentMethod);
                            }
                        }

                        @Override
                        public void onFailure(Call<Booking> call, Throwable t) {
                            if (isFinishing() || isDestroyed()) return;
                            item.statusText = getString(R.string.booking_failed_format, ApiError.networkErrorMessage(t));
                            item.isError = true;
                            adapter.notifyItemChanged(index);

                            processItemAt(index + 1, touristId, paymentMethod);
                        }
                    });
        } else {
            // Booking already created, execute payment directly
            executePaymentForItem(index, item, paymentMethod, touristId);
        }
    }

    private void executePaymentForItem(int index, CheckoutItemAdapter.ItemState item, String paymentMethod, Long touristId) {
        if (isFinishing() || isDestroyed()) return;

        PaymentRequest paymentRequest = PaymentRequest.forBooking(item.createdBookingId, item.price, paymentMethod);
        ApiClient.getApiService().createPayment(paymentRequest).enqueue(new Callback<Payment>() {
            @Override
            public void onResponse(Call<Payment> call, Response<Payment> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    item.paymentDone = true;
                    item.isError = false;
                    item.statusText = getString(R.string.checkout_success_title);
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    item.statusText = getString(R.string.payment_failed_format, msg);
                    item.isError = true;
                }
                adapter.notifyItemChanged(index);

                // Move to next item
                processItemAt(index + 1, touristId, paymentMethod);
            }

            @Override
            public void onFailure(Call<Payment> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                item.statusText = getString(R.string.payment_failed_format, ApiError.networkErrorMessage(t));
                item.isError = true;
                adapter.notifyItemChanged(index);

                processItemAt(index + 1, touristId, paymentMethod);
            }
        });
    }

    private void onAllItemsProcessed() {
        if (isFinishing() || isDestroyed()) return;

        setProcessing(false);

        List<ConfirmedBookingItem> confirmedItems = new ArrayList<>();
        double totalPaid = 0.0;
        boolean hasFailures = false;

        for (CheckoutItemAdapter.ItemState item : items) {
            if (item.paymentDone) {
                confirmedItems.add(new ConfirmedBookingItem(
                        item.createdBookingId,
                        item.title,
                        item.price,
                        Constants.BOOKING_STATUS_CONFIRMED
                ));
                totalPaid += item.price;

                if (!isDirectBooking) {
                    CartManager.getInstance().remove(item.slotId);
                }
            } else {
                hasFailures = true;
            }
        }

        if (!hasFailures && !confirmedItems.isEmpty()) {
            // Complete success!
            startActivity(BookingConfirmationActivity.createIntent(this, confirmedItems, totalPaid));
            finish();
        } else {
            // Partial or complete failure
            binding.tvCheckoutError.setVisibility(View.VISIBLE);
            binding.tvCheckoutError.setText(R.string.checkout_partial_error);
            Toast.makeText(this, R.string.checkout_partial_error, Toast.LENGTH_LONG).show();
        }
    }

    private void setProcessing(boolean processing) {
        this.isProcessing = processing;
        binding.btnPay.setEnabled(!processing);
        binding.rbCard.setEnabled(!processing);
        binding.rbMobile.setEnabled(!processing);
        binding.rbCash.setEnabled(!processing);
        binding.progressBar.setVisibility(processing ? View.VISIBLE : View.GONE);
        binding.tvProcessingStatus.setVisibility(processing ? View.VISIBLE : View.GONE);
    }
}
