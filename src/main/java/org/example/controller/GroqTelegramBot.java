package org.example.controller;

import org.example.service.ConversationService;
import org.example.util.MarkdownToHtmlConverter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class GroqTelegramBot extends TelegramLongPollingBot {
    private static final int TELEGRAM_MESSAGE_LIMIT = 4096;
    private static final int UPDATE_INTERVAL_MS = 750;
    private static final String AUDIO_TEMP_DIR = "temp_audio";

    private final String botUsername;
    private final ConversationService conversationService;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public GroqTelegramBot(String botToken, String botUsername, ConversationService conversationService) {
        super(botToken);
        this.botUsername = botUsername;
        this.conversationService = conversationService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                virtualThreadExecutor.submit(() -> handleTextMessage(update));
            } else if (update.getMessage().hasVoice()) {
                virtualThreadExecutor.submit(() -> handleVoiceMessage(update));
            }
        }
    }

    private void handleTextMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        switch (text) {
            case "/start":
                sendMessage(chatId, "Salom! Menga matnli yoki ovozli xabar yuboring.");
                break;
            case "/new":
                conversationService.clearHistory(chatId);
                sendMessage(chatId, "Suhbat tarixi tozalandi.");
                break;
            default:
                handleStreamResponse(chatId, text);
                break;
        }
    }

    private void handleVoiceMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        Voice voice = update.getMessage().getVoice();
        sendMessage(chatId, "Ovozli xabar qabul qilindi, matnga o'girilmoqda...");

        File tempDir = new File(AUDIO_TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File localFile = new File(tempDir, voice.getFileId() + ".ogg");

        try {
            GetFile getFileMethod = new GetFile(voice.getFileId());
            org.telegram.telegrambots.meta.api.objects.File telegramFile = execute(getFileMethod);
            downloadFile(telegramFile, localFile);

            conversationService.transcribeAudio(localFile)
                    .thenAccept(transcribedText -> {
                        if (transcribedText != null && !transcribedText.isBlank()) {
                            sendMessage(chatId, "<b>Matn:</b> <i>" + transcribedText + "</i>\n\nJavob tayyorlanmoqda...");
                            handleStreamResponse(chatId, transcribedText);
                        } else {
                            sendMessage(chatId, "Uzur, ovozli xabarni matnga o'gira olmadim.");
                        }
                        localFile.delete();
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        sendMessage(chatId, "Ovozli xabarni qayta ishlashda xatolik yuz berdi.");
                        localFile.delete();
                        return null;
                    });

        } catch (TelegramApiException e) {
            e.printStackTrace();
            sendMessage(chatId, "Telegramdan faylni yuklab olishda xatolik yuz berdi.");
        }
    }

    private void handleStreamResponse(long chatId, String text) {
        final StringBuilder fullResponse = new StringBuilder();
        final AtomicBoolean isComplete = new AtomicBoolean(false);

        conversationService.processMessageStream(
                chatId,
                text,
                fullResponse::append,
                () -> isComplete.set(true)
        );

        int currentMessageId = -1;
        String lastSentTextInPart = "";
        int totalSentLength = 0;

        while (!isComplete.get() || totalSentLength < fullResponse.length()) {
            try {
                Thread.sleep(UPDATE_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (currentMessageId == -1) {
                currentMessageId = sendPlaceholderMessage(chatId);
                if (currentMessageId == -1) break;
            }

            String currentFullText = fullResponse.toString();
            String partContent = currentFullText.substring(totalSentLength);
            boolean isFinalChunk = isComplete.get() && (totalSentLength + partContent.length() == currentFullText.length());

            if (partContent.isEmpty() && !isFinalChunk) {
                continue;
            }

            if (partContent.length() > TELEGRAM_MESSAGE_LIMIT - 100) {
                int splitIndex = findBestSplitIndex(partContent);
                String partToSend = partContent.substring(0, splitIndex);
                editMessage(chatId, currentMessageId, partToSend, true);
                totalSentLength += splitIndex;
                lastSentTextInPart = "";
                currentMessageId = -1;
                continue;
            }

            if (!partContent.equals(lastSentTextInPart)) {
                editMessage(chatId, currentMessageId, partContent, isFinalChunk);
                lastSentTextInPart = partContent;
            }
        }
    }

    private int findBestSplitIndex(String part) {
        int limit = TELEGRAM_MESSAGE_LIMIT - 200;
        int lastNewline = part.lastIndexOf('\n', limit);
        if (lastNewline != -1) return lastNewline;
        int lastSpace = part.lastIndexOf(' ', limit);
        if (lastSpace != -1) return lastSpace;
        return limit;
    }

    private int sendPlaceholderMessage(long chatId) {
        try {
            SendMessage message = new SendMessage(String.valueOf(chatId), "🤔");
            return execute(message).getMessageId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void editMessage(long chatId, int messageId, String text, boolean isFinal) {
        String suffix = isFinal ? "" : " ✍️";
        String htmlText = MarkdownToHtmlConverter.convert(text) + suffix;

        if (htmlText.length() > TELEGRAM_MESSAGE_LIMIT) {
            htmlText = htmlText.substring(0, TELEGRAM_MESSAGE_LIMIT - 10) + "...";
        }

        EditMessageText edit = new EditMessageText();
        edit.setChatId(String.valueOf(chatId));
        edit.setMessageId(messageId);
        edit.setText(htmlText);
        edit.setParseMode("HTML");
        try {
            execute(edit);
        } catch (TelegramApiException e) {
            if (!e.getMessage().contains("message is not modified")) {
                System.err.println("Xabar tahrirlashda xatolik: " + e.getMessage());
            }
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setParseMode("HTML");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }
}
