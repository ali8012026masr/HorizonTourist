package com.example.horizontourist.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.ErrorResponse;
import com.example.horizontourist.data.model.ForgotPasswordRequest;
import com.example.horizontourist.data.model.MessageResponse;
import com.example.horizontourist.databinding.ActivityForgotPasswordBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.Validators;

public class ForgotPasswordActivity extends BaseActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String prefillEmail = getIntent().getStringExtra(LoginActivity.EXTRA_EMAIL);
        if (prefillEmail != null && !prefillEmail.trim().isEmpty() && binding.etEmail.getText() != null) {
            binding.etEmail.setText(prefillEmail.trim());
        }

        binding.btnSendResetToken.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            attemptForgotPassword();
        });

        binding.btnGoToReset.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void attemptForgotPassword() {
        binding.tilEmail.setError(null);

        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";

        if (!Validators.isNonEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_required));
            binding.tilEmail.requestFocus();
            return;
        }

        if (!Validators.isEmailValid(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            binding.tilEmail.requestFocus();
            return;
        }

        setLoading(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        ApiClient.getApiService().forgotPassword(request).enqueue(new ApiCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse response) {
                if (!isSafe()) {
                    return;
                }
                setLoading(false);
                String msg = (response != null && response.message != null && !response.message.trim().isEmpty())
                        ? response.message
                        : getString(R.string.forgot_password_instructions);

                binding.cardMessage.setVisibility(View.VISIBLE);
                binding.tvMessage.setText(msg);
                Toast.makeText(ForgotPasswordActivity.this, msg, Toast.LENGTH_LONG).show();
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
                Toast.makeText(ForgotPasswordActivity.this, displayMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message, int code) {
                onError(message, code, null);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnSendResetToken.setEnabled(!loading);
        binding.btnSendResetToken.setText(loading ? "" : getString(R.string.action_send_reset_token));
        binding.progressForgot.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.etEmail.setEnabled(!loading);
    }
}
