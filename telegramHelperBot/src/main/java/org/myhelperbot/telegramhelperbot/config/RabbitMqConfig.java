package org.myhelperbot.telegramhelperbot.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String GPT_REQUEST_QUEUE = "gpt-request-queue";
    public static final String GPT_RESPONSE_QUEUE = "gpt-response-queue";

    public static final String NEWS_REQUEST_QUEUE = "news-request-queue";
    public static final String NEWS_RESPONSE_QUEUE = "news-response-queue";

    public static final String WEATHER_REQUEST_QUEUE = "weather-request-queue";
    public static final String WEATHER_RESPONSE_QUEUE = "weather-response-queue";
    public static final String MOVIE_REQUEST_QUEUE = "movie-request-queue";

    public static final String MOVIE_RESPONSE_QUEUE = "movie-response-queue";
    public static final String SPOONACULAR_SEARCH_QUEUE = "spoonacular-search-queue";
    public static final String SPOONACULAR_RESPONSE_QUEUE = "spoonacular-response-queue";
    public static final String SPOONACULAR_RECIPE_DETAILS_QUEUE = "spoonacular-recipe-details-queue";
    @Bean
    public Queue spoonacularRecipeDetailsQueue() {
        return new Queue(SPOONACULAR_RECIPE_DETAILS_QUEUE, true);
    }

    @Bean
    public Queue movieRequestQueue(){
        return new Queue(MOVIE_REQUEST_QUEUE);
    }
    @Bean
    public Queue spoonacularResponseQueue() {
        return new Queue(SPOONACULAR_RESPONSE_QUEUE, true);
    }
    @Bean
    public Queue spoonacularSearchQueue() {
        return new Queue(SPOONACULAR_SEARCH_QUEUE, true);
    }


    @Bean
    public Queue movieResponseQueue(){
        return new Queue(MOVIE_RESPONSE_QUEUE);
    }

    @Bean
    public Queue weatherRequestQueue() {
        return new Queue(WEATHER_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue weatherResponseQueue() {
        return new Queue(WEATHER_RESPONSE_QUEUE, true);
    }
    @Bean
    public Queue requestNewsQueue() {
        return new Queue(NEWS_REQUEST_QUEUE);
    }

    @Bean
    public Queue responseNewsQueue() {
        return new Queue(NEWS_RESPONSE_QUEUE);
    }

    @Bean
    public Queue requestOpenAIQueue() {
        return new Queue(GPT_REQUEST_QUEUE);
    }

    @Bean
    public Queue responseOpenAIQueue() {
        return new Queue(GPT_RESPONSE_QUEUE);
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
