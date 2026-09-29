package com.example.horizontourist.ui.profile;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.Notification;
import com.example.horizontourist.databinding.ActivityNotificationsBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends BaseActivity implements NotificationAdapter.OnNotificationClickListener {

    private ActivityNotificationsBinding binding;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        loadNotifications();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);

        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.secondary);
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_mark_all_read) {
            markAllNotificationsAsRead();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNotifications() {
        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) {
            binding.swipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (!binding.swipeRefreshLayout.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService().getNotifications(touristId).enqueue(new ApiCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                if (!isSafe()) {
                    return;
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);

                if (notifications == null || notifications.isEmpty()) {
                    adapter.setItems(new ArrayList<>());
                    binding.rvNotifications.setVisibility(View.GONE);
                    binding.layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutEmptyState.setVisibility(View.GONE);
                    binding.rvNotifications.setVisibility(View.VISIBLE);
                    adapter.setItems(notifications);
                }
            }

            @Override
            public void onError(String message, int code) {
                if (!isSafe()) {
                    return;
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);

                if (adapter.getItemCount() == 0) {
                    binding.layoutEmptyState.setVisibility(View.VISIBLE);
                    binding.rvNotifications.setVisibility(View.GONE);
                }
                toast(message);
            }
        });
    }

    @Override
    public void onNotificationClick(Notification notification, int position) {
        if (notification == null || notification.id == null) {
            return;
        }

        if (!notification.isRead()) {
            // Optimistically mark as read in UI
            adapter.markItemAsRead(position);

            // Send read request to API
            ApiClient.getApiService().markNotificationRead(notification.id).enqueue(new ApiCallback<Notification>() {
                @Override
                public void onSuccess(Notification updated) {
                    // Successfully marked read on server
                }

                @Override
                public void onError(String message, int code) {
                    // Non-fatal optimistic update
                }
            });
        }
    }

    private void markAllNotificationsAsRead() {
        List<Notification> currentList = adapter.getItems();
        List<Long> unreadIds = new ArrayList<>();
        for (Notification n : currentList) {
            if (n != null && !n.isRead() && n.id != null) {
                unreadIds.add(n.id);
            }
        }

        if (unreadIds.isEmpty()) {
            toast(R.string.all_notifications_read);
            return;
        }

        // Optimistically update all items
        adapter.markAllAsRead();
        toast(R.string.all_notifications_read);

        // Send API requests for each unread notification
        for (Long id : unreadIds) {
            ApiClient.getApiService().markNotificationRead(id).enqueue(new ApiCallback<Notification>() {
                @Override
                public void onSuccess(Notification response) {
                }

                @Override
                public void onError(String message, int code) {
                }
            });
        }
    }
}
