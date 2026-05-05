package com.mobileforge;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends Activity {
    private WebView webView;
    private String appDataDir;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        webView = new WebView(this);
        setContentView(webView);

        appDataDir = getFilesDir().getAbsolutePath();
        
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        webView.setWebViewClient(new MyWebViewClient());
        
        webView.addJavascriptInterface(new MobileForgeJS(), "MobileForge");
        
        webView.loadUrl("file://" + appDataDir + "/www/index.html");
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("file://")) {
                view.loadUrl(url);
                return true;
            }
            return false;
        }
    }

    class MobileForgeJS {
        @JavascriptInterface
        public String readFile(String path) {
            try {
                File file = new File(path);
                if (!file.exists()) return "ERROR: File not found";
                FileReader reader = new FileReader(file);
                char[] buffer = new char[(int) file.length()];
                reader.read(buffer);
                reader.close();
                return new String(buffer);
            } catch (IOException e) {
                return "ERROR: " + e.getMessage();
            }
        }

        @JavascriptInterface
        public String writeFile(String path, String content) {
            try {
                File file = new File(path);
                FileWriter writer = new FileWriter(file);
                writer.write(content);
                writer.close();
                return "OK";
            } catch (IOException e) {
                return "ERROR: " + e.getMessage();
            }
        }

        @JavascriptInterface
        public String listDir(String path) {
            try {
                File dir = new File(path);
                if (!dir.exists() || !dir.isDirectory()) return "ERROR: Not a directory";
                String[] files = dir.list();
                if (files == null) return "ERROR: Cannot list directory";
                StringBuilder sb = new StringBuilder();
                for (String f : files) {
                    File child = new File(path + "/" + f);
                    sb.append(f).append("|").append(child.isDirectory() ? "dir" : "file").append("\n");
                }
                return sb.toString();
            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            }
        }

        @JavascriptInterface
        public String getAppDataDir() {
            return appDataDir;
        }

        @JavascriptInterface
        public String getWorkspaceDir() {
            return "/home/workspace";
        }

        @JavascriptInterface
        public void showToast(String message) {
            android.widget.Toast.makeText(MainActivity.this, message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}