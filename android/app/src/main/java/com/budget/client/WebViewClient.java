package com.budget.client;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.getcapacitor.Bridge;
public class WebViewClient extends android.webkit.WebViewClient {

  private Bridge bridge;
  private int viewHeightOffset = 300;
  private int viewWidthOffset = 1;
  double  newScale = 0;
  public WebViewClient(Bridge bridge) {
    this.bridge = bridge;
  }

  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
    //bridge.getLocalServer().hostAssets();
    return bridge.getLocalServer().shouldInterceptRequest(request);
  }

 // @Nullable



  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    return bridge.launchIntent(Uri.parse(url));
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    super.onPageFinished(view, url);
    // This setting is changing the zoom
   // Toast.makeText(this.bridge.getContext(), "onPageFinished "+url, Toast.LENGTH_SHORT).show();

    ////view.loadUrl("javascript:document.body.style.zoom = "+ String.valueOf(getScale(view)/3)  +";");
    try {
      Thread.sleep(2000);
      //Toast.makeText(this.bridge.getContext(), " new scale "+view.getHeight() +": "+view.getWidth(), Toast.LENGTH_SHORT).show();

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private double getScale(View view) {
    Toast.makeText(this.bridge.getContext(), "scale "+view.getHeight() +": "+view.getWidth(), Toast.LENGTH_SHORT).show();
    double  newScale  = (view.getHeight()+ viewHeightOffset)/ ((double) view.getWidth() -viewWidthOffset);


    return (newScale);
  }

  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {

    Toast.makeText(this.bridge.getContext(), "onPageStarted ", Toast.LENGTH_SHORT).show();


    super.onPageStarted(view, url, favicon);
    //bridge.reset();
  }
}
