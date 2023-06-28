package git.artdeell.skymodloader.auth;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tgc.sky.GameActivity;
import com.tgc.sky.accounts.SystemAccountClientInfo;
import com.tgc.sky.accounts.SystemAccountClientRequestState;
import com.tgc.sky.accounts.SystemAccountClientState;
import com.tgc.sky.accounts.SystemAccountInterface;
import com.tgc.sky.accounts.SystemAccountServerInfo;
import com.tgc.sky.accounts.SystemAccountServerState;
import com.tgc.sky.accounts.SystemAccountType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Facebook implements SystemAccountInterface {
    /* access modifiers changed from: private */
    public SystemAccountClientInfo m_accountClientInfo;
    private SystemAccountServerInfo m_accountServerInfo;
    /* access modifiers changed from: private */
    public GameActivity m_activity;
    /* access modifiers changed from: private */
    public SystemAccountInterface.UpdateClientInfoCallback m_callback;
    /* access modifiers changed from: private */
    private SharedPreferences m_accountStorage;


    public OnPermissionCallback m_permissionCallback;

    public interface OnPermissionCallback {
        void onCallback(boolean z, String str);
    }


    public void Initialize(GameActivity gameActivity, SystemAccountInterface.UpdateClientInfoCallback updateClientInfoCallback) {
        this.m_accountStorage = gameActivity.getSharedPreferences("accounts", Context.MODE_PRIVATE);
        this.m_activity = gameActivity;
        this.m_callback = updateClientInfoCallback;
        SystemAccountClientInfo systemAccountClientInfo = new SystemAccountClientInfo();
        this.m_accountClientInfo = systemAccountClientInfo;
        systemAccountClientInfo.accountType = SystemAccountType.kSystemAccountType_Facebook;
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
        SystemAccountServerInfo systemAccountServerInfo = new SystemAccountServerInfo();
        this.m_accountServerInfo = systemAccountServerInfo;
        systemAccountServerInfo.type = SystemAccountType.kSystemAccountType_Facebook;
        this.m_accountServerInfo.state = SystemAccountServerState.kSystemAccountServerState_UnLinked;
    }

    public SystemAccountClientInfo GetClientInfo() {
        return this.m_accountClientInfo;
    }

    public SystemAccountServerInfo GetServerInfo() {
        return this.m_accountServerInfo;
    }

    public void SignIn() {
        Log.i("FacebookAuth","Called SignIn()");
        if(!m_accountStorage.contains("facebookToken")) {
            authenticate();
            return;
        }
        new Thread(()->{
            if(graphAuthorize(m_accountStorage.getString("facebookToken", ""))) {
                authenticate();
            }
        }).start();
        Log.i("FacebookAuth","Call Over!");
    }

    public void SignOut() {
        m_accountStorage.edit().remove("facebookToken").apply();
        m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
        m_callback.UpdateClientInfo(m_accountClientInfo);
    }

    public void RefreshCredentials(final SystemAccountClientRequestState systemAccountClientRequestState) {
        SignIn();
    }

    public boolean HasAppFriendsPermission() {
        return this.m_accountClientInfo.permissions != null && this.m_accountClientInfo.permissions.contains("user_friends");
    }

    public boolean GetAppFriendsPermission(OnPermissionCallback onPermissionCallback) {
        return false;
    }


    private void authenticate() {
        m_activity.runOnUiThread(()->{
            Log.i("FacebookAuth","Beginning authentification...");
            final Dialog dialog = new Dialog(this.m_activity);
            WebView webView = new WebView(this.m_activity);
            dialog.setContentView(webView);
            webView.loadUrl("https://m.facebook.com/login.php?" +
                    "skip_api_login=1&" +
                    "api_key=293746044767069&" +
                    "app_id=293746044767069&" +
                    "signed_next=1&" +
                    "next=https%3A%2F%2Fm.facebook.com%2Fv6.0%2Fdialog%2Foauth%3Fclient_id%3D293746044767069%26cbt%3D1628350401972%26e2e%3D%257B%2522init%2522%253A1628350401972%257D%26ies%3D0%26sdk%3Dandroid-6.5.1%26scope%3Dpublic_profile%252Cuser_friends%26state%3D%257B%25220_auth_logger_id%2522%253A%2522a71242a5-8fa3-40ce-b310-fc5d0d6d5040%2522%252C%25223_method%2522%253A%2522web_view%2522%257D%26default_audience%3Dfriends%26login_behavior%3DNATIVE_WITH_FALLBACK%26redirect_uri%3Dfbconnect%253A%252F%252Fsuccess%26auth_type%3Drerequest%26display%3Dtouch%26response_type%3Dtoken%252Csigned_request%252Cgraph_domain%26return_scopes%3Dtrue%26ret%3Dlogin%26fbapp_pres%3D0%26logger_id%3Da71242a5-8fa3-40ce-b310-fc5d0d6d5040%26tp%3Dunspecified" +
                    "&cancel_url=fbconnect%3A%2F%2Fsuccess%3Ferror%3Daccess_denied%26error_code%3D200%26error_description%3DPermissions%2Berror%26error_reason%3Duser_denied%26state%3D%257B%25220_auth_logger_id%2522%253A%2522a71242a5-8fa3-40ce-b310-fc5d0d6d5040%2522%252C%25223_method%2522%253A%2522web_view%2522%257D" +
                    "&display=touch");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if(request.getUrl().getScheme().equals("fbconnect")) {
                        String url = request.getUrl().toString();
                        final String tok = "access_token=";
                        int accessTokenIndex = url.indexOf(tok);
                        if(accessTokenIndex == -1) {
                            m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
                            m_callback.UpdateClientInfo(m_accountClientInfo);
                            return true;
                        }
                        final String token = url.substring(accessTokenIndex + tok.length(), url.indexOf('&',accessTokenIndex));
                        m_activity.runOnUiThread(()->{
                            dialog.dismiss();
                            ProgressDialog progressDialog = new ProgressDialog(m_activity);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.setIndeterminate(true);
                            progressDialog.show();
                            new Thread(()->{
                                if(graphAuthorize(token)) {
                                    m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
                                    m_callback.UpdateClientInfo(m_accountClientInfo);
                                }
                                m_activity.runOnUiThread(progressDialog::dismiss);
                            }).start();
                        });
                    }
                    return false;
                }
            });
            dialog.setTitle("Facebook Signin");
            dialog.setCancelable(true);
            dialog.setOnCancelListener((d)->{
                m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
                m_callback.UpdateClientInfo(m_accountClientInfo);
            });
            dialog.show();
        });
    }
    @SuppressLint("ApplySharedPref")
    private boolean graphAuthorize(String token) {
        try {
            JSONObject resp = doGraphRequest("me?field=name&access_token="+token);
            m_accountClientInfo.accountId = resp.getString("id");
            m_accountClientInfo.alias = resp.getString("name");
            m_accountClientInfo.signature = token;
            m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedIn;
            m_accountStorage.edit().putString("facebookToken",token).commit();
            m_callback.UpdateClientInfo(m_accountClientInfo);
            Log.i("FacebookAuth","graphAuthorize done");
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private static JSONObject doGraphRequest(String call) throws IOException, JSONException {
        URLConnection connection = new URL("https://graph.fb.gg/v7.0/"+call).openConnection();
        connection.connect();
        try (InputStream rd = connection.getInputStream()) {
            StringBuilder ret = new StringBuilder();
            int cpt;
            byte[] buf = new byte[1024];
            while ((cpt = rd.read(buf)) != -1) {
                ret.append(new String(buf, 0, cpt));
            }
            return new JSONObject(ret.toString());
        }
    }
}
