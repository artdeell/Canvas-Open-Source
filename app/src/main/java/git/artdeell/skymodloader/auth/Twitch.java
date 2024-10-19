package git.artdeell.skymodloader.auth;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.StrictMode;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.tgc.sky.BuildConfig;

import com.tgc.sky.GameActivity;
import com.tgc.sky.SystemIO_android;
import com.tgc.sky.accounts.SystemAccountClientInfo;
import com.tgc.sky.accounts.SystemAccountClientRequestState;
import com.tgc.sky.accounts.SystemAccountClientState;
import com.tgc.sky.accounts.SystemAccountInterface;
import com.tgc.sky.accounts.SystemAccountServerInfo;
import com.tgc.sky.accounts.SystemAccountType;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;


public class Twitch implements SystemAccountInterface {
    volatile boolean dialogCancelled;
    String loginUrl;
    private SystemAccountClientInfo m_accountClientInfo;
    private SystemAccountServerInfo m_accountServerInfo;
    private GameActivity m_activity;
    private SystemAccountInterface.UpdateClientInfoCallback m_callback;
    private int m_previousOrientation;
    String redirectUrl;

    @Override // com.tgc.sky.accounts.SystemAccountInterface
    public void Initialize(GameActivity gameActivity, SystemAccountInterface.UpdateClientInfoCallback updateClientInfoCallback) {
        this.m_activity = gameActivity;
        this.m_callback = updateClientInfoCallback;
        SystemAccountClientInfo systemAccountClientInfo = new SystemAccountClientInfo();
        this.m_accountClientInfo = systemAccountClientInfo;
        systemAccountClientInfo.accountType = SystemAccountType.kSystemAccountType_Twitch;
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
        this.m_callback.UpdateClientInfo(this.m_accountClientInfo);
    }

    @Override // com.tgc.sky.accounts.SystemAccountInterface
    public SystemAccountClientInfo GetClientInfo() {
        return this.m_accountClientInfo;
    }

    public SystemAccountServerInfo GetServerInfo() {
        return this.m_accountServerInfo;
    }

    @Override // com.tgc.sky.accounts.SystemAccountInterface
    public void SignIn() {
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SigningIn;
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.accounts.Twitch.1
            @Override // java.lang.Runnable
            public void run() {
                Twitch.this.m_callback.UpdateClientInfo(Twitch.this.m_accountClientInfo);
                Twitch.this.StartFlow();
            }
        });
    }

    @Override // com.tgc.sky.accounts.SystemAccountInterface
    public void SignOut() {
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SigningOut;
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.accounts.Twitch.2
            @Override // java.lang.Runnable
            public void run() {
                Twitch.this.m_callback.UpdateClientInfo(Twitch.this.m_accountClientInfo);
                CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() { // from class: com.tgc.sky.accounts.Twitch.2.1
                    @Override // android.webkit.ValueCallback
                    public void onReceiveValue(Boolean bool) {
                        Twitch.this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
                        Twitch.this.m_callback.UpdateClientInfo(Twitch.this.m_accountClientInfo);
                    }
                });
            }
        });
    }

    public void RefreshCredentials(SystemAccountClientRequestState systemAccountClientRequestState) {
        SignIn();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void OnAccountSuccess(final String str, final String str2, final String str3, final String str4) {
        this.m_activity.runOnUiThread(new Runnable() { // from class: com.tgc.sky.accounts.Twitch.3
            @Override // java.lang.Runnable
            public void run() {
                if (!str4.isEmpty() || str.isEmpty() || str3.isEmpty()) {
                    Twitch.this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
                } else {
                    Twitch.this.m_accountClientInfo.accountId = str;
                    Twitch.this.m_accountClientInfo.alias = str2;
                    Twitch.this.m_accountClientInfo.signature = str3;
                    Twitch.this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedIn;
                }
                Twitch.this.m_callback.UpdateClientInfo(Twitch.this.m_accountClientInfo);
            }
        });
    }


    public void StartFlow() {
        //this.m_activity.portraitOnResume = true;
        //this.m_previousOrientation = this.m_activity.getRequestedOrientation();
        //this.m_activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        final Dialog dialog = new Dialog(this.m_activity);
        WebView webView = new WebView(this.m_activity);
        dialog.setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        String GetPushNotificationToken = SystemIO_android.getInstance().GetPushNotificationToken();
        if (GetPushNotificationToken == null) {
            GetPushNotificationToken = "";
        }
        try {
            GetPushNotificationToken = URLEncoder.encode(GetPushNotificationToken, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.loginUrl = String.format("https://%s/account/auth/oauth_signin?type=Twitch&token=cKN45n7UTSKHNoyzdugWNE:APA91bFg8MGDK26uj-RjRrRSANDGST4AqE29kh3ygCzN0IZWLgGis2K16aD9JoYXnaRBD2LgghA18Bc0ZG76AuWEzr3eAMTSRen8SsBPjtPftUVnuXECrjVfhd9z_WeDbx9MaHUO7GS9", BuildConfig.SKY_SERVER_HOSTNAME, GetPushNotificationToken);
        this.redirectUrl = String.format("https://%s/account/auth/oauth_redirect", BuildConfig.SKY_SERVER_HOSTNAME);
        webView.loadUrl(this.loginUrl);
        webView.setWebViewClient(new WebViewClient() { // from class: com.tgc.sky.accounts.Twitch.4
            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView2, WebResourceRequest webResourceRequest) {
                String uri = webResourceRequest.getUrl().toString();
                if (uri.startsWith(Twitch.this.redirectUrl)) {
                    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
                    try {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(uri).openConnection();
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] bArr = new byte[1024];
                        while (true) {
                            int read = bufferedInputStream.read(bArr, 0, 1024);
                            if (read == -1) {
                                break;
                            }
                            byteArrayOutputStream.write(bArr, 0, read);
                        }
                        byteArrayOutputStream.flush();
                        String str = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
                        httpURLConnection.disconnect();
                        JSONObject jSONObject = new JSONObject(str);
                        Twitch.this.OnAccountSuccess(jSONObject.optString("id"), jSONObject.optString("alias"), jSONObject.optString("token"), "");
                    } catch (MalformedURLException e2) {
                        e2.printStackTrace();
                        Twitch.this.OnAccountSuccess("", "", "", "url error");
                    } catch (IOException e3) {
                        e3.printStackTrace();
                        Twitch.this.OnAccountSuccess("", "", "", "io error");
                    } catch (JSONException e4) {
                        e4.printStackTrace();
                        Twitch.this.OnAccountSuccess("", "", "", "json error");
                    }
                    Twitch.this.dialogCancelled = false;
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        dialog.setTitle("Twitch Sign In");
        dialog.setCancelable(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.tgc.sky.accounts.Twitch.5
            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                if (Twitch.this.dialogCancelled) {
                    Twitch.this.OnAccountSuccess("", "", "", "cancel");
                }
                //Twitch.this.m_activity.setRequestedOrientation(Twitch.this.m_previousOrientation);
                //Twitch.this.m_activity.portraitOnResume = false;
            }
        });
        this.dialogCancelled = true;

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.show();
    }
}