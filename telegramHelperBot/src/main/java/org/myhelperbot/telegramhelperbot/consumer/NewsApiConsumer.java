package org.myhelperbot.telegramhelperbot.consumer;

import lombok.RequiredArgsConstructor;
import org.myhelperbot.telegramhelperbot.HelperX;
import org.myhelperbot.telegramhelperbot.dto.ApiResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static org.myhelperbot.telegramhelperbot.config.RabbitMqConfig.NEWS_RESPONSE_QUEUE;

@Component
@RequiredArgsConstructor
public class NewsApiConsumer {
    private final HelperX telegramBot;

    @RabbitListener(queues = NEWS_RESPONSE_QUEUE)
    public void receiveNewsResponse(ApiResponse<String> response) {
        Long chatId = response.getChatId();
        String newsResponse = response.getResponseData();

        // Отправляем ответ пользователю через Telegram бота
        telegramBot.sendMessage(chatId, newsResponse);
    }
}
