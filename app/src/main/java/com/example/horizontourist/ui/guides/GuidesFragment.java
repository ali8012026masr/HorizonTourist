package com.example.horizontourist.ui.guides;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.databinding.FragmentGuidesBinding;
import com.example.horizontourist.ui.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuidesFragment extends BaseFragment implements GuideAdapter.OnGuideClickListener {

    private static final int SORT_BY_RATING = 0;
    private static final int SORT_BY_PRICE = 1;

    private FragmentGuidesBinding binding;
    private GuideAdapter adapter;
    private final List<Guide> allGuides = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private int currentSort = SORT_BY_RATING;
    private String currentQuery = "";
    private Call<List<Guide>> currentCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGuidesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSearchAndFilters();
        setupListeners();
        loadGuides();
    }

    private void setupRecyclerView() {
        adapter = new GuideAdapter(this);
        binding.rvGuides.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGuides.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    currentQuery = (s != null) ? s.toString().trim() : "";
                    applyFilterAndSort();
                };
                searchHandler.postDelayed(searchRunnable, 350);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.chipAvailableNow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadGuides();
        });

        binding.btnSort.setOnClickListener(v -> {
            if (currentSort == SORT_BY_RATING) {
                currentSort = SORT_BY_PRICE;
                binding.btnSort.setText(R.string.sort_price);
            } else {
                currentSort = SORT_BY_RATING;
                binding.btnSort.setText(R.string.sort_rating);
            }
            applyFilterAndSort();
        });
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary);
        binding.swipeRefresh.setOnRefreshListener(this::loadGuides);
        binding.btnRetry.setOnClickListener(v -> loadGuides());
    }

    private void loadGuides() {
        if (currentCall != null) {
            currentCall.cancel();
        }

        boolean availableOnly = binding.chipAvailableNow.isChecked();
        showLoading(true);

        currentCall = ApiClient.getApiService().getGuides(availableOnly);
        currentCall.enqueue(new Callback<List<Guide>>() {
            @Override
            public void onResponse(@NonNull Call<List<Guide>> call, @NonNull Response<List<Guide>> response) {
                if (!isSafe()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    allGuides.clear();
                    allGuides.addAll(response.body());
                    applyFilterAndSort();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    showError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Guide>> call, @NonNull Throwable t) {
                if (!isSafe() || call.isCanceled()) return;
                showLoading(false);
                showError(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void applyFilterAndSort() {
        List<Guide> filtered = new ArrayList<>();
        String query = currentQuery.toLowerCase(Locale.US);

        for (Guide guide : allGuides) {
            if (query.isEmpty() || matchesQuery(guide, query)) {
                filtered.add(guide);
            }
        }

        if (currentSort == SORT_BY_RATING) {
            Collections.sort(filtered, (g1, g2) -> {
                double r1 = g1.ratingAvg != null ? g1.ratingAvg : 0.0;
                double r2 = g2.ratingAvg != null ? g2.ratingAvg : 0.0;
                return Double.compare(r2, r1); // Descending
            });
        } else {
            Collections.sort(filtered, (g1, g2) -> Double.compare(g1.defaultPrice, g2.defaultPrice)); // Ascending
        }

        adapter.setItems(filtered);

        boolean isEmpty = filtered.isEmpty();
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvGuides.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private boolean matchesQuery(Guide guide, String query) {
        if (guide.fullName != null && guide.fullName.toLowerCase(Locale.US).contains(query)) {
            return true;
        }
        if (guide.location != null && guide.location.toLowerCase(Locale.US).contains(query)) {
            return true;
        }
        if (guide.languages != null) {
            for (String lang : guide.languages) {
                if (lang != null && lang.toLowerCase(Locale.US).contains(query)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onGuideClick(Guide guide) {
        if (!isSafe() || guide == null || guide.id == null) return;
        Intent intent = new Intent(requireContext(), GuideDetailActivity.class);
        intent.putExtra(GuideDetailActivity.EXTRA_GUIDE_ID, guide.id);
        startActivity(intent);
    }

    @Override
    public void showLoading(boolean show) {
        if (binding == null) return;
        binding.swipeRefresh.setRefreshing(show);
        if (show) {
            binding.layoutError.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError(String message) {
        if (binding == null) return;
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message != null ? message : getString(R.string.error_unexpected));
        binding.rvGuides.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        if (currentCall != null) {
            currentCall.cancel();
        }
        binding = null;
    }
}
