package me.raska.opendvs.core.amqp;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.core.FanoutEvent;
import me.raska.opendvs.core.service.WebSocketService;

/**
 * Listener used for obtaining events from other services.
 * 
 * @author raskaluk
 *
 */
@Service
public class CoreFanoutQueueListener {

    @Autowired
    private WebSocketService websocketService;

    @RabbitListener(queues = "#{@coreFanoutQueue}")
    public void handleInputQueue(FanoutEvent evt) {
        websocketService.publishEvent(evt);
    }

}
