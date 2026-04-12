package com.mota.marketingagent.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "mota_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHOTO = "user_photo_url";
    private static final String KEY_WORKSPACE_ID = "workspace_id";
    private static final String KEY_WORKSPACE_NAME = "workspace_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveUser(String userId, String name, String email, String photoUrl) {
        SharedPreferences.Editor editor = prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email);
        if (photoUrl != null) {
            editor.putString(KEY_USER_PHOTO, photoUrl);
        }
        editor.apply();
    }

    /** Backward-compatible overload (no photo) */
    public void saveUser(String userId, String name, String email) {
        saveUser(userId, name, email, null);
    }

    public void saveWorkspace(String workspaceId, String workspaceName) {
        prefs.edit()
            .putString(KEY_WORKSPACE_ID, workspaceId)
            .putString(KEY_WORKSPACE_NAME, workspaceName)
            .apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserPhotoUrl() {
        return prefs.getString(KEY_USER_PHOTO, null);
    }

    public String getWorkspaceId() {
        return prefs.getString(KEY_WORKSPACE_ID, null);
    }

    public String getWorkspaceName() {
        return prefs.getString(KEY_WORKSPACE_NAME, "");
    }
}
