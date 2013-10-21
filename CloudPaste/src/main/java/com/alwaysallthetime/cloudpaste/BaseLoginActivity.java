package com.alwaysallthetime.cloudpaste;

import android.app.Activity;
import android.content.Intent;

import com.alwaysallthetime.adnlib.data.Token;
import com.alwaysallthetime.adnlibutils.ADNSharedPreferences;
import com.alwaysallthetime.adnlibutils.ConfigurationUtility;
import com.alwaysallthetime.cloudpaste.client.CloudPasteADNClient;

public abstract class BaseLoginActivity extends Activity {

    protected void onTokenObtained(String accessToken, Token token) {
        ADNSharedPreferences.saveCredentials(accessToken, token);
        ConfigurationUtility.updateConfiguration(CloudPasteADNClient.getInstance());
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
