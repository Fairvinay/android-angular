package com.budget.client;

public class OAuthConstants {

  public static String client_id = "396135579027-b4gvu1u72l9lhhvov29eild52emov1ak.apps.googleusercontent.com";
  public static int  STATE_LENGTH = 32;
  public static int  NONCE_LENGTH = 16;

  public static String responseType = "id_token";

  public static String  authorizeUrl  = "https://accounts.google.com/o/oauth2/v2/auth";
  public static String  accessTokenUrl = "https://oauth2.googleapis.com/token";
  public static String  userInfoUrl   = "https://openidconnect.googleapis.com/v1/userinfo";
  public static String  redirectUri  = "https://reach.glaubhanta.site/api/auth/external/google/callback";
  public static String scope = "openid email profile";

  public static String generateRandomString(int length) {
    String chars =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    String state = "";
    for (int  i = 0; i < length; i++) {
      state += chars.charAt((int) Math.floor(Math.random() * chars.length()));
    }
    return state;
  }
  public static String  getOauthIdToken(){
    String state = generateRandomString(STATE_LENGTH);
    String nonce = generateRandomString(NONCE_LENGTH);

    String url =authorizeUrl +
      "?client_id="+client_id +
      "&redirect_uri="+redirectUri +
      "&response_type="+responseType +
      "&scope=" +scope+
      "&state=" +state+
      "&nonce="+nonce;

    return  url ;
  }
}
