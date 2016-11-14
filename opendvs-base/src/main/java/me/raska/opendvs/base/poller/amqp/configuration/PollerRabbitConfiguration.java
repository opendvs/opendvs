package me.raska.opendvs.base.poller.amqp.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "opendvs.poller.rabbitmq")
@Component
public class PollerRabbitConfiguration {
    private DirectQueueBlock coreQueue;
    private DirectQueueBlock coreDeadLetterQueue;
    private DirectQueueBlock workerQueue;
    private DirectQueueBlock workerDeadLetterQueue;

    @Data
    public static class DirectQueueBlock {
        private String queueName;
        private String exchangeName;
        private String routingKey;
    }

}
