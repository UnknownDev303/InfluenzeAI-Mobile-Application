package com.mota.marketingagent.data;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSource;

public final class GatewayService {
    private static final String TAG = "GatewayService";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private static final ExecutorService streamExecutor = Executors.newSingleThreadExecutor();

    public interface GatewayCallback {
        void onSuccess(JsonObject payload);
        void onError(String message);
    }

    public interface GatewayStreamListener {
        void onEvent(JsonObject event);
        void onError(String message);
        void onComplete();
    }

    private GatewayService() {}

    public static void callChatNonStreaming(JsonObject body, GatewayCallback callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("route", "chat");
        payload.addProperty("method", "POST");
        payload.add("body", body == null ? new JsonObject() : body);
        payload.add("query", new JsonObject());
        payload.addProperty("access_token", ApiConfig.GATEWAY_TOKEN);

        RequestBody requestBody = RequestBody.create(payload.toString(), JSON);
        Request request = new Request.Builder()
            .url(ApiConfig.BASE_URL + "/api/gateway")
            .addHeader("ngrok-skip-browser-warning", "1")
            .post(requestBody)
            .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Chat call failed", e);
                callback.onError(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP " + response.code());
                    return;
                }

                StringBuilder text = new StringBuilder();
                JsonArray toolResults = new JsonArray();
                JsonArray statuses = new JsonArray();
                String responseId = null;

                if (response.body() != null) {
                    BufferedSource source = response.body().source();
                    while (!source.exhausted()) {
                        String line = source.readUtf8Line();
                        if (line == null || line.trim().isEmpty()) {
                            continue;
                        }
                        try {
                            JsonObject event = gson.fromJson(line, JsonObject.class);
                            String type = event.has("type") ? event.get("type").getAsString() : "";
                            if ("delta".equals(type)) {
                                if (event.has("content")) {
                                    text.append(event.get("content").getAsString());
                                }
                                if (event.has("response_id")) {
                                    responseId = event.get("response_id").getAsString();
                                }
                            } else if ("tool_result".equals(type)) {
                                JsonObject entry = new JsonObject();
                                if (event.has("tool_name")) {
                                    entry.addProperty("tool_name", event.get("tool_name").getAsString());
                                }
                                if (event.has("result") && event.get("result").isJsonObject()) {
                                    entry.add("result", event.getAsJsonObject("result"));
                                }
                                toolResults.add(entry);
                            } else if ("status".equals(type)) {
                                if (event.has("status")) {
                                    statuses.add(event.get("status").getAsString());
                                }
                            } else if ("done".equals(type)) {
                                if (event.has("response_id")) {
                                    responseId = event.get("response_id").getAsString();
                                }
                            }
                        } catch (Exception parseError) {
                            Log.e(TAG, "Chat parse failed", parseError);
                        }
                    }
                }

                JsonObject result = new JsonObject();
                result.addProperty("text", text.toString());
                result.add("tool_results", toolResults);
                result.add("statuses", statuses);
                if (responseId != null) {
                    result.addProperty("response_id", responseId);
                }
                callback.onSuccess(result);
            }
        });
    }

    public static void callGateway(String route, String method, JsonObject body, JsonObject query, GatewayCallback callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("route", route);
        payload.addProperty("method", method);
        payload.add("body", body == null ? new JsonObject() : body);
        payload.add("query", query == null ? new JsonObject() : query);
        payload.addProperty("access_token", ApiConfig.GATEWAY_TOKEN);

        RequestBody requestBody = RequestBody.create(payload.toString(), JSON);
        Request request = new Request.Builder()
            .url(ApiConfig.BASE_URL + "/api/gateway")
            .addHeader("ngrok-skip-browser-warning", "1")
            .post(requestBody)
            .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Gateway call failed", e);
                callback.onError(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP " + response.code());
                    return;
                }
                String responseBody = response.body() != null ? response.body().string() : "{}";
                JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                callback.onSuccess(json);
            }
        });
    }

    public static void streamChat(JsonObject body, GatewayStreamListener listener) {
        JsonObject payload = new JsonObject();
        payload.addProperty("route", "chat");
        payload.addProperty("method", "POST");
        payload.add("body", body == null ? new JsonObject() : body);
        payload.add("query", new JsonObject());
        payload.addProperty("access_token", ApiConfig.GATEWAY_TOKEN);

        RequestBody requestBody = RequestBody.create(payload.toString(), JSON);
        Request request = new Request.Builder()
            .url(ApiConfig.BASE_URL + "/api/gateway")
            .addHeader("ngrok-skip-browser-warning", "1")
            .post(requestBody)
            .build();

        streamExecutor.execute(() -> {
            try (Response response = ApiClient.getClient().newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    listener.onError("HTTP " + response.code());
                    return;
                }
                BufferedSource source = response.body().source();
                while (!source.exhausted()) {
                    String line = source.readUtf8Line();
                    if (line == null || line.trim().isEmpty()) {
                        continue;
                    }
                    try {
                        JsonObject event = gson.fromJson(line, JsonObject.class);
                        listener.onEvent(event);
                    } catch (Exception parseError) {
                        listener.onError("Parse error: " + parseError.getMessage());
                    }
                }
                listener.onComplete();
            } catch (IOException e) {
                Log.e(TAG, "Gateway stream failed", e);
                listener.onError(e.toString());
            }
        });
    }
}
