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
        if ("file".equals(uri.getScheme()) || "https".equals(uri.getScheme()) && ("www.youtube.com".equals(uri.getHost()) || "www.youtube-nocookie.com".equals(uri.getHost()))) return false;
        try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
        return true;
      }
    });
    web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
  }
  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
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