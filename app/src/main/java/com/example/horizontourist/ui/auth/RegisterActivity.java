package com.example.horizontourist.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.ErrorResponse;
import com.example.horizontourist.data.model.RegisterTouristRequest;
import com.example.horizontourist.data.model.Tourist;
import com.example.horizontourist.databinding.ActivityRegisterBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.Validators;

import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            attemptRegister();
        });

        binding.tvLoginLink.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            finish();
        });
    }

    private void attemptRegister() {
        clearErrors();

        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        boolean cancel = false;
        View focusView = null;

        if (!Validators.isNonEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_password_required));
            focusView = binding.tilConfirmPassword;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_do_not_match));
            focusView = binding.tilConfirmPassword;
            cancel = true;
        }

        if (!Validators.isNonEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_password_required));
            focusView = binding.tilPassword;
            cancel = true;
        } else if (!Validators.isPasswordValid(password)) {
            binding.tilPassword.setError(getString(R.string.error_password_length));
            focusView = binding.tilPassword;
            cancel = true;
        }

        if (!Validators.isNonEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_required));
            focusView = binding.tilEmail;
            cancel = true;
        } else if (!Validators.isEmailValid(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            focusView = binding.tilEmail;
            cancel = true;
        }

        if (!Validators.isNonEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.error_name_required));
            focusView = binding.tilFullName;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        setLoading(true);

        RegisterTouristRequest request = new RegisterTouristRequest(
                fullName,
                email,
                password,
                phone.isEmpty() ? null : phone,
                address.isEmpty() ? null : address
        );

        ApiClient.getApiService().registerTourist(request).enqueue(new ApiCallback<Tourist>() {
            @Override
            public void onSuccess(Tourist response) {
                if (!isSafe()) {
                    return;
                }
                setLoading(false);
                Toast.makeText(RegisterActivity.this, R.string.register_success, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra(LoginActivity.EXTRA_EMAIL, email);
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

                if (code == 409) {
                    binding.tilEmail.setError(getString(R.string.error_email_already_registered));
                    binding.tilEmail.requestFocus();
                    return;
                }

                boolean errorShown = false;
                if (errorResponse != null && errorResponse.errors != null && !errorResponse.errors.isEmpty()) {
                    for (Map.Entry<String, String> entry : errorResponse.errors.entrySet()) {
                        String field = entry.getKey().toLowerCase(Locale.US);
                        String errorMsg = entry.getValue();

                        if (field.contains("email")) {
                            binding.tilEmail.setError(errorMsg);
                            errorShown = true;
                        } else if (field.contains("name")) {
                            binding.tilFullName.setError(errorMsg);
                            errorShown = true;
                        } else if (field.contains("password")) {
                            binding.tilPassword.setError(errorMsg);
                            errorShown = true;
                        } else if (field.contains("phone")) {
                            binding.tilPhone.setError(errorMsg);
                            errorShown = true;
                        } else if (field.contains("address")) {
                            binding.tilAddress.setError(errorMsg);
                            errorShown = true;
                        }
                    }
                }

                if (!errorShown) {
                    String displayMsg = (errorResponse != null && errorResponse.message != null && !errorResponse.message.trim().isEmpty())
                            ? errorResponse.message
                            : message;
                    Toast.makeText(RegisterActivity.this, displayMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message, int code) {
                onError(message, code, null);
            }
        });
    }

    private void clearErrors() {
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilAddress.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    private void setLoading(boolean loading) {
        binding.btnRegister.setEnabled(!loading);
        binding.btnRegister.setText(loading ? "" : getString(R.string.action_register));
        binding.progressRegister.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.etFullName.setEnabled(!loading);
        binding.etEmail.setEnabled(!loading);
        binding.etPhone.setEnabled(!loading);
        binding.etAddress.setEnabled(!loading);
        binding.etPassword.setEnabled(!loading);
        binding.etConfirmPassword.setEnabled(!loading);
        binding.tvLoginLink.setEnabled(!loading);
    }
}
