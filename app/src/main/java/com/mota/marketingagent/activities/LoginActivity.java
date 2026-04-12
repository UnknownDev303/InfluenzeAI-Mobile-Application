package com.mota.marketingagent.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.mota.marketingagent.R;
import com.mota.marketingagent.data.GatewayService;
import com.mota.marketingagent.data.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient signInClient;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        progressBar = findViewById(R.id.loginProgress);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();
        signInClient = GoogleSignIn.getClient(this, gso);

        MaterialButton btnLogin = findViewById(R.id.btnGoogleLogin);
        btnLogin.setOnClickListener(v -> launchSignIn());
    }

    private void launchSignIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignIn(task);
        }
    }

    private void handleSignIn(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account == null) {
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);

            JsonObject body = new JsonObject();
            body.addProperty("auth_method", "google");
            body.addProperty("google_id", account.getId());
            body.addProperty("email", account.getEmail());
            body.addProperty("name", account.getDisplayName());
            if (account.getPhotoUrl() != null) {
                body.addProperty("picture", account.getPhotoUrl().toString());
            }

            GatewayService.callGateway("auth_user", "POST", body, null, new GatewayService.GatewayCallback() {
                @Override
                public void onSuccess(JsonObject payload) {
                    runOnUiThread(() -> handleAuthSuccess(payload));
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        showError("Login error", message);
                    });
                }
            });
        } catch (ApiException e) {
            showError("Sign in error", "Status code: " + e.getStatusCode() + "\n" + e.toString());
        }
    }

    private void showError(String title, String message) {
        String safeMessage = (message == null || message.trim().isEmpty()) ? "Unknown error" : message;
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(safeMessage)
            .setPositiveButton("OK", null)
            .show();
    }

    private void handleAuthSuccess(JsonObject payload) {
        progressBar.setVisibility(View.GONE);
        if (!payload.has("status") || !"success".equals(payload.get("status").getAsString())) {
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
            return;
        }
        JsonObject user = payload.getAsJsonObject("user");
        String userId = user.get("user_id").getAsString();
        String name = user.has("name") ? user.get("name").getAsString() : "";
        String email = user.has("email") ? user.get("email").getAsString() : "";
        String photoUrl = user.has("picture") ? user.get("picture").getAsString() : null;
        sessionManager.saveUser(userId, name, email, photoUrl);

        if (user.has("workspace_id")) {
            String workspaceId = user.get("workspace_id").getAsString();
            String workspaceName = user.has("workspace_name") ? user.get("workspace_name").getAsString() : "";
            sessionManager.saveWorkspace(workspaceId, workspaceName);
            startActivity(new Intent(this, ChatActivity.class));
        } else {
            startActivity(new Intent(this, WorkspaceSelectionActivity.class));
        }
        finish();
    }
}
