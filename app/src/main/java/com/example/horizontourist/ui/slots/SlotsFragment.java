package com.example.horizontourist.ui.slots;

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
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.ServiceSlot;
import com.example.horizontourist.databinding.BottomSheetSlotFilterBinding;
import com.example.horizontourist.databinding.FragmentSlotsBinding;
import com.example.horizontourist.ui.BaseFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SlotsFragment extends BaseFragment {

    private FragmentSlotsBinding binding;
    private SlotAdapter adapter;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Filter state
    private String currentSearchText = "";
    private String filterCategory = null;
    private String filterOrigin = null;
    private String filterDestination = null;
    private Double filterMinPrice = null;
    private Double filterMaxPrice = null;
    private String filterDateFrom = null;
    private String filterDateTo = null;
    private String displayDateFrom = null;
    private String displayDateTo = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSlotsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchAndFilter();

        boolean filtered = false;
        if (getActivity() != null && getActivity().getIntent() != null) {
            filtered = handleIntentFilters(getActivity().getIntent());
        }
        if (!filtered) {
            loadSlots();
        }
    }

    public boolean handleIntentFilters(Intent intent) {
        if (intent == null) {
            return false;
        }
        boolean hasFilter = false;
        if (intent.hasExtra("category")) {
            filterCategory = intent.getStringExtra("category");
            hasFilter = true;
        }
        if (intent.hasExtra("origin")) {
            filterOrigin = intent.getStringExtra("origin");
            hasFilter = true;
        }
        if (intent.hasExtra("destination")) {
            filterDestination = intent.getStringExtra("destination");
            hasFilter = true;
        }
        if (intent.hasExtra("locationName")) {
            currentSearchText = intent.getStringExtra("locationName");
            if (binding != null && binding.etSearch != null) {
                binding.etSearch.setText(currentSearchText);
            }
            hasFilter = true;
        }
        if (intent.hasExtra("minPrice")) {
            try {
                double p = intent.getDoubleExtra("minPrice", -1.0);
                if (p >= 0) {
                    filterMinPrice = p;
                } else {
                    String str = intent.getStringExtra("minPrice");
                    if (str != null) filterMinPrice = Double.parseDouble(str);
                }
                hasFilter = true;
            } catch (Exception ignored) {}
        }
        if (intent.hasExtra("maxPrice")) {
            try {
                double p = intent.getDoubleExtra("maxPrice", -1.0);
                if (p >= 0) {
                    filterMaxPrice = p;
                } else {
                    String str = intent.getStringExtra("maxPrice");
                    if (str != null) filterMaxPrice = Double.parseDouble(str);
                }
                hasFilter = true;
            } catch (Exception ignored) {}
        }

        if (hasFilter) {
            loadSlots();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new SlotAdapter(slot -> {
            if (!isSafe() || slot == null || slot.id == null) {
                return;
            }
            Intent intent = new Intent(requireContext(), SlotDetailActivity.class);
            intent.putExtra(SlotDetailActivity.EXTRA_SLOT_ID, slot.id);
            startActivity(intent);
        });

        binding.rvSlots.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSlots.setAdapter(adapter);

        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary);
        binding.swipeRefresh.setOnRefreshListener(this::loadSlots);
        binding.btnRetry.setOnClickListener(v -> loadSlots());
    }

    private void setupSearchAndFilter() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    currentSearchText = s != null ? s.toString().trim() : "";
                    loadSlots();
                };
                searchHandler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnFilter.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        BottomSheetSlotFilterBinding filterBinding = BottomSheetSlotFilterBinding.inflate(getLayoutInflater());
        dialog.setContentView(filterBinding.getRoot());

        // Restore category chip
        restoreCategoryChip(filterBinding);

        // Restore texts
        if (filterOrigin != null) {
            filterBinding.etFilterOrigin.setText(filterOrigin);
        }
        if (filterDestination != null) {
            filterBinding.etFilterDestination.setText(filterDestination);
        }
        if (filterMinPrice != null) {
            filterBinding.etFilterMinPrice.setText(String.valueOf(filterMinPrice));
        }
        if (filterMaxPrice != null) {
            filterBinding.etFilterMaxPrice.setText(String.valueOf(filterMaxPrice));
        }
        if (displayDateFrom != null) {
            filterBinding.btnFilterDateFrom.setText(displayDateFrom);
        }
        if (displayDateTo != null) {
            filterBinding.btnFilterDateTo.setText(displayDateTo);
        }

        // Date pickers
        filterBinding.btnFilterDateFrom.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.select_date_from)
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00", Locale.US);
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
                Date date = new Date(selection);
                filterDateFrom = isoFormat.format(date);
                displayDateFrom = displayFormat.format(date);
                filterBinding.btnFilterDateFrom.setText(displayDateFrom);
            });
            picker.show(getChildFragmentManager(), "date_from_picker");
        });

        filterBinding.btnFilterDateTo.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.select_date_to)
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'23:59:59", Locale.US);
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
                Date date = new Date(selection);
                filterDateTo = isoFormat.format(date);
                displayDateTo = displayFormat.format(date);
                filterBinding.btnFilterDateTo.setText(displayDateTo);
            });
            picker.show(getChildFragmentManager(), "date_to_picker");
        });

        // Clear action
        filterBinding.btnClearFilter.setOnClickListener(v -> {
            filterCategory = null;
            filterOrigin = null;
            filterDestination = null;
            filterMinPrice = null;
            filterMaxPrice = null;
            filterDateFrom = null;
            filterDateTo = null;
            displayDateFrom = null;
            displayDateTo = null;
            dialog.dismiss();
            loadSlots();
        });

        // Apply action
        filterBinding.btnApplyFilter.setOnClickListener(v -> {
            int checkedId = filterBinding.chipGroupCategory.getCheckedChipId();
            filterCategory = getCategoryFromChipId(checkedId);

            String originText = filterBinding.etFilterOrigin.getText() != null ? filterBinding.etFilterOrigin.getText().toString().trim() : "";
            filterOrigin = !originText.isEmpty() ? originText : null;

            String destText = filterBinding.etFilterDestination.getText() != null ? filterBinding.etFilterDestination.getText().toString().trim() : "";
            filterDestination = !destText.isEmpty() ? destText : null;

            String minText = filterBinding.etFilterMinPrice.getText() != null ? filterBinding.etFilterMinPrice.getText().toString().trim() : "";
            filterMinPrice = !minText.isEmpty() ? parseDouble(minText) : null;

            String maxText = filterBinding.etFilterMaxPrice.getText() != null ? filterBinding.etFilterMaxPrice.getText().toString().trim() : "";
            filterMaxPrice = !maxText.isEmpty() ? parseDouble(maxText) : null;

            dialog.dismiss();
            loadSlots();
        });

        dialog.show();
    }

    private void restoreCategoryChip(BottomSheetSlotFilterBinding fb) {
        if (filterCategory == null) {
            fb.chipCatAll.setChecked(true);
        } else {
            switch (filterCategory) {
                case "BUS": fb.chipCatBus.setChecked(true); break;
                case "MICROBUS": fb.chipCatMicrobus.setChecked(true); break;
                case "TRAIN": fb.chipCatTrain.setChecked(true); break;
                case "LAUNCH": fb.chipCatLaunch.setChecked(true); break;
                case "AIRPLANE": fb.chipCatAirplane.setChecked(true); break;
                case "SHIP": fb.chipCatShip.setChecked(true); break;
                case "HOTEL": fb.chipCatHotel.setChecked(true); break;
                case "RESORT": fb.chipCatResort.setChecked(true); break;
                case "BUFFET": fb.chipCatBuffet.setChecked(true); break;
                case "AMUSEMENT_PARK": fb.chipCatPark.setChecked(true); break;
                default: fb.chipCatAll.setChecked(true); break;
            }
        }
    }

    private String getCategoryFromChipId(int chipId) {
        if (chipId == R.id.chipCatBus) return "BUS";
        if (chipId == R.id.chipCatMicrobus) return "MICROBUS";
        if (chipId == R.id.chipCatTrain) return "TRAIN";
        if (chipId == R.id.chipCatLaunch) return "LAUNCH";
        if (chipId == R.id.chipCatAirplane) return "AIRPLANE";
        if (chipId == R.id.chipCatShip) return "SHIP";
        if (chipId == R.id.chipCatHotel) return "HOTEL";
        if (chipId == R.id.chipCatResort) return "RESORT";
        if (chipId == R.id.chipCatBuffet) return "BUFFET";
        if (chipId == R.id.chipCatPark) return "AMUSEMENT_PARK";
        return null; // All
    }

    private Double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void loadSlots() {
        if (!isSafe()) {
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        // Prepare query parameters
        String searchParam = !currentSearchText.isEmpty() ? currentSearchText : null;
        String originQuery = filterOrigin;
        String destQuery = filterDestination != null ? filterDestination : searchParam;
        String locationQuery = searchParam;

        ApiClient.getApiService().searchSlots(
                filterCategory,
                originQuery,
                destQuery,
                locationQuery,
                filterDateFrom,
                filterDateTo,
                filterMinPrice,
                filterMaxPrice
        ).enqueue(new ApiCallback<List<ServiceSlot>>() {
            @Override
            public void onSuccess(List<ServiceSlot> slots) {
                if (!isSafe()) {
                    return;
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (slots == null || slots.isEmpty()) {
                    adapter.setSlots(null);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    adapter.setSlots(slots);
                    binding.layoutEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message, int code) {
                if (!isSafe()) {
                    return;
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvErrorMessage.setText(message);
            }
        });
    }
}
