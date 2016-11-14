package me.raska.opendvs.resolver.amqp;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.raska.opendvs.base.resolver.ResolverAction;
import me.raska.opendvs.resolver.service.ResolverService;

@Service
public class ResolverQueueListener {

    @Autowired
    private ResolverService resolverService;

    @RabbitListener(queues = "#{@resolverQueue}")
    @Transactional
    public void handleInputQueue(ResolverAction action) {
        try {
            resolverService.handleMessage(action);
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}
