package me.raska.opendvs.resolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "me.raska.opendvs.resolver", "me.raska.opendvs.base.resolver.amqp",
        "me.raska.opendvs.base.poller.amqp", "me.raska.opendvs.base.core.amqp", "me.raska.opendvs.base.bean" })
@EntityScan(basePackages = { "me.raska.opendvs.resolver", "me.raska.opendvs.base.model" })
@SpringBootApplication
public class Resolver {

    public static void main(String[] args) {
        SpringApplication.run(Resolver.class, args);
    }

}
