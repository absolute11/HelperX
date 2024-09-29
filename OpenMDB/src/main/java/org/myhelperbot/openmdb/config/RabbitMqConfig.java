package org.myhelperbot.openmdb.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String MOVIE_REQUEST_QUEUE = "movie-request-queue";

    public static final String MOVIE_RESPONSE_QUEUE = "movie-response-queue";

    @Bean
    public Queue movieRequestQueue(){
        return new Queue(MOVIE_REQUEST_QUEUE);
    }

    @Bean
    public Queue movieResponseQueue(){
        return new Queue(MOVIE_RESPONSE_QUEUE);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
