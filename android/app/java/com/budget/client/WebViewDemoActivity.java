package com.budget.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import net.openid.appauthdemo.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WebViewDemoActivity<Store_locator> extends Activity {
  WebView dshB;
  private static final String TAG = "WebViewDemoActivity";
  ProgressDialog _dialog ;
  @SuppressLint("SetJavaScriptEnabled")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_dashboard);

    WebView dshB = findViewById(R.id.dashboard_web_view);

    WebSettings webSettings = dshB.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setAllowFileAccess(true);
    webSettings.setAllowContentAccess(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//      webSettings.setMixedContentMode(0);
//      dshB.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//    } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//      dshB.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//    } else {
      dshB.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

    //Another option is to set the webview to be see-through.
    //
    //dshB.setBackgroundColor(0x00000000);

    String hardtoken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjkiLCJhY2NvdW50SWQiOiIzIiwiZW1haWwiOiJhbnZla2FyLnYuYW5hbmRpQGdtYWlsLmNvbSIsInJvbGUiOiJPV05FUiIsImNvbmZpcm1lZCI6ZmFsc2UsImlhdCI6MTcwMzg2Mjk3NiwiZXhwIjoxNzAzODYzNTc2fQ.S1Mjrh1we7BnrNrcKX1UzSqOsf1YxHiXx-6LjCFSdnU";

    //WebView dshB = findViewById(R.id.dashboard_view);
    String cctoken = getIntent().getExtras().getString("jwt_token");
    String id_token = getIntent().getExtras().getString("id_token");
    String state = getIntent().getExtras().getString("state");
    //dshB.setWebChromeClient();
    String cctokenWithBearer = getIntent().getExtras().getString("jwt_token");
    int berIndx = cctoken.indexOf("Bearer ") + "Bearer ".length()-1;

    cctoken = cctoken.substring(berIndx);
    HashMap<String, String> hMap = new HashMap<>();
    hMap.put("Authorization",cctokenWithBearer);
    hMap.put("id_token",id_token);
    hMap.put("state",state);

    dshB.setWebViewClient( new WebViewClient(){

      @Override
      public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        //bridge.getLocalServer().hostAssets();
         Uri url =  request.getUrl();

        Toast.makeText(getApplicationContext(), "WebView NoBridge Dashboard Loading.. "+url.getPath(), Toast.LENGTH_SHORT).show();
        return super.shouldInterceptRequest(view , request);
      }

      @Override
      public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return super.shouldInterceptRequest(view, url);
      }
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Uri url = request.getUrl();
        Toast.makeText(getApplicationContext(), "WebView NoBridge Dashboard Loading.. "+url.getPath(), Toast.LENGTH_SHORT).show();
        HashMap<String, String> hMap = new HashMap<>();
        String cctoken = getIntent().getExtras().getString("jwt_token");
        String cctokenWithBearer = getIntent().getExtras().getString("jwt_token");
        int berIndx = cctoken.indexOf("Bearer ") + "Bearer ".length()-1;

        cctoken = cctoken.substring(berIndx);
        hMap.put("Authorization",cctokenWithBearer);
        hMap.put("Authorization",hardtoken);
        Map<String, String> reqMap  = request.getRequestHeaders();
        Iterator itr = reqMap.keySet().iterator();
        Toast.makeText(getApplicationContext(), " "+url.getScheme()+"://"+url.getHost()+url.getPath(), Toast.LENGTH_SHORT).show();

        boolean overLoadIfAuthMissing = true;
        while (itr.hasNext()){
              String isAuth = (String) itr.next();
              if(isAuth.equals("Authorization")){
                overLoadIfAuthMissing = false;
              }
        }
        if(url.getPath().contains("logout")){
              return false;
        }
         //reqMap.entrySet().stream().filter(e -> e.getKey().equals("Authorization")).count(); //"http://localhost/app/dashboard?jwt_token="+cctoken
        // view.loadUrl("https://www.glaubhanta.site/login?jwt_token="+hardtoken, hMap);
        return  overLoadIfAuthMissing;
      }
      @Override
      public void onLoadResource( WebView view , String url){
        super.onLoadResource(view, url);

      }
      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon){
        super.onPageStarted( view , url , favicon);

        _dialog =ProgressDialog.show(getApplicationContext(), "", "Please wait...");
        String cctoken = getIntent().getExtras().getString("jwt_token");
        String cctokenWithBearer = getIntent().getExtras().getString("jwt_token");
        int berIndx = cctoken.indexOf("Bearer ") + "Bearer ".length()-1;

        cctoken = cctoken.substring(berIndx);
        HashMap<String, String> hMap = new HashMap<>();

        hMap.put("Authorization",cctokenWithBearer);


        Toast.makeText(getApplicationContext(), "No Bridge Page started.. "+cctoken, Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "No Bridge  Page started url.. "+url, Toast.LENGTH_SHORT).show();

       // view.loadUrl("https://locahost/app?jwt_token="+cctoken);
        // view.loadUrl("https://glaubhanta.site/app?jwt_token="+cctoken);

      }
      @Override
      public void onPageFinished(WebView view, String url) {
        // TODO Auto-generated method stub
        super.onPageFinished(view, url);
        _dialog.dismiss();
      }
      @Override
      public void onReceivedError(WebView view, int errorCode,
                                  String description, String failingUrl) {
        // TODO Auto-generated method stub
        super.onReceivedError(view, errorCode, description, failingUrl);
        try{
          _dialog.dismiss();
        }catch (Exception e) {
          // TODO: handle exception
        }
      }
    });

   //dshB.loadUrl("file:///android_asset/index.html");
    //dshB.loadUrl("https://www.glaubhanta.site/login?jwt_token="+hardtoken ,hMap);
    dshB.loadUrl("https://glaubhanta.site/oauth#id_token="+hardtoken+"&state="+state ,hMap);
    //appendLog("id_token="+id_token+"&state="+state);
    //writeFileOnInternalStorage(getApplicationContext(),TAG+".txt","id_token="+id_token+"&state="+state);

    //dshB.loadUrl("https://localhost/login?jwt_token="+cctoken ,hMap);
    // goes back to main page
    //dshB.loadUrl("https://www.glaubhanta.site/login?jwt_token="+hardtoken ,hMap);
    // causes connectin refused
    //dshB.loadUrl("https://localhost/app/dashboard?jwt_token="+cctoken);
      // dshB.loadUrl("https://localhost:8080/#?jwt_token="+cctoken ,hMap);
    //dshB.loadUrl("https://192.168.1.4:8080/#?jwt_token="+cctoken ,hMap);
   // dshB.loadUrl("https://localhost/?jwt_token="+cctoken ,hMap);


  }
  public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
    try {
      File dowDire = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      File dir = new File(mcoContext.getFilesDir(), "budget");
      if (dir != null) {

      } else {

        dir = new File(dowDire, "budget");

        }

      if (!dir.exists()) {
        dir.mkdir();
        Toast.makeText(mcoContext,"file created at "+dir.getAbsolutePath() , Toast.LENGTH_SHORT).show();
      }

      try {
        File gpxfile = new File(dir, sFileName);
        FileWriter writer = new FileWriter(gpxfile);
        writer.append(sBody);
        writer.flush();
        writer.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }catch (Exception er){
      Log.i(    TAG ,
      "Webwriting to file failed ");
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
