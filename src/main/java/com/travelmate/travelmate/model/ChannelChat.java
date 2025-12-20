package com.travelmate.travelmate.model;

import com.travelmate.travelmate.session.ChannelList;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ChannelChat extends ChatRoom {
    Channel channel;

    public ChannelChat(String id, Channel channel) throws ExecutionException, InterruptedException {
        super(id, "channelChat");
        this.channel = channel;
    }

    public ChannelChat(String id, ArrayList<String> messages, ArrayList<String> activeUsers) throws ExecutionException, InterruptedException {
        super(id, "channelChat", messages, activeUsers);
        this.channel = ChannelList.getChannel(id);
    }

    public Channel getChannel() {
        return channel;
    } 
}
