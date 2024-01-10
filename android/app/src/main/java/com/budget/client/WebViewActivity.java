  package com.budget.client;

  import android.annotation.SuppressLint;
  import android.app.ProgressDialog;
  import android.content.Intent;
  import android.graphics.Bitmap;
  import android.net.Uri;
  import android.os.Bundle;
  import android.webkit.WebResourceRequest;
  import android.webkit.WebResourceResponse;
  import android.webkit.WebSettings;
  import android.webkit.WebView;
  import android.view.View;
  import android.view.MotionEvent;

  import android.widget.Toast;

  import com.getcapacitor.Bridge;
  import com.getcapacitor.BridgeActivity;

  import net.openid.appauth.connectivity.DefaultConnectionBuilder;
  import net.openid.appauthdemo.LoginActivity;
  import net.openid.appauthdemo.R;

  import java.io.IOException;
  import java.net.HttpURLConnection;
  import java.nio.charset.Charset;
  import java.text.DateFormat;
  import java.util.Date;
  import java.util.HashMap;

  import okio.Okio;
  //import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

  public class WebViewActivity extends BridgeActivity {
    private long pressedTime;
    protected String mUrl;
    private static final String TAG = "WebViewActivity";
    ProgressDialog _dialog ;
    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      WebView dshB = getBridge().getWebView();
      WebSettings webSettings = dshB.getSettings();
      //WebView dshB = findViewById(R.id.dashboard_view);
      String cctoken = getIntent().getExtras().getString("jwt_token");
      String id_token = getIntent().getExtras().getString("id_token");
      String state = getIntent().getExtras().getString("state");
      String hardtoken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjkiLCJhY2NvdW50SWQiOiIzIiwiZW1haWwiOiJhbnZla2FyLnYuYW5hbmRpQGdtYWlsLmNvbSIsInJvbGUiOiJPV05FUiIsImNvbmZpcm1lZCI6ZmFsc2UsImlhdCI6MTcwMzg2Mjk3NiwiZXhwIjoxNzAzODYzNTc2fQ.S1Mjrh1we7BnrNrcKX1UzSqOsf1YxHiXx-6LjCFSdnU";
      HashMap<String, String> hMap = new HashMap<>();
      hMap.put("Authorization",hardtoken);
      hMap.put("id_token",id_token);
      hMap.put("state",state);
      //
      // https://www.glaubhanta.site/app?jwt_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjkiLCJhY2NvdW50SWQiOiIzIiwiZW1haWwiOiJhbnZla2FyLnYuYW5hbmRpQGdtYWlsLmNvbSIsInJvbGUiOiJPV05FUiIsImNvbmZpcm1lZCI6ZmFsc2UsImlhdCI6MTcwMzg2Mjk3NiwiZXhwIjoxNzAzODYzNTc2fQ.S1Mjrh1we7BnrNrcKX1UzSqOsf1YxHiXx-6LjCFSdnU
       dshB.setOnTouchListener(new View.OnTouchListener() {

         @SuppressLint("ClickableViewAccessibility")
         public boolean onTouch(View v, MotionEvent event) {
           WebView.HitTestResult hr = ((WebView) v).getHitTestResult();
           if(hr.getExtra() != null) {
             Bridge cu = getBridge();
             //Log.d(TAG, "clicked - url = " + hr.getExtra());
          //   Toast.makeText(cu.getContext(),"clicked "+hr.getExtra(), Toast.LENGTH_SHORT).show();
             if (hr.getExtra().contains("Logout") || hr.getExtra().contains("(click)=\"logout()\"")) {
               String url =hr.getExtra();
               //Toast.makeText(MainActivity.class, "onTouch " + url, Toast.LENGTH_SHORT).show();

               Intent i = new Intent(cu.getContext(), MainActivity.class);
               i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
               startActivity(i);
               finish();

             }
           }
           return false;
         }
       });
      dshB.setWebViewClient( new WebViewClient(getBridge()){
       /* @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
          //bridge.getLocalServer().hostAssets();

          String bPath = bridge.getLocalServer().getBasePath();
          Toast.makeText(getApplicationContext(), "shouldInterceptRequest..set "+bPath, Toast.LENGTH_SHORT).show();
          return bridge.getLocalServer().shouldInterceptRequest(request);
        }*/

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
          return super.shouldInterceptRequest(view, url);
         /* try {
            final String acToken = getIntent().getExtras().getString("jwt_token");
            int berIndx = acToken.indexOf("Bearer ") + "Bearer ".length()-1;

            String cctoken = acToken.substring(berIndx);
            Toast.makeText(getApplicationContext(), "shouldInterceptRequest..set "+cctoken, Toast.LENGTH_SHORT).show();


            HttpURLConnection conn = DefaultConnectionBuilder.INSTANCE.openConnection(Uri.parse("https://glaubhanta.site/app?jwt_token="+acToken));
            conn.setRequestProperty("Authorization", "Bearer " + acToken);
            conn.setInstanceFollowRedirects(false);
            String response = Okio.buffer(Okio.source(conn.getInputStream()))
              .readString(Charset.forName("UTF-8"));
            return new WebResourceResponse("text/html", // You can set something other as default content-type
               "utf-8",  // Again, you can set another encoding as default
              conn.getInputStream());


          } catch (IOException e) {
              e.printStackTrace();
            return null;
          }catch (Exception e) {
            //return null to tell WebView we failed to fetch it WebView should try again.
            return null;
          }*/
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
          Uri url = request.getUrl();

          onLogoutClick(url);
         // Toast.makeText(getApplicationContext(), "App Dashboard Loading.. "+url.getPath(), Toast.LENGTH_SHORT).show();
          HashMap<String, String> hMap = new HashMap<>();
          String cctoken = getIntent().getExtras().getString("jwt_token");
          String cctokenBearer = getIntent().getExtras().getString("jwt_token");
          int berIndx = cctoken.indexOf("Bearer ") + "Bearer ".length()-1;

          cctoken = cctoken.substring(berIndx);
          hMap.put("Authorization",cctokenBearer);
          // +url.getPath()
        //  Toast.makeText(getApplicationContext(), "AUthorization set.. ", Toast.LENGTH_SHORT).show();
          checkLogedOutRelogin(url);
          //view.loadUrl(url.getScheme()+"://"+url.getHost()+url.getPath(), hMap);
          //
              // url+"?jwt_token="+cctoken "https://glaubhanta.site/app?jwt_token="+cctoken
          return true;//bridge.launchIntent(url);
        }

        public void onLogoutClick(Uri url) {
          if (url.getPath().contains("logout")) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
          }
        }
        public void checkLogedOutRelogin(Uri url ) {
          if (url.getPath().contains("o/oauth2/v2")) {
            StringBuffer headBuf = new StringBuffer() ;
         /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            request.getRequestHeaders().forEach((k ,v ) -> { headBuf.append("k:").append(k).append("v: " ).append(v);} );
          }*/
           // Toast.makeText(getBridge().getContext(), "req method  " + request.getMethod() + "re head "+headBuf.toString() , Toast.LENGTH_SHORT).show();
            Bridge cu = getBridge();
            //Intent intent = new Intent(cu.getContext(), WebViewActivity.class);
            //Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(OAuthConstants.getOauthIdToken())));
            //    launchSomeActivity.launch(intent);
            startActivity(i);
            finish();
            // intent.setData();

          }

        }
/*        public void onLoadResource( WebView view , String url){
          super.onLoadResource(view, url);
          //Toast.makeText(getApplicationContext(), "Loading.. ", Toast.LENGTH_SHORT).show();
          HashMap<String, String> hMap = new HashMap<>();
          String cctoken = getIntent().getExtras().getString("jwt_token");
          String cctokenBearer = getIntent().getExtras().getString("jwt_token");
          int berIndx = cctoken.indexOf("Bearer ") + "Bearer ".length()-1;

          cctoken = cctoken.substring(berIndx);
          Toast.makeText(getApplicationContext(), "Authorization..set "+cctoken, Toast.LENGTH_SHORT).show();
          hMap.put("Authorization",cctokenBearer);
          // Toast.makeText(getApplicationContext(), "Authorization..set ", Toast.LENGTH_SHORT).show();
          Toast.makeText(getApplicationContext(), "onLoadRes.url "+url, Toast.LENGTH_SHORT).show();
          String day = "mit der Aufgabe fertig \n" + DateFormat.getInstance().format(new Date());
          String postJson  = "{ \"jwt\": "+cctoken+" , \"day\": "+day+" }";
          //view.loadUrl(url+"?jwt_token="+cctoken, hMap); // postUrl("https://localhost/login", postJson.getBytes());
          //view.(url, hMap);
        }*/
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
          super.onPageStarted( view , url , favicon);
          //_dialog =ProgressDialog.show(getApplicationContext(), "", "Please wait...");
         /* String cctoken = getIntent().getExtras().getString("jwt_token");
          int berIndx = cctoken.indexOf("Bearer ") + "Bearer ".length()-1;

          cctoken = cctoken.substring(berIndx);
          Toast.makeText(getApplicationContext(), "Page started.. "+cctoken, Toast.LENGTH_SHORT).show();
          Toast.makeText(getApplicationContext(), "Page started url.. "+url, Toast.LENGTH_SHORT).show();*/

         // bridge.reset();
          //view.loadUrl("https://localhost/app?jwt_token="+cctoken);
         // view.loadUrl("https://glaubhanta.site/app?jwt_token="+cctoken);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
          // TODO Auto-generated method stub
          super.onPageFinished(view, url);
         // _dialog.dismiss();
        }
        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
          // TODO Auto-generated method stub
          super.onReceivedError(view, errorCode, description, failingUrl);
          try{

           // _dialog.dismiss();
          }catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(getApplicationContext(), "Page finished exception .. ", Toast.LENGTH_SHORT).show();
          }
        }








      });
      //dshB.setWebChromeClient(new WebChromeClient());
      int berIndx = cctoken.indexOf("Bearer ") + "Bearer ".length()-1;

      cctoken = cctoken.substring(berIndx);

      //dshB.loadUrl("https://glaubhanta.site/app/dashboard?jwt_token="+cctoken);
      webSettings.setJavaScriptEnabled(true); // Enable JavaScript if needed
      webSettings.setAllowFileAccess(true);
      webSettings.setAllowContentAccess(true);
      webSettings.setDomStorageEnabled(true);
      webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
      dshB.addJavascriptInterface(new MyJsInterface(), "AndroidInterface");
      dshB.loadUrl("https://localhost/app?jwt_token="+cctoken ,hMap);
      //dshB.loadUrl("https://www.glaubhanta.site/app?jwt_token="+cctoken ,hMap);
      //dshB.loadUrl("https://glaubhanta.site/oauth#id_token="+hardtoken+"&state="+state ,hMap);
      //dshB.loadUrl("https://glaubhanta.site/app/dashboard?jwt_token="+cctoken);


      // Configure sign-in to request the user's ID, email address, and basic
  // profile. ID and basic profile are included in DEFAULT_SIGN_IN
  //
      /* gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build();
        */

      /*
        which you will need to pass to the requestIdToken or requestServerAuthCode
         method when you create the GoogleSignInOptions object.
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
          Intent i = new Intent(getApplicationContext(), MainActivity.class);
          i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          startActivity(i);
          finish();

        } else {
          Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
      } else {
        Toast.makeText(getApplicationContext(),"Back Not Allowed ", Toast.LENGTH_SHORT).show();

        super.onBackPressed();
      }

    }
  }
