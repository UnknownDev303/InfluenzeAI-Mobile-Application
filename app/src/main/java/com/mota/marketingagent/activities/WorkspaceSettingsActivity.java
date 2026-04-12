package com.mota.marketingagent.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mota.marketingagent.R;
import com.mota.marketingagent.data.GatewayService;
import com.mota.marketingagent.data.SessionManager;

public class WorkspaceSettingsActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private boolean linkedinConnected = false;
    private boolean twitterConnected = false;

    private TextView linkedinStatus;
    private TextView twitterStatus;
    private MaterialButton btnLinkedinConnect;
    private MaterialButton btnLinkedinDisconnect;
    private MaterialButton btnTwitterConnect;
    private MaterialButton btnTwitterDisconnect;

    // Track current preferred_actor URN for highlighting
    private String currentPreferredUrn = null;

    private static final String[] TIMEZONES = {
        "UTC", "America/New_York", "America/Chicago", "America/Denver",
        "America/Los_Angeles", "America/Toronto", "America/Vancouver",
        "Europe/London", "Europe/Paris", "Europe/Berlin",
        "Asia/Dubai", "Asia/Kolkata", "Asia/Singapore", "Asia/Tokyo",
        "Australia/Sydney", "Pacific/Auckland"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_settings);

        sessionManager = new SessionManager(this);

        // ===== Wire UI elements =====
        TextView userName = findViewById(R.id.settingsUserName);
        TextView userEmail = findViewById(R.id.settingsUserEmail);
        TextView authMethod = findViewById(R.id.settingsAuthMethod);
        TextView workspaceName = findViewById(R.id.settingsWorkspaceName);
        TextView workspaceRole = findViewById(R.id.settingsWorkspaceRole);
        TextView inviteCode = findViewById(R.id.settingsInviteCode);
        TextInputEditText notificationEmail = findViewById(R.id.inputNotificationEmail);
        TextInputEditText inputDomain = findViewById(R.id.inputDomain);
        TextInputEditText inputAudience = findViewById(R.id.inputAudience);
        Spinner spinnerTimezone = findViewById(R.id.spinnerTimezone);
        ImageView avatarImage = findViewById(R.id.settingsUserAvatar);
        TextView avatarInitial = findViewById(R.id.settingsUserInitial);

        linkedinStatus = findViewById(R.id.linkedinStatus);
        twitterStatus = findViewById(R.id.twitterStatus);
        btnLinkedinConnect = findViewById(R.id.btnLinkedinConnect);
        btnLinkedinDisconnect = findViewById(R.id.btnLinkedinDisconnect);
        btnTwitterConnect = findViewById(R.id.btnTwitterConnect);
        btnTwitterDisconnect = findViewById(R.id.btnTwitterDisconnect);

        ImageView btnClose = findViewById(R.id.btnSettingsClose);
        MaterialButton btnSave = findViewById(R.id.btnSaveSettings);
        MaterialButton btnSaveStrategy = findViewById(R.id.btnSaveStrategy);
        MaterialButton btnInvite = findViewById(R.id.btnInvite);
        MaterialButton btnSwitch = findViewById(R.id.btnSwitchWorkspace);
        MaterialButton btnLeave = findViewById(R.id.btnLeaveWorkspace);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        // ===== Set initial user data =====
        userName.setText(sessionManager.getUserName());
        userEmail.setText(sessionManager.getUserEmail());
        authMethod.setText("GOOGLE ACCOUNT");
        notificationEmail.setText(sessionManager.getUserEmail());

        // ===== Dynamic Avatar =====
        loadUserAvatar(avatarImage, avatarInitial);

        // ===== Timezone Spinner =====
        ArrayAdapter<String> tzAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, TIMEZONES);
        tzAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimezone.setAdapter(tzAdapter);

        // ===== Button Listeners =====
        btnClose.setOnClickListener(v -> finish());
        btnSwitch.setOnClickListener(v -> {
            startActivity(new Intent(this, WorkspaceSelectionActivity.class));
            finish();
        });
        btnLeave.setOnClickListener(v -> leaveWorkspace());
        btnLogout.setOnClickListener(v -> {
            sessionManager.saveUser("", "", "");
            sessionManager.saveWorkspace("", "");
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });

        btnInvite.setOnClickListener(v -> {
            String code = inviteCode.getText() != null ? inviteCode.getText().toString() : "";
            if (!code.isEmpty() && !code.equals("—")) {
                android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("invite_code", code);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Invite code copied!", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> {
            String email = notificationEmail.getText() != null ? notificationEmail.getText().toString().trim() : "";
            saveNotificationEmail(email);
        });

        btnSaveStrategy.setOnClickListener(v -> {
            String domain = inputDomain.getText() != null ? inputDomain.getText().toString().trim() : "";
            String audience = inputAudience.getText() != null ? inputAudience.getText().toString().trim() : "";
            saveContentStrategy(domain, audience);
        });

        // ===== Integration Buttons =====
        btnLinkedinConnect.setOnClickListener(v -> openAuth("linkedin/auth-url"));
        btnTwitterConnect.setOnClickListener(v -> openAuth("twitter/auth-url"));
        btnLinkedinDisconnect.setOnClickListener(v -> disconnectPlatform("linkedin/disconnect", true));
        btnTwitterDisconnect.setOnClickListener(v -> disconnectPlatform("twitter/disconnect", false));

        // ===== Load all data =====
        loadWorkspaceDetails(workspaceName, workspaceRole, inviteCode, inputDomain, inputAudience, spinnerTimezone);
        loadPlatformStatus("linkedin/status", linkedinStatus, true);
        loadPlatformStatus("twitter/status", twitterStatus, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh integration statuses when returning from browser OAuth
        loadPlatformStatus("linkedin/status", linkedinStatus, true);
        loadPlatformStatus("twitter/status", twitterStatus, false);
    }

    /**
     * Loads user's Google profile image via Glide, or displays first-initial fallback.
     */
    private void loadUserAvatar(ImageView avatarImage, TextView avatarInitial) {
        String photoUrl = sessionManager.getUserPhotoUrl();
        String name = sessionManager.getUserName();

        if (!TextUtils.isEmpty(photoUrl)) {
            avatarImage.setVisibility(View.VISIBLE);
            avatarInitial.setVisibility(View.GONE);
            Glide.with(this)
                .load(photoUrl)
                .transform(new CircleCrop())
                .placeholder(R.drawable.bg_avatar_circle)
                .into(avatarImage);
        } else {
            avatarImage.setVisibility(View.GONE);
            avatarInitial.setVisibility(View.VISIBLE);
            String initial = !TextUtils.isEmpty(name) ? name.substring(0, 1).toUpperCase() : "U";
            avatarInitial.setText(initial);
        }
    }

    private void loadWorkspaceDetails(TextView name, TextView role, TextView invite,
                                       TextInputEditText domain, TextInputEditText audience,
                                       Spinner timezone) {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", sessionManager.getUserId());
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());

        GatewayService.callGateway("workspaces/details", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    JsonObject workspace = payload.has("workspace") ? payload.getAsJsonObject("workspace") : payload;
                    if (workspace.has("workspace_name")) {
                        name.setText(workspace.get("workspace_name").getAsString());
                    }
                    if (workspace.has("user_role")) {
                        role.setText(workspace.get("user_role").getAsString());
                    }
                    if (workspace.has("invite_code")) {
                        invite.setText(workspace.get("invite_code").getAsString());
                    }

                    // Load settings
                    if (workspace.has("settings") && workspace.get("settings").isJsonObject()) {
                        JsonObject settings = workspace.getAsJsonObject("settings");
                        if (settings.has("domain") && !settings.get("domain").isJsonNull()) {
                            domain.setText(settings.get("domain").getAsString());
                        }
                        if (settings.has("focus_audience") && !settings.get("focus_audience").isJsonNull()) {
                            audience.setText(settings.get("focus_audience").getAsString());
                        }
                        if (settings.has("timezone") && !settings.get("timezone").isJsonNull()) {
                            String tz = settings.get("timezone").getAsString();
                            for (int i = 0; i < TIMEZONES.length; i++) {
                                if (TIMEZONES[i].equals(tz)) {
                                    timezone.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSettingsActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadPlatformStatus(String route, TextView statusLabel, boolean isLinkedin) {
        JsonObject body = new JsonObject();
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("user_id", sessionManager.getUserId());

        GatewayService.callGateway(route, "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    boolean connected = payload.has("connected") && payload.get("connected").getAsBoolean();
                    updatePlatformUI(statusLabel, connected, payload, isLinkedin);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    updatePlatformUI(statusLabel, false, null, isLinkedin);
                });
            }
        });
    }

    private void updatePlatformUI(TextView statusLabel, boolean connected, JsonObject payload, boolean isLinkedin) {
        if (connected) {
            statusLabel.setText("Connected");
            statusLabel.setTextColor(getColor(R.color.green_700));
            statusLabel.setBackgroundResource(R.drawable.bg_status_connected);
        } else {
            statusLabel.setText("Disconnected");
            statusLabel.setTextColor(getColor(R.color.slate_500));
            statusLabel.setBackgroundResource(R.drawable.bg_status_disconnected);
        }

        if (isLinkedin) {
            linkedinConnected = connected;
            btnLinkedinConnect.setVisibility(connected ? View.GONE : View.VISIBLE);
            btnLinkedinDisconnect.setVisibility(connected ? View.VISIBLE : View.GONE);
            
            LinearLayout detailsContainer = findViewById(R.id.linkedinDetailsContainer);
            if (connected && payload != null) {
                detailsContainer.setVisibility(View.VISIBLE);
                
                TextView memberName = findViewById(R.id.linkedinMemberName);
                TextView memberHeadline = findViewById(R.id.linkedinMemberHeadline);
                LinearLayout pagesList = findViewById(R.id.linkedinPagesList);
                
                // Extract current preferred_actor URN
                if (payload.has("preferred_actor") && payload.get("preferred_actor").isJsonObject()) {
                    JsonObject pref = payload.getAsJsonObject("preferred_actor");
                    currentPreferredUrn = pref.has("urn") ? pref.get("urn").getAsString() : null;
                }

                String memberUrn = null;
                if (payload.has("member") && !payload.get("member").isJsonNull()) {
                    JsonObject member = payload.getAsJsonObject("member");
                    String fName = member.has("firstName") ? member.get("firstName").getAsString() : "";
                    String lName = member.has("lastName") ? member.get("lastName").getAsString() : "";
                    String fullName = (fName + " " + lName).trim();
                    memberName.setText(fullName);
                    memberUrn = member.has("urn") ? member.get("urn").getAsString() : null;
                    
                    if (member.has("headline") && !member.get("headline").isJsonNull()) {
                        memberHeadline.setText(member.get("headline").getAsString());
                    } else {
                        memberHeadline.setText("Member");
                    }
                }
                
                pagesList.removeAllViews();

                // Add personal profile as a selectable card
                if (memberUrn != null) {
                    String mName = memberName.getText().toString();
                    if (TextUtils.isEmpty(mName)) mName = "Personal LinkedIn Profile";
                    addLinkedinPageCard(pagesList, mName, "Personal profile", memberUrn, "member");
                }

                // Add organization pages as selectable cards
                if (payload.has("organizations") && payload.get("organizations").isJsonArray()) {
                    JsonArray orgs = payload.getAsJsonArray("organizations");
                    for (int i = 0; i < orgs.size(); i++) {
                        JsonObject org = orgs.get(i).getAsJsonObject();
                        String orgName = org.has("name") ? org.get("name").getAsString() : 
                            (org.has("localizedName") ? org.get("localizedName").getAsString() : "Organization");
                        String orgUrn = org.has("urn") ? org.get("urn").getAsString() : null;
                        if (orgUrn != null) {
                            addLinkedinPageCard(pagesList, orgName, "Organization page", orgUrn, "organization");
                        }
                    }
                }
            } else {
                detailsContainer.setVisibility(View.GONE);
            }
        } else {
            twitterConnected = connected;
            btnTwitterConnect.setVisibility(connected ? View.GONE : View.VISIBLE);
            btnTwitterDisconnect.setVisibility(connected ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Creates a clickable LinkedIn page/member card that sends preference updates to the backend.
     */
    private void addLinkedinPageCard(LinearLayout parent, String name, String subtitle, String urn, String type) {
        boolean isSelected = urn != null && urn.equals(currentPreferredUrn);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dpToPx(4), 0, dpToPx(4));
        card.setLayoutParams(params);
        card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(10));
        bg.setStroke(dpToPx(1), isSelected ? Color.parseColor("#1e293b") : Color.parseColor("#e2e8f0"));
        bg.setColor(isSelected ? Color.parseColor("#1e293b") : Color.WHITE);
        card.setBackground(bg);

        // Name label
        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextSize(14f);
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#0f172a"));
        card.addView(tvName);

        // Subtitle label
        TextView tvSub = new TextView(this);
        tvSub.setText(subtitle);
        tvSub.setTextSize(11f);
        tvSub.setTextColor(isSelected ? Color.parseColor("#cbd5e1") : Color.parseColor("#64748b"));
        tvSub.setPadding(0, dpToPx(2), 0, 0);
        card.addView(tvSub);

        // Selection indicator
        if (isSelected) {
            TextView tvActive = new TextView(this);
            tvActive.setText("✓ Active");
            tvActive.setTextSize(10f);
            tvActive.setTextColor(Color.parseColor("#4ade80"));
            tvActive.setPadding(0, dpToPx(4), 0, 0);
            card.addView(tvActive);
        }

        card.setOnClickListener(v -> {
            if (isSelected) return; // already active
            setLinkedinPreference(urn, name, type);
        });

        parent.addView(card);
    }

    /**
     * Sends the user's LinkedIn page preference to the backend.
     */
    private void setLinkedinPreference(String urn, String name, String type) {
        JsonObject actor = new JsonObject();
        actor.addProperty("type", type);
        actor.addProperty("urn", urn);
        actor.addProperty("name", name);

        JsonObject body = new JsonObject();
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("user_id", sessionManager.getUserId());
        body.add("preferred_actor", actor);

        Toast.makeText(this, "Updating preference...", Toast.LENGTH_SHORT).show();

        GatewayService.callGateway("linkedin/preference", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    Toast.makeText(WorkspaceSettingsActivity.this, "Posting preference updated", Toast.LENGTH_SHORT).show();
                    currentPreferredUrn = urn;
                    // Refresh LinkedIn status to rebuild the cards with new selection
                    loadPlatformStatus("linkedin/status", linkedinStatus, true);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSettingsActivity.this, "Failed: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
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
                    Intent intent = new Intent(WorkspaceSettingsActivity.this, OAuthWebViewActivity.class);
                    intent.putExtra("url", url);
                    startActivity(intent);
                }
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSettingsActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void disconnectPlatform(String route, boolean isLinkedin) {
        JsonObject body = new JsonObject();
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        body.addProperty("user_id", sessionManager.getUserId());

        GatewayService.callGateway(route, "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    Toast.makeText(WorkspaceSettingsActivity.this,
                        (isLinkedin ? "LinkedIn" : "Twitter") + " disconnected", Toast.LENGTH_SHORT).show();
                    updatePlatformUI(isLinkedin ? linkedinStatus : twitterStatus, false, null, isLinkedin);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSettingsActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveNotificationEmail(String email) {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", sessionManager.getUserId());
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        JsonObject settings = new JsonObject();
        settings.addProperty("notification_email", email);
        body.add("settings", settings);

        GatewayService.callGateway("workspaces/update-settings", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSettingsActivity.this, "Saved", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSettingsActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveContentStrategy(String domain, String audience) {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", sessionManager.getUserId());
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());
        JsonObject settings = new JsonObject();
        settings.addProperty("domain", domain);
        settings.addProperty("focus_audience", audience);
        body.add("settings", settings);

        GatewayService.callGateway("workspaces/update-settings", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSettingsActivity.this, "Domain & audience saved", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSettingsActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void leaveWorkspace() {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", sessionManager.getUserId());
        body.addProperty("workspace_id", sessionManager.getWorkspaceId());

        GatewayService.callGateway("workspaces/leave", "POST", body, null, new GatewayService.GatewayCallback() {
            @Override
            public void onSuccess(JsonObject payload) {
                runOnUiThread(() -> {
                    Toast.makeText(WorkspaceSettingsActivity.this, "Left workspace", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(WorkspaceSettingsActivity.this, WorkspaceSelectionActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(WorkspaceSettingsActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
