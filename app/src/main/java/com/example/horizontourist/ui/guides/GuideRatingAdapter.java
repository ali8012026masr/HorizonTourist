package com.example.horizontourist.ui.guides;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.data.model.GuideRating;
import com.example.horizontourist.databinding.ItemGuideRatingBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuideRatingAdapter extends RecyclerView.Adapter<GuideRatingAdapter.ViewHolder> {

    private final List<GuideRating> items = new ArrayList<>();

    public void setItems(List<GuideRating> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideRatingBinding binding = ItemGuideRatingBinding.inflate(
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
        private final ItemGuideRatingBinding binding;

        ViewHolder(ItemGuideRatingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GuideRating rating) {
            if (rating == null) return;

            // Score
            binding.tvRatingScore.setText(String.format(Locale.US, "★ %d / 5", rating.score));

            // Date
            if (rating.ratedDate != null && !rating.ratedDate.trim().isEmpty()) {
                binding.tvRatingDate.setText(Formatters.formatDate(rating.ratedDate));
            } else {
                binding.tvRatingDate.setText("");
            }

            // Comment
            if (rating.comment != null && !rating.comment.trim().isEmpty()) {
                binding.tvRatingComment.setText(rating.comment);
            } else {
                binding.tvRatingComment.setText("");
            }
        }
    }
}
