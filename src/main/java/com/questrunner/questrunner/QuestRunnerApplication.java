package com.questrunner.questrunner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class QuestRunnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestRunnerApplication.class, args);
    }

}
