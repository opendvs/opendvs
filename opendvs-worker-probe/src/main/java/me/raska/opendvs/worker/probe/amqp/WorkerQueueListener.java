package me.raska.opendvs.worker.probe.amqp;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.raska.opendvs.base.model.probe.ProbeAction;
import me.raska.opendvs.base.probe.amqp.ProbeRabbitService;
import me.raska.opendvs.worker.probe.service.ProbeService;

@Service
public class WorkerQueueListener {

    @Autowired
    private ProbeService probeService;

    @Autowired
    @Qualifier(ProbeRabbitService.CORE_QUALIFIER)
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "#{@probeWorkerQueue}")
    @Transactional
    public void handleInputQueue(ProbeAction action) {
        try {
            probeService.scanArtifact(action);
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
        rabbitTemplate.convertAndSend(action);
    }

}
