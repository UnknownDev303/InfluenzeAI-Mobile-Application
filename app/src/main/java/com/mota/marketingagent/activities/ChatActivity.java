package com.mota.marketingagent.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mota.marketingagent.R;
import com.mota.marketingagent.data.GatewayService;
import com.mota.marketingagent.data.SessionManager;
import com.mota.marketingagent.data.models.ChatItem;
import com.mota.marketingagent.ui.adapters.ChatAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements ChatAdapter.OptionClickListener {
    private SessionManager sessionManager;
    private ChatAdapter adapter;
    private final List<ChatItem> items = new ArrayList<>();
    private String sessionId;
    private String previousResponseId;
    private RecyclerView chatList;
    private TextView chatTitle;

    // Welcome state views
    private View welcomeTopSpacer;
    private View welcomeBottomSpacer;
    private LinearLayout layoutWelcomeInfo;
    private TextView welcomeTitle;
    private TextView chatSubtitle;
    private EditText inputField;

    // Track user message count for auto-title generation
    private int userMessageCount = 0;
    private boolean isWelcomeState = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sessionManager = new SessionManager(this);
        sessionId = UUID.randomUUID().toString();

        chatTitle = findViewById(R.id.chatTitle);
        chatList = findViewById(R.id.chatList);
        chatSubtitle = findViewById(R.id.chatSubtitle);
        welcomeTopSpacer = findViewById(R.id.welcomeTopSpacer);
        welcomeBottomSpacer = findViewById(R.id.welcomeBottomSpacer);
        layoutWelcomeInfo = findViewById(R.id.layoutWelcomeInfo);
        welcomeTitle = findViewById(R.id.welcomeTitle);
        inputField = findViewById(R.id.inputMessage);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatList.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(items, this);
        chatList.setAdapter(adapter);
        chatList.setItemAnimator(null);

        MaterialButton btnSend = findViewById(R.id.btnSend);
        MaterialButton btnSessions = findViewById(R.id.btnSessions);
        MaterialButton btnSettings = findViewById(R.id.btnSettings);

        // Personalize welcome title with user's first name
        String userName = sessionManager.getUserName();
        if (!TextUtils.isEmpty(userName)) {
            String firstName = userName.split("\\s+")[0];
            welcomeTitle.setText("Hi " + firstName + ", Ready to automate your social media workflow?");
        }

        // Settings now opens unified WorkspaceSettingsActivity (with integrations built-in)
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, WorkspaceSettingsActivity.class)));
        btnSessions.setOnClickListener(v -> startActivity(new Intent(this, ChatSessionsActivity.class)));
        btnSend.setOnClickListener(v -> {
            String message = inputField.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                inputField.setText("");
                sendMessage(message);
            }
        });

        String incomingSessionId = getIntent().getStringExtra("session_id");
        String incomingTitle = getIntent().getStringExtra("session_title");
        if (!TextUtils.isEmpty(incomingTitle)) {
            chatTitle.setText(incomingTitle);
        }
        if (!TextUtils.isEmpty(incomingSessionId)) {
            sessionId = incomingSessionId;
            transitionToChatState(); // Existing session = skip welcome
            loadSession(incomingSessionId);
        } else {
            // Show welcome state — session created lazily on first message
            showWelcomeState();
        }
    }

    /**
     * Shows the centered welcome layout with Lily avatar + greeting + input in the middle.
     */
    private void showWelcomeState() {
        isWelcomeState = true;
        welcomeTopSpacer.setVisibility(View.VISIBLE);
        welcomeBottomSpacer.setVisibility(View.VISIBLE);
        layoutWelcomeInfo.setVisibility(View.VISIBLE);
        chatList.setVisibility(View.GONE);
        chatSubtitle.setVisibility(View.GONE);
        inputField.setHint("Say Hi To Get Started");
    }

    /**
     * Transitions from welcome state to active chat state.
     * Hides spacers + welcome info, shows RecyclerView, moves input to bottom.
     */
    private void transitionToChatState() {
        if (!isWelcomeState) return;
        isWelcomeState = false;
        welcomeTopSpacer.setVisibility(View.GONE);
        welcomeBottomSpacer.setVisibility(View.GONE);
        layoutWelcomeInfo.setVisibility(View.GONE);
        chatList.setVisibility(View.VISIBLE);
        chatSubtitle.setVisibility(View.VISIBLE);
        inputField.setHint("Ask Lily...");
    }

    /**
     * Creates a session on the backend. Called lazily before the first message is dispatched.
     */
    private void createSessionAndSendMessage(String message) {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", sessionManager.getUserId());
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("title", "New Chat");

        GatewayService.callGateway("chat-sessions/create", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    if (payload.has("session")) {
                        sessionId = payload.getAsJsonObject("session").get("session_id").getAsString();
                    }
                    // Now that session exists, dispatch the message
                    dispatchMessage(message);
                });
            }

            @Override
            public void onError(String errorMsg) {
                runOnUiThread(() -> {
                    // Use a temporary session ID and send anyway
                    dispatchMessage(message);
                });
            }
        });
    }

    private void sendMessage(String message) {
        // Transition away from welcome on first message
        if (isWelcomeState) {
            transitionToChatState();
        }

        // If this is the first message, create the session first
        if (userMessageCount == 0) {
            createSessionAndSendMessage(message);
        } else {
            dispatchMessage(message);
        }
    }

    /**
     * Actually sends the message to the backend and handles the response.
     * Also persists messages and triggers title generation.
     */
    private void dispatchMessage(String message) {
        userMessageCount++;

        items.add(ChatItem.user(message));
        adapter.notifyItemInserted(items.size() - 1);
        items.add(ChatItem.status("Thinking..."));
        int statusIndex = items.size() - 1;
        adapter.notifyItemInserted(items.size() - 1);
        scrollToBottom();

        // Persist user message to backend
        saveMessageToSession("user", message, null);

        JsonObject body = new JsonObject();
        body.addProperty("message", message);
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("user_id", sessionManager.getUserId());
        body.addProperty("session_id", sessionId);
        if (!TextUtils.isEmpty(previousResponseId)) {
            body.addProperty("previous_response_id", previousResponseId);
        }

        GatewayService.callChatNonStreaming(body, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    if (statusIndex >= 0 && statusIndex < items.size()) {
                        items.remove(statusIndex);
                        adapter.notifyItemRemoved(statusIndex);
                    }

                    // Clean assistant text (remove :::blocks::: and assistant_meta)
                    String text = payload.has("text") ? payload.get("text").getAsString() : "";
                    String cleanText = cleanAssistantText(text);
                    if (!TextUtils.isEmpty(cleanText)) {
                        items.add(ChatItem.assistant(cleanText));
                        adapter.notifyItemInserted(items.size() - 1);
                    }

                    // Persist assistant response (use the raw text for full fidelity)
                    saveMessageToSession("assistant", text, null);

                    if (payload.has("response_id")) {
                        previousResponseId = payload.get("response_id").getAsString();
                    }

                    // Process tool results
                    if (payload.has("tool_results") && payload.get("tool_results").isJsonArray()) {
                        payload.getAsJsonArray("tool_results").forEach(element -> {
                            if (!element.isJsonObject()) return;
                            JsonObject toolObj = element.getAsJsonObject();
                            if (toolObj.has("tool_name") && toolObj.has("result") && toolObj.get("result").isJsonObject()) {
                                handleToolResult(toolObj.get("tool_name").getAsString(), toolObj.getAsJsonObject("result"));
                            }
                        });
                    }
                    scrollToBottom();

                    // Auto-generate title after the 2nd user message
                    if (userMessageCount == 2) {
                        generateSessionTitle();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    if (statusIndex >= 0 && statusIndex < items.size()) {
                        items.remove(statusIndex);
                        adapter.notifyItemRemoved(statusIndex);
                    }
                    items.add(ChatItem.status("Error: " + message));
                    adapter.notifyItemInserted(items.size() - 1);
                    scrollToBottom();
                });
            }
        });
    }

    /**
     * Persists a single message to the backend session store.
     */
    private void saveMessageToSession(String role, String content, String responseId) {
        if (TextUtils.isEmpty(sessionId)) return;

        JsonObject body = new JsonObject();
        body.addProperty("session_id", sessionId);
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("role", role);
        body.addProperty("content", content);
        body.addProperty("message_id", UUID.randomUUID().toString());
        if ("user".equals(role)) {
            body.addProperty("user_id", sessionManager.getUserId());
        }
        if (!TextUtils.isEmpty(responseId)) {
            body.addProperty("response_id", responseId);
        }

        GatewayService.callGateway("chat-sessions/add-message", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                // Silent success
            }

            @Override
            public void onError(String message) {
                // Silently log — don't interrupt user flow for persistence failures
                android.util.Log.w("ChatActivity", "Failed to persist message: " + message);
            }
        });
    }

    /**
     * Calls the backend to AI-generate a session title based on conversation content.
     */
    private void generateSessionTitle() {
        if (TextUtils.isEmpty(sessionId)) return;

        JsonObject body = new JsonObject();
        body.addProperty("session_id", sessionId);
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());

        GatewayService.callGateway("chat-sessions/generate-title", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    if (payload.has("title")) {
                        String newTitle = payload.get("title").getAsString();
                        chatTitle.setText(newTitle);
                    }
                });
            }

            @Override
            public void onError(String message) {
                android.util.Log.w("ChatActivity", "Failed to generate title: " + message);
            }
        });
    }

    private void loadSession(String targetSessionId) {
        JsonObject body = new JsonObject();
        body.addProperty("session_id", targetSessionId);
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());

        GatewayService.callGateway("chat-sessions/get", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    if (!payload.has("session")) return;
                    JsonObject session = payload.getAsJsonObject("session");
                    if (session.has("title")) {
                        chatTitle.setText(session.get("title").getAsString());
                    }
                    items.clear();
                    if (session.has("messages")) {
                        JsonArray messages = session.getAsJsonArray("messages");
                        for (JsonElement element : messages) {
                            if (!element.isJsonObject()) continue;
                            JsonObject msg = element.getAsJsonObject();
                            String role = msg.has("role") ? msg.get("role").getAsString() : "";
                            String content = msg.has("content") ? msg.get("content").getAsString() : "";

                            if ("user".equals(role)) {
                                items.add(ChatItem.user(content));
                                userMessageCount++;
                            } else if ("assistant".equals(role)) {
                                String clean = cleanAssistantText(content);
                                if (!TextUtils.isEmpty(clean)) {
                                    items.add(ChatItem.assistant(clean));
                                }
                                // Parse tool_payloads from stored messages
                                if (msg.has("tool_payloads") && msg.get("tool_payloads").isJsonArray()) {
                                    JsonArray toolPayloads = msg.getAsJsonArray("tool_payloads");
                                    for (JsonElement tpEl : toolPayloads) {
                                        if (!tpEl.isJsonObject()) continue;
                                        JsonObject tp = tpEl.getAsJsonObject();
                                        String toolName = tp.has("tool_name") ? tp.get("tool_name").getAsString() : "";
                                        JsonObject result = tp.has("result") && tp.get("result").isJsonObject()
                                                ? tp.getAsJsonObject("result") : null;
                                        if (result != null && !toolName.isEmpty()) {
                                            handleToolResult(toolName, result);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    items.add(ChatItem.status("Load failed: " + message));
                    adapter.notifyItemInserted(items.size() - 1);
                    scrollToBottom();
                });
            }
        });
    }

    private void scrollToBottom() {
        if (chatList != null && items.size() > 0) {
            chatList.post(() -> chatList.scrollToPosition(items.size() - 1));
        }
    }

    /**
     * Handles all 14 backend emitter tool types and creates appropriate ChatItem cards.
     */
    private void handleToolResult(String toolName, JsonObject result) {
        ChatItem item = null;

        switch (toolName) {
            // Welcome / Agent Options
            case "emit_welcome_options":
            case "show_agent_options":
                item = ChatItem.welcomeOptions(result);
                break;

            // Topic Selector
            case "emit_topic_selector":
                item = ChatItem.topicSelector(result);
                break;

            // Format Selector
            case "emit_format_selector":
                item = ChatItem.formatSelector(result);
                break;

            // Content Drafts
            case "emit_content_drafts":
                item = ChatItem.contentDrafts(result);
                break;

            // Social Post Collection (LinkedIn/Twitter posts)
            case "emit_social_post_collection":
                item = ChatItem.socialPosts(result);
                break;

            // Influencer List (single + multi-platform)
            case "emit_influencer_list":
            case "emit_influencer_list_multi":
                item = ChatItem.influencerList(result);
                break;

            // Next Steps
            case "emit_next_steps":
                item = ChatItem.nextSteps(result);
                break;

            // Sources
            case "emit_sources":
                item = ChatItem.sources(result);
                break;

            // HTML Reports (metrics dashboards)
            case "emit_html":
                String html = result.has("html") ? result.get("html").getAsString() : "";
                if (!TextUtils.isEmpty(html)) {
                    item = ChatItem.metrics(html);
                }
                break;

            // Post Options (post-specific actions)
            case "emit_post_options":
                item = ChatItem.postOptions(result);
                break;

            // Image Results
            case "emit_image_results":
                item = ChatItem.imageResults(result);
                break;

            // YouTube Scripts — render as content drafts
            case "emit_youtube_scripts":
                item = ChatItem.contentDrafts(convertYouTubeScriptsToDrafts(result));
                break;

            // Metrics dashboards (native JSON)
            case "get_linkedin_metrics":
            case "get_twitter_metrics":
            case "get_instagram_metrics":
            case "get_youtube_metrics":
                if (result.has("metrics")) {
                    String metricsHtml = buildMetricsHtml(toolName, result);
                    if (!TextUtils.isEmpty(metricsHtml)) {
                        item = ChatItem.metrics(metricsHtml);
                    }
                }
                break;

            // Skip non-UI tools
            case "update_memory":
            case "emit_image_post_canvas":
            case "emit_analysis_dashboard":
            case "emit_markdown_report":
                // Not supported on mobile
                break;

            default:
                // Unknown tool — ignore silently
                break;
        }

        if (item != null) {
            items.add(item);
            adapter.notifyItemInserted(items.size() - 1);
        }
    }

    /**
     * Cleans raw assistant text by removing :::block::: markers, assistant_meta JSON,
     * context summaries, and leaked structured data.
     */
    private String cleanAssistantText(String text) {
        if (TextUtils.isEmpty(text)) return "";

        String cleaned = text;

        // Remove context summary
        if (cleaned.contains("[PREVIOUS CONTEXT SUMMARY]")) {
            if (!cleaned.contains("[END SUMMARY]")) return "";
            cleaned = cleaned.replaceAll("\\[PREVIOUS CONTEXT SUMMARY\\]:[\\s\\S]*?\\[END SUMMARY\\]", "");
        }

        // Remove assistant_meta blocks
        cleaned = cleaned.replaceAll("```assistant_meta[\\s\\S]*?```", "");

        // Remove :::block::: markers (paired and standard)
        String[] blockLabels = {"next_steps", "sources", "html", "youtube", "content_drafts"};
        for (String label : blockLabels) {
            // Paired: :::label::: ... :::label:::
            cleaned = cleaned.replaceAll(":::" + label + ":::[\\s\\S]*?:::" + label + ":::", "\n");
            // Standard: :::label::: ... :::other:::
            cleaned = cleaned.replaceAll(":::" + label + ":::[\\s\\S]*?(?=:::[a-zA-Z_]+:::|$)", "\n");
        }

        // Remove leftover markers
        cleaned = cleaned.replaceAll(":::[a-zA-Z_]+:::", "\n");
        cleaned = cleaned.replaceAll(":::", "\n");

        // Remove leaked JSON
        cleaned = cleaned.replaceAll("\\{[^{}]*\"(?:topic|formats|content_type|next_steps|sources)\"[^{}]*\\}", "");
        cleaned = cleaned.replaceAll("```(?:json)?\\s*\\{[^`]*\"(?:topic|formats|content_type)\"[^`]*\\}\\s*```", "");

        // Remove leaked sources/next_steps text
        cleaned = cleaned.replaceAll("\\n*\\*?\\*?Sources\\*?\\*?\\s*:\\s*\\[.*?\\]\\s*", "\n");
        cleaned = cleaned.replaceAll("\\n*\\*?\\*?(?:Next steps|Suggested next steps)\\*?\\*?\\s*:?\\s*\\n(?:\\s*[-*]\\s*.*\\n?)*", "\n");

        // Normalize whitespace
        cleaned = cleaned.replaceAll("\\r\\n", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        return cleaned;
    }

    /**
     * Converts YouTube script payloads to content draft format for rendering.
     */
    private JsonObject convertYouTubeScriptsToDrafts(JsonObject result) {
        JsonObject drafts = new JsonObject();
        drafts.addProperty("topic", "YouTube Scripts");
        drafts.addProperty("content_type", "content_draft_collection");
        JsonArray draftArray = new JsonArray();

        if (result.has("scripts") && result.get("scripts").isJsonArray()) {
            JsonArray scripts = result.getAsJsonArray("scripts");
            for (int i = 0; i < scripts.size(); i++) {
                JsonObject script = scripts.get(i).getAsJsonObject();
                JsonObject draft = new JsonObject();
                draft.addProperty("id", "script_" + (i + 1));
                draft.addProperty("format", "short_video");
                draft.addProperty("title", script.has("title") ? script.get("title").getAsString() : "Script " + (i + 1));

                // Build content from scenes
                StringBuilder content = new StringBuilder();
                if (script.has("scenes") && script.get("scenes").isJsonArray()) {
                    for (JsonElement scene : script.getAsJsonArray("scenes")) {
                        if (scene.isJsonObject()) {
                            JsonObject s = scene.getAsJsonObject();
                            if (s.has("narration")) {
                                content.append(s.get("narration").getAsString()).append("\n\n");
                            }
                        }
                    }
                }
                draft.addProperty("content", content.toString().trim());
                draftArray.add(draft);
            }
        }
        drafts.add("drafts", draftArray);
        return drafts;
    }

    /**
     * Builds a simple HTML metrics dashboard from native JSON payload.
     */
    private String buildMetricsHtml(String toolName, JsonObject result) {
        String platform = toolName.replace("get_", "").replace("_metrics", "");
        platform = platform.substring(0, 1).toUpperCase() + platform.substring(1);

        JsonObject metrics = result.has("metrics") ? result.getAsJsonObject("metrics") : new JsonObject();

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta name='viewport' content='width=device-width, initial-scale=1'>");
        html.append("<style>");
        html.append("body{font-family:system-ui,sans-serif;margin:0;padding:16px;background:#f8fafc;color:#1e293b}");
        html.append("h2{font-size:18px;margin:0 0 16px;color:#0f172a}");
        html.append(".grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}");
        html.append(".card{background:white;border-radius:12px;padding:16px;border:1px solid #e2e8f0;text-align:center}");
        html.append(".num{font-size:24px;font-weight:800;color:#0f172a}");
        html.append(".label{font-size:11px;color:#94a3b8;text-transform:uppercase;letter-spacing:0.05em;margin-top:4px}");
        html.append("</style></head><body>");
        html.append("<h2>📊 ").append(platform).append(" Metrics</h2>");
        html.append("<div class='grid'>");

        for (String key : metrics.keySet()) {
            try {
                String value = metrics.get(key).getAsString();
                String label = key.replace("_", " ");
                html.append("<div class='card'><div class='num'>").append(value).append("</div>");
                html.append("<div class='label'>").append(label).append("</div></div>");
            } catch (Exception ignored) {}
        }

        html.append("</div></body></html>");
        return html.toString();
    }

    @Override
    public void onOptionClicked(String optionText) {
        sendMessage(optionText);
    }
}
