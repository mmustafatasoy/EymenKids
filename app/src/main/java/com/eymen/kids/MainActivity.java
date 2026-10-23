package com.eymen.kids;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.content.Intent;
import android.net.Uri;
public class MainActivity extends Activity {
 WebView web;
 @Override public void onCreate(Bundle b) {
  super.onCreate(b);
  web=new WebView(this);setContentView(web);
  web.getSettings().setJavaScriptEnabled(true);
  web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setAllowFileAccess(true);
  web.setWebChromeClient(new WebChromeClient());
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r) {
    Uri u=r.getUrl();
    if (!"file".equals(u.getScheme())) {
     try {startActivity(new Intent(Intent.ACTION_VIEW,u));} catch(Exception ignored){}
     return true;
    }
    return false;
   }
  });
  web.loadUrl("file:///android_asset/index.html");
 }
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}