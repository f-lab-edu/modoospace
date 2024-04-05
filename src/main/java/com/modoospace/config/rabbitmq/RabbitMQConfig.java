package com.modoospace.config.rabbitmq;

import com.modoospace.config.rabbitmq.errorhandler.CustomErrorHandler;
import com.modoospace.config.rabbitmq.errorhandler.CustomFatalExceptionStrategy;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

@Configuration
public class RabbitMQConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new CustomErrorHandler(fatalExceptionStrategy());
    }

    @Bean
    FatalExceptionStrategy fatalExceptionStrategy() {
        return new CustomFatalExceptionStrategy();
    }
}
