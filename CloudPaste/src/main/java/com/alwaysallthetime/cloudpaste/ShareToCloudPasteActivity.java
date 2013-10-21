package com.alwaysallthetime.cloudpaste;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.alwaysallthetime.adnlib.AppDotNetClient;
import com.alwaysallthetime.adnlib.data.Message;
import com.alwaysallthetime.adnlib.response.MessageResponseHandler;
import com.alwaysallthetime.adnlibutils.ADNSharedPreferences;
import com.alwaysallthetime.cloudpaste.client.CloudPasteADNClient;

public class ShareToCloudPasteActivity extends BaseCloudPasteActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Handler handler = new Handler();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        showProgress(R.string.share_progress);

        if(Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            if(ADNSharedPreferences.isLoggedIn()) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if(sharedText != null) {
                    AppDotNetClient client = CloudPasteADNClient.getInstance();
                    CloudPaste.pasteToCloud(sharedText, client, new MessageResponseHandler() {
                        @Override
                        public void onSuccess(Message responseData) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ShareToCloudPasteActivity.this, R.string.copied_toast, Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception error) {
                            super.onError(error);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ShareToCloudPasteActivity.this, R.string.generic_error, Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        }
                    });
                }
            } else {
                Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.generic_error, Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
