package com.example.horizontourist.ui.guides;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.GuideAvailability;
import com.example.horizontourist.databinding.ItemGuideAvailabilityBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class GuideAvailabilityAdapter extends RecyclerView.Adapter<GuideAvailabilityAdapter.ViewHolder> {

    public interface OnAvailabilitySelectedListener {
        void onAvailabilitySelected(GuideAvailability availability);
    }

    private final List<GuideAvailability> items = new ArrayList<>();
    private final OnAvailabilitySelectedListener listener;
    private Long selectedId = null;

    public GuideAvailabilityAdapter(OnAvailabilitySelectedListener listener) {
        this.listener = listener;
    }

    public void setItems(List<GuideAvailability> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setSelectedId(Long id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    public Long getSelectedId() {
        return selectedId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideAvailabilityBinding binding = ItemGuideAvailabilityBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuideAvailabilityBinding binding;

        ViewHolder(ItemGuideAvailabilityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GuideAvailability item) {
            if (item == null) return;
            Context context = itemView.getContext();

            // Date
            String dateFormatted = item.date != null ? Formatters.formatDate(item.date) : "";
            binding.tvSlotDate.setText(dateFormatted);

            // Time
            String start = item.startTime != null ? item.startTime : "";
            String end = item.endTime != null ? item.endTime : "";
            if (!start.isEmpty() || !end.isEmpty()) {
                binding.tvSlotTime.setText(String.format("%s - %s", start, end));
                binding.tvSlotTime.setVisibility(View.VISIBLE);
            } else {
                binding.tvSlotTime.setVisibility(View.GONE);
            }

            // Location
            if (item.location != null && !item.location.trim().isEmpty()) {
                binding.tvSlotLocation.setText(item.location);
                binding.tvSlotLocation.setVisibility(View.VISIBLE);
            } else {
                binding.tvSlotLocation.setVisibility(View.GONE);
            }

            // Notes
            if (item.notes != null && !item.notes.trim().isEmpty()) {
                binding.tvSlotNotes.setText(item.notes);
                binding.tvSlotNotes.setVisibility(View.VISIBLE);
            } else {
                binding.tvSlotNotes.setVisibility(View.GONE);
            }

            // Selection state
            boolean isSelected = selectedId != null && selectedId.equals(item.id);
            if (isSelected) {
                binding.getRoot().setStrokeColor(context.getColor(R.color.primary));
                binding.getRoot().setStrokeWidth(4);
            } else {
                binding.getRoot().setStrokeColor(context.getColor(R.color.divider));
                binding.getRoot().setStrokeWidth(2);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    selectedId = item.id;
                    notifyDataSetChanged();
                    listener.onAvailabilitySelected(item);
                }
            });
        }
    }
}
