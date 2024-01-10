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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.AnyThread;
import androidx.annotation.ColorRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import net.openid.appauthdemo.R;

import com.budget.client.MainActivity;
import com.google.android.material.snackbar.Snackbar;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.RegistrationRequest;
import net.openid.appauth.RegistrationResponse;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.browser.AnyBrowserMatcher;
import net.openid.appauth.browser.BrowserMatcher;
import net.openid.appauth.browser.ExactBrowserMatcher;
import net.openid.appauthdemo.BrowserSelectionAdapter.BrowserInfo;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
//import net.openid.appauthdemo.R;

/**
 * Demonstrates the usage of the AppAuth to authorize a user with an OAuth2 / OpenID Connect
 * provider. Based on the configuration provided in `res/raw/auth_config.json`, the code
 * contained here will:
 *
 * - Retrieve an OpenID Connect discovery document for the provider, or use a local static
 *   configuration.
 * - Utilize dynamic client registration, if no static client id is specified.
 * - Initiate the authorization request using the built-in heuristics or a user-selected browser.
 *  - 9665755588  Sasil Bansode  9880428285 victoria dias
 *  - 9731142977  Pragathi Archarya
 * _NOTE_: From a clean checkout of this project, the authorization service is not configured.
 * Edit `res/raw/auth_config.json` to provide the required configuration properties. See the
 * README.md in the app/ directory for configuration instructions, and the adjacent IDP-specific
 * instructions.
 */
public final class LoginActivity extends AppCompatActivity {

  private long pressedTime;

  private static final String TAG = "LoginActivity";
    private static final String EXTRA_FAILED = "failed";
    private static final int RC_AUTH = 100;

    private AuthorizationService mAuthService;
    private AuthStateManager mAuthStateManager;
    private Configuration mConfiguration;

    private final AtomicReference<String> mClientId = new AtomicReference<>();
    private final AtomicReference<AuthorizationRequest> mAuthRequest = new AtomicReference<>();
    private final AtomicReference<CustomTabsIntent> mAuthIntent = new AtomicReference<>();
    private CountDownLatch mAuthIntentLatch = new CountDownLatch(1);
    private ExecutorService mExecutor;

    private boolean mUsePendingIntents;

    @NonNull
    private BrowserMatcher mBrowserMatcher = AnyBrowserMatcher.INSTANCE;


  ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    new ActivityResultCallback<ActivityResult>() {
      @Override
      public void onActivityResult(ActivityResult result) {
        Intent data = result.getData();
        if (result.getResultCode() == 2) {
          data.toString();
         // Toast.makeText(getApplicationContext()," back from Browser "+data.getExtras().toString(), Toast.LENGTH_SHORT).show();
          Snackbar.make(findViewById(R.id.coordinator),
              "AuthorizationRequestIntent OKAY",
              Snackbar.LENGTH_SHORT)
            .show();
          //  toastComon.setText(" back from Browser "+data.getExtras().toString());
          // toastComon.show();
        }
        else{
          Snackbar.make(findViewById(R.id.coordinator),
              "AuthorizationRequestIntent Failed",
              Snackbar.LENGTH_SHORT)
            .show();
         // Toast.makeText(getApplicationContext()," back from Browser "+data.getExtras().toString(), Toast.LENGTH_SHORT).show();
          //toastComon.setText(" back from Browser issue ");
        }
      }
    });



  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExecutor = Executors.newSingleThreadExecutor();
        mAuthStateManager = AuthStateManager.getInstance(this);
        mConfiguration = Configuration.getInstance(this);

        if (mAuthStateManager.getCurrent().isAuthorized()
                && !mConfiguration.hasConfigurationChanged()) {
            Log.i(TAG, "User is already authenticated, proceeding to token activity");
        //  Toast.makeText(getApplicationContext(),"User is already authenticated ", Toast.LENGTH_SHORT).show();

          //  startActivity(new Intent(this, TokenActivity.class));
           // finish();
           // return;

        }

        setContentView(R.layout.activity_login);

        findViewById(R.id.retry).setOnClickListener((View view) ->
                mExecutor.submit(this::initializeAppAuth));
        findViewById(R.id.start_auth).setOnClickListener((View view) -> startAuth());

        ((EditText)findViewById(R.id.login_hint_value)).addTextChangedListener(
                new LoginHintChangeHandler());
        BroadcastReceiver br = new BackGroundReceiver();
        IntentFilter serIntFil = new IntentFilter("com.budget.authservice.NOTIFICATION");
        int receiverFlags = ContextCompat.RECEIVER_EXPORTED;
        ContextCompat.registerReceiver(getApplicationContext(),br,serIntFil,receiverFlags);
            if (!mConfiguration.isValid()) {
                displayError(mConfiguration.getConfigurationError(), false);
                return;
            }

        configureBrowserSelector();
        if (mConfiguration.hasConfigurationChanged()) {
            // discard any existing authorization state due to the change of configuration
            Log.i(TAG, "Configuration change detected, discarding old state");
            mAuthStateManager.replace(new AuthState());
            mConfiguration.acceptConfiguration();
          displayLoading("Configuration change detected");
        }

        if (getIntent().getBooleanExtra(EXTRA_FAILED, false)) {
            displayAuthCancelled();
        }

        displayLoading("Initializing");
        mExecutor.submit(this::initializeAppAuth);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mExecutor.isShutdown()) {
            mExecutor = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mExecutor.shutdownNow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAuthService != null) {
            mAuthService.dispose();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        displayAuthOptions();
        if (resultCode == RESULT_CANCELED) {
            displayAuthCancelled();
        } else {
            Intent intent = new Intent(this, TokenActivity.class);
            intent.putExtras(data.getExtras());
            startActivity(intent);
        }
    }

    @MainThread
    void startAuth() {
        displayLoading("Making authorization request");

        mUsePendingIntents = ((CheckBox) findViewById(R.id.pending_intents_checkbox)).isChecked();

        // WrongThread inference is incorrect for lambdas
        // noinspection WrongThread
        mExecutor.submit(this::doAuth);
    }

    /**
     * Initializes the authorization service configuration if necessary, either from the local
     * static values or by retrieving an OpenID discovery document.
     */
    //@SuppressLint("WrongThread")
    @WorkerThread
    private void initializeAppAuth() {
        Log.i(TAG, "Initializing AppAuth");
        recreateAuthorizationService();

        if (mAuthStateManager.getCurrent().getAuthorizationServiceConfiguration() != null) {
            // configuration is already created, skip to client initialization
            Log.i(TAG, "auth config already established");
         // Toast.makeText(getApplicationContext(), "LoginActivty : initializeAppAuth : auth config already established ", Toast.LENGTH_SHORT).show();
        //  displayLoading("LoginActivty : initializeAppAuth : auth config already established  ");
            initializeClient();
            return;
        }
        else{
         // Toast.makeText(getApplicationContext(), "LoginActivty : initializeAppAuth : auth config null ", Toast.LENGTH_SHORT).show();
         // displayLoading("LoginActivty : initializeAppAuth : auth config null ");
         // displayErrorLater("LoginActivty : initializeAppAuth : auth config null ",false);
        }

        // if we are not using discovery, build the authorization service configuration directly
        // from the static configuration values.
        if (mConfiguration.getDiscoveryUri() == null) {
            Log.i(TAG, "Creating auth config from res/raw/auth_config.json");
            AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(
                    mConfiguration.getAuthEndpointUri(),
                    mConfiguration.getTokenEndpointUri(),
                    mConfiguration.getRegistrationEndpointUri(),
                    mConfiguration.getEndSessionEndpoint());
         // Toast.makeText(getApplicationContext(), "LoginActivty : initializeAppAuth : auth config SET "+config.toJsonString(), Toast.LENGTH_SHORT).show();
          //displayLoading("LoginActivty : initializeAppAuth :auth config SET "+config.toJsonString());
            mAuthStateManager.replace(new AuthState(config));
            initializeClient();
            return;
        }

        // WrongThread inference is incorrect for lambdas
        // noinspection WrongThread
        runOnUiThread(() -> displayLoading("Retrieving discovery document"));
        Log.i(TAG, "Retrieving OpenID discovery doc");
        AuthorizationServiceConfiguration.fetchFromUrl(
                mConfiguration.getDiscoveryUri(),
                this::handleConfigurationRetrievalResult,
                mConfiguration.getConnectionBuilder());
    }

    @MainThread
    private void handleConfigurationRetrievalResult(
            AuthorizationServiceConfiguration config,
            AuthorizationException ex) {
        if (config == null) {
            Log.i(TAG, "Failed to retrieve discovery document", ex);
            displayError("Failed to retrieve discovery document: " + ex.getMessage(), true);
            return;
        }

        Log.i(TAG, "Discovery document retrieved");
        mAuthStateManager.replace(new AuthState(config));
        mExecutor.submit(this::initializeClient);
    }

    /**
     * Initiates a dynamic registration request if a client ID is not provided by the static
     * configuration.
     */
    //@SuppressLint("WrongThread")
    @WorkerThread
    private void initializeClient() {
        if (mConfiguration.getClientId() != null) {
            Log.i(TAG, "Using static client ID: " + mConfiguration.getClientId());
            // use a statically configured client ID
         // Toast.makeText(getApplicationContext(), "LoginActivty : initializeClient : Using static client ID Not Null ", Toast.LENGTH_SHORT).show();
          displayErrorLater("Using static client ID Not Null " , true);
            mClientId.set(mConfiguration.getClientId());
            runOnUiThread(this::initializeAuthRequest);
            return;
        }

        RegistrationResponse lastResponse =
                mAuthStateManager.getCurrent().getLastRegistrationResponse();
        if (lastResponse != null) {
            Log.i(TAG, "Using dynamic client ID: " + lastResponse.clientId);
            // already dynamically registered a client ID
         // Toast.makeText(getApplicationContext(), "LoginActivty : initializeClient : lastRegistraion Not Null Response ", Toast.LENGTH_SHORT).show();
            mClientId.set(lastResponse.clientId);
          displayErrorLater("lastRegistraion Not Null Response " , true);
            runOnUiThread(this::initializeAuthRequest);
            return;
        }

        // WrongThread inference is incorrect for lambdas
        // noinspection WrongThread
        runOnUiThread(() -> displayLoading("Dynamically registering client"));
        Log.i(TAG, "Dynamically registering client");

        RegistrationRequest registrationRequest = null;
          try {
            registrationRequest =  new RegistrationRequest.Builder(
              mAuthStateManager.getCurrent().getAuthorizationServiceConfiguration(),
              Collections.singletonList(mConfiguration.getRedirectUri()))
              .setTokenEndpointAuthenticationMethod(ClientSecretBasic.NAME)
              .build();

          } catch (Exception rt ){
           // Toast.makeText(getApplicationContext(), "LoginActivty : initializeClient : client id null: auth Config failed:  ", Toast.LENGTH_SHORT).show();
           // Toast.makeText(getApplicationContext(), "LoginActivty : initializeClient : "+rt.getMessage(), Toast.LENGTH_SHORT).show();
            displayErrorLater("client id null: auth Config failed: " , true);
          //  displayLoading("client id null: auth Config failed: ");
          }

         mAuthService.performRegistrationRequest(
        registrationRequest,
        this::handleRegistrationResponse);



    }

    @MainThread
    private void handleRegistrationResponse(
            RegistrationResponse response,
            AuthorizationException ex) {
        mAuthStateManager.updateAfterRegistration(response, ex);
        if (response == null) {
            Log.i(TAG, "Failed to dynamically register client", ex);
          displayLoading(" Register Response :  null ");
            displayErrorLater("Failed to register client: " + ex.getMessage(), true);
            return;
        }

        Log.i(TAG, "Dynamically registered client: " + response.clientId);
        mClientId.set(response.clientId);
        initializeAuthRequest();
    }

    /**
     * Enumerates the browsers installed on the device and populates a spinner, allowing the
     * demo user to easily test the authorization flow against different browser and custom
     * tab configurations.
     */
    @MainThread
    private void configureBrowserSelector() {
        Spinner spinner = (Spinner) findViewById(R.id.browser_selector);
        final BrowserSelectionAdapter adapter = new BrowserSelectionAdapter(this);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BrowserInfo info = adapter.getItem(position);
                if (info == null) {
                    mBrowserMatcher = AnyBrowserMatcher.INSTANCE;
                    return;
                } else {
                    mBrowserMatcher = new ExactBrowserMatcher(info.mDescriptor);
                }

                recreateAuthorizationService();
                createAuthRequest(getLoginHint());
                warmUpBrowser();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBrowserMatcher = AnyBrowserMatcher.INSTANCE;
            }
        });
    }

    /**
     * Performs the authorization request, using the browser selected in the spinner,
     * and a user-provided `login_hint` if available.
     */
    @WorkerThread
    private void doAuth() {
        try {
            mAuthIntentLatch.await();
        } catch (InterruptedException ex) {
            Log.w(TAG, "Interrupted while waiting for auth intent");
        }

        if (mUsePendingIntents) {
            final Intent completionIntent = new Intent(this, TokenActivity.class);
            final Intent cancelIntent = new Intent(this, LoginActivity.class);
            cancelIntent.putExtra(EXTRA_FAILED, true);
            cancelIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            int flags = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }

            mAuthService.performAuthorizationRequest(
                    mAuthRequest.get(),
                    PendingIntent.getActivity(this, 0, completionIntent, flags),
                    PendingIntent.getActivity(this, 0, cancelIntent, flags),
                    mAuthIntent.get());
        } else {
          /*if (mAuthService != null) {
            Log.i(TAG, "Discarding existing AuthService instance");
            mAuthService.dispose();
            AuthorizationRequest mAuthRequestEmp =  mAuthRequest.get();
            CustomTabsIntent mAuthIntentEmp  =  mAuthIntent.get();

            recreateAuthorizationService();
            mAuthRequest.set(mAuthRequestEmp);
            mAuthIntent.set(mAuthIntentEmp);
            // clone auth req
            //RecreateAuthRequestTask newAuth =new RecreateAuthRequestTask() ; //RecreateAuthRequestTask();




          }else {
          //  displayLoading(" AuthService instance is NULL ");
          }
          */
            Intent intent = mAuthService.getAuthorizationRequestIntent(
                    mAuthRequest.get(),
                    mAuthIntent.get());
          startActivityForResult(intent, RC_AUTH);
            //intent.setAction("com.budget.authservice.NOTIFICATION");
            //sendBroadcast(intent);
          //launchSomeActivity.launch(intent);//(intent, RC_AUTH);
        }
    }

    private void recreateAuthorizationService() {
        if (mAuthService != null) {
            Log.i(TAG, "Discarding existing AuthService instance");
            mAuthService.dispose();
        }
        mAuthService = createAuthorizationService();
        mAuthRequest.set(null);
        mAuthIntent.set(null);
    }

    private AuthorizationService createAuthorizationService() {
        Log.i(TAG, "Creating authorization service");
        AppAuthConfiguration.Builder builder = new AppAuthConfiguration.Builder();
        if(builder !=null ){
          displayLoading("LginAcitivty :createAuthorizationService :AppAuthConfiguration OK "+builder.toString());
         // Toast.makeText(getApplicationContext(),"LginAcitivty :createAuthorizationService :AppAuthConfiguration OK ", Toast.LENGTH_SHORT).show();
          //Toast.makeText(this, "LginAcitivty :createAuthorizationService :AppAuthConfiguration OK "+builder.toString() , Toast.LENGTH_SHORT).show();
        }
        else{
          displayLoading("LginAcitivty :createAuthorizationService :AppAuthConfiguration BUILD failed ");
          //.makeText(this, "LginAcitivty :createAuthorizationService :AppAuthConfiguration BUILD failed " , Toast.LENGTH_SHORT).show();
         // builder.toString()
        }
        builder.setBrowserMatcher(mBrowserMatcher);
        builder.setConnectionBuilder(mConfiguration.getConnectionBuilder());

        return new AuthorizationService(this, builder.build());
    }

    @MainThread
    private void displayLoading(String loadingMessage) {
        findViewById(R.id.loading_container).setVisibility(View.VISIBLE);
        findViewById(R.id.auth_container).setVisibility(View.GONE);
        findViewById(R.id.error_container).setVisibility(View.GONE);

        ((TextView)findViewById(R.id.loading_description)).setText(loadingMessage);
    }

    @MainThread
    private void displayError(String error, boolean recoverable) {
        findViewById(R.id.error_container).setVisibility(View.VISIBLE);
        findViewById(R.id.loading_container).setVisibility(View.GONE);
        findViewById(R.id.auth_container).setVisibility(View.GONE);

        ((TextView)findViewById(R.id.error_description)).setText(error);
        findViewById(R.id.retry).setVisibility(recoverable ? View.VISIBLE : View.GONE);
    }

    // WrongThread inference is incorrect in this case
    @SuppressWarnings("WrongThread")
    @AnyThread
    private void displayErrorLater(final String error, final boolean recoverable) {
        runOnUiThread(() -> displayError(error, recoverable));
    }

    @MainThread
    private void initializeAuthRequest() {
        createAuthRequest(getLoginHint());
        warmUpBrowser();
        displayAuthOptions();
    }

    @MainThread
    private void displayAuthOptions() {
        findViewById(R.id.auth_container).setVisibility(View.VISIBLE);
        findViewById(R.id.loading_container).setVisibility(View.GONE);
        findViewById(R.id.error_container).setVisibility(View.GONE);

        AuthState state = mAuthStateManager.getCurrent();
        AuthorizationServiceConfiguration config = state.getAuthorizationServiceConfiguration();

        String authEndpointStr;
        if (config.discoveryDoc != null) {
            authEndpointStr = "Discovered auth endpoint: \n";
        } else {
            authEndpointStr = "Static auth endpoint: \n";
        }
        authEndpointStr += config.authorizationEndpoint;
        ((TextView)findViewById(R.id.auth_endpoint)).setText(authEndpointStr);

        String clientIdStr;
        if (state.getLastRegistrationResponse() != null) {
            clientIdStr = "Dynamic client ID: \n";
        } else {
            clientIdStr = "Static client ID: \n";
        }
        clientIdStr += mClientId;
        ((TextView)findViewById(R.id.client_id)).setText(clientIdStr);
    }

    private void displayAuthCancelled() {
        Snackbar.make(findViewById(R.id.coordinator),
                "Authorization canceled",
                Snackbar.LENGTH_SHORT)
                .show();
    }

    private void warmUpBrowser() {
        mAuthIntentLatch = new CountDownLatch(1);
        mExecutor.execute(() -> {
            Log.i(TAG, "Warming up browser instance for auth request");
            CustomTabsIntent.Builder intentBuilder =
                    mAuthService.createCustomTabsIntentBuilder(mAuthRequest.get().toUri());
            intentBuilder.setToolbarColor(getColorCompat(R.color.colorPrimary));
            mAuthIntent.set(intentBuilder.build());
            mAuthIntentLatch.countDown();
        });
    }

    private void createAuthRequest(@Nullable String loginHint) {
        Log.i(TAG, "Creating auth request for login hint: " + loginHint);
        AuthorizationServiceConfiguration tChecl =  mAuthStateManager.getCurrent().getAuthorizationServiceConfiguration();
        if(tChecl != null) {
         // Toast.makeText(getApplicationContext(),"LoginActivity  createAuthRequest "+tChecl.toJsonString(), Toast.LENGTH_SHORT).show();
        }
        else{
         // Toast.makeText(getApplicationContext(),"LoginActivity  createAuthRequest : AuthServiceConfig null ", Toast.LENGTH_SHORT).show();
        }
        AuthorizationRequest.Builder authRequestBuilder = new AuthorizationRequest.Builder(
                mAuthStateManager.getCurrent().getAuthorizationServiceConfiguration(),
                mClientId.get(),
                ResponseTypeValues.CODE,
                mConfiguration.getRedirectUri())
                .setScope(mConfiguration.getScope());

        if (!TextUtils.isEmpty(loginHint)) {
            authRequestBuilder.setLoginHint(loginHint);
        }
      AuthorizationRequest treq = authRequestBuilder.build();
      if(treq != null) {
       // Toast.makeText(getApplicationContext(),"LoginActivity  createAuthRequest  freq "+treq.jsonSerializeString(), Toast.LENGTH_SHORT).show();
      }  else{
       // Toast.makeText(getApplicationContext(),"LoginActivity  createAuthRequest AuthReq Null ", Toast.LENGTH_SHORT).show();
      }

        mAuthRequest.set(treq);
    }

    private String getLoginHint() {
        return ((EditText)findViewById(R.id.login_hint_value))
                .getText()
                .toString()
                .trim();
    }

    //@TargetApi(Build.VERSION_CODES.M)
    @SuppressWarnings("deprecation")
    private int getColorCompat(@ColorRes int color) {
      return getColor(color);
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getColor(color);
        } else {
            return getResources().getColor(color);
        }*/
    }

    /**
     * Responds to changes in the login hint. After a "debounce" delay, warms up the browser
     * for a request with the new login hint; this avoids constantly re-initializing the
     * browser while the user is typing.
     */
    private final class LoginHintChangeHandler implements TextWatcher {

        private static final int DEBOUNCE_DELAY_MS = 500;

        private Handler mHandler;
        private RecreateAuthRequestTask mTask;

        LoginHintChangeHandler() {
            mHandler = new Handler(Looper.getMainLooper());
            mTask = new RecreateAuthRequestTask();
        }

        @Override
        public void beforeTextChanged(CharSequence cs, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence cs, int start, int before, int count) {
            mTask.cancel();
            mTask = new RecreateAuthRequestTask();
            mHandler.postDelayed(mTask, DEBOUNCE_DELAY_MS);
        }

        @Override
        public void afterTextChanged(Editable ed) {}
    }

    private final class RecreateAuthRequestTask implements Runnable {

        private final AtomicBoolean mCanceled = new AtomicBoolean();

        @Override
        public void run() {
            if (mCanceled.get()) {
                return;
            }
         // Toast.makeText(getApplicationContext()," RecreateAuthRequestTask ", Toast.LENGTH_SHORT).show();
            createAuthRequest(getLoginHint());
            warmUpBrowser();
        }

        public void cancel() {
            mCanceled.set(true);
        }
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
}
