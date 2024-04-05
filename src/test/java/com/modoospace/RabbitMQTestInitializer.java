package com.modoospace;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import org.testcontainers.containers.RabbitMQContainer;

public class RabbitMQTestInitializer {

    static void initializeRabbitMQ(RabbitMQContainer container) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(container.getHost());
        factory.setPort(container.getAmqpPort());
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            createExchangeAndQueue(channel);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Failed to create RabbitMQ queue", e);
        }
    }

    static void createExchangeAndQueue(Channel channel) throws IOException {
        channel.exchangeDeclare("x.reservation", BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare("x.reservation.dlx", BuiltinExchangeType.FANOUT);

        HashMap<String, Object> argumentsMap = new HashMap<>();
        argumentsMap.put("x-dead-letter-exchange", "x.reservation.dlx");
        channel.queueDeclare("q.reservation", false, false, false, argumentsMap);
        channel.queueBind("q.reservation", "x.reservation", "");

        channel.queueDeclare("q.reservation.dlx", false, false, false, null);
        channel.queueBind("q.reservation.dlx", "x.reservation.dlx", "");
    }
}
