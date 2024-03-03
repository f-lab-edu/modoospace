package com.modoospace;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.testcontainers.containers.RabbitMQContainer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQTestInitializer {

    private static final String QUEUE_NAME = "RESERVATION";

    static void initializeRabbitMQ(RabbitMQContainer container) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(container.getHost());
        factory.setPort(container.getAmqpPort());
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println("Queue '" + QUEUE_NAME + "' created successfully.");
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Failed to create RabbitMQ queue", e);
        }
    }
}
