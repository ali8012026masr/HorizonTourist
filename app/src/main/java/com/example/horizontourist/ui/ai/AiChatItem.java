package com.example.horizontourist.ui.ai;

import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.data.model.ServiceSlot;

import java.util.Map;

public class AiChatItem {
    public static final String ROLE_USER = "user";
    public static final String ROLE_MODEL = "model";

    public static final String ACTION_FILTER_SLOTS = "FILTER_SLOTS";
    public static final String ACTION_RECOMMEND_BUNDLE = "RECOMMEND_BUNDLE";
    public static final String ACTION_RECOMMEND_GROUP_BUNDLE = "RECOMMEND_GROUP_BUNDLE";
    public static final String ACTION_NONE = "NONE";

    public String role;
    public String text;
    public String actionType;
    public String actionPayloadJson;
    public boolean isError;

    // Parsed payload fields
    public Map<String, Object> filterMap;
    public Long slotId;
    public Long guideId;

    // Async fetched bundle details
    public ServiceSlot slot;
    public Guide guide;
    public boolean isLoadingBundle;

    public AiChatItem(String role, String text) {
        this.role = role;
        this.text = text;
        this.actionType = ACTION_NONE;
        this.isError = false;
    }

    public static AiChatItem createUserMessage(String text) {
        return new AiChatItem(ROLE_USER, text);
    }

    public static AiChatItem createAssistantMessage(String text, String actionType, String actionPayloadJson) {
        AiChatItem item = new AiChatItem(ROLE_MODEL, text);
        item.actionType = actionType != null ? actionType : ACTION_NONE;
        item.actionPayloadJson = actionPayloadJson;
        item.isError = false;
        return item;
    }

    public static AiChatItem createErrorMessage(String text) {
        AiChatItem item = new AiChatItem(ROLE_MODEL, text);
        item.isError = true;
        return item;
    }

    public boolean isUser() {
        return ROLE_USER.equals(role);
    }
}
