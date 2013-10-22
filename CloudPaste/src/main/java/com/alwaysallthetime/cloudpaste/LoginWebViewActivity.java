package com.alwaysallthetime.cloudpaste;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alwaysallthetime.adnlib.AppDotNetClient;
import com.alwaysallthetime.adnlib.data.Token;
import com.alwaysallthetime.adnlib.response.TokenResponseHandler;
import com.alwaysallthetime.cloudpaste.client.CloudPasteADNClient;

public class LoginWebViewActivity extends BaseLoginActivity {

    private static final String REDIRECT_URL = "http://localhost:8000/";
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_webview);

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        mWebView = (WebView) findViewById(R.id.LoginWebView);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String url = String.format("https://account.app.net/oauth/authenticate?client_id=%s&response_type=token&scope=%s", CloudPasteADNClient.CLIENT_ID, CloudPasteADNClient.AUTH_SCOPES);

        mWebView.setWebViewClient(new LoginWebViewClient());
        mWebView.loadUrl(url);
    }

    private class LoginWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(url.indexOf(REDIRECT_URL) == 0) {
                String access = "#access_token=";
                final String token = url.substring(url.indexOf(access) + access.length());
                final AppDotNetClient client = CloudPasteADNClient.getInstance();
                client.setToken(token);
                client.retrieveCurrentToken(new TokenResponseHandler() {
                    @Override
                    public void onSuccess(Token responseData) {
                        onTokenObtained(token, responseData);
                    }
                });
            }
        }
    }
}
