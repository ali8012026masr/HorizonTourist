package com.example.horizontourist.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.ErrorResponse;
import com.example.horizontourist.data.model.LoginRequest;
import com.example.horizontourist.data.model.LoginResponse;
import com.example.horizontourist.databinding.ActivityLoginBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.ui.main.MainActivity;
import com.example.horizontourist.util.SessionManager;
import com.example.horizontourist.util.Validators;

public class LoginActivity extends BaseActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Prefill email if passed from Registration or Reset
        String prefillEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        if (prefillEmail != null && !prefillEmail.trim().isEmpty() && binding.etEmail.getText() != null && binding.etEmail.getText().toString().isEmpty()) {
            binding.etEmail.setText(prefillEmail.trim());
            binding.etPassword.requestFocus();
        }

        binding.btnLogin.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            attemptLogin();
        });

        binding.tvRegisterLink.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            String currentEmail = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            if (!currentEmail.isEmpty()) {
                intent.putExtra(EXTRA_EMAIL, currentEmail);
            }
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        boolean cancel = false;

        if (!Validators.isNonEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_password_required));
            binding.tilPassword.requestFocus();
            cancel = true;
        }

        if (!Validators.isNonEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_required));
            binding.tilEmail.requestFocus();
            cancel = true;
        } else if (!Validators.isEmailValid(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            binding.tilEmail.requestFocus();
            cancel = true;
        }

        if (cancel) {
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);
        ApiClient.getApiService().login(request).enqueue(new ApiCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                if (!isSafe()) {
                    return;
                }
                setLoading(false);

                if (response == null || !Constants.ROLE_TOURIST.equalsIgnoreCase(response.role)) {
                    Toast.makeText(LoginActivity.this, R.string.error_tourists_only, Toast.LENGTH_LONG).show();
                    return;
                }

                SessionManager.getInstance().save(response);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message, int code, ErrorResponse errorResponse) {
                if (!isSafe()) {
                    return;
                }
                setLoading(false);

                if (code == 401) {
                    binding.tilPassword.setError(getString(R.string.error_invalid_credentials));
                    Toast.makeText(LoginActivity.this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
                } else if (code == 423) {
                    Toast.makeText(LoginActivity.this, R.string.error_account_locked_retry, Toast.LENGTH_LONG).show();
                } else {
                    String displayMsg = (errorResponse != null && errorResponse.message != null && !errorResponse.message.trim().isEmpty())
                            ? errorResponse.message
                            : message;
                    Toast.makeText(LoginActivity.this, displayMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message, int code) {
                onError(message, code, null);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
        binding.btnLogin.setText(loading ? "" : getString(R.string.action_login));
        binding.progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.etEmail.setEnabled(!loading);
        binding.etPassword.setEnabled(!loading);
        binding.tvRegisterLink.setEnabled(!loading);
        binding.tvForgotPassword.setEnabled(!loading);
    }
}
