package me.raska.opendvs.core.amqp;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.core.amqp.CoreRabbitService;
import me.raska.opendvs.base.core.event.ArtifactUpdateEvent;
import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.probe.ProbeAction;
import me.raska.opendvs.base.resolver.amqp.ResolverRabbitService;
import me.raska.opendvs.core.service.ProbeWorkerService;

@Service
public class ProbeCoreQueueListener {

    @Autowired
    private ProbeWorkerService probeService;

    @Autowired
    @Qualifier(ResolverRabbitService.RESOLVER_QUALIFIER)
    private RabbitTemplate resolverTemplate;

    @Autowired
    @Qualifier(CoreRabbitService.FANOUT_QUALIFIER)
    private RabbitTemplate fanoutTemplate;

    @RabbitListener(queues = "#{@probeCoreQueue}")
    public void handleInputQueue(ProbeAction action) {
        try {
            resolverTemplate.convertAndSend(probeService.process(action));

            Artifact art = action.getArtifact().clone();
            art.setState(Artifact.State.RESOLVING); // avoid concurrency issues
            fanoutTemplate.convertAndSend(new ArtifactUpdateEvent(art, null));
        } catch (Exception e) {
            System.out.println("Caught exception");
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}
