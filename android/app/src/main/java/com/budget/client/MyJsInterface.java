package com.budget.client;

import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class MyJsInterface {
  @JavascriptInterface
  public String launchActivity() {
    // ...;
   // Toast.makeText(null,"Webview Can Go Back ", Toast.LENGTH_SHORT).show();
    return "This is launch from MyJsInterface";
  }
}
