package org.myhelperbot.telegramhelperbot.config;

import org.myhelperbot.telegramhelperbot.HelperX;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    private final HelperX helperX;

    public BotConfig(HelperX helperX) {
        this.helperX = helperX;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(helperX); // Регистрируем вашего бота
        return botsApi;
    }
}