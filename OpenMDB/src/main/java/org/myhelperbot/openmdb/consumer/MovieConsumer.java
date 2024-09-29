package org.myhelperbot.openmdb.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.myhelperbot.openmdb.dto.ApiRequest;
import org.myhelperbot.openmdb.dto.ApiResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.myhelperbot.openmdb.config.RabbitMqConfig.MOVIE_REQUEST_QUEUE;
import static org.myhelperbot.openmdb.config.RabbitMqConfig.MOVIE_RESPONSE_QUEUE;

@Component
@RequiredArgsConstructor
public class MovieConsumer {
    @Value("${open.moviedb}")
    private String apiKey;

    private final RabbitTemplate rabbitTemplate;
    private static final String BASE_URL = "http://www.omdbapi.com/";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = MOVIE_REQUEST_QUEUE)
    public void receiveMessage(ApiRequest<String> request){
        try {
            String query = request.getRequestData();
            Long chatId = request.getChatId();
            String encodingQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = BASE_URL + "?apikey=" + apiKey + "&t=" + encodingQuery;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // Отправляем запрос и получаем ответ
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // Парсим JSON ответ
            JsonNode jsonResponse = objectMapper.readTree(response.body());

            // Извлекаем данные из ответа
            StringBuilder movieInfo = new StringBuilder();
            if (jsonResponse.has("Title")) {
                String title = jsonResponse.path("Title").asText();
                String year = jsonResponse.path("Year").asText();
                String plot = jsonResponse.path("Plot").asText();
                String poster = jsonResponse.path("Poster").asText();

                movieInfo.append("Название: ").append(title).append("\n")
                        .append("Год: ").append(year).append("\n")
                        .append("Описание: ").append(plot).append("\n")
                        .append("Постер: ").append(poster).append("\n");
            } else {
                movieInfo.append("Фильм не найден по запросу: ").append(query);
            }

            // Отправляем результат обратно в RabbitMQ
            ApiResponse<String> apiResponse = new ApiResponse<>(chatId, movieInfo.toString());
            rabbitTemplate.convertAndSend(MOVIE_RESPONSE_QUEUE, apiResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}