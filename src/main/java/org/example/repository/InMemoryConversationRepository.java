package org.example.repository;

import org.example.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryConversationRepository implements ConversationRepository {
    private final Map<Long, List<Message>> history = new ConcurrentHashMap<>();

    @Override
    public Optional<List<Message>> getHistory(long chatId) {
        return Optional.ofNullable(history.get(chatId));
    }

    @Override
    public void addMessage(long chatId, Message message) {
        history.computeIfAbsent(chatId, k -> new ArrayList<>()).add(message);
    }

    @Override
    public void clearHistory(long chatId) {
        history.remove(chatId);
    }
}
