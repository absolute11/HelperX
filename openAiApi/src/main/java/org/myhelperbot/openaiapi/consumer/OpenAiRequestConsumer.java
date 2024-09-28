package org.myhelperbot.openaiapi.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.myhelperbot.openaiapi.dto.ApiRequest;
import org.myhelperbot.openaiapi.dto.ApiResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class OpenAiRequestConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${proxy.api.key}")
    private String proxyApiKey;

    public OpenAiRequestConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "gpt-request-queue")
    public void receiveMessage(ApiRequest<String> request) {
        try {
            String userMessage = request.getRequestData();
            Long chatId = request.getChatId();

            // Отправка запроса к OpenAI
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.proxyapi.ru/openai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + proxyApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"model\": \"gpt-4-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \"" + userMessage + "\"}]}"
                    ))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // Парсим JSON и извлекаем ответ ChatGPT
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            String gptResponse = jsonResponse
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            // Отправляем ответ обратно в очередь
            ApiResponse<String> apiResponse = new ApiResponse<>(chatId, gptResponse);
            rabbitTemplate.convertAndSend("gpt-response-queue", apiResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
