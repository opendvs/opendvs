package me.raska.opendvs.base.probe.amqp;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.probe.amqp.configuration.ProbeRabbitConfiguration;

@Service
public class ProbeRabbitService {
    public static final String WORKER_QUALIFIER = "probe_worker";
    public static final String WORKER_DL_QUALIFIER = "probe_worker_dl";
    public static final String CORE_QUALIFIER = "probe_core";
    public static final String CORE_DL_QUALIFIER = "probe_core_dl";

    @Autowired
    private ConnectionFactory cachingConnectionFactory;

    @Autowired
    private ProbeRabbitConfiguration configuration;

    @Autowired
    private Jackson2JsonMessageConverter converter;

    @Bean
    @Qualifier(WORKER_QUALIFIER)
    private Queue probeWorkerQueue() {
        return QueueBuilder.durable(configuration.getWorkerQueue().getQueueName())
                .withArgument("x-dead-letter-exchange", configuration.getWorkerDeadLetterQueue().getExchangeName())
                .withArgument("x-dead-letter-routing-key", configuration.getWorkerDeadLetterQueue().getRoutingKey())
                .build();
    }

    @Bean
    @Qualifier(CORE_QUALIFIER)
    private Queue probeCoreQueue() {
        return QueueBuilder.durable(configuration.getCoreQueue().getQueueName())
                .withArgument("x-dead-letter-exchange", configuration.getCoreDeadLetterQueue().getExchangeName())
                .withArgument("x-dead-letter-routing-key", configuration.getCoreDeadLetterQueue().getRoutingKey())
                .build();
    }

    @Bean
    @Qualifier(WORKER_DL_QUALIFIER)
    private Queue probeWorkerDeadLetterQueue() {
        return QueueBuilder.durable(configuration.getWorkerDeadLetterQueue().getQueueName()).build();
    }

    @Bean
    @Qualifier(CORE_DL_QUALIFIER)
    private Queue probeCoreDeadLetterQueue() {
        return QueueBuilder.durable(configuration.getCoreDeadLetterQueue().getQueueName()).build();
    }

    @Bean
    @Qualifier(WORKER_QUALIFIER)
    public RabbitTemplate probeWorkerRabbitTemplate(@Qualifier(WORKER_QUALIFIER) Queue queue) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setQueue(queue.getName());
        rabbitTemplate.setRoutingKey(queue.getName());
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Bean
    @Qualifier(CORE_QUALIFIER)
    public RabbitTemplate probeCoreRabbitTemplate(@Qualifier(CORE_QUALIFIER) Queue queue) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setQueue(queue.getName());
        rabbitTemplate.setRoutingKey(queue.getName());
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Bean
    @Qualifier(WORKER_QUALIFIER)
    private Exchange probeWorkerExchange() {
        return ExchangeBuilder.directExchange(configuration.getWorkerQueue().getExchangeName()).durable().build();
    }

    @Bean
    @Qualifier(CORE_QUALIFIER)
    private Exchange probeCoreExchange() {
        return ExchangeBuilder.directExchange(configuration.getCoreQueue().getExchangeName()).durable().build();
    }

    @Bean
    @Qualifier(WORKER_DL_QUALIFIER)
    private Exchange probeWorkerDeadLetterExchange() {
        return ExchangeBuilder.directExchange(configuration.getWorkerDeadLetterQueue().getExchangeName()).durable()
                .build();
    }

    @Bean
    @Qualifier(CORE_DL_QUALIFIER)
    private Exchange probeCoreDeadLetterExchange() {
        return ExchangeBuilder.directExchange(configuration.getCoreDeadLetterQueue().getExchangeName()).durable()
                .build();
    }

    @Bean
    @Qualifier(WORKER_QUALIFIER)
    private Binding probeWorkerQueueBinding(@Qualifier(WORKER_QUALIFIER) Queue queue,
            @Qualifier(WORKER_QUALIFIER) Exchange exchange) {
        assert exchange instanceof DirectExchange : "Exchange is supposed to be direct";

        return BindingBuilder.bind(queue).to((DirectExchange) exchange)
                .with(configuration.getWorkerQueue().getRoutingKey());
    }

    @Bean
    @Qualifier(CORE_QUALIFIER)
    private Binding probeCoreQueueBinding(@Qualifier(CORE_QUALIFIER) Queue queue,
            @Qualifier(CORE_QUALIFIER) Exchange exchange) {
        assert exchange instanceof DirectExchange : "Exchange is supposed to be direct";

        return BindingBuilder.bind(queue).to((DirectExchange) exchange)
                .with(configuration.getCoreQueue().getRoutingKey());
    }

    @Bean
    @Qualifier(WORKER_DL_QUALIFIER)
    private Binding probeWorkerDeadLetterQueueBinding(@Qualifier(WORKER_DL_QUALIFIER) Queue queue,
            @Qualifier(WORKER_DL_QUALIFIER) Exchange exchange) {
        assert exchange instanceof DirectExchange : "Exchange is supposed to be direct";

        return BindingBuilder.bind(queue).to((DirectExchange) exchange)
                .with(configuration.getWorkerDeadLetterQueue().getRoutingKey());
    }

    @Bean
    @Qualifier(CORE_DL_QUALIFIER)
    private Binding probeCoreDeadLetterQueueBinding(@Qualifier(CORE_DL_QUALIFIER) Queue queue,
            @Qualifier(CORE_DL_QUALIFIER) Exchange exchange) {
        assert exchange instanceof DirectExchange : "Exchange is supposed to be direct";

        return BindingBuilder.bind(queue).to((DirectExchange) exchange)
                .with(configuration.getCoreDeadLetterQueue().getRoutingKey());
    }
}
