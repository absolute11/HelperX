package org.myhelperbot.telegramhelperbot.consumer;

import lombok.RequiredArgsConstructor;
import org.myhelperbot.telegramhelperbot.HelperX;
import org.myhelperbot.telegramhelperbot.dto.ApiResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static org.myhelperbot.telegramhelperbot.config.RabbitMqConfig.GPT_RESPONSE_QUEUE;
import static org.myhelperbot.telegramhelperbot.config.RabbitMqConfig.WEATHER_RESPONSE_QUEUE;

@Component
@RequiredArgsConstructor
public class WeatherApiConsumer {
    private final HelperX telegramBot;

    @RabbitListener(queues = WEATHER_RESPONSE_QUEUE)
    public void receiveResponse(ApiResponse<String> response) {
        Long chatId = response.getChatId();
        String weatherResponse = response.getResponseData();

        // Отправляем ответ пользователю через Telegram бота
        telegramBot.sendMessage(chatId, weatherResponse);
    }
}
