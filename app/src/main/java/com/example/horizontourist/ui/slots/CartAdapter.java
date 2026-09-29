package com.example.horizontourist.ui.slots;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.CartItem;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.databinding.ItemCartBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartItemListener {
        void onRemoveItem(CartItem item);
        void onItemClick(CartItem item);
    }

    private final List<CartItem> items = new ArrayList<>();
    private final OnCartItemListener listener;

    public CartAdapter(OnCartItemListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CartItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
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
        private final ItemCartBinding binding;

        ViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item) {
            if (item == null) return;
            Context context = itemView.getContext();

            binding.ivCategoryIcon.setImageResource(Formatters.getCategoryIconRes(item.category));
            binding.tvTitle.setText(item.title != null ? item.title : "");
            binding.tvPrice.setText(Formatters.formatMoney(item.price));

            if (item.startDateTime != null && !item.startDateTime.trim().isEmpty()) {
                binding.tvSchedule.setText(Formatters.formatDateTime(item.startDateTime));
                binding.tvSchedule.setVisibility(View.VISIBLE);
            } else {
                binding.tvSchedule.setVisibility(View.GONE);
            }

            // Check if status is known and unavailable
            boolean isUnavailable = false;
            if (item.status != null && !Constants.SLOT_STATUS_OPEN.equalsIgnoreCase(item.status)) {
                isUnavailable = true;
            } else if (item.availableSeats != null && item.availableSeats <= 0) {
                isUnavailable = true;
            }

            if (isUnavailable) {
                binding.tvWarning.setVisibility(View.VISIBLE);
                binding.tvWarning.setText(R.string.cart_item_unavailable);
                binding.tvWarning.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error)));
            } else {
                binding.tvWarning.setVisibility(View.GONE);
            }

            binding.btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(item);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
