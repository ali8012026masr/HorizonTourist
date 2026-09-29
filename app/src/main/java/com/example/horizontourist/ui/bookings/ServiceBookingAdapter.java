package com.example.horizontourist.ui.bookings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.Booking;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.ServiceSlot;
import com.example.horizontourist.databinding.ItemServiceBookingBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceBookingAdapter extends RecyclerView.Adapter<ServiceBookingAdapter.ViewHolder> {

    public interface OnServiceBookingListener {
        void onBookingClick(Booking booking);
        void onNeedSlot(Long slotId);
    }

    private final List<Booking> items = new ArrayList<>();
    private final Map<Long, ServiceSlot> slotCache = new HashMap<>();
    private final OnServiceBookingListener listener;

    public ServiceBookingAdapter(OnServiceBookingListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Booking> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void putSlot(Long slotId, ServiceSlot slot) {
        if (slotId == null || slot == null) return;
        slotCache.put(slotId, slot);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceBookingBinding binding = ItemServiceBookingBinding.inflate(
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
        private final ItemServiceBookingBinding binding;

        ViewHolder(ItemServiceBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Booking booking) {
            if (booking == null) return;
            Context context = itemView.getContext();

            long id = booking.id != null ? booking.id : 0L;
            binding.tvBookingId.setText(context.getString(R.string.booking_id_prefix, id));

            if (booking.bookingDate != null) {
                binding.tvBookingDate.setText(String.format(context.getString(R.string.booking_date_prefix),
                        Formatters.formatDateTime(booking.bookingDate)));
            } else {
                binding.tvBookingDate.setText("");
            }

            // Status chip
            String status = booking.status != null ? booking.status : Constants.BOOKING_STATUS_PENDING;
            binding.tvBookingStatus.setText(status);
            if (Constants.BOOKING_STATUS_CONFIRMED.equalsIgnoreCase(status)) {
                binding.tvBookingStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.success)));
            } else if (Constants.BOOKING_STATUS_PENDING.equalsIgnoreCase(status)) {
                binding.tvBookingStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning)));
            } else if (Constants.BOOKING_STATUS_COMPLETED.equalsIgnoreCase(status)) {
                binding.tvBookingStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
            } else {
                binding.tvBookingStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error)));
            }

            // Payment chip
            String paymentStatus = booking.paymentStatus != null ? booking.paymentStatus : Constants.PAYMENT_STATUS_UNPAID;
            binding.tvPaymentStatus.setText(String.format(context.getString(R.string.status_payment_prefix), paymentStatus));
            if (Constants.PAYMENT_STATUS_PAID.equalsIgnoreCase(paymentStatus)) {
                binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.success)));
            } else if (Constants.PAYMENT_STATUS_UNPAID.equalsIgnoreCase(paymentStatus)) {
                binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning)));
            } else {
                binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.outline)));
            }

            binding.tvTotalAmount.setText(Formatters.formatMoney(booking.totalAmount));

            // Lazy slot info
            if (booking.serviceSlotId != null) {
                ServiceSlot slot = slotCache.get(booking.serviceSlotId);
                if (slot != null) {
                    binding.ivCategoryIcon.setImageResource(Formatters.getCategoryIconRes(slot.category));
                    binding.tvSlotTitle.setText(buildSlotTitle(slot));
                    binding.tvSlotSchedule.setText(Formatters.formatDateTime(slot.startDateTime));
                    binding.tvSlotSchedule.setVisibility(View.VISIBLE);
                } else {
                    binding.ivCategoryIcon.setImageResource(R.drawable.ic_nav_explore);
                    binding.tvSlotTitle.setText(R.string.loading_slot_details);
                    binding.tvSlotSchedule.setVisibility(View.GONE);
                    if (listener != null) {
                        listener.onNeedSlot(booking.serviceSlotId);
                    }
                }
            } else {
                binding.tvSlotTitle.setText("");
                binding.tvSlotSchedule.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking);
                }
            });
        }

        private String buildSlotTitle(ServiceSlot slot) {
            Context context = itemView.getContext();
            if (slot.origin != null && !slot.origin.trim().isEmpty() && slot.destination != null && !slot.destination.trim().isEmpty()) {
                return context.getString(R.string.route_format, slot.origin.trim(), slot.destination.trim());
            } else if (slot.locationName != null && !slot.locationName.trim().isEmpty()) {
                return slot.locationName;
            } else {
                return Formatters.capitalizeEnum(slot.category);
            }
        }
    }
}
