package org.example.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.BotConfig;
import org.example.model.Message;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GroqService {
    private static final String GROQ_CHAT_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_AUDIO_API_URL = "https://api.groq.com/openai/v1/audio/transcriptions";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GroqService() {
        this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofSeconds(20)).build();
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public CompletableFuture<String> transcribeAudio(File audioFile) {
        String boundary = "Boundary-" + UUID.randomUUID().toString();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_AUDIO_API_URL))
                    .header("Authorization", "Bearer " + BotConfig.GROQ_API_KEY)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(ofMimeMultipartData(audioFile, boundary))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(this::parseWhisperResponse);

        } catch (IOException e) {
            System.err.println("Whisper so'rovini yaratishda xatolik: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    private HttpRequest.BodyPublisher ofMimeMultipartData(File file, String boundary) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        String separator = "--" + boundary + "\r\nContent-Disposition: form-data; name=";

        byteArrays.add((separator + "\"model\"\r\n\r\n" + BotConfig.GROQ_AUDIO_MODEL + "\r\n").getBytes(StandardCharsets.UTF_8));

        byteArrays.add((separator + "\"file\"; filename=\"" + file.getName() + "\"\r\nContent-Type: audio/ogg\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(Files.readAllBytes(file.toPath()));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));

        byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    private String parseWhisperResponse(String jsonResponse) {
        try {
            WhisperResponse response = objectMapper.readValue(jsonResponse, WhisperResponse.class);
            return response.text();
        } catch (JsonProcessingException e) {
            System.err.println("Whisper javobini parse qilishda xatolik: " + e.getMessage());
            return null;
        }
    }

    public CompletableFuture<Void> getStreamingResponse(List<Message> messages, Consumer<String> onChunkReceived, Runnable onComplete) {
        try {
            var requestBody = new StreamGroqRequest(messages, BotConfig.GROQ_CHAT_MODEL, true);
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_CHAT_API_URL))
                    .header("Authorization", "Bearer " + BotConfig.GROQ_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> response.body().forEach(line -> {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if (!data.equals("[DONE]")) {
                                try {
                                    StreamGroqResponse chunk = objectMapper.readValue(data, StreamGroqResponse.class);
                                    if (chunk != null && chunk.choices() != null && !chunk.choices().isEmpty()) {
                                        String content = chunk.choices().get(0).delta().content();
                                        if (content != null) {
                                            onChunkReceived.accept(content);
                                        }
                                    }
                                } catch (JsonProcessingException e) { /* Ignore */ }
                            }
                        }
                    }))
                    .whenComplete((res, err) -> onComplete.run());

        } catch (Exception e) {
            onComplete.run();
            return CompletableFuture.failedFuture(e);
        }
    }

    private record StreamGroqRequest(List<Message> messages, String model, boolean stream) {}
    private record Delta(String content) {}
    private record StreamChoice(Delta delta) {}
    private record StreamGroqResponse(@JsonProperty("choices") List<StreamChoice> choices) {}
    private record WhisperResponse(String text){}
}
