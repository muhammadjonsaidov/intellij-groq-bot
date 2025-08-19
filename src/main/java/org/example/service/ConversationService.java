package org.example.service;

import org.example.config.BotConfig;
import org.example.model.Message;
import org.example.repository.ConversationRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ConversationService {
    private static final int MAX_HISTORY_SIZE = 10;
    private final ConversationRepository repository;
    private final GroqService groqService;

    public ConversationService(ConversationRepository repository, GroqService groqService) {
        this.repository = repository;
        this.groqService = groqService;
    }

    public CompletableFuture<Void> processMessageStream(long chatId, String text, Consumer<String> onChunkReceived, Runnable onComplete) {
        repository.addMessage(chatId, new Message("user", text));

        List<Message> history = new ArrayList<>(repository.getHistory(chatId).orElse(List.of()));

        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(history.size() - MAX_HISTORY_SIZE, history.size());
        }

        List<Message> messagesForGroq = new ArrayList<>();
        if (!BotConfig.SYSTEM_PROMPT.isEmpty()) {
            messagesForGroq.add(new Message("system", BotConfig.SYSTEM_PROMPT));
        }
        messagesForGroq.addAll(history);

        StringBuilder fullResponse = new StringBuilder();

        return groqService.getStreamingResponse(
                messagesForGroq,
                chunk -> {
                    fullResponse.append(chunk);
                    onChunkReceived.accept(chunk);
                },
                () -> {
                    repository.addMessage(chatId, new Message("assistant", fullResponse.toString()));
                    onComplete.run();
                }
        );
    }

    public CompletableFuture<String> transcribeAudio(File audioFile) {
        return groqService.transcribeAudio(audioFile);
    }

    public void clearHistory(long chatId) {
        repository.clearHistory(chatId);
    }
}
