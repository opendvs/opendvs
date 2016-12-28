package me.raska.opendvs.core.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.core.FanoutEvent;
import me.raska.opendvs.core.configuration.WebSocketConfiguration;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate websocket;

    public void publishEvent(FanoutEvent event) {
        // TODO: switch to convertAndSendToUser after Spring Security
        // integration
        websocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/event", event,
                Collections.singletonMap("eventType", event.getClass().getSimpleName()));
    }
}
