package com.alwaysallthetime.cloudpaste;

import com.alwaysallthetime.adnlib.AppDotNetClient;
import com.alwaysallthetime.adnlib.data.Channel;
import com.alwaysallthetime.adnlib.data.Message;
import com.alwaysallthetime.adnlib.response.MessageResponseHandler;
import com.alwaysallthetime.adnlibutils.MessagePlus;
import com.alwaysallthetime.adnlibutils.PrivateChannelUtility;
import com.alwaysallthetime.adnlibutils.manager.MessageManager;

import java.util.List;

public class CloudPaste {
    public static final String CLOUDPASTE_CHANNEL_TYPE = "com.alwaysallthetime.cloudpaste";

    public static void pasteToCloud(final String text, final AppDotNetClient client) {
        pasteToCloud(text, client, (MessageResponseHandler) null);
    }

    public static void pasteToCloud(final String text, final AppDotNetClient client, final MessageResponseHandler responseHandler) {
        Channel channel = PrivateChannelUtility.getChannel(CLOUDPASTE_CHANNEL_TYPE);
        if(channel != null) {
            pasteToCloud(text, client, channel, responseHandler);
        } else {
            PrivateChannelUtility.retrieveChannel(client, CLOUDPASTE_CHANNEL_TYPE, new PrivateChannelUtility.PrivateChannelHandler() {
                @Override
                public void onResponse(Channel channel) {
                    if(channel == null) {
                        PrivateChannelUtility.createChannel(client, CLOUDPASTE_CHANNEL_TYPE, new PrivateChannelUtility.PrivateChannelHandler() {
                            @Override
                            public void onResponse(Channel channel) {
                                pasteToCloud(text, client, channel, responseHandler);
                            }

                            @Override
                            public void onError(Exception error) {
                                if(responseHandler != null) {
                                    responseHandler.onError(error);
                                }
                            }
                        });
                    } else {
                        pasteToCloud(text, client, channel, responseHandler);
                    }
                }

                @Override
                public void onError(Exception error) {
                    if(responseHandler != null) {
                        responseHandler.onError(error);
                    }
                }
            });
        }
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

    public static void pasteToCloud(final String text, Channel channel, MessageManager messageManager) {
        pasteToCloud(text, channel, messageManager, null);
    }

    public static void pasteToCloud(final String text, Channel channel, MessageManager messageManager, final MessageResponseHandler responseHandler) {
        messageManager.createMessage(channel.getId(), new Message(text), new MessageManager.MessageManagerResponseHandler() {
            @Override
            public void onSuccess(List<MessagePlus> responseData, boolean appended) {
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
