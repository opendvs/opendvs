package me.raska.opendvs.worker.poller.amqp;

import java.util.Date;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.model.poller.PollerAction.State;
import me.raska.opendvs.base.poller.amqp.PollerRabbitService;
import me.raska.opendvs.worker.poller.service.PollerService;

@Slf4j
@Service
public class WorkerQueueListener {

    @Autowired
    private PollerService pollerService;

    @Autowired
    @Qualifier(PollerRabbitService.CORE_QUALIFIER)
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "#{@pollerWorkerQueue}")
    @Transactional
    public void handleInputQueue(PollerAction action) {
        log.info("Started processing PollerAction " + action.getId());

        try {
            action.setStarted(new Date());
            action.setState(State.IN_PROGRESS);
            rabbitTemplate.convertAndSend(action);

            pollerService.handleAction(action);
            log.info("Ended processing PollerAction " + action.getId());
            action.setState(State.SUCCESS);
        } catch (Exception e) {
            log.error("Failed processing PollerAction " + action.getId(), e);
            action.setState(State.FAILURE);
            throw new AmqpRejectAndDontRequeueException(e);
        }
        action.setEnded(new Date());
        rabbitTemplate.convertAndSend(action);
    }

}
