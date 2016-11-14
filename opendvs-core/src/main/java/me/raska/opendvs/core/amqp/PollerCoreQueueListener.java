package me.raska.opendvs.core.amqp;

import java.util.List;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.resolver.ResolverAction;
import me.raska.opendvs.base.resolver.amqp.ResolverRabbitService;
import me.raska.opendvs.core.service.PollerWorkerService;

@Service
public class PollerCoreQueueListener {
    @Autowired
    private PollerWorkerService pollerService;

    @Autowired
    @Qualifier(ResolverRabbitService.RESOLVER_QUALIFIER)
    private RabbitTemplate resolverTemplate;

    @RabbitListener(queues = "#{@pollerCoreQueue}")
    public void handleInputQueue(PollerAction action) {
        try {
            List<ResolverAction> resolverActions = pollerService.process(action);
            for (ResolverAction act : resolverActions) {
                resolverTemplate.convertAndSend(act);
            }
        } catch (Exception e) {
            System.out.println("Caught exception");
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}
