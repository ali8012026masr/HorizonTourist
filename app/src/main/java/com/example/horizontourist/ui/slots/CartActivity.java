package com.example.horizontourist.ui.slots;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.CartItem;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.ServiceSlot;
import com.example.horizontourist.databinding.ActivityCartBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.CartManager;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends BaseActivity implements CartAdapter.OnCartItemListener {

    public static Intent createIntent(Context context) {
        return new Intent(context, CartActivity.class);
    }

    private ActivityCartBinding binding;
    private CartAdapter adapter;
    private final List<CartItem> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
        refreshSlotAvailability();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_cart);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(this);
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(adapter);

        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshSlotAvailability);
    }

    private void setupActions() {
        binding.btnExploreServices.setOnClickListener(v -> finish());

        binding.btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if any items are unavailable
            boolean hasUnavailable = false;
            for (CartItem item : cartItems) {
                if (item.status != null && !Constants.SLOT_STATUS_OPEN.equalsIgnoreCase(item.status)) {
                    hasUnavailable = true;
                    break;
                }
                if (item.availableSeats != null && item.availableSeats <= 0) {
                    hasUnavailable = true;
                    break;
                }
            }

            if (hasUnavailable) {
                Toast.makeText(this, R.string.cart_item_unavailable, Toast.LENGTH_LONG).show();
            }

            startActivity(CheckoutActivity.createCartIntent(this));
        });
    }

    private void loadCartItems() {
        cartItems.clear();
        cartItems.addAll(CartManager.getInstance().getItems());
        adapter.setItems(cartItems);

        updateTotalAndEmptyState();
    }

    private void updateTotalAndEmptyState() {
        boolean isEmpty = cartItems.isEmpty();
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.swipeRefreshLayout.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.btnCheckout.setEnabled(!isEmpty);

        double total = CartManager.getInstance().getTotal();
        binding.tvTotalAmount.setText(Formatters.formatMoney(total));
    }

    private void refreshSlotAvailability() {
        if (cartItems.isEmpty()) {
            binding.swipeRefreshLayout.setRefreshing(false);
            return;
        }

        binding.swipeRefreshLayout.setRefreshing(true);
        AtomicInteger pendingCalls = new AtomicInteger(cartItems.size());
        boolean[] anyPriceChanged = new boolean[]{false};

        for (CartItem item : cartItems) {
            if (item.slotId == null) {
                if (pendingCalls.decrementAndGet() == 0) {
                    onRefreshComplete(anyPriceChanged[0]);
                }
                continue;
            }

            ApiClient.getApiService().getSlotById(item.slotId).enqueue(new Callback<ServiceSlot>() {
                @Override
                public void onResponse(Call<ServiceSlot> call, Response<ServiceSlot> response) {
                    if (isFinishing() || isDestroyed()) return;

                    if (response.isSuccessful() && response.body() != null) {
                        ServiceSlot fresh = response.body();
                        if (Math.abs(item.price - fresh.price) > 0.001) {
                            item.price = fresh.price;
                            anyPriceChanged[0] = true;
                        }
                        item.status = fresh.status;
                        item.availableSeats = fresh.availableSeats;
                        item.startDateTime = fresh.startDateTime;
                        item.category = fresh.category;
                        CartManager.getInstance().updateItem(item);
                    } else if (response.code() == 404) {
                        item.status = Constants.SLOT_STATUS_CANCELLED;
                        item.availableSeats = 0;
                        CartManager.getInstance().updateItem(item);
                    }

                    if (pendingCalls.decrementAndGet() == 0) {
                        onRefreshComplete(anyPriceChanged[0]);
                    }
                }

                @Override
                public void onFailure(Call<ServiceSlot> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;

                    if (pendingCalls.decrementAndGet() == 0) {
                        onRefreshComplete(anyPriceChanged[0]);
                    }
                }
            });
        }
    }

    private void onRefreshComplete(boolean priceChanged) {
        if (isFinishing() || isDestroyed()) return;
        binding.swipeRefreshLayout.setRefreshing(false);

        loadCartItems();

        if (priceChanged) {
            Toast.makeText(this, R.string.cart_item_price_changed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRemoveItem(CartItem item) {
        if (item == null || item.slotId == null) return;
        CartManager.getInstance().remove(item.slotId);
        loadCartItems();
    }

    @Override
    public void onItemClick(CartItem item) {
        if (item == null || item.slotId == null) return;
        startActivity(SlotDetailActivity.createIntent(this, item.slotId));
    }
}
