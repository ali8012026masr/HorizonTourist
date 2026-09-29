package com.example.horizontourist.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.ChangePasswordRequest;
import com.example.horizontourist.databinding.ActivityChangePasswordBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.ui.auth.LoginActivity;
import com.example.horizontourist.util.SessionManager;
import com.example.horizontourist.util.Validators;

public class ChangePasswordActivity extends BaseActivity {

    private ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupClickListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        binding.btnUpdatePassword.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            changePassword();
        });
    }

    private void changePassword() {
        String currentPassword = binding.etCurrentPassword.getText() != null ?
                binding.etCurrentPassword.getText().toString().trim() : "";
        String newPassword = binding.etNewPassword.getText() != null ?
                binding.etNewPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ?
                binding.etConfirmPassword.getText().toString().trim() : "";

        binding.tilCurrentPassword.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        boolean hasError = false;

        if (!Validators.isNonEmpty(currentPassword)) {
            binding.tilCurrentPassword.setError(getString(R.string.error_empty_password));
            hasError = true;
        }

        if (!Validators.isNonEmpty(newPassword)) {
            binding.tilNewPassword.setError(getString(R.string.error_empty_password));
            hasError = true;
        } else if (!Validators.isPasswordValid(newPassword)) {
            binding.tilNewPassword.setError(getString(R.string.error_password_too_short));
            hasError = true;
        }

        if (!Validators.isNonEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_empty_password));
            hasError = true;
        } else if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        setLoading(true);

        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);
        ApiClient.getApiService().changePassword(request).enqueue(new ApiCallback<com.example.horizontourist.data.model.MessageResponse>() {
            @Override
            public void onSuccess(com.example.horizontourist.data.model.MessageResponse response) {
                if (!isSafe()) {
                    return;
                }
                setLoading(false);
                // Clear session because the backend invalidates old tokens after password change
                SessionManager.getInstance().clear();
                toast(R.string.password_change_success);

                Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message, int code) {
                if (!isSafe()) {
                    return;
                }
                setLoading(false);
                if (code == 400 || code == 401) {
                    binding.tilCurrentPassword.setError(message);
                    binding.etCurrentPassword.requestFocus();
                } else {
                    toast(message);
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnUpdatePassword.setEnabled(!loading);
        binding.etCurrentPassword.setEnabled(!loading);
        binding.etNewPassword.setEnabled(!loading);
        binding.etConfirmPassword.setEnabled(!loading);
    }
}
