package com.example.horizontourist.ui.groups;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Group;
import com.example.horizontourist.databinding.ActivityGroupsBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsActivity extends BaseActivity implements GroupAdapter.OnGroupClickListener {

    private ActivityGroupsBinding binding;
    private GroupAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroups();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new GroupAdapter(this);
        binding.rvGroups.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGroups.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary);
        binding.swipeRefresh.setOnRefreshListener(this::loadGroups);
        binding.btnRetry.setOnClickListener(v -> loadGroups());

        binding.fabAction.setOnClickListener(v -> showActionDialog());
    }

    private void showActionDialog() {
        String[] options = new String[]{
                getString(R.string.btn_create_group),
                getString(R.string.btn_join_group)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.group_actions_prompt)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(this, CreateGroupActivity.class));
                    } else if (which == 1) {
                        startActivity(new Intent(this, JoinGroupActivity.class));
                    }
                })
                .show();
    }

    private void loadGroups() {
        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) {
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        showLoading(true);
        ApiClient.getApiService().getTouristGroups(touristId).enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(@NonNull Call<List<Group>> call, @NonNull Response<List<Group>> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Group> list = response.body();
                    adapter.setItems(list);
                    boolean isEmpty = list.isEmpty();
                    binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    binding.rvGroups.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                    binding.layoutError.setVisibility(View.GONE);
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    showError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Group>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                showError(ApiError.networkErrorMessage(t));
            }
        });
    }

    @Override
    public void onGroupClick(Group group) {
        if (group == null || group.id == null) return;
        Intent intent = GroupDetailActivity.createIntent(this, group.id);
        startActivity(intent);
    }

    @Override
    public void onCopyCodeClick(Group group) {
        if (group == null || group.joinCode == null) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("Join Code", group.joinCode);
            clipboard.setPrimaryClip(clip);
            toast(R.string.join_code_copied);
        }
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
        binding.rvGroups.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
    }
}
