package com.mota.marketingagent.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.mota.marketingagent.R;
import com.mota.marketingagent.data.GatewayService;
import com.mota.marketingagent.data.SessionManager;

public class WorkspaceCreateActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private FrameLayout animationOverlay;
    private TextView tvSystemLogText;
    
    private final String[] SYSTEM_LOGS = {
        "> Initializing Lily Agent...",
        "> Connecting to Azure OpenAI...",
        "> Syncing LinkedIn Graph...",
        "> Analyzing Twitter Trends..."
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_create);

        sessionManager = new SessionManager(this);
        
        TextInputEditText nameInput = findViewById(R.id.inputWorkspaceName);
        TextInputEditText descInput = findViewById(R.id.inputWorkspaceDescription);
        Spinner spinnerDomain = findViewById(R.id.spinnerDomain);
        Spinner spinnerAudience = findViewById(R.id.spinnerAudience);
        
        MaterialButton btnCreate = findViewById(R.id.btnCreate);
        TextView btnCancel = findViewById(R.id.btnCancel);
        
        animationOverlay = findViewById(R.id.animationOverlay);
        tvSystemLogText = findViewById(R.id.tvSystemLogText);

        btnCancel.setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            String description = descInput.getText() != null ? descInput.getText().toString().trim() : "";
            
            String domain = spinnerDomain.getSelectedItem().toString();
            if (domain.equals("Select domain...")) domain = "";
            
            String audience = spinnerAudience.getSelectedItem().toString();
            if (audience.equals("Select audience...")) audience = "";
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Workspace name required", Toast.LENGTH_SHORT).show();
                return;
            }
            createWorkspace(name, description, domain, audience);
        });
    }

    private void createWorkspace(String name, String description, String domain, String audience) {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", sessionManager.getUserId());
        body.addProperty("action", "create");
        body.addProperty("workspace_name", name);
        body.addProperty("workspace_description", description);
        if (!domain.isEmpty()) body.addProperty("domain", domain);
        if (!audience.isEmpty()) body.addProperty("focus_audience", audience);

        GatewayService.callGateway("workspaces", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                if (!"success".equals(payload.get("status").getAsString())) {
                    runOnUiThread(() -> Toast.makeText(WorkspaceCreateActivity.this, "Create failed", Toast.LENGTH_SHORT).show());
                    return;
                }
                
                JsonObject workspace = payload.getAsJsonObject("workspace");
                sessionManager.saveWorkspace(workspace.get("workspace_id").getAsString(), workspace.get("workspace_name").getAsString());
                
                runOnUiThread(() -> startSystemLogAnimation());
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceCreateActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    private void startSystemLogAnimation() {
        animationOverlay.setVisibility(View.VISIBLE);
        
        Handler handler = new Handler(Looper.getMainLooper());
        
        for (int i = 0; i < SYSTEM_LOGS.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                tvSystemLogText.setText(SYSTEM_LOGS[index]);
            }, i * 800L);
        }
        
        handler.postDelayed(() -> {
            startActivity(new Intent(WorkspaceCreateActivity.this, WorkspaceSettingsActivity.class));
            finish();
        }, SYSTEM_LOGS.length * 800L + 500L);
    }
}
