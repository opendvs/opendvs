package me.raska.opendvs.resolver.amqp;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.poller.amqp.PollerRabbitService;
import me.raska.opendvs.base.resolver.ResolverAction;
import me.raska.opendvs.resolver.service.ResolverService;

@Service
public class ResolverQueueListener {

    @Autowired
    private ResolverService resolverService;

    @Autowired
    @Qualifier(PollerRabbitService.WORKER_QUALIFIER)
    private RabbitTemplate pollerExchange;

    @RabbitListener(queues = "#{@resolverQueue}")
    public void handleInputQueue(ResolverAction action) {
        try {
            Map<String, List<ArtifactComponent>> components = resolverService.handleMessage(action);
            if (!components.isEmpty()) {
                resolverService.publishScanning(components).forEach(pollerExchange::convertAndSend);
            }
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}
