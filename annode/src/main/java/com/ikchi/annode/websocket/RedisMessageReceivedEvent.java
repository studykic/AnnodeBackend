package com.ikchi.annode.websocket;

import java.util.Map;
import org.springframework.context.ApplicationEvent;

public class RedisMessageReceivedEvent extends ApplicationEvent {

    private final Map<String, String> messageMap;
    private final String channelName;

    public RedisMessageReceivedEvent(Object source, Map<String, String> messageMap,
        String channelName) {
        super(source);
        this.messageMap = messageMap;
        this.channelName = channelName;
    }

    public Map<String, String> getMessageMap() {
        return this.messageMap;
    }

    public String getChannelName() {
        return this.channelName;
    }
}