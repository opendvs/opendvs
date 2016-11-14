package me.raska.opendvs.base.resolver.amqp.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "opendvs.resolver.rabbitmq")
@Component
public class ResolverRabbitConfiguration {
    private DirectQueueBlock queue;
    private DirectQueueBlock deadLetterQueue;

    @Data
    public static class DirectQueueBlock {
        private String queueName;
        private String exchangeName;
        private String routingKey;
    }

}
