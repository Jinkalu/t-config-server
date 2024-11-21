package com.trrings.config_server.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@Slf4j
public class KafkaStarter {

    private Process zookeeperProcess;
    private Process kafkaProcess;

    @Value("${application.kafka-path}")
    private String KAFKA_PATH;

    @PostConstruct
    public void startKafkaAndZookeeper() {
        try {
            // Start Zookeeper
            ProcessBuilder zookeeperProcessBuilder = new ProcessBuilder(
                    KAFKA_PATH.concat("/bin/zookeeper-server-start.sh"),
                    KAFKA_PATH.concat("/config/zookeeper.properties")
            );
            StringBuilder stringBuilder=new StringBuilder();
            zookeeperProcessBuilder.redirectErrorStream(true);
            zookeeperProcess = zookeeperProcessBuilder.start();
            printProcessOutput(zookeeperProcess);

            log.info("Waiting for Zookeeper to start...");
            Thread.sleep(20000);

            // Start Kafka
            ProcessBuilder kafkaProcessBuilder = new ProcessBuilder(
                    KAFKA_PATH + "/bin/kafka-server-start.sh",
                    KAFKA_PATH + "/config/server.properties"
            );
            kafkaProcessBuilder.redirectErrorStream(true);
            kafkaProcess = kafkaProcessBuilder.start();
            printProcessOutput(kafkaProcess);

            // Wait for Kafka to be available
            System.out.println("Waiting for Kafka to start...");
            Thread.sleep(20000);

            log.info("Zookeeper and Kafka have started successfully. Proceeding with the main application.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stopKafkaAndZookeeper() {
        try {
            // Stop Kafka
            if (kafkaProcess != null) {
                log.info("Stopping Kafka...");
                kafkaProcess.destroy();
                kafkaProcess.waitFor();
                log.info("Kafka stopped.");
            }

            // Stop Zookeeper
            if (zookeeperProcess != null) {
                log.info("Stopping Zookeeper...");
                zookeeperProcess.destroy();
                zookeeperProcess.waitFor();
                log.info("Zookeeper stopped.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printProcessOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
