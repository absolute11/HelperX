package org.myhelperbot.telegramhelperbot.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.myhelperbot.telegramhelperbot.HelperX;
import org.myhelperbot.telegramhelperbot.dto.ApiResponse;
import org.myhelperbot.telegramhelperbot.dto.Recipe;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.myhelperbot.telegramhelperbot.config.RabbitMqConfig.MOVIE_RESPONSE_QUEUE;
import static org.myhelperbot.telegramhelperbot.config.RabbitMqConfig.SPOONACULAR_RESPONSE_QUEUE;

@Component
@RequiredArgsConstructor
public class FoodConsumer {
    private final HelperX telegramBot;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = SPOONACULAR_RESPONSE_QUEUE)
    public void receiveFoodResponse(ApiResponse<?> response) {
        Long chatId = response.getChatId();

        if (response.getResponseData() instanceof List) {
            // Получаем список рецептов
            List<Recipe> recipes = objectMapper.convertValue(response.getResponseData(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Recipe.class));
            telegramBot.handleRecipeSearchResponse(chatId, recipes); // Передаем список в бот для отправки пользователю
        } else if (response.getResponseData() instanceof String) {
            // Если это ошибка или сообщение, просто отправляем текст
            String errorMessage = (String) response.getResponseData();
            telegramBot.sendMessage(chatId, errorMessage);
        }
    }
}