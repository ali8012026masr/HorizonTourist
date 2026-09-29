package com.example.horizontourist.ui.slots;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.data.model.ServiceRating;
import com.example.horizontourist.databinding.ItemProviderRatingBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProviderRatingAdapter extends RecyclerView.Adapter<ProviderRatingAdapter.ViewHolder> {

    private final List<ServiceRating> items = new ArrayList<>();

    public void setItems(List<ServiceRating> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProviderRatingBinding binding = ItemProviderRatingBinding.inflate(
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProviderRatingBinding binding;

        ViewHolder(ItemProviderRatingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ServiceRating rating) {
            if (rating == null) return;

            binding.tvRatingScore.setText(String.format(Locale.US, "★ %d / 5", rating.score));
            binding.tvRatingDate.setText(Formatters.formatDate(rating.ratedDate));

            if (rating.comment != null && !rating.comment.trim().isEmpty()) {
                binding.tvRatingComment.setText(rating.comment.trim());
                binding.tvRatingComment.setVisibility(View.VISIBLE);
            } else {
                binding.tvRatingComment.setVisibility(View.GONE);
            }
        }
    }
}
