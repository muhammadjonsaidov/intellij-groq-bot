package org.example.repository;

import org.example.model.Message;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository {
    Optional<List<Message>> getHistory(long chatId);

    void addMessage(long chatId, Message message);

    void clearHistory(long chatId);
}