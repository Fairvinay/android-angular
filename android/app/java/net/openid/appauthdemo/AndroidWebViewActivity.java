package net.openid.appauthdemo;


import android.os.Bundle;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class AndroidWebViewActivity extends BridgeActivity {

    protected String mUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = getBridge().getWebView();
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
}
