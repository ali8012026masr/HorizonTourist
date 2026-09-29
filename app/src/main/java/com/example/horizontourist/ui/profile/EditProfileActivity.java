package com.example.horizontourist.ui.profile;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.Tourist;
import com.example.horizontourist.data.model.UpdateTouristRequest;
import com.example.horizontourist.databinding.ActivityEditProfileBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.SessionManager;
import com.example.horizontourist.util.Validators;

public class EditProfileActivity extends BaseActivity {

    private ActivityEditProfileBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        prefillFromIntent();
        loadFreshProfile();
        setupClickListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void prefillFromIntent() {
        String name = getIntent().getStringExtra("full_name");
        String phone = getIntent().getStringExtra("phone");
        String address = getIntent().getStringExtra("address");

        if (name != null) {
            binding.etFullName.setText(name);
        } else {
            binding.etFullName.setText(SessionManager.getInstance().getFullName());
        }

        if (phone != null) {
            binding.etPhone.setText(phone);
        }

        if (address != null) {
            binding.etAddress.setText(address);
        }
    }

    private void loadFreshProfile() {
        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) {
            return;
        }

        ApiClient.getApiService().getTouristProfile(touristId).enqueue(new ApiCallback<Tourist>() {
            @Override
            public void onSuccess(Tourist tourist) {
                if (!isSafe() || tourist == null) {
                    return;
                }
                if (tourist.getFullName() != null) {
                    binding.etFullName.setText(tourist.getFullName());
                }
                if (tourist.getPhone() != null) {
                    binding.etPhone.setText(tourist.getPhone());
                }
                if (tourist.getAddress() != null) {
                    binding.etAddress.setText(tourist.getAddress());
                }
            }

            @Override
            public void onError(String message, int code) {
                // Non-fatal if prefilled
            }
        });
    }

    private void setupClickListeners() {
        binding.btnSaveProfile.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            saveProfile();
        });
    }

    private void saveProfile() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : "";

        binding.tilFullName.setError(null);

        if (!Validators.isNonEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.error_name_required));
            binding.etFullName.requestFocus();
            return;
        }

        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) {
            toast(getString(R.string.error_unauthorized));
            return;
        }

        setLoading(true);

        UpdateTouristRequest request = new UpdateTouristRequest(fullName, phone, address);
        ApiClient.getApiService().updateTouristProfile(touristId, request).enqueue(new ApiCallback<Tourist>() {
            @Override
            public void onSuccess(Tourist updatedTourist) {
                if (!isSafe()) {
                    return;
                }
                setLoading(false);
                SessionManager.getInstance().updateProfile(fullName);
                toast(getString(R.string.edit_profile_success));
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String message, int code) {
                if (!isSafe()) {
                    return;
                }
                setLoading(false);
                toast(message);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSaveProfile.setEnabled(!loading);
        binding.etFullName.setEnabled(!loading);
        binding.etPhone.setEnabled(!loading);
        binding.etAddress.setEnabled(!loading);
    }
}
