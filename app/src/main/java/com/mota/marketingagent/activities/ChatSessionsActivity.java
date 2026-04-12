package com.mota.marketingagent.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
import com.mota.marketingagent.data.models.ChatSessionItem;
import com.mota.marketingagent.ui.adapters.ChatSessionAdapter;
import java.util.ArrayList;
import java.util.List;

public class ChatSessionsActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private ChatSessionAdapter adapter;
    private final List<ChatSessionItem> sessions = new ArrayList<>();

    private RecyclerView sessionList;
    private LinearLayout loadingContainer;
    private LinearLayout emptyContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_sessions);

        sessionManager = new SessionManager(this);

        sessionList = findViewById(R.id.sessionList);
        loadingContainer = findViewById(R.id.sessionLoadingContainer);
        emptyContainer = findViewById(R.id.sessionEmptyContainer);

        sessionList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatSessionAdapter(sessions, this::openSession);
        sessionList.setAdapter(adapter);

        TextView workspaceLabel = findViewById(R.id.sessionWorkspace);
        workspaceLabel.setText(sessionManager.getWorkspaceName());

        MaterialButton btnBack = findViewById(R.id.btnSessionBack);
        MaterialButton btnNew = findViewById(R.id.btnNewSession);
        btnBack.setOnClickListener(v -> finish());
        btnNew.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));

        loadSessions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSessions();
    }

    private void showLoading() {
        loadingContainer.setVisibility(View.VISIBLE);
        sessionList.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showList() {
        loadingContainer.setVisibility(View.GONE);
        sessionList.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showEmpty() {
        loadingContainer.setVisibility(View.GONE);
        sessionList.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.VISIBLE);
    }

    private void loadSessions() {
        showLoading();

        JsonObject body = new JsonObject();
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("summary_only", true);

        GatewayService.callGateway("chat-sessions/list", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    sessions.clear();
                    if (payload.has("sessions")) {
                        JsonArray array = payload.getAsJsonArray("sessions");
                        for (JsonElement element : array) {
                            if (!element.isJsonObject()) {
                                continue;
                            }
                            JsonObject obj = element.getAsJsonObject();
                            ChatSessionItem item = new ChatSessionItem();
                            item.sessionId = obj.has("session_id") ? obj.get("session_id").getAsString() : "";
                            item.title = obj.has("title") ? obj.get("title").getAsString() : "Chat Session";
                            item.updatedAt = obj.has("updated_at") ? obj.get("updated_at").getAsString() : "";

                            // Safely get message count — may be an array or a number
                            if (obj.has("message_count")) {
                                try {
                                    item.messageCount = obj.get("message_count").getAsInt();
                                } catch (Exception e) {
                                    item.messageCount = 0;
                                }
                            } else if (obj.has("messages") && obj.get("messages").isJsonArray()) {
                                item.messageCount = obj.getAsJsonArray("messages").size();
                            } else {
                                item.messageCount = 0;
                            }

                            sessions.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (sessions.isEmpty()) {
                        showEmpty();
                    } else {
                        showList();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatSessionsActivity.this, message, Toast.LENGTH_SHORT).show();
                    showEmpty();
                });
            }
        });
    }

    private void openSession(ChatSessionItem item) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("session_id", item.sessionId);
        intent.putExtra("session_title", item.title);
        startActivity(intent);
    }
}
