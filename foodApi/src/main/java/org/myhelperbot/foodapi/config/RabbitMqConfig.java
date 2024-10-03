package org.myhelperbot.foodapi.config;



import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String SPOONACULAR_SEARCH_QUEUE = "spoonacular-search-queue";
    public static final String SPOONACULAR_NUTRIENTS_QUEUE = "spoonacular-nutrients-queue";
    public static final String SPOONACULAR_RANDOM_QUEUE = "spoonacular-random-queue";
    public static final String SPOONACULAR_SIMILAR_QUEUE = "spoonacular-similar-queue";
    public static final String SPOONACULAR_MEALPLAN_QUEUE = "spoonacular-mealplan-queue";
    public static final String SPOONACULAR_RESPONSE_QUEUE = "spoonacular-response-queue";
    public static final String SPOONACULAR_INTOLERANCES_QUEUE = "spoonacular-intolerances-queue";

    public static final String SPOONACULAR_RECIPE_DETAILS_QUEUE = "spoonacular-recipe-details-queue";
    @Bean
    public Queue spoonacularSearchQueue() {
        return new Queue(SPOONACULAR_SEARCH_QUEUE, true);
    }
    @Bean
    public Queue spoonacularRecipeDetailsQueue() {
        return new Queue(SPOONACULAR_RECIPE_DETAILS_QUEUE, true);
    }
    @Bean
    public Queue spoonacularResponseQueue() {
        return new Queue(SPOONACULAR_RESPONSE_QUEUE, true);
    }

    @Bean
    public Queue spoonacularNutrientsQueue() {
        return new Queue(SPOONACULAR_NUTRIENTS_QUEUE, true);
    }

    @Bean
    public Queue spoonacularRandomQueue() {
        return new Queue(SPOONACULAR_RANDOM_QUEUE, true);
    }

    @Bean
    public Queue spoonacularSimilarQueue() {
        return new Queue(SPOONACULAR_SIMILAR_QUEUE, true);
    }

    @Bean
    public Queue spoonacularMealplanQueue() {
        return new Queue(SPOONACULAR_MEALPLAN_QUEUE, true);
    }

    @Bean
    public Queue spoonacularIntolerancesQueue() {
        return new Queue(SPOONACULAR_INTOLERANCES_QUEUE, true);
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
