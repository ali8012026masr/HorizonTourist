package com.example.horizontourist.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.CountResponse;
import com.example.horizontourist.data.model.Tourist;
import com.example.horizontourist.databinding.FragmentProfileBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.ui.BaseFragment;
import com.example.horizontourist.ui.groups.GroupsActivity;
import com.example.horizontourist.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class ProfileFragment extends BaseFragment {

    private FragmentProfileBinding binding;
    private Tourist currentTourist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        setupClickListeners();
        loadProfileData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupViews() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.secondary);
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadProfileData);
    }

    private void setupClickListeners() {
        binding.rowEditProfile.setOnClickListener(v -> {
            if (!isSafe()) {
                return;
            }
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            if (currentTourist != null) {
                intent.putExtra("full_name", currentTourist.getFullName());
                intent.putExtra("phone", currentTourist.getPhone());
                intent.putExtra("address", currentTourist.getAddress());
            }
            startActivity(intent);
        });

        binding.rowChangePassword.setOnClickListener(v -> {
            if (!isSafe()) {
                return;
            }
            startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
        });

        binding.rowNotifications.setOnClickListener(v -> {
            if (!isSafe()) {
                return;
            }
            startActivity(new Intent(requireContext(), NotificationsActivity.class));
        });

        binding.rowMyGroups.setOnClickListener(v -> {
            if (!isSafe()) {
                return;
            }
            startActivity(new Intent(requireContext(), GroupsActivity.class));
        });

        binding.rowLogout.setOnClickListener(v -> showLogoutConfirmation());

        binding.rowDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
    }

    private void loadProfileData() {
        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) {
            if (binding != null) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        if (binding != null && !binding.swipeRefreshLayout.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        // 1. Fetch Profile
        ApiClient.getApiService().getTouristProfile(touristId).enqueue(new ApiCallback<Tourist>() {
            @Override
            public void onSuccess(Tourist tourist) {
                if (!isSafe()) {
                    return;
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);

                if (tourist != null) {
                    currentTourist = tourist;
                    bindTouristData(tourist);
                }
            }

            @Override
            public void onError(String message, int code) {
                if (!isSafe()) {
                    return;
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                toast(message);
            }
        });

        // 2. Fetch Unread Notifications Count
        ApiClient.getApiService().getUnreadNotificationCount(touristId).enqueue(new ApiCallback<CountResponse>() {
            @Override
            public void onSuccess(CountResponse countResponse) {
                if (!isSafe()) {
                    return;
                }
                if (countResponse != null && countResponse.count > 0) {
                    binding.tvNotificationBadge.setText(getString(R.string.unread_notifications_badge, countResponse.count));
                    binding.tvNotificationBadge.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNotificationBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message, int code) {
                if (!isSafe()) {
                    return;
                }
                binding.tvNotificationBadge.setVisibility(View.GONE);
            }
        });
    }

    private void bindTouristData(@NonNull Tourist tourist) {
        String fullName = tourist.getFullName();
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = SessionManager.getInstance().getFullName();
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "Tourist";
        } else {
            SessionManager.getInstance().updateProfile(fullName);
        }

        binding.tvFullName.setText(fullName);
        binding.tvAvatarInitials.setText(computeInitials(fullName));

        String email = tourist.getEmail();
        if (email == null || email.trim().isEmpty()) {
            email = SessionManager.getInstance().getEmail();
        }
        binding.tvEmail.setText(email != null ? email : "");

        String status = tourist.getStatus();
        if (status != null && !status.trim().isEmpty()) {
            binding.tvStatus.setText(status.toUpperCase(Locale.US));
            binding.tvStatus.setVisibility(View.VISIBLE);
        } else {
            binding.tvStatus.setVisibility(View.GONE);
        }

        int points = tourist.getLoyaltyPoints();
        binding.tvLoyaltyPoints.setText(getString(R.string.loyalty_points_format, points));

        String phone = tourist.getPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            binding.tvPhone.setText(phone);
        } else {
            binding.tvPhone.setText(R.string.no_phone_set);
        }

        String address = tourist.getAddress();
        if (address != null && !address.trim().isEmpty()) {
            binding.tvAddress.setText(address);
        } else {
            binding.tvAddress.setText(R.string.no_address_set);
        }
    }

    private String computeInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "T";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            String single = parts[0];
            return single.substring(0, Math.min(2, single.length())).toUpperCase(Locale.US);
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase(Locale.US);
    }

    private void showLogoutConfirmation() {
        if (!isSafe()) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_msg)
                .setPositiveButton(R.string.btn_logout_confirm, (dialog, which) -> {
                    if (getActivity() instanceof BaseActivity) {
                        ((BaseActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteAccountConfirmation() {
        if (!isSafe()) {
            return;
        }
        // First confirmation
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_account_confirm_title)
                .setMessage(R.string.delete_account_confirm_msg)
                .setPositiveButton(R.string.ok, (dialog, which) -> showDoubleDeleteAccountConfirmation())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDoubleDeleteAccountConfirmation() {
        if (!isSafe()) {
            return;
        }
        // Second double confirmation
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_account_double_confirm_title)
                .setMessage(R.string.delete_account_double_confirm_msg)
                .setPositiveButton(R.string.btn_confirm_delete, (dialog, which) -> executeDeleteAccount())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executeDeleteAccount() {
        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) {
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().deleteTouristAccount(touristId).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                if (!isSafe()) {
                    return;
                }
                binding.progressBar.setVisibility(View.GONE);
                toast(R.string.delete_account_success);
                if (getActivity() instanceof BaseActivity) {
                    ((BaseActivity) getActivity()).logout();
                }
            }

            @Override
            public void onError(String message, int code) {
                if (!isSafe()) {
                    return;
                }
                binding.progressBar.setVisibility(View.GONE);
                toast(message);
            }
        });
    }
}
