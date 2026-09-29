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
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.data.model.GuideBooking;
import com.example.horizontourist.databinding.ItemGuideBookingBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuideBookingAdapter extends RecyclerView.Adapter<GuideBookingAdapter.ViewHolder> {

    public interface OnGuideBookingListener {
        void onGuideBookingClick(GuideBooking booking);
        void onNeedGuide(Long guideId);
        void onPayClick(GuideBooking booking);
        void onViewLocationClick(GuideBooking booking, Guide guide);
        void onRateGuideClick(GuideBooking booking);
    }

    private final List<GuideBooking> items = new ArrayList<>();
    private final Map<Long, Guide> guideCache = new HashMap<>();
    private final OnGuideBookingListener listener;

    public GuideBookingAdapter(OnGuideBookingListener listener) {
        this.listener = listener;
    }

    public void setItems(List<GuideBooking> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void putGuide(Long guideId, Guide guide) {
        if (guideId == null || guide == null) return;
        guideCache.put(guideId, guide);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideBookingBinding binding = ItemGuideBookingBinding.inflate(
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
        private final ItemGuideBookingBinding binding;

        ViewHolder(ItemGuideBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GuideBooking booking) {
            if (booking == null) return;
            Context context = itemView.getContext();

            long id = booking.id != null ? booking.id : 0L;
            binding.tvGuideBookingId.setText(context.getString(R.string.booking_id_prefix, id));

            if (booking.scheduleDate != null) {
                binding.tvScheduleDate.setText(String.format(context.getString(R.string.guide_booking_date),
                        Formatters.formatDate(booking.scheduleDate)));
            } else {
                binding.tvScheduleDate.setText("");
            }

            // Status chip
            String status = booking.status != null ? booking.status : Constants.GUIDE_BOOKING_STATUS_REQUESTED;
            binding.tvGuideStatus.setText(status);
            if (Constants.GUIDE_BOOKING_STATUS_ACCEPTED.equalsIgnoreCase(status)) {
                binding.tvGuideStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.success)));
            } else if (Constants.GUIDE_BOOKING_STATUS_REQUESTED.equalsIgnoreCase(status)) {
                binding.tvGuideStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning)));
            } else if (Constants.GUIDE_BOOKING_STATUS_COMPLETED.equalsIgnoreCase(status)) {
                binding.tvGuideStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
            } else {
                binding.tvGuideStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error)));
            }

            // Payment chip
            boolean isPaid = booking.paymentReceived != null && booking.paymentReceived;
            String payText = isPaid ? Constants.PAYMENT_STATUS_PAID : Constants.PAYMENT_STATUS_UNPAID;
            binding.tvGuidePaymentStatus.setText(String.format(context.getString(R.string.status_payment_prefix), payText));
            binding.tvGuidePaymentStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, isPaid ? R.color.success : R.color.warning)));

            binding.tvAgreedPrice.setText(Formatters.formatMoney(booking.agreedPrice));

            // Lazy guide info
            Guide guide = null;
            if (booking.tourGuideId != null) {
                guide = guideCache.get(booking.tourGuideId);
                if (guide != null) {
                    binding.tvGuideName.setText(String.format(context.getString(R.string.guide_name_prefix), guide.fullName));
                } else {
                    binding.tvGuideName.setText(R.string.loading_guide_name);
                    if (listener != null) {
                        listener.onNeedGuide(booking.tourGuideId);
                    }
                }
            } else {
                binding.tvGuideName.setText("");
            }

            // Actions per status
            boolean isAccepted = Constants.GUIDE_BOOKING_STATUS_ACCEPTED.equalsIgnoreCase(status);
            boolean isCompleted = Constants.GUIDE_BOOKING_STATUS_COMPLETED.equalsIgnoreCase(status);

            boolean showPay = isAccepted && !isPaid;
            boolean showLocation = isAccepted;
            boolean showRate = isCompleted;

            binding.btnPayGuide.setVisibility(showPay ? View.VISIBLE : View.GONE);
            binding.btnViewLocation.setVisibility(showLocation ? View.VISIBLE : View.GONE);
            binding.btnRateGuide.setVisibility(showRate ? View.VISIBLE : View.GONE);

            if (showPay || showLocation || showRate) {
                binding.layoutGuideActions.setVisibility(View.VISIBLE);
            } else {
                binding.layoutGuideActions.setVisibility(View.GONE);
            }

            final Guide finalGuide = guide;
            binding.btnPayGuide.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPayClick(booking);
                }
            });

            binding.btnViewLocation.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewLocationClick(booking, finalGuide);
                }
            });

            binding.btnRateGuide.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRateGuideClick(booking);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGuideBookingClick(booking);
                }
            });
        }
    }
}
