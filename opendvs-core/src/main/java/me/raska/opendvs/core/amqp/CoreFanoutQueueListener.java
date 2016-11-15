package me.raska.opendvs.core.amqp;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Listener used for obtaining events from other services.
 * 
 * @author raskaluk
 *
 */
@Service
public class CoreFanoutQueueListener {

    @RabbitListener(queues = "#{@coreFanoutQueue}")
    public void handleInputQueue(Object obj) {
        System.out.println(obj);
        // TODO
    }

}
