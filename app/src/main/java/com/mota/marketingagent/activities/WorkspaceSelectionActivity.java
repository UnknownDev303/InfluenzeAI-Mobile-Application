package com.mota.marketingagent.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
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
import com.mota.marketingagent.data.models.WorkspaceItem;
import com.mota.marketingagent.ui.adapters.WorkspaceAdapter;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceSelectionActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private final List<WorkspaceItem> workspaces = new ArrayList<>();
    private WorkspaceAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_selection);

        sessionManager = new SessionManager(this);
        RecyclerView list = findViewById(R.id.workspaceList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkspaceAdapter(workspaces, this::selectWorkspace);
        list.setAdapter(adapter);

        MaterialButton btnCreate = findViewById(R.id.btnCreateWorkspace);
        MaterialButton btnJoin = findViewById(R.id.btnJoinWorkspace);

        btnCreate.setOnClickListener(v -> startActivity(new Intent(this, WorkspaceCreateActivity.class)));
        btnJoin.setOnClickListener(v -> showJoinDialog());

        loadWorkspaces();
    }

    private void loadWorkspaces() {
        JsonObject query = new JsonObject();
        query.addProperty("user_id", sessionManager.getUserId());
        GatewayService.callGateway("workspaces", "GET", null, query, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    workspaces.clear();
                    if (payload.has("workspaces")) {
                        JsonArray array = payload.getAsJsonArray("workspaces");
                        for (JsonElement element : array) {
                            JsonObject obj = element.getAsJsonObject();
                            WorkspaceItem item = new WorkspaceItem();
                            item.workspaceId = obj.get("workspace_id").getAsString();
                            item.workspaceName = obj.get("workspace_name").getAsString();
                            item.userRole = obj.get("user_role").getAsString();
                            workspaces.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSelectionActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showJoinDialog() {
        EditText input = new EditText(this);
        input.setHint("Invite code");
        new AlertDialog.Builder(this)
            .setTitle("Join Workspace")
            .setView(input)
            .setPositiveButton("Join", (dialog, which) -> joinWorkspace(input.getText().toString().trim()))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void joinWorkspace(String inviteCode) {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", sessionManager.getUserId());
        body.addProperty("action", "join");
        body.addProperty("invite_code", inviteCode);

        GatewayService.callGateway("workspaces", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                if (!"success".equals(payload.get("status").getAsString())) {
                    runOnUiThread(() -> Toast.makeText(WorkspaceSelectionActivity.this, "Join failed", Toast.LENGTH_SHORT).show());
                    return;
                }
                JsonObject workspace = payload.getAsJsonObject("workspace");
                sessionManager.saveWorkspace(workspace.get("workspace_id").getAsString(), workspace.get("workspace_name").getAsString());
                startActivity(new Intent(WorkspaceSelectionActivity.this, ChatActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSelectionActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void selectWorkspace(WorkspaceItem item) {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", sessionManager.getUserId());
        body.addProperty("workspace_id", item.workspaceId);

        GatewayService.callGateway("workspaces", "PUT", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                sessionManager.saveWorkspace(item.workspaceId, item.workspaceName);
                startActivity(new Intent(WorkspaceSelectionActivity.this, ChatActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSelectionActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
