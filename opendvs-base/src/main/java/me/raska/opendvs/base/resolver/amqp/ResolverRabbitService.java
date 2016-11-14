package me.raska.opendvs.base.resolver.amqp;

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

import me.raska.opendvs.base.resolver.amqp.configuration.ResolverRabbitConfiguration;

@Service
public class ResolverRabbitService {
    public static final String RESOLVER_QUALIFIER = "resolver_worker";
    public static final String RESOLVER_DL_QUALIFIER = "resolver_worker_dl";

    @Autowired
    private ConnectionFactory cachingConnectionFactory;

    @Autowired
    private ResolverRabbitConfiguration configuration;

    @Autowired
    private Jackson2JsonMessageConverter converter;

    @Bean
    @Qualifier(RESOLVER_QUALIFIER)
    private Queue resolverQueue() {
        return QueueBuilder.durable(configuration.getQueue().getQueueName())
                .withArgument("x-dead-letter-exchange", configuration.getDeadLetterQueue().getExchangeName())
                .withArgument("x-dead-letter-routing-key", configuration.getDeadLetterQueue().getRoutingKey()).build();
    }

    @Bean
    @Qualifier(RESOLVER_DL_QUALIFIER)
    private Queue resolverDeadLetterQueue() {
        return QueueBuilder.durable(configuration.getDeadLetterQueue().getQueueName()).build();
    }

    @Bean
    @Qualifier(RESOLVER_QUALIFIER)
    public RabbitTemplate resolverRabbitTemplate(@Qualifier(RESOLVER_QUALIFIER) Queue queue) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setQueue(queue.getName());
        rabbitTemplate.setRoutingKey(queue.getName());
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Bean
    @Qualifier(RESOLVER_QUALIFIER)
    private Exchange resolverWorkerExchange() {
        return ExchangeBuilder.directExchange(configuration.getQueue().getExchangeName()).durable().build();
    }

    @Bean
    @Qualifier(RESOLVER_DL_QUALIFIER)
    private Exchange resolverDeadLetterExchange() {
        return ExchangeBuilder.directExchange(configuration.getDeadLetterQueue().getExchangeName()).durable().build();
    }

    @Bean
    @Qualifier(RESOLVER_QUALIFIER)
    private Binding resolverQueueBinding(@Qualifier(RESOLVER_QUALIFIER) Queue queue,
            @Qualifier(RESOLVER_QUALIFIER) Exchange exchange) {
        assert exchange instanceof DirectExchange : "Exchange is supposed to be direct";

        return BindingBuilder.bind(queue).to((DirectExchange) exchange).with(configuration.getQueue().getRoutingKey());
    }

    @Bean
    @Qualifier(RESOLVER_DL_QUALIFIER)
    private Binding probeWorkerDeadLetterQueueBinding(@Qualifier(RESOLVER_DL_QUALIFIER) Queue queue,
            @Qualifier(RESOLVER_DL_QUALIFIER) Exchange exchange) {
        assert exchange instanceof DirectExchange : "Exchange is supposed to be direct";

        return BindingBuilder.bind(queue).to((DirectExchange) exchange)
                .with(configuration.getDeadLetterQueue().getRoutingKey());
    }

}
