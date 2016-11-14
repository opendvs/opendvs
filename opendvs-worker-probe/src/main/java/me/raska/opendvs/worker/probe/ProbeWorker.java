package me.raska.opendvs.worker.probe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "me.raska.opendvs.worker.probe", "me.raska.opendvs.base.probe.amqp",
        "me.raska.opendvs.base.bean" })
@SpringBootApplication
public class ProbeWorker {

    public static void main(String[] args) {
        SpringApplication.run(ProbeWorker.class, args);
    }

}
