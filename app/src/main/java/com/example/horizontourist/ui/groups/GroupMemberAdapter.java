package com.example.horizontourist.ui.groups;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.data.model.GroupMember;
import com.example.horizontourist.databinding.ItemGroupMemberBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private final List<GroupMember> items = new ArrayList<>();
    private Long creatorId;

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
        notifyDataSetChanged();
    }

    public void setItems(List<GroupMember> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<GroupMember> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupMemberBinding binding = ItemGroupMemberBinding.inflate(
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
        private final ItemGroupMemberBinding binding;

        ViewHolder(ItemGroupMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GroupMember member) {
            if (member == null) return;
            Context context = itemView.getContext();

            // Name
            String name = (member.touristName != null && !member.touristName.trim().isEmpty())
                    ? member.touristName
                    : "Tourist #" + (member.touristId != null ? member.touristId : "");
            binding.tvMemberName.setText(name);

            // Creator badge
            boolean isCreator = (creatorId != null && member.touristId != null && creatorId.equals(member.touristId));
            binding.tvCreatorBadge.setVisibility(isCreator ? View.VISIBLE : View.GONE);

            // Paid status
            boolean isPaid = member.isPaid();
            if (isPaid) {
                binding.tvPaidStatus.setText(R.string.status_paid);
                binding.tvPaidStatus.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.success)));
            } else {
                binding.tvPaidStatus.setText(R.string.status_unpaid);
                binding.tvPaidStatus.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning)));
            }

            // Member status
            String status = member.status != null ? member.status : "";
            binding.tvMemberStatus.setText(String.format(context.getString(R.string.status_member_prefix), status));

            // Amount owed
            binding.tvAmountOwed.setText(String.format(
                    context.getString(R.string.amount_owed_format), Formatters.formatMoney(member.amountOwed)));
        }
    }
}
