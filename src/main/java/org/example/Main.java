package org.example;

import org.example.config.BotConfig;
import org.example.controller.GroqTelegramBot;
import org.example.repository.ConversationRepository;
import org.example.repository.InMemoryConversationRepository;
import org.example.service.ConversationService;
import org.example.service.GroqService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class Main {
    public static void main(String[] args) {
        try {
            ConversationRepository repository = new InMemoryConversationRepository();
            GroqService groqService = new GroqService();
            ConversationService conversationService = new ConversationService(repository, groqService);
            GroqTelegramBot bot = new GroqTelegramBot(
                    BotConfig.TELEGRAM_BOT_TOKEN,
                    BotConfig.TELEGRAM_BOT_USERNAME,
                    conversationService
            );

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);

            System.out.println("Bot muvaffaqiyatli ishga tushdi! Username: @" + bot.getBotUsername());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
