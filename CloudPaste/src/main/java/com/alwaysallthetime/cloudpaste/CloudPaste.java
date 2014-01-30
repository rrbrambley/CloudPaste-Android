package com.alwaysallthetime.cloudpaste;

import com.alwaysallthetime.adnlib.AppDotNetClient;
import com.alwaysallthetime.adnlib.data.Channel;
import com.alwaysallthetime.adnlib.data.Message;
import com.alwaysallthetime.adnlib.response.MessageResponseHandler;
import com.alwaysallthetime.messagebeast.PrivateChannelUtility;
import com.alwaysallthetime.messagebeast.manager.MessageManager;
import com.alwaysallthetime.messagebeast.model.MessagePlus;

import java.util.List;

public class CloudPaste {
    public static final String CLOUDPASTE_CHANNEL_TYPE = "com.alwaysallthetime.cloudpaste";

    public static void pasteToCloud(final String text, final AppDotNetClient client) {
        pasteToCloud(text, client, (MessageResponseHandler) null);
    }

    public static void pasteToCloud(final String text, final AppDotNetClient client, final MessageResponseHandler responseHandler) {
        PrivateChannelUtility.getOrCreateChannel(client, CLOUDPASTE_CHANNEL_TYPE, new PrivateChannelUtility.PrivateChannelGetOrCreateHandler() {
            @Override
            public void onResponse(Channel channel, boolean createdNewChannel) {
                pasteToCloud(text, client, channel, responseHandler);
            }

            @Override
            public void onError(Exception error) {
                if(responseHandler != null) {
                    responseHandler.onError(error);
                }
            }
        });
    }

    public static void pasteToCloud(String text, AppDotNetClient client, Channel cloudPasteChannel) {
        pasteToCloud(text, client, cloudPasteChannel, null);
    }

    public static void pasteToCloud(String text, AppDotNetClient client, Channel cloudPasteChannel, final MessageResponseHandler responseHandler) {
        client.createMessage(cloudPasteChannel, new Message(text), new MessageResponseHandler() {
            @Override
            public void onSuccess(Message responseData) {
                if(responseHandler != null) {
                    responseHandler.onSuccess(responseData);
                }
            }

            @Override
            public void onError(Exception error) {
                super.onError(error);
                if(responseHandler != null) {
                    responseHandler.onError(error);
                }
            }
        });
    }

    public static void pasteToCloud(final String text, final MessageManager messageManager, final MessageResponseHandler responseHandler) {
        PrivateChannelUtility.getOrCreateChannel(messageManager.getClient(), CLOUDPASTE_CHANNEL_TYPE, new PrivateChannelUtility.PrivateChannelGetOrCreateHandler() {
            @Override
            public void onResponse(Channel channel, boolean createdNewChannel) {
                pasteToCloud(text, channel, messageManager, responseHandler);
            }

            @Override
            public void onError(Exception error) {
                if(responseHandler != null) {
                    responseHandler.onError(error);
                }
            }
        });
    }

    public static void pasteToCloud(final String text, Channel channel, MessageManager messageManager) {
        pasteToCloud(text, channel, messageManager, null);
    }

    public static void pasteToCloud(final String text, Channel channel, MessageManager messageManager, final MessageResponseHandler responseHandler) {
        messageManager.createMessage(channel.getId(), new Message(text), new MessageManager.MessageManagerResponseHandler() {
            @Override
            public void onSuccess(List<MessagePlus> responseData) {
                if(responseHandler != null) {
                    responseHandler.onSuccess(responseData.get(0).getMessage());
                }
            }

            @Override
            public void onError(Exception exception) {
                if(responseHandler != null) {
                    responseHandler.onError(exception);
                }
            }
        });
    }
}
