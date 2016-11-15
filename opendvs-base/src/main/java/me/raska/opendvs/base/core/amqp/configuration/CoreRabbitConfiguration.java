package me.raska.opendvs.base.core.amqp.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "opendvs.core.rabbitmq.fanout")
@Component
public class CoreRabbitConfiguration {
    private String queueName;
    private String exchangeName;
}
