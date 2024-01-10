/*
 * Copyright 2015 The AppAuth for Android Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openid.appauthdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import net.openid.appauthdemo.R;

import com.budget.client.MainActivity;
import com.budget.client.User;
import com.budget.client.UsersList;
import com.budget.client.WebViewActivity;
//import com.budget.client.WebViewClient;
import com.budget.client.WebViewDemoActivity;
import com.bumptech.glide.Glide;
import com.getcapacitor.Bridge;
import com.google.android.material.snackbar.Snackbar;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.AuthorizationServiceDiscovery;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.EndSessionRequest;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import okio.Okio;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Displays the authorized state of the user. This activity is provided with the outcome of the
 * authorization flow, which it uses to negotiate the final authorized state,
 * by performing an authorization code exchange if necessary. After this, the activity provides
 * additional post-authorization operations if available, such as fetching user info and refreshing
 * access tokens.
 */
public class TokenActivity extends AppCompatActivity {
    private static final String TAG = "TokenActivity";

    private static final String KEY_USER_INFO = "userInfo";

    private static final int END_SESSION_REQUEST_CODE = 911;

    private AuthorizationService mAuthService;
    private AuthStateManager mStateManager;
    private final AtomicReference<JSONObject> mUserInfoJson = new AtomicReference<>();
    private ExecutorService mExecutor;
    private Configuration mConfiguration;
  private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStateManager = AuthStateManager.getInstance(this);
        mExecutor = Executors.newSingleThreadExecutor();
        mConfiguration = Configuration.getInstance(this);

        Configuration config = Configuration.getInstance(this);
        if (config.hasConfigurationChanged()) {
            Toast.makeText(
                    getApplicationContext(),
                    "Configuration change detected",
                    Toast.LENGTH_SHORT)
                    .show();
            signOut();
            return;
        }

        mAuthService = new AuthorizationService(
                this,
                new AppAuthConfiguration.Builder()
                        .setConnectionBuilder(config.getConnectionBuilder())
                        .build());

        setContentView(R.layout.activity_token);
        displayLoading("Restoring state...");

        if (savedInstanceState != null) {
            try {
                mUserInfoJson.set(new JSONObject(savedInstanceState.getString(KEY_USER_INFO)));
            } catch (JSONException ex) {
                Log.e(TAG, "Failed to parse saved user info JSON, discarding", ex);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mExecutor.isShutdown()) {
            mExecutor = Executors.newSingleThreadExecutor();
        }

        if (mStateManager.getCurrent().isAuthorized()) {
            displayAuthorized();


            return;
        }

        // the stored AuthState is incomplete, so check if we are currently receiving the result of
        // the authorization flow from the browser.
        AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException ex = AuthorizationException.fromIntent(getIntent());

        if (response != null || ex != null) {
            mStateManager.updateAfterAuthorization(response, ex);
        }

        if (response != null && response.authorizationCode != null) {
            // authorization code exchange is required
            mStateManager.updateAfterAuthorization(response, ex);
            exchangeAuthorizationCode(response);
           /* String cc   = response.accessToken;
            String id = response.idToken;
             this.getQuickUserInfo (cc,id, ex);*/

        } else if (ex != null) {
            displayNotAuthorized("Authorization flow failed: " + ex.getMessage());
        } else {
            displayNotAuthorized("No authorization state retained - reauthorization required");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        // user info is retained to survive activity restarts, such as when rotating the
        // device or switching apps. This isn't essential, but it helps provide a less
        // jarring UX when these events occur - data does not just disappear from the view.
        if (mUserInfoJson.get() != null) {
            state.putString(KEY_USER_INFO, mUserInfoJson.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuthService.dispose();
        mExecutor.shutdownNow();
    }

    @MainThread
    private void displayNotAuthorized(String explanation) {
        findViewById(R.id.not_authorized).setVisibility(View.VISIBLE);
        findViewById(R.id.authorized).setVisibility(View.GONE);
        findViewById(R.id.loading_container).setVisibility(View.GONE);

        ((TextView)findViewById(R.id.explanation)).setText(explanation);
        findViewById(R.id.reauth).setOnClickListener((View view) -> signOut());
    }

    @MainThread
    private void displayLoading(String message) {
        findViewById(R.id.loading_container).setVisibility(View.VISIBLE);
        findViewById(R.id.authorized).setVisibility(View.GONE);
        findViewById(R.id.not_authorized).setVisibility(View.GONE);

        ((TextView)findViewById(R.id.loading_description)).setText(message);
    }

    @MainThread
    private void displayAuthorized() {
        findViewById(R.id.authorized).setVisibility(View.VISIBLE);
        findViewById(R.id.not_authorized).setVisibility(View.GONE);
        findViewById(R.id.loading_container).setVisibility(View.GONE);

        AuthState state = mStateManager.getCurrent();

        TextView refreshTokenInfoView = findViewById(R.id.refresh_token_info);
        refreshTokenInfoView.setText((state.getRefreshToken() == null)
                ? R.string.no_refresh_token_returned
                : R.string.refresh_token_returned);
         refreshTokenInfoView.setVisibility(View.INVISIBLE);

        TextView idTokenInfoView = (TextView) findViewById(R.id.id_token_info);
        idTokenInfoView.setText((state.getIdToken()) == null
                ? R.string.no_id_token_returned
                : R.string.id_token_returned);

        TextView accessTokenInfoView = (TextView) findViewById(R.id.access_token_info);
        if (state.getAccessToken() == null) {
            accessTokenInfoView.setText(R.string.no_access_token_returned);
        } else {
            Long expiresAt = state.getAccessTokenExpirationTime();
            if (expiresAt == null) {
                accessTokenInfoView.setText(R.string.no_access_token_expiry);
            } else if (expiresAt < System.currentTimeMillis()) {
                accessTokenInfoView.setText(R.string.access_token_expired);
            } else {
                String template = getResources().getString(R.string.access_token_expires_at);
                accessTokenInfoView.setText(String.format(template,
                        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZ").print(expiresAt)));
            }
        }

        Button refreshTokenButton = (Button) findViewById(R.id.refresh_token);
        refreshTokenButton.setVisibility(state.getRefreshToken() != null
                ? View.VISIBLE
                : View.GONE);
        refreshTokenButton.setOnClickListener((View view) -> refreshAccessToken());
         refreshTokenButton.setVisibility(View.INVISIBLE);
        Button viewProfileButton = (Button) findViewById(R.id.view_profile);

        AuthorizationServiceDiscovery discoveryDoc =
                state.getAuthorizationServiceConfiguration().discoveryDoc;
        if ((discoveryDoc == null || discoveryDoc.getUserinfoEndpoint() == null)
                && mConfiguration.getUserInfoEndpointUri() == null) {
            viewProfileButton.setVisibility(View.GONE);
        } else {
            viewProfileButton.setVisibility(View.VISIBLE);
            viewProfileButton.setOnClickListener((View view) -> fetchUserInfo());
        }

        findViewById(R.id.sign_out).setOnClickListener((View view) -> endSession());

        View userInfoCard = findViewById(R.id.userinfo_card);
        JSONObject userInfo = mUserInfoJson.get();
        if (userInfo == null) {
            userInfoCard.setVisibility(View.INVISIBLE);
        } else {
            try {
                String name = "???";
                if (userInfo.has("name")) {
                    name = userInfo.getString("name");
                }
                ((TextView) findViewById(R.id.userinfo_name)).setText(name);

                if (userInfo.has("picture")) {
                    Glide.with(TokenActivity.this)
                            .load(Uri.parse(userInfo.getString("picture")))
                            .fitCenter()
                            .into((ImageView) findViewById(R.id.userinfo_profile));
                }
                   String email = "???";
              if (userInfo.has("email")) {
                email = userInfo.getString("name");
              }

                ((TextView) findViewById(R.id.userinfo_json)).setText(mUserInfoJson.toString());
                 userInfoCard.setVisibility(View.INVISIBLE);

                String  cctoken =    state.getAccessToken(); // mLastTokenResponse
                AuthorizationResponse  mlastAuthIDState = state.getLastAuthorizationResponse();
                 String id_token = mlastAuthIDState.idToken !=null ? mlastAuthIDState.idToken : cctoken;
                 String state_ang = mlastAuthIDState.state;

//                    String jsonBase64 =   JSONObject.quote("{ \"name\" : "+name+" , \"email\" :" + email+" }" );
//                        byte[] jsonBase64ar  = android.util.Base64.encode(jsonBase64.getBytes() ,android.util.Base64.DEFAULT);
//                         jsonBase64   =new String (jsonBase64ar);
                    /*
                        "{ \"name\" : \"vinayak anvekar\" , \"email\" :\" vvanvekar@gmail.com\" }"

                          InsgXCJuYW1lXCIgOiBcInZpbmF5YWsgYW52ZWthclwiICwgXCJlbWFpbFwiIDpcIiB2dmFudmVrYXJAZ21haWwuY29tXCIgfSI=
                     */
              ((TextView) findViewById(R.id.userinfo_token)).setText(cctoken);
              ((TextView) findViewById(R.id.userinfo_id_token)).setText(id_token);
              ((TextView) findViewById(R.id.userinfo_state)).setText(state_ang);
              User user = new User("0","0","0@0.com","role", "comfirmationCode", false,"exteranlid" , "createdwith");
              String jwt = "";
              try {
                Thread.sleep(300);
                // wait for 40 sec to display the Token
                showSnackbar("User: "+name+" Email: "+email+" logged in");
                 user = UsersList.getUserList(userInfo);
                showSnackbar("User: "+user.toString()+" found in DB");
                 if (!user.getEmail().equalsIgnoreCase(email) ) {user.setEmail(email);}
                // Jwts.builder()
                jwt = Jwts.builder().setPayload(user.toJSONString())
                  .signWith(SignatureAlgorithm.HS256, UsersList.getKey().getBytes())
                  .compact();
                Log.v("JWT : - ",jwt);

                Intent intent = new Intent(this, WebViewActivity.class); //
                intent.putExtra("jwt_token", jwt);
                // intent.putExtra("id_token", id_token);
                // intent.putExtra("state", state_ang);
                // intent.putExtra("encdeusSec",jsonBase64);
                startActivity(intent);



              } catch (InterruptedException e) {
                 showSnackbar("User Info to JWT convertion failed  ");
              }
              String hardtoken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjkiLCJhY2NvdW50SWQiOiIzIiwiZW1haWwiOiJhbnZla2FyLnYuYW5hbmRpQGdtYWlsLmNvbSIsInJvbGUiOiJPV05FUiIsImNvbmZpcm1lZCI6ZmFsc2UsImlhdCI6MTcwMzg2Mjk3NiwiZXhwIjoxNzAzODYzNTc2fQ.S1Mjrh1we7BnrNrcKX1UzSqOsf1YxHiXx-6LjCFSdnU";

              writeFileOnInternalStorage(getApplicationContext(), TAG+".txt","User:"+user.toString()+
                " login jwt:"+(jwt !="" ? jwt : "failed"));


              //Intent intent = new Intent(  Intent.ACTION_VIEW,Uri.parse("https://glaubhanta.site/#?jwt_token="+hardtoken));


            } catch (JSONException ex) {
                Log.e(TAG, "Failed to read userinfo JSON", ex);
            }
        }
    }

    @MainThread
    private void refreshAccessToken() {
        displayLoading("Refreshing access token");
        performTokenRequest(
                mStateManager.getCurrent().createTokenRefreshRequest(),
                this::handleAccessTokenResponse);
    }

    @MainThread
    private void exchangeAuthorizationCode(AuthorizationResponse authorizationResponse) {
        displayLoading("Exchanging authorization code");
        performTokenRequest(
                authorizationResponse.createTokenExchangeRequest(),
                this::handleCodeExchangeResponse);
    }

    @MainThread
    private void performTokenRequest(
            TokenRequest request,
            AuthorizationService.TokenResponseCallback callback) {
        ClientAuthentication clientAuthentication;
        try {
            clientAuthentication = mStateManager.getCurrent().getClientAuthentication();
        } catch (ClientAuthentication.UnsupportedAuthenticationMethod ex) {
            Log.d(TAG, "Token request cannot be made, client authentication for the token "
                            + "endpoint could not be constructed (%s)", ex);
            displayNotAuthorized("Client authentication method is unsupported");
            return;
        }

        mAuthService.performTokenRequest(
                request,
                clientAuthentication,
                callback);
    }

    @WorkerThread
    private void handleAccessTokenResponse(
            @Nullable TokenResponse tokenResponse,
            @Nullable AuthorizationException authException) {
        mStateManager.updateAfterTokenResponse(tokenResponse, authException);
             String r=   tokenResponse.accessToken;
          if(authException!= null) {
            Log.e(TAG, "Failed fetching user info");
            mUserInfoJson.set(null);
            runOnUiThread(this::displayAuthorized);
            return;
          }
          //mStateManager.getCurrent().performActionWithFreshTokens(mAuthService, this::getQuickUserInfo);

        runOnUiThread(this::displayAuthorized);
    }

    @WorkerThread
    private void handleCodeExchangeResponse(
            @Nullable TokenResponse tokenResponse,
            @Nullable AuthorizationException authException) {

        mStateManager.updateAfterTokenResponse(tokenResponse, authException);
        if (!mStateManager.getCurrent().isAuthorized()) {
            final String message = "Authorization Code exchange failed"
                    + ((authException != null) ? authException.error : "");

            // WrongThread inference is incorrect for lambdas
            //noinspection WrongThread
            runOnUiThread(() -> displayNotAuthorized(message));
        } else {
            String cc = tokenResponse.accessToken;
            String id = tokenResponse.idToken;
          this.getQuickUserInfo (cc,id, authException);

            //runOnUiThread(this::displayAuthorized);
        }
    }
    private Object getQuickUserInfo(String accessToken, String idToken, AuthorizationException ex){

      AuthorizationServiceDiscovery discovery =
        mStateManager.getCurrent()
          .getAuthorizationServiceConfiguration()
          .discoveryDoc;

      Uri userInfoEndpoint =
        mConfiguration.getUserInfoEndpointUri() != null
          ? Uri.parse(mConfiguration.getUserInfoEndpointUri().toString())
          : Uri.parse(discovery.getUserinfoEndpoint().toString());

      mExecutor.submit(() -> {
        try {
          HttpURLConnection conn = mConfiguration.getConnectionBuilder().openConnection(
            userInfoEndpoint);
          conn.setRequestProperty("Authorization", "Bearer " + accessToken);
          conn.setInstanceFollowRedirects(false);
          String response = Okio.buffer(Okio.source(conn.getInputStream()))
            .readString(Charset.forName("UTF-8"));
          mUserInfoJson.set(new JSONObject(response));

          User user = UsersList.getUserList(new JSONObject(response));

          String jwt = Jwts.builder().setPayload(user.toJSONString())
            .signWith(SignatureAlgorithm.HS256, UsersList.getKey().getBytes())
            .compact();
          Log.v("JWT : - ",jwt);



        } catch (IOException ioEx) {
          Log.e(TAG, "Network error when querying userinfo endpoint", ioEx);
          showSnackbar("Fetching user info failed");
        } catch (JSONException jsonEx) {
          Log.e(TAG, "Failed to parse userinfo response");
          showSnackbar("Failed to parse user info");
        }
        runOnUiThread(this::loginUserToDashboard);
        //runOnUiThread(this::displayAuthorized);
      });
        return "";
    }

  @MainThread
  private void loginUserToDashboard() {
      JSONObject response =   mUserInfoJson.get();
      if(response != null ){
        try {
          User user = UsersList.getUserList(response);

          String jwt = Jwts.builder().setPayload(user.toJSONString())
            .signWith(SignatureAlgorithm.HS256, UsersList.getKey().getBytes())
            .compact();
          Log.v("JWT : - ", jwt);

          //                        byte[] jsonBase64ar  = android.util.Base64.encode(jsonBase64.getBytes() ,android.util.Base64.DEFAULT);
          //                         jsonBase64   =new String (jsonBase64ar);

          Intent intent = new Intent(this, WebViewActivity.class); //
          intent.putExtra("jwt_token", jwt);
          // intent.putExtra("id_token", id_token);
          // intent.putExtra("state", state_ang);
          // intent.putExtra("encdeusSec",jsonBase64);
          startActivity(intent);
        }
        catch (Exception je) {
             showSnackbar(" Fetch User Info Parsed Exception ");
        }
    }
    else {
           showSnackbar(" Fetch User Info Response issue ");
      }
  }

  /**
     * Demonstrates the use of {link AuthState#performActionWithFreshTokens} to retrieve
     * user info from the IDP's user info endpoint. This callback will negotiate a new access
     * token / id token for use in a follow-up action, or provide an error if this fails.
     */
    @MainThread
    private void fetchUserInfo() {
        displayLoading("Fetching user info");
        mStateManager.getCurrent().performActionWithFreshTokens(mAuthService, this::fetchUserInfo);
    }

    @MainThread
    private void fetchUserInfo(String accessToken, String idToken, AuthorizationException ex) {
        if (ex != null) {
            Log.e(TAG, "Token refresh failed when fetching user info");
            mUserInfoJson.set(null);
            runOnUiThread(this::displayAuthorized);
            return;
        }

        AuthorizationServiceDiscovery discovery =
                mStateManager.getCurrent()
                        .getAuthorizationServiceConfiguration()
                        .discoveryDoc;

        Uri userInfoEndpoint =
                    mConfiguration.getUserInfoEndpointUri() != null
                        ? Uri.parse(mConfiguration.getUserInfoEndpointUri().toString())
                        : Uri.parse(discovery.getUserinfoEndpoint().toString());

        mExecutor.submit(() -> {
            try {
                HttpURLConnection conn = mConfiguration.getConnectionBuilder().openConnection(
                        userInfoEndpoint);
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setInstanceFollowRedirects(false);
                String response = Okio.buffer(Okio.source(conn.getInputStream()))
                        .readString(Charset.forName("UTF-8"));
                mUserInfoJson.set(new JSONObject(response));
            } catch (IOException ioEx) {
                Log.e(TAG, "Network error when querying userinfo endpoint", ioEx);
                showSnackbar("Fetching user info failed");
            } catch (JSONException jsonEx) {
                Log.e(TAG, "Failed to parse userinfo response");
                showSnackbar("Failed to parse user info");
            }

            runOnUiThread(this::displayAuthorized);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == END_SESSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            signOut();
            finish();
        } else {
            displayEndSessionCancelled();
        }
    }

    private void displayEndSessionCancelled() {
        Snackbar.make(findViewById(R.id.coordinator),
            "Sign out canceled",
            Snackbar.LENGTH_SHORT)
                .show();
    }

    @MainThread
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(R.id.coordinator),
                message,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @MainThread
    private void endSession() {
        AuthState currentState = mStateManager.getCurrent();
        AuthorizationServiceConfiguration config =
                currentState.getAuthorizationServiceConfiguration();
        if (config.endSessionEndpoint != null) {
            Intent endSessionIntent = mAuthService.getEndSessionRequestIntent(
                    new EndSessionRequest.Builder(config)
                        .setIdTokenHint(currentState.getIdToken())
                        .setPostLogoutRedirectUri(mConfiguration.getEndSessionRedirectUri())
                        .build());
            startActivityForResult(endSessionIntent, END_SESSION_REQUEST_CODE);
        } else {
            signOut();
        }
    }

    @MainThread
    private void signOut() {
        // discard the authorization and token state, but retain the configuration and
        // dynamic client registration (if applicable), to save from retrieving them again.
        AuthState currentState = mStateManager.getCurrent();
        AuthState clearedState =
                new AuthState(currentState.getAuthorizationServiceConfiguration());
        if (currentState.getLastRegistrationResponse() != null) {
            clearedState.update(currentState.getLastRegistrationResponse());
        }
        mStateManager.replace(clearedState);

        Intent mainIntent = new Intent(this, LoginActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }


  @Override
  public void onBackPressed() {

      if (pressedTime + 2000 > System.currentTimeMillis()) {
        super.onBackPressed();
        finish();
      } else {
        Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
      }
      pressedTime = System.currentTimeMillis();


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
        Log.e(TAG, "appendLog create new file failed ", e);
        showSnackbar("appendLog create new file failed ");
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
      Log.e(TAG, "appendLog IOException failed ", e);
      showSnackbar("appendLog IOException failed ");
    }



  }













}
