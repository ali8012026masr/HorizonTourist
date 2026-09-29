package com.example.horizontourist.ui.guides;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.databinding.ItemGuideBinding;
import com.example.horizontourist.util.Formatters;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.ViewHolder> {

    public interface OnGuideClickListener {
        void onGuideClick(Guide guide);
    }

    private final List<Guide> items = new ArrayList<>();
    private final OnGuideClickListener listener;

    public GuideAdapter(OnGuideClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Guide> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideBinding binding = ItemGuideBinding.inflate(
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
        private final ItemGuideBinding binding;

        ViewHolder(ItemGuideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Guide guide) {
            if (guide == null) return;
            Context context = itemView.getContext();

            // Name
            binding.tvGuideName.setText(guide.fullName != null ? guide.fullName : "");

            // Verified Badge
            boolean isVerified = Constants.VERIFICATION_STATUS_VERIFIED.equalsIgnoreCase(guide.verificationStatus);
            binding.tvVerifiedBadge.setVisibility(isVerified ? View.VISIBLE : View.GONE);
            if (isVerified) {
                binding.tvVerifiedBadge.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
            }

            // Availability Badge
            if (guide.isAvailableNow()) {
                binding.tvAvailabilityBadge.setText(R.string.badge_available);
                binding.tvAvailabilityBadge.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.success)));
            } else {
                binding.tvAvailabilityBadge.setText(R.string.badge_busy);
                binding.tvAvailabilityBadge.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.outline)));
            }

            // Rating
            double rating = guide.ratingAvg != null ? guide.ratingAvg : 0.0;
            int ratingCount = guide.ratingCount != null ? guide.ratingCount : 0;
            binding.tvRating.setText(String.format(Locale.US, "★ %.1f (%d)", rating, ratingCount));

            // Experience
            int exp = guide.experienceYears != null ? guide.experienceYears : 0;
            binding.tvExperience.setText(context.getString(R.string.exp_years_format, exp));

            // Location
            if (guide.location != null && !guide.location.trim().isEmpty()) {
                binding.tvLocation.setText(guide.location);
                binding.tvLocation.setVisibility(View.VISIBLE);
                binding.tvDot2.setVisibility(View.VISIBLE);
            } else {
                binding.tvLocation.setVisibility(View.GONE);
                binding.tvDot2.setVisibility(View.GONE);
            }

            // Languages
            binding.cgLanguages.removeAllViews();
            if (guide.languages != null && !guide.languages.isEmpty()) {
                binding.cgLanguages.setVisibility(View.VISIBLE);
                for (String lang : guide.languages) {
                    if (lang != null && !lang.trim().isEmpty()) {
                        Chip chip = new Chip(context);
                        chip.setText(lang.trim());
                        chip.setCheckable(false);
                        chip.setClickable(false);
                        chip.setEnsureMinTouchTargetSize(false);
                        binding.cgLanguages.addView(chip);
                    }
                }
            } else {
                binding.cgLanguages.setVisibility(View.GONE);
            }

            // Price
            binding.tvPrice.setText(Formatters.formatMoney(guide.defaultPrice));

            // Negotiable Badge
            if (guide.isNegotiable()) {
                binding.tvNegotiableBadge.setText(R.string.badge_negotiable);
                binding.tvNegotiableBadge.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary)));
            } else {
                binding.tvNegotiableBadge.setText(R.string.badge_fixed_price);
                binding.tvNegotiableBadge.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.outline)));
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGuideClick(guide);
                }
            });
        }
    }
}
