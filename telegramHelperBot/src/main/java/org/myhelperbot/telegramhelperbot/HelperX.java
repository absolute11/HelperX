package org.myhelperbot.telegramhelperbot;

import lombok.RequiredArgsConstructor;
import org.myhelperbot.telegramhelperbot.dto.ApiRequest;
import org.myhelperbot.telegramhelperbot.dto.ApiResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.myhelperbot.telegramhelperbot.config.RabbitMqConfig.NEWS_REQUEST_QUEUE;
import static org.myhelperbot.telegramhelperbot.config.RabbitMqConfig.WEATHER_REQUEST_QUEUE;

@Component
@RequiredArgsConstructor
public class HelperX extends TelegramLongPollingBot {
    private final RabbitTemplate rabbitTemplate;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private boolean waitingForNewsRequest = false;
    private boolean waitingForQuestion = false; // Для ожидания вопроса ChatGPT
    private boolean waitingForWeatherRequest = false;

    // Хранение новостей для каждого пользователя


    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String receivedMessage = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Проверяем, если бот ожидает ввод темы для новостей
            if (waitingForNewsRequest && !receivedMessage.startsWith("/")) {
                sendNewsRequest(chatId, receivedMessage); // Отправляем запрос на получение новостей
                waitingForNewsRequest = false; // Сбрасываем флаг
                sendMessage(chatId, "Идет поиск новостей, подождите...");
            }
            // Если бот ожидает вопрос для ChatGPT
            else if (waitingForQuestion && !receivedMessage.startsWith("/")) {
                sendGptRequest(chatId, receivedMessage); // Отправляем вопрос в GPT
                waitingForQuestion = false; // Сбрасываем флаг
                sendMessage(chatId, "Ваш запрос отправлен, ожидайте ответа...");
            } else if (waitingForWeatherRequest && !receivedMessage.startsWith("/")) {
                sendWeatherRequest(chatId,receivedMessage);
                waitingForWeatherRequest = false;
                sendMessage(chatId,"Получаю данные о погоде, подождите...");

            } else {
                switch (receivedMessage) {
                    case "/start":
                        sendStartMessage(chatId);
                        break;
                    case "/menu":
                        sendMenuMessage(chatId);
                        break;
                    case "/help":
                        sendHelpMessage(chatId);
                        break;
                    case "/about":
                        sendAboutMessage(chatId);
                        break;
                    case "/weather":
                        sendMessage(chatId, "Введите город в котором хотите узнать погоду:");
                        waitingForWeatherRequest = true;
                        // Активируем режим ожидания темы новостей
                        break;
                    case "/news":
                        sendMessage(chatId, "Введите тему для поиска новостей:");
                        waitingForNewsRequest = true; // Активируем режим ожидания темы новостей
                        break;
                    case "/ask":
                        sendMessage(chatId, "Введите свой вопрос для ChatGPT:");
                        waitingForQuestion = true; // Активируем режим ожидания вопроса для ChatGPT
                        break;
                    default:
                        sendDefaultMessage(chatId); // Обрабатываем неизвестные команды
                }
            }
        }
    }

    // Метод для отправки запроса в ChatGPT
    private void sendGptRequest(Long chatId, String userMessage) {
        try {
            ApiRequest<String> request = new ApiRequest<>(chatId, userMessage);
            rabbitTemplate.convertAndSend("gpt-request-queue", request); // Отправляем запрос в очередь GPT
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(chatId, "Произошла ошибка при отправке запроса в ChatGPT.");
        }
    }

    // Метод для отправки запроса на получение новостей
    private void sendNewsRequest(Long chatId, String userMessage) {
        try {
            ApiRequest<String> request = new ApiRequest<>(chatId, userMessage);
            rabbitTemplate.convertAndSend(NEWS_REQUEST_QUEUE, request);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(chatId, "Произошла ошибка при отправке запроса в News API.");
        }
    }
    private void sendWeatherRequest(Long chatId, String userMessage) {
        try {
            ApiRequest<String> request = new ApiRequest<>(chatId, userMessage);
            rabbitTemplate.convertAndSend(WEATHER_REQUEST_QUEUE, request);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(chatId, "Произошла ошибка при отправке запроса в Weather API.");
        }
    }

    public void handleNewsResponse(Long chatId, String news) {
        sendMessage(chatId, news); // Отправляем строку с новостями
    }





    private void sendStartMessage(Long chatId) {
        String startText = "Привет! Этот бот поможет вам с рекомендациями музыки, рецептов, новостей и мест. Он также может ответить на любые ваши вопросы!";
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(startText);
        message.setReplyMarkup(createKeyboardMarkup());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Клавиатура для команд
    private ReplyKeyboardMarkup createKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("/menu"));
        row1.add(new KeyboardButton("/help"));
        row1.add(new KeyboardButton("/weather"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("/about"));
        row2.add(new KeyboardButton("/ask"));
        row2.add(new KeyboardButton("/news"));

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private void sendMenuMessage(Long chatId) {
        String menuText = "Меню команд:\n" +
                "/start - начать работу с ботом\n" +
                "/menu - показать это меню\n" +
                "/help - получить помощь по командам бота\n" +
                "/about - узнать больше о боте\n" +
                "/news - узнать новости на любую тему\n" +
                "/news - узнать погоду в любом городе\n" +
                "/ask - задать вопрос ChatGPT";

        sendMessage(chatId, menuText);
    }

    private void sendHelpMessage(Long chatId) {
        String helpText = "Помощь по использованию бота!";
        sendMessage(chatId, helpText);
    }

    private void sendAboutMessage(Long chatId) {
        String aboutText = "Это мультифункциональный бот, который предоставляет рекомендации по рецептам, музыке, новостям и интересным местам!";
        sendMessage(chatId, aboutText);
    }

    private void sendDefaultMessage(Long chatId) {
        String defaultText = "Извините, я не понимаю эту команду. Попробуйте /help для списка доступных команд.";
        sendMessage(chatId, defaultText);
    }

    public void sendMessage(Long chatId, String text) {
        if (text.length() > 4096) {
            int start = 0;
            while (start < text.length()) {
                int end = Math.min(start + 4096, text.length());
                String part = text.substring(start, end);
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText(part);
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                start = end;
            }
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}