package com.example.horizontourist.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.CountResponse;
import com.example.horizontourist.data.model.Tourist;
import com.example.horizontourist.databinding.FragmentHomeBinding;
import com.example.horizontourist.ui.BaseFragment;
import com.example.horizontourist.ui.ai.AiConciergeActivity;
import com.example.horizontourist.ui.groups.GroupsActivity;
import com.example.horizontourist.ui.profile.NotificationsActivity;
import com.example.horizontourist.ui.slots.CartActivity;
import com.example.horizontourist.util.CartManager;
import com.example.horizontourist.util.SessionManager;

public class HomeFragment extends BaseFragment {

    private FragmentHomeBinding binding;
    private final CartManager.OnCartChangeListener cartChangeListener = this::updateCartBadge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        setupClickListeners();
        loadHomeData();
    }

    @Override
    public void onResume() {
        super.onResume();
        CartManager.getInstance().addListener(cartChangeListener);
        updateCartBadge(CartManager.getInstance().count());
        updateGreeting();
        loadHomeData();
    }

    @Override
    public void onPause() {
        super.onPause();
        CartManager.getInstance().removeListener(cartChangeListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupViews() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.secondary);
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadHomeData);
        updateGreeting();
    }

    private void updateGreeting() {
        if (!isSafe()) {
            return;
        }
        String name = SessionManager.getInstance().getFullName();
        if (name == null || name.trim().isEmpty()) {
            name = SessionManager.getInstance().getEmail();
        }
        binding.tvGreetingName.setText(name != null ? name : "");
    }

    private void updateCartBadge(int count) {
        if (!isSafe()) {
            return;
        }
        if (count > 0) {
            binding.tvCartBadge.setText(String.valueOf(count));
            binding.tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            binding.tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Quick Action 1: Browse Services -> Explore tab
        binding.cardBrowseServices.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.navigation_explore);
            }
        });

        // Quick Action 2: Find a Guide -> Guides tab
        binding.cardFindGuide.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.navigation_guides);
            }
        });

        // Quick Action 3: My Bookings -> Bookings tab
        binding.cardMyBookings.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.navigation_bookings);
            }
        });

        // Quick Action 4: My Groups
        binding.cardMyGroups.setOnClickListener(v -> {
            if (isSafe()) {
                startActivity(new Intent(requireContext(), GroupsActivity.class));
            }
        });

        // Quick Action 5: Cart
        View.OnClickListener openCart = v -> {
            if (isSafe()) {
                startActivity(new Intent(requireContext(), CartActivity.class));
            }
        };
        binding.cardCart.setOnClickListener(openCart);
        binding.btnCart.setOnClickListener(openCart);

        // Quick Action 6: AI Concierge Card & Floating Entry Point
        View.OnClickListener openAiConcierge = v -> {
            if (isSafe()) {
                startActivity(new Intent(requireContext(), AiConciergeActivity.class));
            }
        };
        binding.cardAiConcierge.setOnClickListener(openAiConcierge);
        binding.fabAiConcierge.setOnClickListener(openAiConcierge);

        // Header Notifications Button
        binding.btnNotifications.setOnClickListener(v -> {
            if (isSafe()) {
                startActivity(new Intent(requireContext(), NotificationsActivity.class));
            }
        });
    }

    private void loadHomeData() {
        long touristId = SessionManager.getInstance().getTouristId();
        if (touristId <= 0) {
            binding.swipeRefreshLayout.setRefreshing(false);
            return;
        }

        // 1. Fetch Profile / Loyalty Points
        ApiClient.getApiService().getTouristProfile(touristId).enqueue(new ApiCallback<Tourist>() {
            @Override
            public void onSuccess(Tourist tourist) {
                if (!isSafe()) {
                    return;
                }
                binding.swipeRefreshLayout.setRefreshing(false);
                if (tourist != null) {
                    if (tourist.fullName != null && !tourist.fullName.isEmpty()) {
                        SessionManager.getInstance().updateProfile(tourist.fullName);
                        binding.tvGreetingName.setText(tourist.fullName);
                    }
                    int points = tourist.getLoyaltyPoints();
                    binding.tvLoyaltyPoints.setText(getString(R.string.loyalty_points_format, points));
                }
            }

            @Override
            public void onError(String message, int code) {
                if (!isSafe()) {
                    return;
                }
                binding.swipeRefreshLayout.setRefreshing(false);
                // Non-fatal on home screen load
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
                    binding.tvNotificationBadge.setText(String.valueOf(countResponse.count));
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
}
