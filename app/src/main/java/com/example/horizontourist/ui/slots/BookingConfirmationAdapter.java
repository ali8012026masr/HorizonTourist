package com.example.horizontourist.ui.slots;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.ConfirmedBookingItem;
import com.example.horizontourist.databinding.ItemConfirmedBookingBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class BookingConfirmationAdapter extends RecyclerView.Adapter<BookingConfirmationAdapter.ViewHolder> {

    private final List<ConfirmedBookingItem> items = new ArrayList<>();

    public void setItems(List<ConfirmedBookingItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConfirmedBookingBinding binding = ItemConfirmedBookingBinding.inflate(
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
        private final ItemConfirmedBookingBinding binding;

        ViewHolder(ItemConfirmedBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ConfirmedBookingItem item) {
            if (item == null) return;

            long id = item.bookingId != null ? item.bookingId : 0L;
            binding.tvBookingId.setText(itemView.getContext().getString(R.string.booking_id_prefix, id));
            binding.tvBookingAmount.setText(Formatters.formatMoney(item.amount));
            binding.tvBookingTitle.setText(item.title != null ? item.title : "");

            String status = item.status != null ? item.status : "CONFIRMED";
            binding.tvBookingStatus.setText(status);
        }
    }
}
