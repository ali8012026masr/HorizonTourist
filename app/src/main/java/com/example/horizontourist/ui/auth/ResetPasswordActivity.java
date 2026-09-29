package com.example.horizontourist.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.ErrorResponse;
import com.example.horizontourist.data.model.MessageResponse;
import com.example.horizontourist.data.model.ResetPasswordRequest;
import com.example.horizontourist.databinding.ActivityResetPasswordBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.Validators;

public class ResetPasswordActivity extends BaseActivity {

    public static final String EXTRA_TOKEN = "token";

    private ActivityResetPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractTokenFromIntent(getIntent());

        binding.btnResetPassword.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            attemptResetPassword();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        extractTokenFromIntent(intent);
    }

    private void extractTokenFromIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String token = null;
        Uri data = intent.getData();
        if (data != null) {
            token = data.getQueryParameter("token");
        }
        if (token == null || token.trim().isEmpty()) {
            token = intent.getStringExtra(EXTRA_TOKEN);
        }
        if (token != null && !token.trim().isEmpty() && binding.etToken.getText() != null) {
            binding.etToken.setText(token.trim());
            binding.etNewPassword.requestFocus();
        }
    }

    private void attemptResetPassword() {
        binding.tilToken.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        String token = binding.etToken.getText() != null ? binding.etToken.getText().toString().trim() : "";
        String newPassword = binding.etNewPassword.getText() != null ? binding.etNewPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        boolean cancel = false;
        View focusView = null;

        if (!Validators.isNonEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_password_required));
            focusView = binding.tilConfirmPassword;
            cancel = true;
        } else if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_do_not_match));
            focusView = binding.tilConfirmPassword;
            cancel = true;
        }

        if (!Validators.isNonEmpty(newPassword)) {
            binding.tilNewPassword.setError(getString(R.string.error_password_required));
            focusView = binding.tilNewPassword;
            cancel = true;
        } else if (!Validators.isPasswordValid(newPassword)) {
            binding.tilNewPassword.setError(getString(R.string.error_password_length));
            focusView = binding.tilNewPassword;
            cancel = true;
        }

        if (!Validators.isNonEmpty(token)) {
            binding.tilToken.setError(getString(R.string.error_token_required));
            focusView = binding.tilToken;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        setLoading(true);

        ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword);
        ApiClient.getApiService().resetPassword(request).enqueue(new ApiCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse response) {
                if (!isSafe()) {
                    return;
                }
                setLoading(false);
                Toast.makeText(ResetPasswordActivity.this, R.string.reset_password_success, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message, int code, ErrorResponse errorResponse) {
                if (!isSafe()) {
                    return;
                }
                setLoading(false);
                String displayMsg = (errorResponse != null && errorResponse.message != null && !errorResponse.message.trim().isEmpty())
                        ? errorResponse.message
                        : message;
                Toast.makeText(ResetPasswordActivity.this, displayMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message, int code) {
                onError(message, code, null);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnResetPassword.setEnabled(!loading);
        binding.btnResetPassword.setText(loading ? "" : getString(R.string.action_reset_password));
        binding.progressReset.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.etToken.setEnabled(!loading);
        binding.etNewPassword.setEnabled(!loading);
        binding.etConfirmPassword.setEnabled(!loading);
    }
}
