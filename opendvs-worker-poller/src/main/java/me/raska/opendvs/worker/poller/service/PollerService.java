package me.raska.opendvs.worker.poller.service;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.poller.NativePoller;
import me.raska.opendvs.base.poller.amqp.PollerRabbitService;
import me.raska.opendvs.worker.poller.exception.UnsatisfiedDependencyException;

@Service
public class PollerService {
    private static final Logger logger = LoggerFactory.getLogger(PollerService.class);

    @Autowired
    @Qualifier(PollerRabbitService.CORE_QUALIFIER)
    private RabbitTemplate rabbitTemplate;

    private Set<NativePoller> nativePollers;

    public void handleAction(PollerAction action) {
        for (NativePoller poller : nativePollers) {
            poller.process(action, rabbitTemplate::convertAndSend);
        }
    }

    @PostConstruct
    private void init() {
        Reflections ref = new Reflections(ClasspathHelper.forClassLoader(ClasspathHelper.contextClassLoader(),
                ClasspathHelper.staticClassLoader(), ClassLoader.getSystemClassLoader()));
        initNativePollers(ref);
    }

    private void initNativePollers(Reflections ref) {
        nativePollers = new LinkedHashSet<>();

        for (Class<? extends NativePoller> poller : ref.getSubTypesOf(NativePoller.class)) {
            try {
                NativePoller p = poller.newInstance();
                nativePollers.add(p);

                if (logger.isDebugEnabled()) {
                    logger.debug("Instantiated native probe " + poller);
                }

            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Cannot instantiate native probe " + poller, e);
            }
        }

        if (nativePollers.isEmpty()) {
            throw new UnsatisfiedDependencyException("Cannot detect any native pollers!");
        }

        logger.info("Instantiated {} native probes ", nativePollers.size());
    }

}
