package org.myhelperbot.newsapi.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.myhelperbot.newsapi.dto.ApiRequest;
import org.myhelperbot.newsapi.dto.ApiResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.myhelperbot.newsapi.config.RabbitMQConfig.NEWS_RESPONSE_QUEUE;

@Component
@RequiredArgsConstructor
public class NewsApiRequestConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${newsapi.key}")
    private String newsApiKey;

    @RabbitListener(queues = "news-request-queue")
    public void receiveMessage(ApiRequest<String> request) {
        try {
            String query = request.getRequestData();
            Long chatId = request.getChatId();

            // Отправляем запрос к NewsAPI
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://newsapi.org/v2/everything?q=" + query + "&apiKey=" + newsApiKey))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // Парсим JSON и извлекаем статьи
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            JsonNode articles = jsonResponse.path("articles");

            // Составляем одну строку для отправки
            StringBuilder formattedArticles = new StringBuilder();
            for (int i = 0; i < Math.min(articles.size(), 5); i++) { // Ограничиваем до 5 новостей
                JsonNode article = articles.get(i);
                String title = article.path("title").asText();
                String description = article.path("description").asText();
                String url = article.path("url").asText();
                formattedArticles.append("**").append(title).append("**\n")
                        .append(description).append("\n")
                        .append("[Читать далее](").append(url).append(")\n\n");
            }

            // Отправляем одну строку в responseData
            ApiResponse<String> apiResponse = new ApiResponse<>(chatId, formattedArticles.toString());
            rabbitTemplate.convertAndSend(NEWS_RESPONSE_QUEUE, apiResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
