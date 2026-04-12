package com.mota.marketingagent.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.mota.marketingagent.R;
import com.mota.marketingagent.data.GatewayService;
import com.mota.marketingagent.data.SessionManager;

public class IntegrationActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private TextView linkedinStatus;
    private TextView twitterStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integration);

        sessionManager = new SessionManager(this);
        linkedinStatus = findViewById(R.id.linkedinStatus);
        twitterStatus = findViewById(R.id.twitterStatus);

        MaterialButton btnLinkedinConnect = findViewById(R.id.btnLinkedinConnect);
        MaterialButton btnLinkedinDisconnect = findViewById(R.id.btnLinkedinDisconnect);
        MaterialButton btnTwitterConnect = findViewById(R.id.btnTwitterConnect);
        MaterialButton btnTwitterDisconnect = findViewById(R.id.btnTwitterDisconnect);
        MaterialButton btnContinue = findViewById(R.id.btnIntegrationContinue);

        btnLinkedinConnect.setOnClickListener(v -> openAuth("linkedin/auth-url"));
        btnTwitterConnect.setOnClickListener(v -> openAuth("twitter/auth-url"));
        btnLinkedinDisconnect.setOnClickListener(v -> disconnect("linkedin/disconnect"));
        btnTwitterDisconnect.setOnClickListener(v -> disconnect("twitter/disconnect"));
        btnContinue.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));

        loadStatuses();
    }

    private void loadStatuses() {
        loadStatus("linkedin/status", linkedinStatus);
        loadStatus("twitter/status", twitterStatus);
    }

    private void loadStatus(String route, TextView label) {
        JsonObject body = new JsonObject();
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("user_id", sessionManager.getUserId());
        GatewayService.callGateway(route, "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    boolean connected = payload.has("connected") && payload.get("connected").getAsBoolean();
                    label.setText("Status: " + (connected ? "Connected" : "Not connected"));
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(IntegrationActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openAuth(String route) {
        JsonObject body = new JsonObject();
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("user_id", sessionManager.getUserId());

        GatewayService.callGateway(route, "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                if (payload.has("auth_url")) {
                    String url = payload.get("auth_url").getAsString();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(IntegrationActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void disconnect(String route) {
        JsonObject body = new JsonObject();
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("user_id", sessionManager.getUserId());

        GatewayService.callGateway(route, "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                loadStatuses();
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(IntegrationActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
