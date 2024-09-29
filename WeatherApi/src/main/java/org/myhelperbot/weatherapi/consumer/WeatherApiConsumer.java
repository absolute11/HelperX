package org.myhelperbot.weatherapi.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.myhelperbot.weatherapi.dto.ApiRequest;
import org.myhelperbot.weatherapi.dto.ApiResponse;
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

import static org.myhelperbot.weatherapi.config.RabbitMqConfig.WEATHER_REQUEST_QUEUE;
import static org.myhelperbot.weatherapi.config.RabbitMqConfig.WEATHER_RESPONSE_QUEUE;

@Component
@RequiredArgsConstructor
public class WeatherApiConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${weather.api.key}")
    private String apiKey;

    @RabbitListener(queues = WEATHER_REQUEST_QUEUE)
    public void receiveMessage(ApiRequest<String> request) {
        try {
            String query = request.getRequestData();
            Long chatId = request.getChatId();

            // Кодируем строку для корректного запроса
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            // Первый запрос: получение координат
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest geoRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openweathermap.org/geo/1.0/direct?q=" + encodedQuery + "&limit=1&appid=" + apiKey))
                    .GET()
                    .build();

            HttpResponse<String> geoResponse = client.send(geoRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode geoJsonResponse = objectMapper.readTree(geoResponse.body());

            if (geoJsonResponse.isArray() && geoJsonResponse.size() > 0) {
                JsonNode firstResult = geoJsonResponse.get(0);
                double lat = firstResult.path("lat").asDouble();
                double lon = firstResult.path("lon").asDouble();

                // Второй запрос: получение текущей погоды по координатам
                HttpRequest weatherRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + apiKey + "&lang=ru"))
                        .GET()
                        .build();

                HttpResponse<String> weatherResponse = client.send(weatherRequest, HttpResponse.BodyHandlers.ofString());
                JsonNode weatherJsonResponse = objectMapper.readTree(weatherResponse.body());

                // Извлекаем необходимые данные о погоде
                StringBuilder weatherReport = new StringBuilder();
                String temp = weatherJsonResponse.path("main").path("temp").asText();
                String description = weatherJsonResponse.path("weather").get(0).path("description").asText();
                String windSpeed = weatherJsonResponse.path("wind").path("speed").asText();
                String humidity = weatherJsonResponse.path("main").path("humidity").asText();

                weatherReport.append("Погода в городе: ").append(query).append("\n")
                        .append("Температура: ").append(temp).append(" °C\n")
                        .append("Описание: ").append(description).append("\n")
                        .append("Скорость ветра: ").append(windSpeed).append(" м/с\n")
                        .append("Влажность: ").append(humidity).append("%\n");

                // Отправляем результат в RabbitMQ
                ApiResponse<String> apiResponse = new ApiResponse<>(chatId, weatherReport.toString());
                rabbitTemplate.convertAndSend(WEATHER_RESPONSE_QUEUE, apiResponse);

            } else {
                // Город не найден
                ApiResponse<String> apiResponse = new ApiResponse<>(chatId, "Город не найден.");
                rabbitTemplate.convertAndSend(WEATHER_RESPONSE_QUEUE, apiResponse);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}