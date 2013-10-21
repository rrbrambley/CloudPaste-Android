package com.alwaysallthetime.cloudpaste.client;

import com.alwaysallthetime.adnlib.AppDotNetClient;
import com.alwaysallthetime.adnlibutils.ADNSharedPreferences;
import com.alwaysallthetime.adnlibutils.ConfigurationUtility;

public class CloudPasteADNClient {
    public static final String CLIENT_ID = "ApLZCN8QxnG2D54JcnUkUApH88nLHZeC";
    public static final String AUTH_SCOPES = "basic,messages";

    private static AppDotNetClient sInstance;

    public static AppDotNetClient getInstance() {
        if(sInstance == null) {
            sInstance = new AppDotNetClient();

            if(ADNSharedPreferences.isLoggedIn()) {
                sInstance.setToken(ADNSharedPreferences.getAccessToken());
                ConfigurationUtility.updateConfiguration(sInstance);
            }
        }
        return sInstance;
    }
}
