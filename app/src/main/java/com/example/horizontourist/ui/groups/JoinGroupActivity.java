package com.example.horizontourist.ui.groups;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Group;
import com.example.horizontourist.data.model.JoinGroupRequest;
import com.example.horizontourist.databinding.ActivityJoinGroupBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.SessionManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinGroupActivity extends BaseActivity {

    private ActivityJoinGroupBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJoinGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        binding.btnJoinGroup.setOnClickListener(v -> submitJoinGroup());
    }

    private void submitJoinGroup() {
        String code = (binding.etJoinCode.getText() != null)
                ? binding.etJoinCode.getText().toString().trim().toUpperCase(Locale.US)
                : "";

        if (code.isEmpty()) {
            toast(R.string.error_empty_join_code);
            return;
        }

        long myId = SessionManager.getInstance().getTouristId();
        if (myId <= 0) {
            toast(R.string.error_unexpected);
            return;
        }

        showLoading(true);
        JoinGroupRequest request = new JoinGroupRequest(code, myId);

        ApiClient.getApiService().joinGroup(request).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(@NonNull Call<Group> call, @NonNull Response<Group> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Group group = response.body();
                    toast(R.string.join_group_success);
                    startActivity(GroupDetailActivity.createIntent(JoinGroupActivity.this, group.id));
                    finish();
                } else if (response.code() == 404) {
                    toast(R.string.error_invalid_join_code);
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    toast(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Group> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                toast(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnJoinGroup.setEnabled(!show);
        binding.etJoinCode.setEnabled(!show);
    }
}
