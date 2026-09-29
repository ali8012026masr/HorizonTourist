package com.example.horizontourist.ui.slots;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.ConfirmedBookingItem;
import com.example.horizontourist.databinding.ActivityBookingConfirmationBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.ui.main.MainActivity;
import com.example.horizontourist.util.Formatters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BookingConfirmationActivity extends BaseActivity {

    public static final String EXTRA_CONFIRMED_ITEMS_JSON = "extra_confirmed_items_json";
    public static final String EXTRA_TOTAL_PAID = "extra_total_paid";

    public static Intent createIntent(Context context, List<ConfirmedBookingItem> items, double totalPaid) {
        Intent intent = new Intent(context, BookingConfirmationActivity.class);
        intent.putExtra(EXTRA_CONFIRMED_ITEMS_JSON, new Gson().toJson(items));
        intent.putExtra(EXTRA_TOTAL_PAID, totalPaid);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private ActivityBookingConfirmationBinding binding;
    private BookingConfirmationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        double totalPaid = getIntent().getDoubleExtra(EXTRA_TOTAL_PAID, 0.0);
        String json = getIntent().getStringExtra(EXTRA_CONFIRMED_ITEMS_JSON);

        List<ConfirmedBookingItem> items = new ArrayList<>();
        if (json != null && !json.trim().isEmpty()) {
            Type type = new TypeToken<ArrayList<ConfirmedBookingItem>>() {}.getType();
            List<ConfirmedBookingItem> parsed = new Gson().fromJson(json, type);
            if (parsed != null) {
                items.addAll(parsed);
            }
        }

        binding.tvTotalPaid.setText(Formatters.formatMoney(totalPaid));

        adapter = new BookingConfirmationAdapter();
        binding.rvConfirmedItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvConfirmedItems.setAdapter(adapter);
        adapter.setItems(items);

        binding.btnViewMyBookings.setOnClickListener(v -> {
            startActivity(MainActivity.createTabIntent(this, R.id.navigation_bookings));
            finish();
        });

        binding.btnContinueExploring.setOnClickListener(v -> {
            startActivity(MainActivity.createTabIntent(this, R.id.navigation_explore));
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(MainActivity.createTabIntent(BookingConfirmationActivity.this, R.id.navigation_home));
                finish();
            }
        });
    }
}
