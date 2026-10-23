package com.eymen.kids;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import androidx.webkit.WebViewAssetLoader;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
  private WebView web;
  private ValueCallback<Uri[]> fileCallback;
  private static final int PICK_VIDEO = 101;
  private static final int PICK_NATIVE_VIDEO = 102;
  private View fullscreenView;
  private WebChromeClient.CustomViewCallback fullscreenCallback;
  private FrameLayout container;

  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    container = new FrameLayout(this);
    final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder().addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
    web = new WebView(this);
    container.addView(web);
    setContentView(container);
    WebSettings settings = web.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setUserAgentString(settings.getUserAgentString() + " EymenKidsAndroid/1.3");
    settings.setDomStorageEnabled(true);
    settings.setAllowFileAccess(false);
    settings.setMediaPlaybackRequiresUserGesture(false);
    web.setWebChromeClient(new WebChromeClient() {
      @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
        if (fileCallback != null) fileCallback.onReceiveValue(null);
        fileCallback = callback;
        try {
          Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
          pick.addCategory(Intent.CATEGORY_OPENABLE);
          pick.setType("video/*");
          startActivityForResult(Intent.createChooser(pick, "Video seç"), PICK_VIDEO);
          return true;
        } catch (Exception e) {
          fileCallback = null;
          return false;
        }
      }
      @Override public void onShowCustomView(View view, CustomViewCallback callback) {
        if (fullscreenView != null) { callback.onCustomViewHidden(); return; }
        fullscreenView = view;
        fullscreenCallback = callback;
        web.setVisibility(View.GONE);
        container.addView(view, new FrameLayout.LayoutParams(-1,-1));
      }
      @Override public void onHideCustomView() { closeFullscreen(); }
    });
    web.setWebViewClient(new WebViewClient() {
      @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return assetLoader.shouldInterceptRequest(request.getUrl());
      }
      @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Uri uri = request.getUrl();
        if ("eymenkids".equals(uri.getScheme())) {
          if (!request.isForMainFrame()) return true;
          if ("play".equals(uri.getHost())) {
            String file = uri.getQueryParameter("file");
            String title = uri.getQueryParameter("title");
            if (file != null && file.matches("[a-z0-9][a-z0-9-]*\\.(mp4|m4v|webm)")) {
              Intent nativePlayer = new Intent(MainActivity.this, OfflinePlayerActivity.class);
              nativePlayer.putExtra(OfflinePlayerActivity.EXTRA_ASSET_FILE, file);
              nativePlayer.putExtra(OfflinePlayerActivity.EXTRA_TITLE, title);
              startActivity(nativePlayer);
            } else {
              android.widget.Toast.makeText(MainActivity.this, "Geçersiz yerel dosya", android.widget.Toast.LENGTH_SHORT).show();
            }
          } else if ("pick".equals(uri.getHost())) {
            try {
              Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
              picker.addCategory(Intent.CATEGORY_OPENABLE);
              picker.setType("video/*");
              picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
              startActivityForResult(picker, PICK_NATIVE_VIDEO);
            } catch (Exception e) {
              android.widget.Toast.makeText(MainActivity.this, "Video seçici açılamadı", android.widget.Toast.LENGTH_SHORT).show();
            }
          }
          return true;
        }
        if ("https".equals(uri.getScheme()) && "appassets.androidplatform.net".equals(uri.getHost())) return false;
        try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
        return true;
      }
    });
    web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
  }
  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PICK_NATIVE_VIDEO) {
      if (resultCode == RESULT_OK && data != null && data.getData() != null) {
        Uri selectedVideo = data.getData();
        Intent nativePlayer = new Intent(this, OfflinePlayerActivity.class);
        nativePlayer.setData(selectedVideo);
        nativePlayer.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        nativePlayer.putExtra(OfflinePlayerActivity.EXTRA_TITLE, "Telefondaki Video");
        startActivity(nativePlayer);
      }
      return;
    }
    if (requestCode == PICK_VIDEO && fileCallback != null) {
      Uri[] result = resultCode == RESULT_OK && data != null && data.getData() != null ? new Uri[]{data.getData()} : null;
      fileCallback.onReceiveValue(result);
      fileCallback = null;
    }
  }
  private void closeFullscreen() {
    if (fullscreenView == null) return;
    container.removeView(fullscreenView);
    fullscreenView = null;
    web.setVisibility(View.VISIBLE);
    if (fullscreenCallback != null) fullscreenCallback.onCustomViewHidden();
    fullscreenCallback = null;
  }
  @Override public void onBackPressed() {
    if (fullscreenView != null) { closeFullscreen(); return; }
    web.evaluateJavascript("String(window.eymenBack && window.eymenBack())", result -> {
      if (!"\"true\"".equals(result)) MainActivity.super.onBackPressed();
    });
  }
  @Override protected void onDestroy() {
    if (fileCallback != null) { fileCallback.onReceiveValue(null); fileCallback = null; }
    web.destroy();
    super.onDestroy();
  }
}