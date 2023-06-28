package git.artdeell.skymodloader.auth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.tgc.sky.GameActivity;
import com.tgc.sky.accounts.SystemAccountClientInfo;
import com.tgc.sky.accounts.SystemAccountClientRequestState;
import com.tgc.sky.accounts.SystemAccountClientState;
import com.tgc.sky.accounts.SystemAccountInterface;
import com.tgc.sky.accounts.SystemAccountServerInfo;
import com.tgc.sky.accounts.SystemAccountServerState;
import com.tgc.sky.accounts.SystemAccountType;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.GrantTypeValues;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

import git.artdeell.skymodloader.appauth.AppAuthDiscoverable;
import git.artdeell.skymodloader.appauth.AppAuthInit;
import git.artdeell.skymodloader.appauth.AppAuthInitIface;

public class Google implements SystemAccountInterface, AppAuthInitIface, GameActivity.OnActivityResultListener {
    private static final String GOOGLE_REDIRECT = "com.googleusercontent.apps.425067885496-t5lthegcq17g1gaco1l90cc3cncr0q0l:/oauth2redirect";
    private static final String GOOGLE_CLIENTID = "425067885496-t5lthegcq17g1gaco1l90cc3cncr0q0l.apps.googleusercontent.com";
    public static final int GOOGLE_REQUEST = 989;
    /* access modifiers changed from: private */
    public SystemAccountClientInfo m_accountClientInfo;
    private SystemAccountServerInfo m_accountServerInfo;
    /* access modifiers changed from: private */
    public GameActivity m_activity;
    /* access modifiers changed from: private */
    public SystemAccountInterface.UpdateClientInfoCallback m_callback;

    private AuthorizationService service;
    private SharedPreferences accountPrefs;

    public void Initialize(GameActivity gameActivity, SystemAccountInterface.UpdateClientInfoCallback updateClientInfoCallback) {
        this.m_activity = gameActivity;
        this.m_callback = updateClientInfoCallback;
        SystemAccountClientInfo systemAccountClientInfo = new SystemAccountClientInfo();
        this.m_accountClientInfo = systemAccountClientInfo;
        systemAccountClientInfo.accountType = SystemAccountType.kSystemAccountType_Google;
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_Initializing;
        SystemAccountServerInfo systemAccountServerInfo = new SystemAccountServerInfo();
        this.m_accountServerInfo = systemAccountServerInfo;
        systemAccountServerInfo.type = SystemAccountType.kSystemAccountType_Google;
        this.m_accountServerInfo.state = SystemAccountServerState.kSystemAccountServerState_Initializing;
        AppAuthInit.postInit(AppAuthDiscoverable.GOOGLE, this, m_activity.getCacheDir());
        service = new AuthorizationService(m_activity);
        accountPrefs = m_activity.getSharedPreferences("google_account", Context.MODE_PRIVATE);
        m_activity.AddOnActivityResultListener(this);
    }

    public SystemAccountClientInfo GetClientInfo() {
        return this.m_accountClientInfo;
    }

    public SystemAccountServerInfo GetServerInfo() {
        return this.m_accountServerInfo;
    }

    public void SignIn() {
        m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SigningIn;
        m_callback.UpdateClientInfo(m_accountClientInfo);
        long expiryTime = accountPrefs.getLong("expiry", 0);
        if(expiryTime != 0 && expiryTime > System.currentTimeMillis()) {
            Log.i("GoogleSignIn","Old token within expiry time, proceeding");
            m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedIn;
            m_accountClientInfo.accountId = accountPrefs.getString("id", "");
            m_accountClientInfo.alias = accountPrefs.getString("alias", "");
            m_accountClientInfo.signature = accountPrefs.getString("signature", "");
            m_activity.runOnUiThread(()->m_callback.UpdateClientInfo(m_accountClientInfo));
            return;
        }
        if(!accountPrefs.contains("refresh")) {
            Log.i("GoogleSignIn","No refresh token, logging in fresh");
            AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                    AppAuthInit.getConfiguration(AppAuthDiscoverable.GOOGLE),
                    GOOGLE_CLIENTID,
                    ResponseTypeValues.CODE,
                    Uri.parse(GOOGLE_REDIRECT)
            );
            builder.setScopes("openid","email","profile");
            m_activity.startActivityForResult(service.getAuthorizationRequestIntent(builder.build()), GOOGLE_REQUEST);
        }else{
            Log.i("GoogleSignIn","Refreshing token...");
            final String currentRefreshToken = accountPrefs.getString("refresh","");
            TokenRequest.Builder refreshRequest = new TokenRequest.Builder(AppAuthInit.getConfiguration(AppAuthDiscoverable.GOOGLE), GOOGLE_CLIENTID);
            refreshRequest.setGrantType(GrantTypeValues.REFRESH_TOKEN);
            refreshRequest.setRefreshToken(currentRefreshToken);
            service.performTokenRequest(refreshRequest.build(),((response, ex) -> {
                if(response != null) {
                    m_accountClientInfo.signature = response.idToken;
                    new Thread(()->requestUserdata(response, response.refreshToken == null ? currentRefreshToken : response.refreshToken)).start();
                    return;
                }
                if(ex != null) {
                   eraseSignInData();
                    Log.e("GoogleSignIn",ex.toString());
                }
                m_activity.runOnUiThread(this::submitSignOutState);
            }));
        }
    }

    public void SignOut() {
        eraseSignInData();
        m_activity.runOnUiThread(this::submitSignOutState);
    }

    public void RefreshCredentials(final SystemAccountClientRequestState systemAccountClientRequestState) {
        SignIn();
    }

    public void onResume() {

    }

    @Override
    public void finish(boolean result) {
        if(result) {
            m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
        }else{
            m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_NotAvailable;
        }
        m_activity.runOnUiThread(()->m_callback.UpdateClientInfo(m_accountClientInfo));
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        Log.i("GoogleSignIn","onActivityResult "+i);
        if(i == GOOGLE_REQUEST) {
            Log.i("GoogleSignIn", "Handling sign-in...");
            AuthorizationResponse authorizationResponse = AuthorizationResponse.fromIntent(intent);
            AuthorizationException authorizationException = AuthorizationException.fromIntent(intent);
            if(authorizationResponse != null && authorizationResponse.authorizationCode != null) {
                Log.i("GoogleSignIn", "Handling valid log-in response");
                service.performTokenRequest(authorizationResponse.createTokenExchangeRequest(), ((response, ex) -> {
                    if(response != null) {
                        m_accountClientInfo.signature = response.idToken;
                        new Thread(()->requestUserdata(response)).start();
                        return;
                    }
                    if(authorizationException != null) Log.e("GoogleSignIn",authorizationException.toString());
                    m_activity.runOnUiThread(this::submitSignOutState);
                }));
                return;
            }
            if(authorizationException != null) Log.e("GoogleSignIn",authorizationException.toString());
            submitSignOutState();
        }
    }
    
    private void requestUserdata(TokenResponse response) {
        requestUserdata(response, response.refreshToken);
    }
    
    @SuppressLint("ApplySharedPref")
    private void requestUserdata(TokenResponse response, String refreshToken) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL("https://www.googleapis.com/oauth2/v1/userinfo").openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + response.accessToken);
            String info = AppAuthInit.dump(urlConnection.getInputStream());
            JSONObject obj = new JSONObject(info);
            m_accountClientInfo.accountId = obj.getString("id");
            m_accountClientInfo.alias = obj.getString("email");
            m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedIn;
            SharedPreferences.Editor editor = accountPrefs.edit()
                    .putString("signature", m_accountClientInfo.signature)
                    .putString("id", m_accountClientInfo.accountId)
                    .putString("alias", m_accountClientInfo.alias);
            if(response.accessTokenExpirationTime != null) {
                editor.putLong("expiry", response.accessTokenExpirationTime);
            }
                    editor.putString("refresh", refreshToken)
                    .commit();
            m_activity.runOnUiThread(()->m_callback.UpdateClientInfo(m_accountClientInfo));
            return;
        }catch (Exception e) {
            e.printStackTrace();
        }
        m_activity.runOnUiThread(this::submitSignOutState);
    }
    private void submitSignOutState() {
        m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
        m_callback.UpdateClientInfo(m_accountClientInfo);
    }
    private void eraseSignInData() {
        accountPrefs.edit().clear().apply();
    }
}

