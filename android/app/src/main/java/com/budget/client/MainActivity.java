package com.budget.client;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultCallback;//ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts; //ActivityResultContracts
import androidx.activity.result.ActivityResult;
//import androidx.activity.
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Bridge;

//import WebViewActivity;
import com.budget.client.OAuthConstants;

import net.openid.appauthdemo.LoginActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//import net.openid.appauthdemo.AndroidWebViewActivity;
//import net.openid.appauthdemo.MyJsInterface;
//import net.openid.appauthdemo.OAuthConstants;

public class MainActivity extends BridgeActivity {
  WebView mWebView;
  Toast toastComon ;
  private long pressedTime;
  // You need to create a launcher variable inside onAttach or onCreate or global, i.e, before the activity is displayed
  ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    new ActivityResultCallback<ActivityResult>() {
      @Override
      public void onActivityResult(ActivityResult result) {
        Intent data = result.getData();
        if (result.getResultCode() == 2) {
          data.toString();
          //Toast.makeText(MainActivity.this," back from Browser "+data.getExtras().toString(), Toast.LENGTH_SHORT).show();
        //  toastComon.setText(" back from Browser "+data.getExtras().toString());
         // toastComon.show();
        }
        else{
          //Toast.makeText(MainActivity.this," back from Browser "+data.getExtras().toString(), Toast.LENGTH_SHORT).show();
          //toastComon.setText(" back from Browser issue ");
        }
      }
    });

  @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
  @Override
  public void onCreate(Bundle savedInstanceState){
      registerPlugin(EchoPlugin.class);
      super.onCreate(savedInstanceState);
   // startActivity(intent);
    WebView webView = getBridge().getWebView();
    WebSettings webSettings = webView.getSettings();
    toastComon = new Toast(getApplicationContext());
    webView.setWebViewClient(new WebViewClient(getBridge()) {

      @Override
      public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (url.contains("accounts.google")) {
        //  Toast.makeText(MainActivity.this,"InterceptRequ "+url, Toast.LENGTH_SHORT).show();
          //Toast.makeText(MainActivity.class, "onTouch " + url, Toast.LENGTH_SHORT).show();
          Bridge cu = getBridge();
          Intent intent = new Intent(cu.getContext(), WebViewActivity.class);
          intent.setData(Uri.parse(String.valueOf(url)));
          startActivity(intent);
        }
        return super.shouldInterceptRequest(view, url);
      }
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Uri url = request.getUrl();
    //   Toast.makeText(getBridge().getContext(), "shouldOverrideUrl "+url.getPath(), Toast.LENGTH_SHORT).show();
        if (url.getPath().contains("o/oauth2/v2")) {
          StringBuffer headBuf = new StringBuffer() ;
         /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            request.getRequestHeaders().forEach((k ,v ) -> { headBuf.append("k:").append(k).append("v: " ).append(v);} );
          }*/
        //  Toast.makeText(getBridge().getContext(), "req method  " + request.getMethod() + "re head "+headBuf.toString() , Toast.LENGTH_SHORT).show();
          Bridge cu = getBridge();
          //Intent intent = new Intent(cu.getContext(), WebViewActivity.class);
          Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
         // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(OAuthConstants.getOauthIdToken())));
         //    launchSomeActivity.launch(intent);
          startActivity(intent);
         // intent.setData();
           return true;
        }

        return bridge.launchIntent(url);
      }
    });
    webView. setOnTouchListener(new View.OnTouchListener() {

      public boolean onTouch(View v, MotionEvent event) {
        WebView.HitTestResult hr = ((WebView) v).getHitTestResult();
        if(hr.getExtra() != null) {
          Log.d(TAG, "clicked - url = " + hr.getExtra());
          Toast.makeText(MainActivity.this,"clicked "+hr.getExtra(), Toast.LENGTH_SHORT).show();
          if (hr.getExtra().contains("accounts.google")) {
            String url =hr.getExtra();
            //Toast.makeText(MainActivity.class, "onTouch " + url, Toast.LENGTH_SHORT).show();
             Bridge cu = getBridge();
            Intent intent = new Intent(cu.getContext(), WebViewActivity.class);
            intent.setData(Uri.parse(String.valueOf(url)));
            startActivity(intent);
          }
        }
        return false;
      }
    });
    webSettings.setJavaScriptEnabled(true); // Enable JavaScript if needed
    webView.addJavascriptInterface(new MyJsInterface(), "AndroidInterface");
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    /*mWebView.setWebChromeClient(new WebChromeClient() {
      @Override
      public boolean onJsAlert(WebView view, String url, String message,
                               final JsResult result) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        result.confirm();
        return true;
      }
    });*/
    /*setContentView(R.layout.activity_main);
    mWebView = (WebView) findViewById(R.id.home_wv_content);

    mWebView.setWebViewClient(new WebViewClient());
    mWebView.setWebChromeClient(new WebChromeClient());
    mWebView.getSettings().setJavaScriptEnabled(true);

    mWebView.getSettings().setDomStorageEnabled(true);
    mWebView.getSettings().setDatabaseEnabled(true);
    //mWebView.getSettings().setDatabasePath(dbpath); //check the documentation for info about dbpath
    mWebView.getSettings().setMinimumFontSize(1);
    mWebView.getSettings().setMinimumLogicalFontSize(1);

    mWebSettings.setAllowFileAccess(true);
     mWebSettings.setAllowContentAccess(true);
     */

  }

  @Override
  public void onBackPressed() {
    WebView webView = getBridge().getWebView();
    if (webView.canGoBack()) {
      //webView.goBack();
     // Toast.makeText(MainActivity.this,"Webview Can Go Back ", Toast.LENGTH_SHORT).show();
      if (pressedTime + 2000 > System.currentTimeMillis()) {
        super.onBackPressed();
        finish();
      } else {
        Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
      }
      pressedTime = System.currentTimeMillis();
    } else {
      Toast.makeText(MainActivity.this,"Back Not Allowed ", Toast.LENGTH_SHORT).show();

      super.onBackPressed();
    }

  }

  public void appendLog(String text)
  {
    File logFile = new File("file:///android_assets/asset/log.file");
    if (!logFile.exists())
    {
      try
      {
        logFile.createNewFile();
      }
      catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    try
    {
      //BufferedWriter for performance, true to set append to file flag
      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
      buf.append(text);
      buf.newLine();
      buf.close();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
