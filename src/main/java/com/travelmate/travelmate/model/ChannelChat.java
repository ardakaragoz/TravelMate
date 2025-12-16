package com.travelmate.travelmate.model;

public class ChannelChat extends ChatRoom {
    Channel channel;

    public ChannelChat(int id, String name, Channel channel) {
        super(id, name);
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    } 
}
