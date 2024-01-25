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

public class Steam implements SystemAccountInterface {
    volatile boolean dialogCancelled;
    String loginUrl;
    public SystemAccountClientInfo m_accountClientInfo;
    private SystemAccountServerInfo m_accountServerInfo;

    public GameActivity m_activity;
    public SystemAccountInterface.UpdateClientInfoCallback m_callback;
    String redirectUrl;

    public void Initialize(GameActivity gameActivity, SystemAccountInterface.UpdateClientInfoCallback updateClientInfoCallback) {
        this.m_activity = gameActivity;
        this.m_callback = updateClientInfoCallback;
        SystemAccountClientInfo systemAccountClientInfo = new SystemAccountClientInfo();
        this.m_accountClientInfo = systemAccountClientInfo;
        systemAccountClientInfo.accountType = SystemAccountType.kSystemAccountType_Steam;
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
        this.m_accountClientInfo.requestState = SystemAccountClientRequestState.kSystemAccountClientRequestState_Idle;
        this.m_callback.UpdateClientInfo(this.m_accountClientInfo);
    }

    public SystemAccountClientInfo GetClientInfo() {
        return this.m_accountClientInfo;
    }

    public SystemAccountServerInfo GetServerInfo() {
        return this.m_accountServerInfo;
    }
    public void SignIn() {
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SigningIn;
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                Steam.this.m_callback.UpdateClientInfo(Steam.this.m_accountClientInfo);
                Steam.this.StartFlow();
            }
        });
    }

    public void SignOut() {
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SigningOut;
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                Steam.this.m_callback.UpdateClientInfo(Steam.this.m_accountClientInfo);
                CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                    public void onReceiveValue(Boolean bool) {
                        Steam.this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
                        Steam.this.m_callback.UpdateClientInfo(Steam.this.m_accountClientInfo);
                    }
                });
            }
        });
    }

    public void RefreshCredentials(SystemAccountClientRequestState systemAccountClientRequestState) {
        SignIn();
    }

    public void OnAccountSuccess(String str, String str2, String str3, String str4) {
        final String str5 = str4;
        final String str6 = str;
        final String str7 = str3;
        final String str8 = str2;
        this.m_activity.runOnUiThread(new Runnable() {
            public void run() {
                if (!str5.isEmpty() || str6.isEmpty() || str7.isEmpty()) {
                    Steam.this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
                } else {
                    Steam.this.m_accountClientInfo.accountId = str6;
                    Steam.this.m_accountClientInfo.alias = str8;
                    Steam.this.m_accountClientInfo.signature = str7;
                    Steam.this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedIn;
                }
                Steam.this.m_callback.UpdateClientInfo(Steam.this.m_accountClientInfo);
            }
        });
    }

    public void StartFlow() {
        final Dialog dialog = new Dialog(this.m_activity);
        WebView webView = new WebView(this.m_activity);
        dialog.setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
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
        this.loginUrl = String.format("https://%s/account/auth/oauth_signin?type=Steam&token=cKN45n7UTSKHNoyzdugWNE:APA91bFg8MGDK26uj-RjRrRSANDGST4AqE29kh3ygCzN0IZWLgGis2K16aD9JoYXnaRBD2LgghA18Bc0ZG76AuWEzr3eAMTSRen8SsBPjtPftUVnuXECrjVfhd9z_WeDbx9MaHUO7GS9", new Object[]{BuildConfig.SKY_SERVER_HOSTNAME});
        this.redirectUrl = String.format("https://%s/account/auth/oauth_redirect", new Object[]{BuildConfig.SKY_SERVER_HOSTNAME});
        webView.loadUrl(this.loginUrl);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                String uri = webResourceRequest.getUrl().toString();
                if (!uri.startsWith(Steam.this.redirectUrl)) {
                    return false;
                }
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
                    Steam.this.OnAccountSuccess(jSONObject.optString("id"), jSONObject.optString("alias"), jSONObject.optString("token"), "");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Steam.this.OnAccountSuccess("", "", "", "url error");
                } catch (IOException e2) {
                    e2.printStackTrace();
                    Steam.this.OnAccountSuccess("", "", "", "io error");
                } catch (JSONException e3) {
                    e3.printStackTrace();
                    Steam.this.OnAccountSuccess("", "", "", "json error");
                }
                Steam.this.dialogCancelled = false;
                dialog.dismiss();
                return true;
            }
        });
        dialog.setTitle("Steam Sign In");
        dialog.setCancelable(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                if (Steam.this.dialogCancelled) {
                    Steam.this.OnAccountSuccess("", "", "", "cancel");
                }
            }
        });
        this.dialogCancelled = true;
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.show();
    }
}
