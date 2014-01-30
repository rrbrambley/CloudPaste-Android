package com.alwaysallthetime.cloudpaste;

import com.alwaysallthetime.cloudpaste.client.CloudPasteADNClient;
import com.alwaysallthetime.messagebeast.manager.MessageManager;

public class MessageManagerInstance {
    private static MessageManager sInstance;

    public static MessageManager getInstance() {
        if(sInstance == null) {
            MessageManager.MessageManagerConfiguration config = new MessageManager.MessageManagerConfiguration();
            sInstance = new MessageManager(CloudPasteADNClient.getInstance(), config);
        }
        return sInstance;
    }
}
