package com.example.horizontourist.ui.groups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.Group;
import com.example.horizontourist.databinding.ItemGroupBinding;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
        void onCopyCodeClick(Group group);
    }

    private final List<Group> items = new ArrayList<>();
    private final OnGroupClickListener listener;

    public GroupAdapter(OnGroupClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Group> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
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
        private final ItemGroupBinding binding;

        ViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Group group) {
            if (group == null) return;
            Context context = itemView.getContext();

            // Name
            binding.tvGroupName.setText(group.groupName != null ? group.groupName : "");

            // Members count
            int count = group.getMemberCount();
            binding.tvMemberCount.setText(String.format(context.getString(R.string.members_count_format), count));

            // Join code
            String code = group.joinCode != null ? group.joinCode : "";
            binding.tvJoinCode.setText(String.format(context.getString(R.string.join_code_format), code));

            binding.btnCopyJoinCode.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCopyCodeClick(group);
                }
            });

            // Linked booking
            if (group.bookingId != null && group.bookingId > 0) {
                binding.tvLinkedBooking.setText(String.format(context.getString(R.string.booking_linked_badge), group.bookingId));
                binding.tvLinkedBooking.setVisibility(View.VISIBLE);
            } else {
                binding.tvLinkedBooking.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGroupClick(group);
                }
            });
        }
    }
}
