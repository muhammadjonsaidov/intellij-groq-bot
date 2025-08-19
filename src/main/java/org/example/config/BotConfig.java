package org.example.config;

import io.github.cdimascio.dotenv.Dotenv;

public class BotConfig {
    private static final Dotenv dotenv = Dotenv.load();

    public static final String TELEGRAM_BOT_TOKEN = getEnv("TELEGRAM_BOT_TOKEN");
    public static final String TELEGRAM_BOT_USERNAME = getEnv("TELEGRAM_BOT_USERNAME");
    public static final String GROQ_API_KEY = getEnv("GROQ_API_KEY");
    public static final String SYSTEM_PROMPT = getEnvOrEmpty();

    public static final String GROQ_CHAT_MODEL = getEnvOrDefault("GROQ_CHAT_MODEL", "llama3-8b-8192");
    public static final String GROQ_AUDIO_MODEL = getEnvOrDefault("GROQ_AUDIO_MODEL", "whisper-large-v3");


    private static String getEnv(String key) {
        String value = dotenv.get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    private static String getEnvOrEmpty() {
        return dotenv.get("SYSTEM_PROMPT", "");
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        return dotenv.get(key, defaultValue);
    }
}
