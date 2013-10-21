package com.alwaysallthetime.cloudpaste;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.Toast;

import com.alwaysallthetime.adnlib.AppDotNetClient;
import com.alwaysallthetime.adnlib.GeneralParameter;
import com.alwaysallthetime.adnlib.QueryParameters;
import com.alwaysallthetime.adnlib.data.Channel;
import com.alwaysallthetime.adnlibutils.MessagePlus;
import com.alwaysallthetime.adnlibutils.PrivateChannelUtility;
import com.alwaysallthetime.adnlibutils.manager.MessageManager;
import com.alwaysallthetime.cloudpaste.adapter.MainListViewAdapter;
import com.alwaysallthetime.cloudpaste.client.CloudPasteADNClient;

import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "CloudPaste_MainActivity";
    private static final String CLOUDPASTE_CHANNEL_TYPE = "com.alwaysallthetime.cloudpaste";

    public static final QueryParameters QUERY_PARAMETERS =
            new QueryParameters(GeneralParameter.INCLUDE_MESSAGE_ANNOTATIONS,
                                GeneralParameter.INCLUDE_ANNOTATIONS,
                                GeneralParameter.EXCLUDE_DELETED);

    private AppDotNetClient mClient;
    private MessageManager mMessageManager;
    private Channel mCloudPasteChannel;
    private Handler mHandler;
    private MainListViewAdapter mListAdapter;
    private ListView mListView;
    private boolean mIsLoadingMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.MainListView);
        mHandler = new Handler();
        mClient = CloudPasteADNClient.getInstance();

        initChannel(new Runnable() {
            @Override
            public void run() {
                initMessageManager(PrivateChannelUtility.getChannel(CLOUDPASTE_CHANNEL_TYPE));

                final String channelId = mCloudPasteChannel.getId();
                final List<MessagePlus> messages = mMessageManager.getMessageList(channelId);
                if(messages != null) {
                    updateListAdapter(messages, false);
                }

                mMessageManager.retrieveNewestMessages(channelId, mMessageManagerResponseHandler);
            }
        });
    }

    private void initChannel(final Runnable completionRunnable) {
        mCloudPasteChannel = PrivateChannelUtility.getChannel(CLOUDPASTE_CHANNEL_TYPE);
        if(mCloudPasteChannel == null) {
            PrivateChannelUtility.retrieveChannel(mClient, CLOUDPASTE_CHANNEL_TYPE, new PrivateChannelUtility.PrivateChannelHandler() {
                @Override
                public void onResponse(Channel channel) {
                    if(channel == null) {
                        PrivateChannelUtility.createChannel(mClient, CLOUDPASTE_CHANNEL_TYPE, new PrivateChannelUtility.PrivateChannelHandler() {
                            @Override
                            public void onResponse(Channel channel) {
                                mCloudPasteChannel = channel;
                                mHandler.post(completionRunnable);
                            }

                            @Override
                            public void onError(Exception error) {
                                showErrorToast();
                            }
                        });
                    } else {
                        mCloudPasteChannel = channel;
                        mHandler.post(completionRunnable);
                    }
                }

                @Override
                public void onError(Exception error) {
                    showErrorToast();
                }
            });
        }
    }

    private void showErrorToast() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, R.string.generic_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initMessageManager(Channel channel) {
        if(mMessageManager == null) {
            mMessageManager = new MessageManager(this, CloudPasteADNClient.getInstance());
            mMessageManager.setParameters(channel.getId(), QUERY_PARAMETERS);
        }
    }

    private void updateListAdapter(List<MessagePlus> messages, boolean appending) {
        if(mListAdapter == null) {
            mListAdapter = new MainListViewAdapter(this, messages);
            mListView.setAdapter(mListAdapter);
        } else {
            if(appending) {
                mListAdapter.appendAndRefresh(messages);
            } else {
                mListAdapter.prependAndRefresh(messages);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private final MessageManager.MessageManagerResponseHandler mMessageManagerResponseHandler = new MessageManager.MessageManagerResponseHandler() {
        @Override
        public void onSuccess(final List<MessagePlus> responseData, final boolean appended) {
            mIsLoadingMore = false;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateListAdapter(responseData, appended);
                }
            });
        }

        @Override
        public void onError(Exception exception) {
            mIsLoadingMore = false;
            Log.d(TAG, exception.getMessage(), exception);
            showErrorToast();
        }
    };
}
