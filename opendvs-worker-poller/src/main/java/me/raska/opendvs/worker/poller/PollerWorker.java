package me.raska.opendvs.worker.poller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "me.raska.opendvs.worker.poller", "me.raska.opendvs.base.poller.amqp",
        "me.raska.opendvs.base.bean" })
@SpringBootApplication
public class PollerWorker {

    public static void main(String[] args) {
        SpringApplication.run(PollerWorker.class, args);
    }

}
