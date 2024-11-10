package org.image.core.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный файл для RabbitMQ
 */
@Configuration
public class RabbitConfig {

    @Value("${rabbit.queue.name}")
    private String queueName;

    @Bean
    public Queue imageEventQueue() {
        return new Queue(queueName, false);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
