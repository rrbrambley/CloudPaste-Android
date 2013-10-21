package com.alwaysallthetime.cloudpaste;

import android.app.Activity;
import android.app.ProgressDialog;

public abstract class BaseCloudPasteActivity extends Activity {

    private ProgressDialog mProgressDialog;

    protected void showProgress(final int messageResourceId) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(messageResourceId));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    protected void hideProgress() {
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
