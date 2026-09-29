package com.example.horizontourist.ui.slots;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.ServiceSlot;
import com.example.horizontourist.databinding.ItemSlotBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    public interface OnSlotClickListener {
        void onSlotClick(ServiceSlot slot);
    }

    private final List<ServiceSlot> slots = new ArrayList<>();
    private final OnSlotClickListener listener;

    public SlotAdapter(OnSlotClickListener listener) {
        this.listener = listener;
    }

    public void setSlots(List<ServiceSlot> newSlots) {
        slots.clear();
        if (newSlots != null) {
            slots.addAll(newSlots);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSlotBinding binding = ItemSlotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SlotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        holder.bind(slots.get(position));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    class SlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemSlotBinding binding;

        SlotViewHolder(ItemSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ServiceSlot slot) {
            Context context = itemView.getContext();

            // Category Icon & Label
            int iconRes = Formatters.getCategoryIconRes(slot.category);
            binding.ivCategoryIcon.setImageResource(iconRes);
            binding.tvCategoryName.setText(Formatters.capitalizeEnum(slot.category));

            // Status Chip
            String status = slot.status != null ? slot.status.toUpperCase(Locale.US) : "";
            binding.tvStatus.setText(status);
            if (Constants.SLOT_STATUS_OPEN.equalsIgnoreCase(status)) {
                binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.success)));
            } else if (Constants.SLOT_STATUS_FULL.equalsIgnoreCase(status)) {
                binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning)));
            } else {
                binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error)));
            }

            // Route or Location Title
            if (slot.origin != null && !slot.origin.trim().isEmpty() && slot.destination != null && !slot.destination.trim().isEmpty()) {
                binding.tvRouteTitle.setText(context.getString(R.string.route_format, slot.origin.trim(), slot.destination.trim()));
            } else if (slot.locationName != null && !slot.locationName.trim().isEmpty()) {
                binding.tvRouteTitle.setText(slot.locationName.trim());
            } else {
                binding.tvRouteTitle.setText(Formatters.capitalizeEnum(slot.category));
            }

            // Provider Name & Rating
            binding.tvProviderName.setText(slot.providerBusinessName != null ? slot.providerBusinessName : "");
            if (slot.providerRatingAvg != null && slot.providerRatingAvg > 0) {
                int count = slot.providerRatingCount != null ? slot.providerRatingCount : 0;
                binding.tvProviderRating.setText(context.getString(R.string.rating_score_format, slot.providerRatingAvg, count));
                binding.tvProviderRating.setVisibility(View.VISIBLE);
            } else {
                binding.tvProviderRating.setVisibility(View.GONE);
            }

            // Schedule Timings
            String start = Formatters.formatDateTime(slot.startDateTime);
            String end = Formatters.formatDateTime(slot.endDateTime);
            binding.tvScheduleTime.setText(context.getString(R.string.route_format, start, end));

            // Price
            binding.tvPrice.setText(Formatters.formatMoney(slot.price));

            // Available Seats
            int seats = slot.availableSeats != null ? slot.availableSeats : 0;
            if (seats > 0) {
                binding.tvSeatsLeft.setText(context.getString(R.string.seats_left, seats));
                if (seats > 5) {
                    binding.tvSeatsLeft.setTextColor(ContextCompat.getColor(context, R.color.success));
                } else {
                    binding.tvSeatsLeft.setTextColor(ContextCompat.getColor(context, R.color.warning));
                }
            } else {
                binding.tvSeatsLeft.setText(R.string.no_seats);
                binding.tvSeatsLeft.setTextColor(ContextCompat.getColor(context, R.color.error));
            }

            // Interaction state: only OPEN slots have enabled card click action
            boolean isOpen = slot.isOpen();
            itemView.setEnabled(isOpen);
            itemView.setAlpha(isOpen ? 1.0f : 0.6f);

            itemView.setOnClickListener(v -> {
                if (isOpen && listener != null) {
                    listener.onSlotClick(slot);
                }
            });
        }
    }
}
