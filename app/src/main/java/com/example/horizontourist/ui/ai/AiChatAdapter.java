package com.example.horizontourist.ui.ai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontourist.R;
import com.example.horizontourist.databinding.ItemAiChatAssistantBinding;
import com.example.horizontourist.databinding.ItemAiChatUserBinding;
import com.example.horizontourist.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AiChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnAiActionClickListener {
        void onFilterSlotsClicked(Map<String, Object> filterMap);
        void onSlotClicked(Long slotId);
        void onGuideClicked(Long guideId);
        void onCreateGroupClicked();
    }

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ASSISTANT = 2;

    private final List<AiChatItem> items = new ArrayList<>();
    private final OnAiActionClickListener listener;

    public AiChatAdapter(OnAiActionClickListener listener) {
        this.listener = listener;
    }

    public void addItem(AiChatItem item) {
        if (item != null) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
        }
    }

    public void updateItem(int position, AiChatItem item) {
        if (position >= 0 && position < items.size()) {
            items.set(position, item);
            notifyItemChanged(position);
        }
    }

    public List<AiChatItem> getItems() {
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        AiChatItem item = items.get(position);
        return item.isUser() ? VIEW_TYPE_USER : VIEW_TYPE_ASSISTANT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            ItemAiChatUserBinding binding = ItemAiChatUserBinding.inflate(inflater, parent, false);
            return new UserMessageViewHolder(binding);
        } else {
            ItemAiChatAssistantBinding binding = ItemAiChatAssistantBinding.inflate(inflater, parent, false);
            return new AssistantMessageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AiChatItem item = items.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(item);
        } else if (holder instanceof AssistantMessageViewHolder) {
            ((AssistantMessageViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemAiChatUserBinding binding;

        UserMessageViewHolder(@NonNull ItemAiChatUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AiChatItem item) {
            binding.tvUserMessage.setText(item.text != null ? item.text : "");
        }
    }

    class AssistantMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemAiChatAssistantBinding binding;

        AssistantMessageViewHolder(@NonNull ItemAiChatAssistantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AiChatItem item) {
            Context context = itemView.getContext();
            binding.tvAssistantMessage.setText(item.text != null ? item.text : "");

            if (item.isError) {
                binding.layoutBubble.setBackgroundResource(R.drawable.bg_chat_bubble_error);
                binding.tvAssistantMessage.setTextColor(ContextCompat.getColor(context, R.color.error));
                binding.btnShowMatchingServices.setVisibility(View.GONE);
                binding.layoutBundle.setVisibility(View.GONE);
                return;
            }

            binding.layoutBubble.setBackgroundResource(R.drawable.bg_chat_bubble_other);
            binding.tvAssistantMessage.setTextColor(ContextCompat.getColor(context, R.color.on_surface));

            // Handle FILTER_SLOTS
            if (AiChatItem.ACTION_FILTER_SLOTS.equals(item.actionType)) {
                binding.btnShowMatchingServices.setVisibility(View.VISIBLE);
                binding.btnShowMatchingServices.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFilterSlotsClicked(item.filterMap);
                    }
                });
                binding.layoutBundle.setVisibility(View.GONE);
            }
            // Handle RECOMMEND_BUNDLE / RECOMMEND_GROUP_BUNDLE
            else if (AiChatItem.ACTION_RECOMMEND_BUNDLE.equals(item.actionType)
                    || AiChatItem.ACTION_RECOMMEND_GROUP_BUNDLE.equals(item.actionType)) {
                binding.btnShowMatchingServices.setVisibility(View.GONE);
                binding.layoutBundle.setVisibility(View.VISIBLE);

                binding.pbBundleLoading.setVisibility(item.isLoadingBundle ? View.VISIBLE : View.GONE);

                // Slot Card
                if (item.slot != null) {
                    String route = (item.slot.origin != null && item.slot.destination != null)
                            ? context.getString(R.string.route_format, item.slot.origin, item.slot.destination)
                            : (item.slot.destination != null ? item.slot.destination : context.getString(R.string.slot_id_prefix, item.slotId));
                    binding.tvSlotTitle.setText(route);
                    binding.tvSlotPrice.setText(Formatters.formatMoney(item.slot.price));
                    binding.cardSlotPreview.setVisibility(View.VISIBLE);
                } else if (item.slotId != null) {
                    binding.tvSlotTitle.setText(context.getString(R.string.slot_id_prefix, item.slotId));
                    binding.tvSlotPrice.setText("");
                    binding.cardSlotPreview.setVisibility(View.VISIBLE);
                } else {
                    binding.cardSlotPreview.setVisibility(View.GONE);
                }

                binding.btnViewSlot.setOnClickListener(v -> {
                    if (listener != null && item.slotId != null) {
                        listener.onSlotClicked(item.slotId);
                    }
                });

                // Guide Card
                if (item.guide != null) {
                    binding.tvGuideName.setText(item.guide.fullName != null ? item.guide.fullName : context.getString(R.string.default_tour_guide));
                    double rating = item.guide.ratingAvg != null ? item.guide.ratingAvg : 0.0;
                    int count = item.guide.ratingCount != null ? item.guide.ratingCount : 0;
                    binding.tvGuideRating.setText(String.format(Locale.US, "★ %.1f (%d reviews)", rating, count));
                    binding.tvGuidePrice.setText(Formatters.formatMoney(item.guide.defaultPrice));
                    binding.cardGuidePreview.setVisibility(View.VISIBLE);
                } else if (item.guideId != null) {
                    binding.tvGuideName.setText(context.getString(R.string.guide_id_prefix, item.guideId));
                    binding.tvGuideRating.setText("");
                    binding.tvGuidePrice.setText("");
                    binding.cardGuidePreview.setVisibility(View.VISIBLE);
                } else {
                    binding.cardGuidePreview.setVisibility(View.GONE);
                }

                binding.btnViewGuide.setOnClickListener(v -> {
                    if (listener != null && item.guideId != null) {
                        listener.onGuideClicked(item.guideId);
                    }
                });

                // Create Group Button
                if (AiChatItem.ACTION_RECOMMEND_GROUP_BUNDLE.equals(item.actionType)) {
                    binding.btnCreateGroup.setVisibility(View.VISIBLE);
                    binding.btnCreateGroup.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onCreateGroupClicked();
                        }
                    });
                } else {
                    binding.btnCreateGroup.setVisibility(View.GONE);
                }
            } else {
                binding.btnShowMatchingServices.setVisibility(View.GONE);
                binding.layoutBundle.setVisibility(View.GONE);
            }
        }
    }
}
