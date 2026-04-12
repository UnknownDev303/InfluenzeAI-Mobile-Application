package com.mota.marketingagent.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.mota.marketingagent.R;
import com.mota.marketingagent.data.GatewayService;
import com.mota.marketingagent.data.SessionManager;
import com.mota.marketingagent.ui.helpers.MarkdownHelper;

public class PublishDraftActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private String draftContent;
    
    private EditText contentEditor;
    private TextView charCountText;
    private TextView statusText;
    private View btnPublish;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_draft);

        sessionManager = new SessionManager(this);
        
        draftContent = getIntent().getStringExtra("draft_content");
        if (draftContent == null) draftContent = "";

        // UI References
        findViewById(R.id.btnPublishClose).setOnClickListener(v -> finish());
        btnPublish = findViewById(R.id.btnPublishPost);
        contentEditor = findViewById(R.id.publishContentEditor);
        charCountText = findViewById(R.id.publishCharCount);
        statusText = findViewById(R.id.publishStatus);
        
        TextView authorName = findViewById(R.id.publishAuthorName);
        TextView avatarInitial = findViewById(R.id.publishAvatarInitial);

        // Set initial user info
        String userName = sessionManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            authorName.setText(userName);
            avatarInitial.setText(userName.substring(0, 1).toUpperCase());
        } else {
            authorName.setText("User");
            avatarInitial.setText("U");
        }

        // Set content and stripped markdown
        String unrendered = draftContent;
        // Basic naive markdown stripping for editor
        unrendered = unrendered.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
        unrendered = unrendered.replaceAll("\\*(.*?)\\*", "$1");
        contentEditor.setText(unrendered);

        contentEditor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                int count = s.length();
                charCountText.setText(count + " characters");
                if (count > 3000) {
                    charCountText.setTextColor(0xFFDC2626); // red for warning
                } else {
                    charCountText.setTextColor(0xFF64748B); // slate
                }
            }
        });
        
        // Trigger initial count
        charCountText.setText(contentEditor.getText().length() + " characters");

        btnPublish.setOnClickListener(v -> publishPost());
    }

    private void publishPost() {
        String finalContent = contentEditor.getText().toString().trim();
        if (finalContent.isEmpty()) {
            Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPublish.setEnabled(false);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText("Publishing to LinkedIn...");
        statusText.setTextColor(0xFF64748B); // Initial publishing color

        JsonObject body = new JsonObject();
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("user_id", sessionManager.getUserId());
        body.addProperty("content", finalContent);

        GatewayService.callGateway("linkedin/post/direct", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    btnPublish.setEnabled(true);
                    if (payload.has("status") && "success".equals(payload.get("status").getAsString())) {
                        statusText.setText("✅ Published successfully!");
                        statusText.setTextColor(0xFF059669); // Green
                        
                        // Close activity after 1.5 seconds
                        statusText.postDelayed(PublishDraftActivity.this::finish, 1500);
                    } else {
                        String err = payload.has("error") ? payload.get("error").getAsString() : "Unknown error";
                        statusText.setText("❌ Failed: " + err);
                        statusText.setTextColor(0xFFDC2626); // Red
                        showErrorDialog("Publish Failed", err);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnPublish.setEnabled(true);
                    statusText.setText("❌ Network error.");
                    statusText.setTextColor(0xFFDC2626); // Red
                    showErrorDialog("Network Error", message);
                });
            }
        });
    }
    
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
}
