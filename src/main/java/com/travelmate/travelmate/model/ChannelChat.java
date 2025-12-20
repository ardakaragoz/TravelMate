package com.travelmate.travelmate.model;

import java.util.concurrent.ExecutionException;

public class ChannelChat extends ChatRoom {
    Channel channel;

    public ChannelChat(String id, Channel channel) throws ExecutionException, InterruptedException {
        super(id, "channelChat");
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    } 
}
