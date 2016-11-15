package me.raska.opendvs.base.core.amqp;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.core.amqp.configuration.CoreRabbitConfiguration;

@Service
public class CoreRabbitService {
    public static final String FANOUT_QUALIFIER = "core_fanout";

    @Autowired
    private ConnectionFactory cachingConnectionFactory;

    @Autowired
    private CoreRabbitConfiguration configuration;

    @Autowired
    private Jackson2JsonMessageConverter converter;

    @Bean
    @Qualifier(FANOUT_QUALIFIER)
    private Queue coreFanoutQueue() {
        return QueueBuilder.durable(configuration.getQueueName()).build();
    }

    @Bean
    @Qualifier(FANOUT_QUALIFIER)
    public RabbitTemplate coreFanoutRabbitTemplate(@Qualifier(FANOUT_QUALIFIER) Queue queue) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setQueue(queue.getName());
        rabbitTemplate.setRoutingKey(queue.getName());
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Bean
    @Qualifier(FANOUT_QUALIFIER)
    private Exchange coreFanoutExchange() {
        return ExchangeBuilder.fanoutExchange(configuration.getExchangeName()).durable().build();
    }

    @Bean
    @Qualifier(FANOUT_QUALIFIER)
    private Binding coreFanoutQueueBinding(@Qualifier(FANOUT_QUALIFIER) Queue queue,
            @Qualifier(FANOUT_QUALIFIER) Exchange exchange) {
        assert exchange instanceof FanoutExchange : "Exchange is supposed to be fanout";

        return BindingBuilder.bind(queue).to((FanoutExchange) exchange);
    }
}
