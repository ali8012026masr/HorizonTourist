package com.example.horizontourist.ui.slots;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.databinding.ItemCheckoutSummaryBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder> {

    public static class ItemState {
        public final Long slotId;
        public final String title;
        public final double price;
        public Long createdBookingId;
        public boolean bookingDone;
        public boolean paymentDone;
        public String statusText;
        public boolean isError;

        public ItemState(Long slotId, String title, double price) {
            this.slotId = slotId;
            this.title = title;
            this.price = price;
        }
    }

    private final List<ItemState> items = new ArrayList<>();

    public void setItems(List<ItemState> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<ItemState> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCheckoutSummaryBinding binding = ItemCheckoutSummaryBinding.inflate(
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
        private final ItemCheckoutSummaryBinding binding;

        ViewHolder(ItemCheckoutSummaryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ItemState item) {
            if (item == null) return;
            Context context = itemView.getContext();

            binding.tvItemTitle.setText(item.title != null ? item.title : "");
            binding.tvItemPrice.setText(Formatters.formatMoney(item.price));

            if (item.statusText != null && !item.statusText.trim().isEmpty()) {
                binding.tvItemStatus.setVisibility(View.VISIBLE);
                binding.tvItemStatus.setText(item.statusText);
                if (item.isError) {
                    binding.tvItemStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
                } else if (item.paymentDone) {
                    binding.tvItemStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
                } else {
                    binding.tvItemStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
                }
            } else {
                binding.tvItemStatus.setVisibility(View.GONE);
            }
        }
    }
}
