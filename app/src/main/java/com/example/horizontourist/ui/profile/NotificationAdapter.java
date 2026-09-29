package com.example.horizontourist.ui.profile;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.Notification;
import com.example.horizontourist.databinding.ItemNotificationBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification, int position);
    }

    private final List<Notification> items = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Notification> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<Notification> getItems() {
        return items;
    }

    public void markItemAsRead(int position) {
        if (position >= 0 && position < items.size()) {
            Notification item = items.get(position);
            item.read = true;
            notifyItemChanged(position);
        }
    }

    public void markAllAsRead() {
        for (Notification item : items) {
            item.read = true;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final ItemNotificationBinding binding;

        public NotificationViewHolder(@NonNull ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(items.get(pos), pos);
                }
            });
        }

        public void bind(Notification notification) {
            if (notification == null) {
                return;
            }

            if (notification.type != null && !notification.type.trim().isEmpty()) {
                binding.tvNotificationType.setText(Formatters.capitalizeEnum(notification.type));
            } else {
                binding.tvNotificationType.setText(R.string.title_notifications);
            }

            binding.tvNotificationMessage.setText(notification.message != null ? notification.message : "");

            if (notification.createdAt != null && !notification.createdAt.trim().isEmpty()) {
                binding.tvNotificationDate.setText(Formatters.formatDateTime(notification.createdAt));
                binding.tvNotificationDate.setVisibility(View.VISIBLE);
            } else {
                binding.tvNotificationDate.setVisibility(View.GONE);
            }

            if (notification.isRead()) {
                binding.viewUnreadDot.setVisibility(View.GONE);
                binding.tvNotificationMessage.setTypeface(null, Typeface.NORMAL);
                binding.tvNotificationMessage.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.on_surface_variant));
            } else {
                binding.viewUnreadDot.setVisibility(View.VISIBLE);
                binding.tvNotificationMessage.setTypeface(null, Typeface.BOLD);
                binding.tvNotificationMessage.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.on_surface));
            }
        }
    }
}
