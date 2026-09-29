package com.example.horizontourist.ui.groups;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.data.model.GroupMessage;
import com.example.horizontourist.databinding.ItemChatMessageMeBinding;
import com.example.horizontourist.databinding.ItemChatMessageOtherBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    private final long myTouristId;
    private final List<GroupMessage> items = new ArrayList<>();
    private final Set<Long> messageIds = new HashSet<>();

    public GroupChatAdapter(long myTouristId) {
        this.myTouristId = myTouristId;
    }

    public boolean setMessages(List<GroupMessage> newMessages) {
        if (newMessages == null) return false;

        boolean changed = false;
        List<GroupMessage> toAdd = new ArrayList<>();

        for (GroupMessage msg : newMessages) {
            if (msg != null && msg.id != null) {
                if (!messageIds.contains(msg.id)) {
                    messageIds.add(msg.id);
                    toAdd.add(msg);
                    changed = true;
                }
            }
        }

        if (changed) {
            int start = items.size();
            items.addAll(toAdd);
            notifyItemRangeInserted(start, toAdd.size());
        }

        return changed;
    }

    public void addMessage(GroupMessage message) {
        if (message == null || message.id == null) return;
        if (!messageIds.contains(message.id)) {
            messageIds.add(message.id);
            int pos = items.size();
            items.add(message);
            notifyItemInserted(pos);
        }
    }

    @Override
    public int getItemViewType(int position) {
        GroupMessage msg = items.get(position);
        if (msg != null && msg.senderId != null && msg.senderId == myTouristId) {
            return VIEW_TYPE_ME;
        }
        return VIEW_TYPE_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_ME) {
            ItemChatMessageMeBinding binding = ItemChatMessageMeBinding.inflate(inflater, parent, false);
            return new MeViewHolder(binding);
        } else {
            ItemChatMessageOtherBinding binding = ItemChatMessageOtherBinding.inflate(inflater, parent, false);
            return new OtherViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupMessage message = items.get(position);
        if (holder instanceof MeViewHolder) {
            ((MeViewHolder) holder).bind(message);
        } else if (holder instanceof OtherViewHolder) {
            ((OtherViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MeViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageMeBinding binding;

        MeViewHolder(ItemChatMessageMeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GroupMessage message) {
            if (message == null) return;
            binding.tvMessage.setText(message.message != null ? message.message : "");
            binding.tvTime.setText(message.sentAt != null ? Formatters.formatDateTime(message.sentAt) : "");
        }
    }

    static class OtherViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageOtherBinding binding;

        OtherViewHolder(ItemChatMessageOtherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GroupMessage message) {
            if (message == null) return;
            String sender = (message.senderName != null && !message.senderName.trim().isEmpty())
                    ? message.senderName
                    : "Tourist #" + (message.senderId != null ? message.senderId : "");
            binding.tvSenderName.setText(sender);
            binding.tvMessage.setText(message.message != null ? message.message : "");
            binding.tvTime.setText(message.sentAt != null ? Formatters.formatDateTime(message.sentAt) : "");
        }
    }
}
