package com.mota.marketingagent.data.models;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class ChatItem {
    public enum Type {
        USER,
        ASSISTANT,
        STATUS,
        WELCOME_OPTIONS,
        TOPIC_SELECTOR,
        FORMAT_SELECTOR,
        CONTENT_DRAFTS,
        SOCIAL_POSTS,
        INFLUENCERS,
        NEXT_STEPS,
        SOURCES,
        POST_OPTIONS,
        IMAGE_RESULTS,
        METRICS
    }

    public Type type;
    public String text;
    public String toolName;
    public JsonObject rawPayload;

    // Legacy fields kept for backward compat
    public List<DraftItem> drafts = new ArrayList<>();
    public List<PostItem> posts = new ArrayList<>();
    public List<InfluencerItem> influencers = new ArrayList<>();
    public List<String> options = new ArrayList<>();
    public String optionsTitle;
    public String html;

    public static ChatItem user(String text) {
        ChatItem item = new ChatItem();
        item.type = Type.USER;
        item.text = text;
        return item;
    }

    public static ChatItem assistant(String text) {
        ChatItem item = new ChatItem();
        item.type = Type.ASSISTANT;
        item.text = text;
        return item;
    }

    public static ChatItem status(String text) {
        ChatItem item = new ChatItem();
        item.type = Type.STATUS;
        item.text = text;
        return item;
    }

    /**
     * Generic factory for tool-result based cards.
     * The rawPayload stores the full JSON result from the emitter tool.
     */
    public static ChatItem fromTool(Type type, String toolName, JsonObject payload) {
        ChatItem item = new ChatItem();
        item.type = type;
        item.toolName = toolName;
        item.rawPayload = payload;
        return item;
    }

    // Convenience factories
    public static ChatItem welcomeOptions(JsonObject payload) {
        return fromTool(Type.WELCOME_OPTIONS, "emit_welcome_options", payload);
    }

    public static ChatItem topicSelector(JsonObject payload) {
        return fromTool(Type.TOPIC_SELECTOR, "emit_topic_selector", payload);
    }

    public static ChatItem formatSelector(JsonObject payload) {
        return fromTool(Type.FORMAT_SELECTOR, "emit_format_selector", payload);
    }

    public static ChatItem contentDrafts(JsonObject payload) {
        return fromTool(Type.CONTENT_DRAFTS, "emit_content_drafts", payload);
    }

    public static ChatItem socialPosts(JsonObject payload) {
        return fromTool(Type.SOCIAL_POSTS, "emit_social_post_collection", payload);
    }

    public static ChatItem influencerList(JsonObject payload) {
        return fromTool(Type.INFLUENCERS, "emit_influencer_list", payload);
    }

    public static ChatItem nextSteps(JsonObject payload) {
        return fromTool(Type.NEXT_STEPS, "emit_next_steps", payload);
    }

    public static ChatItem sources(JsonObject payload) {
        return fromTool(Type.SOURCES, "emit_sources", payload);
    }

    public static ChatItem postOptions(JsonObject payload) {
        return fromTool(Type.POST_OPTIONS, "emit_post_options", payload);
    }

    public static ChatItem imageResults(JsonObject payload) {
        return fromTool(Type.IMAGE_RESULTS, "emit_image_results", payload);
    }

    public static ChatItem metrics(String html) {
        ChatItem item = new ChatItem();
        item.type = Type.METRICS;
        item.html = html;
        return item;
    }
}
