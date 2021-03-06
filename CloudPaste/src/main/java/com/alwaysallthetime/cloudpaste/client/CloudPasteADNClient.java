package com.alwaysallthetime.cloudpaste.client;

import com.alwaysallthetime.adnlib.AppDotNetClient;
import com.alwaysallthetime.messagebeast.ADNApplication;
import com.alwaysallthetime.messagebeast.ADNSharedPreferences;
import com.alwaysallthetime.messagebeast.ConfigurationUtility;

public class CloudPasteADNClient {
    //stick a client id here.
    public static final String CLIENT_ID = "ApLZCN8QxnG2D54JcnUkUApH88nLHZeC";
    public static final String AUTH_SCOPES = "basic,messages";

    private static AppDotNetClient sInstance;

    public static AppDotNetClient getInstance() {
        if(CLIENT_ID == null) {
            throw new RuntimeException("You gosta specify a value for CloudPasteADNClient.CLIENT_ID, bro.");
        }
        if(sInstance == null) {
            sInstance = new AppDotNetClient(ADNApplication.getContext());

            if(ADNSharedPreferences.isLoggedIn()) {
                sInstance.setToken(ADNSharedPreferences.getAccessToken());
                ConfigurationUtility.updateConfiguration(sInstance);
            }
        }
        return sInstance;
    }
}
