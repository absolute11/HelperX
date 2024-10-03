package org.myhelperbot.foodapi.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.myhelperbot.foodapi.dto.ApiRequest;
import org.myhelperbot.foodapi.dto.ApiResponse;
import org.myhelperbot.foodapi.dto.Recipe;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.myhelperbot.foodapi.config.RabbitMqConfig.*;

@Component
@RequiredArgsConstructor
public class FoodConsumer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${food.api.key}")
    private String foodApiKey;

    @RabbitListener(queues = SPOONACULAR_SEARCH_QUEUE)
    public void handleRecipeSearch(ApiRequest<String> request) {
        Long chatId = request.getChatId();
        String recipeName = request.getRequestData();

        try {
            // Формирование URL запроса к Spoonacular API
            String encodedRecipeName = URLEncoder.encode(recipeName, StandardCharsets.UTF_8.toString());
            String url = String.format(
                    "https://api.spoonacular.com/recipes/complexSearch?query=%s&number=5&addRecipeInformation=true&apiKey=%s",
                    encodedRecipeName,
                    foodApiKey
            );

            // Создание HTTP-запроса
            java.net.http.HttpClient client = HttpClient.newHttpClient();
            java.net.http.HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            // Выполнение запроса и получение ответа
           HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode resultsNode = rootNode.path("results");

                List<Recipe> recipes = new ArrayList<>();

                if (resultsNode.isArray()) {
                    for (JsonNode node : resultsNode) {
                        Recipe recipe = new Recipe();
                        recipe.setId(node.path("id").asInt());
                        recipe.setTitle(node.path("title").asText());
                        recipe.setImageUrl(node.path("image").asText());
                        recipe.setSourceUrl(node.path("sourceUrl").asText());

                        recipes.add(recipe);
                    }
                }

                // Формирование ApiResponse и отправка обратно в бот
                ApiResponse<List<Recipe>> apiResponse = new ApiResponse<>(chatId, recipes);
                rabbitTemplate.convertAndSend(SPOONACULAR_RESPONSE_QUEUE, apiResponse);
            } else {
                // Обработка неуспешного ответа от API
                ApiResponse<String> errorResponse = new ApiResponse<>(chatId, "Произошла ошибка при поиске рецептов. Попробуйте позже.");
                rabbitTemplate.convertAndSend(SPOONACULAR_RESPONSE_QUEUE, errorResponse);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Отправка сообщения об ошибке в случае исключения
            ApiResponse<String> exceptionResponse = new ApiResponse<>(chatId, "Произошла ошибка при обработке вашего запроса.");
            rabbitTemplate.convertAndSend(SPOONACULAR_RESPONSE_QUEUE, exceptionResponse);
        }

    }
    @RabbitListener(queues = SPOONACULAR_RECIPE_DETAILS_QUEUE)
    public void handleRecipeDetails(ApiRequest<Integer> request) {
        Long chatId = request.getChatId();
        int recipeId = request.getRequestData();

        try {
            // Формирование URL для получения полного рецепта по ID
            String url = String.format("https://api.spoonacular.com/recipes/%d/information?includeNutrition=false&apiKey=%s", recipeId, foodApiKey);

            // Создаем HTTP-запрос
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // Выполняем запрос и получаем ответ
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                JsonNode rootNode = objectMapper.readTree(responseBody);

                String title = rootNode.path("title").asText();
                String imageUrl = rootNode.path("image").asText();
                String instructions = rootNode.path("instructions").asText();
                JsonNode ingredientsNode = rootNode.path("extendedIngredients");
                instructions = removeHtmlTags(instructions);

                // Формируем список ингредиентов
                StringBuilder ingredientsList = new StringBuilder();
                ingredientsList.append("Ингредиенты:\n");
                for (JsonNode ingredient : ingredientsNode) {
                    ingredientsList.append("- ").append(ingredient.path("original").asText()).append("\n");
                }

                // Формирование ответа для отправки в Telegram-бот
                String recipeDetails = String.format("*%s*\n\n%s\n\nШаги приготовления:\n%s", title, ingredientsList.toString(), instructions);

                // Отправляем ответ в бот через RabbitMQ
                ApiResponse<String> apiResponse = new ApiResponse<>(chatId, recipeDetails);
                rabbitTemplate.convertAndSend(SPOONACULAR_RESPONSE_QUEUE, apiResponse);
            } else {
                // Обработка неуспешного ответа
                ApiResponse<String> errorResponse = new ApiResponse<>(chatId, "Произошла ошибка при получении рецепта. Попробуйте позже.");
                rabbitTemplate.convertAndSend(SPOONACULAR_RESPONSE_QUEUE, errorResponse);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Отправка сообщения об ошибке в случае исключения
            ApiResponse<String> exceptionResponse = new ApiResponse<>(chatId, "Произошла ошибка при обработке вашего запроса.");
            rabbitTemplate.convertAndSend(SPOONACULAR_RESPONSE_QUEUE, exceptionResponse);
        }
    }
    private String removeHtmlTags(String text) {
        return text.replaceAll("<[^>]*>", ""); // Удаляет все HTML-теги
    }
}
