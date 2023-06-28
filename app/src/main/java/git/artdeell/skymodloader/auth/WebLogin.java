package git.artdeell.skymodloader.auth;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Handler;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tgc.sky.BuildConfig;
import com.tgc.sky.GameActivity;
import com.tgc.sky.accounts.SystemAccountClientInfo;
import com.tgc.sky.accounts.SystemAccountClientRequestState;
import com.tgc.sky.accounts.SystemAccountClientState;
import com.tgc.sky.accounts.SystemAccountInterface;
import com.tgc.sky.accounts.SystemAccountServerInfo;
import com.tgc.sky.accounts.SystemAccountServerState;
import com.tgc.sky.accounts.SystemAccountType;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class WebLogin extends WebViewClient implements SystemAccountInterface, Runnable {
    private final SystemAccountType accountType;
    private final String loginUrl;
    private final String redirectUrl;
    private final Handler windowRestoreHandler = new Handler();
    private Dialog dialog;
    private WebView webView;
    private SystemAccountClientInfo m_accountClientInfo;
    private SystemAccountServerInfo m_accountServerInfo;
    private GameActivity m_activity;
    private SystemAccountInterface.UpdateClientInfoCallback m_callback;

    public WebLogin(String webLoginType, SystemAccountType systemAccountType) {
        this.accountType = systemAccountType;
        loginUrl = String.format("https://%s/account/auth/oauth_signin?type=%s&token=", BuildConfig.SKY_SERVER_HOSTNAME, webLoginType);
        redirectUrl = String.format("https://%s/account/auth/oauth_redirect", BuildConfig.SKY_SERVER_HOSTNAME);
    }

    @Override
    public SystemAccountClientInfo GetClientInfo() {
        return m_accountClientInfo;
    }

    @Override
    public SystemAccountServerInfo GetServerInfo() {
        return m_accountServerInfo;
    }

    public void Initialize(GameActivity gameActivity, SystemAccountInterface.UpdateClientInfoCallback updateClientInfoCallback) {
        this.m_activity = gameActivity;
        this.m_callback = updateClientInfoCallback;
        SystemAccountClientInfo systemAccountClientInfo = new SystemAccountClientInfo();
        this.m_accountClientInfo = systemAccountClientInfo;
        systemAccountClientInfo.accountType = accountType;
        this.m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
        this.m_accountClientInfo.requestState = SystemAccountClientRequestState.kSystemAccountClientRequestState_Idle;
        SystemAccountServerInfo systemAccountServerInfo = new SystemAccountServerInfo();
        this.m_accountServerInfo = systemAccountServerInfo;
        systemAccountServerInfo.type = accountType;
        this.m_accountServerInfo.state = SystemAccountServerState.kSystemAccountServerState_Initializing;
        this.m_callback.UpdateClientInfo(this.m_accountClientInfo);
    }

    public void SignIn() {
        m_activity.runOnUiThread(()->{
            m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SigningIn;
            m_callback.UpdateClientInfo(m_accountClientInfo);
            startSignIn();
        });
    }

    public void SignOut() {
        m_activity.runOnUiThread(()->{
            m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SigningOut;
            CookieManager.getInstance().removeAllCookies((bool)-> {
                    m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
                    m_callback.UpdateClientInfo(m_accountClientInfo);
            });
        });
    }

    public void RefreshCredentials(SystemAccountClientRequestState systemAccountClientRequestState) {
        SignIn();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void startSignIn() {
        dialog = new Dialog(m_activity);
        dialog.setOnDismissListener(dialog1 -> submitSignOutState());
        webView = new WebView(m_activity) {
            @Override
            public boolean onCheckIsTextEditor() {
                return true;
            }
        };
        dialog.setContentView(webView);
        webView.setWebViewClient(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadUrl(loginUrl);
        startWatching();
    }


    public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
        final String url = webResourceRequest.getUrl().toString();
        if(url.startsWith(redirectUrl)) {
            dialog.hide();
            new Thread(()-> processLoading(url)).start();
            return true;
        }
        return false;
    }

    private void processLoading(final String url) {
        CookieManager.getInstance().flush();
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[1024]; int read;
            while((read = inputStream.read(bArr)) != -1) {
                byteArrayOutputStream.write(bArr, 0 , read);
            }
            byteArrayOutputStream.flush();
            inputStream.close();
            httpURLConnection.disconnect();
            JSONObject obj = new JSONObject(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
            submitSignInState(obj.optString("id"), obj.optString("alias"), obj.optString("token"));
        }catch (Exception e) {
            e.printStackTrace();
            submitSignOutState();
        }
    }

    private void submitSignOutState() {
        m_activity.runOnUiThread(()-> {
            m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedOut;
            m_callback.UpdateClientInfo(m_accountClientInfo);
        });
    }

    private void submitSignInState(final String id, final String alias, final String signature) {
        m_activity.runOnUiThread(()->{
            m_accountClientInfo.accountId = id;
            m_accountClientInfo.alias = alias;
            m_accountClientInfo.signature = signature;
            m_accountClientInfo.state = SystemAccountClientState.kSystemAccountClientState_SignedIn;
            m_callback.UpdateClientInfo(this.m_accountClientInfo);
        });
    }

    public void startWatching() {
        windowRestoreHandler.postDelayed(this, 0);
    }

    @Override
    public void run() {
        windowRestoreHandler.removeCallbacks(this);
        if(webView.getContentHeight() > 20) {
            dialog.show();
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }else{
            windowRestoreHandler.postDelayed(this, 250);
        }
    }
}
