package org.myhelperbot.weatherapi.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String WEATHER_REQUEST_QUEUE = "weather-request-queue";
    public static final String WEATHER_RESPONSE_QUEUE = "weather-response-queue";

    @Bean
    public Queue weatherRequestQueue() {
        return new Queue(WEATHER_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue weatherResponseQueue() {
        return new Queue(WEATHER_RESPONSE_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter()); // Установка Jackson-конвертера
        return rabbitTemplate;
    }
}
