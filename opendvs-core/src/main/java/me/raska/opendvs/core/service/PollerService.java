package me.raska.opendvs.core.service;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.model.poller.PollerAction.State;
import me.raska.opendvs.base.model.poller.PollerActionStep;
import me.raska.opendvs.base.poller.amqp.PollerRabbitService;
import me.raska.opendvs.core.dto.PollerActionRepository;
import me.raska.opendvs.core.dto.PollerActionStepRepository;
import me.raska.opendvs.core.exception.InvalidRequestException;

@Service
public class PollerService {
    @Autowired
    private PollerActionRepository pollerActionRepository;

    @Autowired
    private PollerActionStepRepository pollerActionStepRepository;

    @Autowired
    @Qualifier(PollerRabbitService.WORKER_QUALIFIER)
    private RabbitTemplate rabbitTemplate;

    @Transactional
    public PollerAction triggerAction(PollerAction action) {
        action.setId(null);
        action.setSteps(null);
        action.setState(State.QUEUED);
        action.setInitiated(new Date());

        PollerAction act = pollerActionRepository.save(action);
        rabbitTemplate.convertAndSend(action);

        return act;
    }

    public Page<PollerAction> getActions(Pageable pageable) {
        Page<PollerAction> actions = pollerActionRepository.findAll(pageable);
        actions.forEach(p -> p.setSteps(null)); // filter steps
        return actions;
    }

    public PollerAction getAction(String id) {
        PollerAction act = pollerActionRepository.findOne(id);
        if (act == null) {
            throw new InvalidRequestException("Poller action doesn't exist");
        }
        return act;
    }

    public Page<PollerActionStep> getActionSteps(String id, Pageable pageable) {
        PollerAction act = pollerActionRepository.findOne(id);
        if (act == null) {
            throw new InvalidRequestException("Poller action doesn't exist");
        }
        return pollerActionStepRepository.findByPollerAction(act, pageable);
    }

}
