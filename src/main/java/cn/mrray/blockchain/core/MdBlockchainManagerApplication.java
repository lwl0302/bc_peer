package cn.mrray.blockchain.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication(exclude = MongoAutoConfiguration.class)
public class MdBlockchainManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MdBlockchainManagerApplication.class, args);
    }
}
