package com.example.horizontourist.ui.bookings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.Payment;
import com.example.horizontourist.databinding.ItemPaymentRecordBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class PaymentRecordAdapter extends RecyclerView.Adapter<PaymentRecordAdapter.ViewHolder> {

    private final List<Payment> items = new ArrayList<>();

    public void setItems(List<Payment> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentRecordBinding binding = ItemPaymentRecordBinding.inflate(
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
        private final ItemPaymentRecordBinding binding;

        ViewHolder(ItemPaymentRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Payment payment) {
            if (payment == null) return;
            Context context = itemView.getContext();

            long id = payment.id != null ? payment.id : 0L;
            binding.tvPaymentId.setText(context.getString(R.string.payment_id_prefix, id));

            binding.tvPaymentMethod.setText(Formatters.capitalizeEnum(payment.method));

            if (payment.transactionDate != null) {
                binding.tvPaymentDate.setText(String.format(context.getString(R.string.tx_date_prefix),
                        Formatters.formatDateTime(payment.transactionDate)));
            } else {
                binding.tvPaymentDate.setText("");
            }

            binding.tvPaymentAmount.setText(Formatters.formatMoney(payment.amount));

            String status = payment.status != null ? payment.status : Constants.TX_STATUS_SUCCESS;
            binding.tvPaymentStatus.setText(status);

            if (Constants.TX_STATUS_SUCCESS.equalsIgnoreCase(status)) {
                binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.success)));
            } else if (Constants.TX_STATUS_FAILED.equalsIgnoreCase(status)) {
                binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error)));
            } else if (Constants.TX_STATUS_PENDING.equalsIgnoreCase(status)) {
                binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning)));
            } else {
                binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.outline)));
            }
        }
    }
}
