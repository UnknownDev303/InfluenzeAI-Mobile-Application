package com.mota.marketingagent.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mota.marketingagent.R;
import com.mota.marketingagent.data.ApiConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import com.mota.marketingagent.data.ApiClient;

public class OAuthWebViewActivity extends AppCompatActivity {
    private static final String TAG = "OAuthWebView";
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_webview);

        webView = findViewById(R.id.oauthWebView);
        progressBar = findViewById(R.id.oauthProgressBar);
        ImageView btnClose = findViewById(R.id.btnOauthClose);

        btnClose.setOnClickListener(v -> finish());

        String url = getIntent().getStringExtra("url");
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "No Auth URL provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupWebView();
        webView.loadUrl(url);
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
                
                // If we successfully loaded the backend's oauth callback page
                if (url != null && url.contains(ApiConfig.BASE_URL) && url.contains("/oauth/callback")) {
                    Log.d(TAG, "Backend OAuth callback page finished loading. Closing.");
                    Toast.makeText(OAuthWebViewActivity.this, "Successfully Connected!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String loadedUrl = request.getUrl().toString();
                Log.d(TAG, "Loading URL: " + loadedUrl);

                // If LinkedIn attempts to redirect back to localhost, intercept and rewrite to Gateway
                if (loadedUrl.startsWith("http://localhost") || loadedUrl.startsWith("https://localhost")) {
                    progressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(OAuthWebViewActivity.this, "Completing Authentication...", Toast.LENGTH_SHORT).show();

                    Uri uri = Uri.parse(loadedUrl);
                    String path = uri.getPath() != null ? uri.getPath() : "/api/linkedin/oauth/callback";
                    String query = uri.getQuery();
                    
                    // Construct final URL
                    String newUrl = ApiConfig.BASE_URL + path + (query != null ? "?" + query : "");
                    Log.d(TAG, "Redirecting localhost to: " + newUrl);
                    
                    Map<String, String> extraHeaders = new HashMap<>();
                    extraHeaders.put("ngrok-skip-browser-warning", "1");
                    
                    view.loadUrl(newUrl, extraHeaders);
                    return true;
                }
                return false;
            }
        });
    }
}
