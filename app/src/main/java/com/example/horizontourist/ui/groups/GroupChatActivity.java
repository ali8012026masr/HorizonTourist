package com.example.horizontourist.ui.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.GroupMessage;
import com.example.horizontourist.data.model.MessageRequest;
import com.example.horizontourist.databinding.ActivityGroupChatBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatActivity extends BaseActivity {

    public static final String EXTRA_GROUP_ID = "extra_group_id";
    public static final String EXTRA_GROUP_NAME = "extra_group_name";
    private static final long POLL_INTERVAL_MS = 5000;

    public static Intent createIntent(Context context, long groupId, String groupName) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_GROUP_NAME, groupName);
        return intent;
    }

    private ActivityGroupChatBinding binding;
    private long groupId;
    private String groupName;
    private long myTouristId;
    private GroupChatAdapter adapter;
    private LinearLayoutManager layoutManager;

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            fetchMessages(false);
            pollHandler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1L);
        groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);
        myTouristId = SessionManager.getInstance().getTouristId();

        if (groupId <= 0 || myTouristId <= 0) {
            toast(R.string.error_unexpected);
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        setupInputBar();
        fetchMessages(true);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (groupName != null && !groupName.trim().isEmpty()) {
            binding.toolbar.setTitle(groupName);
        } else {
            binding.toolbar.setTitle(R.string.title_group_chat);
        }
    }

    private void setupRecyclerView() {
        adapter = new GroupChatAdapter(myTouristId);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(layoutManager);
        binding.rvChatMessages.setAdapter(adapter);
    }

    private void setupInputBar() {
        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable);
    }

    private void fetchMessages(boolean showProgress) {
        if (showProgress) {
            showLoading(true);
        }

        ApiClient.getApiService().getGroupMessages(groupId).enqueue(new Callback<List<GroupMessage>>() {
            @Override
            public void onResponse(@NonNull Call<List<GroupMessage>> call, @NonNull Response<List<GroupMessage>> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<GroupMessage> messages = response.body();
                    boolean hasNew = adapter.setMessages(messages);
                    boolean isEmpty = adapter.getItemCount() == 0;
                    binding.tvEmptyChat.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    if (hasNew) {
                        binding.rvChatMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                } else if (response.code() == 403) {
                    handleRemovedFromGroup();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GroupMessage>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
            }
        });
    }

    private void sendMessage() {
        String text = (binding.etMessage.getText() != null)
                ? binding.etMessage.getText().toString().trim()
                : "";

        if (text.isEmpty()) {
            return;
        }

        binding.etMessage.setText("");
        MessageRequest request = new MessageRequest(myTouristId, text);

        ApiClient.getApiService().sendGroupMessage(groupId, request).enqueue(new Callback<GroupMessage>() {
            @Override
            public void onResponse(@NonNull Call<GroupMessage> call, @NonNull Response<GroupMessage> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    GroupMessage sent = response.body();
                    adapter.addMessage(sent);
                    binding.tvEmptyChat.setVisibility(View.GONE);
                    binding.rvChatMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                } else if (response.code() == 403) {
                    handleRemovedFromGroup();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    toast(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GroupMessage> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                toast(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void handleRemovedFromGroup() {
        pollHandler.removeCallbacks(pollRunnable);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title_group_chat)
                .setMessage(R.string.chat_removed_message)
                .setPositiveButton(R.string.ok, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
