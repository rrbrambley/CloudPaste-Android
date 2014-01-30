package com.alwaysallthetime.cloudpaste;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.alwaysallthetime.adnlib.GeneralParameter;
import com.alwaysallthetime.adnlib.QueryParameters;
import com.alwaysallthetime.adnlib.data.Channel;
import com.alwaysallthetime.adnlib.data.Message;
import com.alwaysallthetime.cloudpaste.adapter.MainListViewAdapter;
import com.alwaysallthetime.cloudpaste.client.CloudPasteADNClient;
import com.alwaysallthetime.messagebeast.ADNSharedPreferences;
import com.alwaysallthetime.messagebeast.PrivateChannelUtility;
import com.alwaysallthetime.messagebeast.db.ADNDatabase;
import com.alwaysallthetime.messagebeast.manager.MessageManager;
import com.alwaysallthetime.messagebeast.model.MessagePlus;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseCloudPasteActivity {

    private static final String TAG = "CloudPaste_MainActivity";

    public static final QueryParameters QUERY_PARAMETERS =
            new QueryParameters(GeneralParameter.INCLUDE_MESSAGE_ANNOTATIONS,
                                GeneralParameter.INCLUDE_ANNOTATIONS,
                                GeneralParameter.EXCLUDE_DELETED);

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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessagePlus item = mListAdapter.getItem(position);
                copyText(item.getMessage().getText());
            }
        });
        registerForContextMenu(mListView);

        mHandler = new Handler();

        initChannel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCloudPasteChannel != null) {
            updateListAdapter();
        }
    }

    private void initChannel() {
        PrivateChannelUtility.getOrCreateChannel(CloudPasteADNClient.getInstance(), CloudPaste.CLOUDPASTE_CHANNEL_TYPE, new PrivateChannelUtility.PrivateChannelGetOrCreateHandler() {
            @Override
            public void onResponse(Channel channel, boolean createdNewChannel) {
                mCloudPasteChannel = channel;

                MessageManager messageManager = MessageManagerInstance.getInstance();
                messageManager.setParameters(channel.getId(), QUERY_PARAMETERS);

                String channelId = mCloudPasteChannel.getId();
                List<MessagePlus> messages = new ArrayList(messageManager.loadPersistedMessages(channelId, 100).values());
                if(messages != null) {
                    updateListAdapter(messages);
                }

                messageManager.retrieveNewestMessages(channelId, mMessageManagerResponseHandler);
            }

            @Override
            public void onError(Exception error) {
                showErrorToast();
            }
        });
    }

    private void showErrorToast() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, R.string.generic_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateListAdapter() {
        updateListAdapter(new ArrayList(MessageManagerInstance.getInstance().getMessageMap(mCloudPasteChannel.getId()).values()));
    }

    private void updateListAdapter(final List<MessagePlus> messages) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mListAdapter == null) {
                    mListAdapter = new MainListViewAdapter(MainActivity.this, messages);
                    mListView.setAdapter(mListAdapter);
                } else {
                    mListAdapter.refresh(messages);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.list_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.MenuDelete) {
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final MessagePlus m = mListAdapter.getItem(info.position);

            showProgress(R.string.delete_progress);
            Message message = m.getMessage();

            MessageManagerInstance.getInstance().deleteMessage(m, new MessageManager.MessageDeletionResponseHandler() {
                @Override
                public void onSuccess() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                            mListAdapter.removeItemAt(info.position);
                        }
                    });
                }

                @Override
                public void onError(Exception exception) {
                    Log.d(TAG, exception.getMessage(), exception);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                            Toast.makeText(MainActivity.this, R.string.generic_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.MenuDeleteAll) {
            confirmDeleteAll();
        } else if(itemId == R.id.MenuRefresh) {
            refresh();
        } else if(itemId == R.id.MenuSignOut) {
            signOut();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete_all_title)
                .setMessage(R.string.delete_all_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAll();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();

    }

    private void deleteAll() {
        showProgress(R.string.delete_progress);
        PrivateChannelUtility.deactivateChannel(CloudPasteADNClient.getInstance(), mCloudPasteChannel, new PrivateChannelUtility.PrivateChannelHandler() {
            @Override
            public void onResponse(Channel channel) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        initChannel();
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        showErrorToast();
                    }
                });
            }
        });
    }

    private void refresh() {
        showProgress(R.string.refresh_progess);
        MessageManagerInstance.getInstance().retrieveNewestMessages(mCloudPasteChannel.getId(), new MessageManager.MessageManagerResponseHandler() {
            @Override
            public void onSuccess(final List<MessagePlus> responseData) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        mListAdapter = null;
                        updateListAdapter();
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        showErrorToast();
                    }
                });
            }
        });
    }

    private void signOut() {
        MessageManagerInstance.getInstance().clear();
        ADNDatabase.getInstance(this).deleteAll();
        PrivateChannelUtility.clearChannels();
        ADNSharedPreferences.clearCredentials();
        Intent intent = new Intent(this, LoginWebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void copyText(String text) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("CloudPaste text", text);
            clipboard.setPrimaryClip(clip);
        }

        Toast.makeText(this, R.string.copied_toast, Toast.LENGTH_SHORT).show();
    }

    private final MessageManager.MessageManagerResponseHandler mMessageManagerResponseHandler = new MessageManager.MessageManagerResponseHandler() {
        @Override
        public void onSuccess(final List<MessagePlus> responseData) {
            mIsLoadingMore = false;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateListAdapter();
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
