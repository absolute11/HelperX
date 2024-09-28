package org.myhelperbot.telegramhelperbot.consumer;

import lombok.RequiredArgsConstructor;
import org.myhelperbot.telegramhelperbot.HelperX;
import org.myhelperbot.telegramhelperbot.dto.ApiResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


import static org.myhelperbot.telegramhelperbot.config.RabbitMqConfig.GPT_RESPONSE_QUEUE;

@Component
@RequiredArgsConstructor
public class TelegramBotConsumer {
    private final HelperX telegramBot;

    @RabbitListener(queues = GPT_RESPONSE_QUEUE)
    public void receiveResponse(ApiResponse<String> response) {
        Long chatId = response.getChatId();
        String gptResponse = response.getResponseData();

        // Отправляем ответ пользователю через Telegram бота
        telegramBot.sendMessage(chatId, gptResponse);
    }
}
