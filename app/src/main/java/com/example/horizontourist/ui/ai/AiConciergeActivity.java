package com.example.horizontourist.ui.ai;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiCallback;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.model.AiChatMessage;
import com.example.horizontourist.data.model.AiChatRequest;
import com.example.horizontourist.data.model.AiChatResponse;
import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.data.model.ServiceSlot;
import com.example.horizontourist.databinding.ActivityAiConciergeBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.ui.groups.CreateGroupActivity;
import com.example.horizontourist.ui.guides.GuideDetailActivity;
import com.example.horizontourist.ui.main.MainActivity;
import com.example.horizontourist.ui.slots.SlotDetailActivity;
import com.example.horizontourist.util.Validators;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AiConciergeActivity extends BaseActivity implements AiChatAdapter.OnAiActionClickListener {

    private ActivityAiConciergeBinding binding;
    private AiChatAdapter adapter;
    private final List<AiChatMessage> conversationHistory = new ArrayList<>();
    private boolean isSending = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiConciergeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupInput();
        addInitialWelcome();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AiChatAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(layoutManager);
        binding.rvChatMessages.setAdapter(adapter);
    }

    private void setupInput() {
        binding.tvCharCounter.setText(getString(R.string.ai_char_counter, 0));

        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s != null ? s.length() : 0;
                binding.tvCharCounter.setText(getString(R.string.ai_char_counter, length));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnSend.setOnClickListener(v -> {
            if (isFastDoubleClick()) {
                return;
            }
            sendMessage();
        });
    }

    private void addInitialWelcome() {
        AiChatItem welcomeItem = AiChatItem.createAssistantMessage(
                getString(R.string.ai_welcome_message),
                AiChatItem.ACTION_NONE,
                null
        );
        adapter.addItem(welcomeItem);
    }

    private void sendMessage() {
        if (isSending) {
            return;
        }

        String message = binding.etMessage.getText() != null ? binding.etMessage.getText().toString().trim() : "";
        if (!Validators.isNonEmpty(message)) {
            toast(R.string.error_empty_ai_message);
            return;
        }

        // Add user message to UI
        AiChatItem userItem = AiChatItem.createUserMessage(message);
        adapter.addItem(userItem);
        scrollToBottom();

        // Clear input
        binding.etMessage.setText("");

        // Prepare history: send up to the last 12 turns
        List<AiChatMessage> historyToSend;
        if (conversationHistory.size() > 12) {
            historyToSend = new ArrayList<>(conversationHistory.subList(conversationHistory.size() - 12, conversationHistory.size()));
        } else {
            historyToSend = new ArrayList<>(conversationHistory);
        }

        setTyping(true);

        AiChatRequest request = new AiChatRequest(message, historyToSend);
        ApiClient.getApiService().chatAi(request).enqueue(new ApiCallback<AiChatResponse>() {
            @Override
            public void onSuccess(AiChatResponse response) {
                if (!isSafe()) {
                    return;
                }
                setTyping(false);

                if (response != null && response.replyText != null) {
                    // Save turns to conversational history
                    conversationHistory.add(new AiChatMessage("user", message));
                    conversationHistory.add(new AiChatMessage("model", response.replyText));

                    // Build Assistant chat item with action handling
                    handleAiResponse(response);
                } else {
                    handleAiError(getString(R.string.ai_error_generic), 500);
                }
            }

            @Override
            public void onError(String errorMsg, int code) {
                if (!isSafe()) {
                    return;
                }
                setTyping(false);
                handleAiError(errorMsg, code);
            }
        });
    }

    private void handleAiResponse(AiChatResponse response) {
        AiChatItem item = AiChatItem.createAssistantMessage(
                response.replyText,
                response.actionType,
                response.actionPayloadJson
        );

        String actionType = response.actionType != null ? response.actionType.trim().toUpperCase(Locale.US) : AiChatItem.ACTION_NONE;
        String payloadJson = response.actionPayloadJson;

        if (AiChatItem.ACTION_FILTER_SLOTS.equals(actionType)) {
            try {
                if (payloadJson != null && !payloadJson.trim().isEmpty()) {
                    Type type = new TypeToken<Map<String, Object>>() {}.getType();
                    Map<String, Object> map = new Gson().fromJson(payloadJson, type);
                    item.filterMap = map;
                }
            } catch (Exception e) {
                item.actionType = AiChatItem.ACTION_NONE;
            }
        } else if (AiChatItem.ACTION_RECOMMEND_BUNDLE.equals(actionType)
                || AiChatItem.ACTION_RECOMMEND_GROUP_BUNDLE.equals(actionType)) {
            try {
                if (payloadJson != null && !payloadJson.trim().isEmpty()) {
                    JsonObject obj = JsonParser.parseString(payloadJson).getAsJsonObject();
                    if (obj.has("slotId") && !obj.get("slotId").isJsonNull()) {
                        item.slotId = parseLongSafe(obj.get("slotId").getAsString());
                    }
                    if (obj.has("guideId") && !obj.get("guideId").isJsonNull()) {
                        item.guideId = parseLongSafe(obj.get("guideId").getAsString());
                    }
                }
            } catch (Exception e) {
                item.actionType = AiChatItem.ACTION_NONE;
            }

            if (item.slotId != null || item.guideId != null) {
                item.isLoadingBundle = true;
                fetchBundleDetails(item, adapter.getItemCount());
            }
        }

        adapter.addItem(item);
        scrollToBottom();
    }

    private Long parseLongSafe(String str) {
        try {
            return Long.parseLong(str.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void fetchBundleDetails(AiChatItem item, int itemPosition) {
        if (item.slotId != null) {
            ApiClient.getApiService().getSlotById(item.slotId).enqueue(new ApiCallback<ServiceSlot>() {
                @Override
                public void onSuccess(ServiceSlot slot) {
                    if (!isSafe()) return;
                    item.slot = slot;
                    item.isLoadingBundle = false;
                    adapter.notifyItemChanged(itemPosition);
                }

                @Override
                public void onError(String message, int code) {
                    if (!isSafe()) return;
                    item.isLoadingBundle = false;
                    adapter.notifyItemChanged(itemPosition);
                }
            });
        }

        if (item.guideId != null) {
            ApiClient.getApiService().getGuideById(item.guideId).enqueue(new ApiCallback<Guide>() {
                @Override
                public void onSuccess(Guide guide) {
                    if (!isSafe()) return;
                    item.guide = guide;
                    item.isLoadingBundle = false;
                    adapter.notifyItemChanged(itemPosition);
                }

                @Override
                public void onError(String message, int code) {
                    if (!isSafe()) return;
                    item.isLoadingBundle = false;
                    adapter.notifyItemChanged(itemPosition);
                }
            });
        }
    }

    private void handleAiError(String message, int code) {
        String errorBubbleText;
        if (code == 429) {
            errorBubbleText = getString(R.string.ai_error_rate_limit);
        } else if (code == 403) {
            errorBubbleText = getString(R.string.error_unauthorized);
        } else {
            errorBubbleText = message != null && !message.trim().isEmpty() ? message : getString(R.string.ai_error_generic);
        }

        // Add error bubble without saving to conversational history
        AiChatItem errorItem = AiChatItem.createErrorMessage(errorBubbleText);
        adapter.addItem(errorItem);
        scrollToBottom();
    }

    private void setTyping(boolean typing) {
        isSending = typing;
        binding.layoutTypingIndicator.setVisibility(typing ? View.VISIBLE : View.GONE);
        binding.btnSend.setEnabled(!typing);
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            binding.rvChatMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    // --- Action Listeners ---

    @Override
    public void onFilterSlotsClicked(Map<String, Object> filterMap) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_SELECT_TAB, R.id.navigation_explore);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if (filterMap != null) {
            if (filterMap.containsKey("destination") && filterMap.get("destination") != null) {
                intent.putExtra("destination", String.valueOf(filterMap.get("destination")));
            }
            if (filterMap.containsKey("origin") && filterMap.get("origin") != null) {
                intent.putExtra("origin", String.valueOf(filterMap.get("origin")));
            }
            if (filterMap.containsKey("category") && filterMap.get("category") != null) {
                intent.putExtra("category", String.valueOf(filterMap.get("category")));
            }
            if (filterMap.containsKey("locationName") && filterMap.get("locationName") != null) {
                intent.putExtra("locationName", String.valueOf(filterMap.get("locationName")));
            }
            if (filterMap.containsKey("minPrice") && filterMap.get("minPrice") != null) {
                try {
                    double min = Double.parseDouble(String.valueOf(filterMap.get("minPrice")));
                    intent.putExtra("minPrice", min);
                } catch (Exception ignored) {}
            }
            if (filterMap.containsKey("maxPrice") && filterMap.get("maxPrice") != null) {
                try {
                    double max = Double.parseDouble(String.valueOf(filterMap.get("maxPrice")));
                    intent.putExtra("maxPrice", max);
                } catch (Exception ignored) {}
            }
        }

        startActivity(intent);
    }

    @Override
    public void onSlotClicked(Long slotId) {
        if (slotId == null) return;
        Intent intent = new Intent(this, SlotDetailActivity.class);
        intent.putExtra(SlotDetailActivity.EXTRA_SLOT_ID, slotId);
        startActivity(intent);
    }

    @Override
    public void onGuideClicked(Long guideId) {
        if (guideId == null) return;
        Intent intent = new Intent(this, GuideDetailActivity.class);
        intent.putExtra("guide_id", guideId);
        startActivity(intent);
    }

    @Override
    public void onCreateGroupClicked() {
        startActivity(new Intent(this, CreateGroupActivity.class));
    }
}
