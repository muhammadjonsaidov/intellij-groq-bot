# Intellekt Bot: Groq API Asosida Yuqori Tezlikdagi Telegram Bot

![Java](https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.9-orange?style=for-the-badge&logo=apache-maven)
![Telegram](https://img.shields.io/badge/Telegram-API-blue?style=for-the-badge&logo=telegram)
![Groq](https://img.shields.io/badge/Groq-API-green?style=for-the-badge)

Bu loyiha — Java 21 yordamida qurilgan, Groq platformasining super-tezkor API'sidan foydalanuvchi aqlli va multimodal Telegram botidir. Bot matnli suhbatlarni va ovozli xabarlarni qayta ishlay oladi, javoblarni esa real vaqtda (streaming) yuboradi.

## 🚀 Asosiy Xususiyatlari

-   **Multimodal Muloqot:** Bot ham matnli, ham ovozli xabarlarni tushunadi va ularga mos javob qaytaradi.
-   **Super-tezkor Javoblar (Streaming):** Groq'ning LPU texnologiyasi yordamida javoblar so'zma-so'z, "yozilayotgandek" effekt bilan uzatiladi, bu esa foydalanuvchi tajribasini keskin yaxshilaydi.
-   **Ovozni Matnga O'girish:** `whisper-large-v3` modeli yordamida yuqori aniqlikdagi speech-to-text funksiyasi mavjud.
-   **Suhbat Kontekstini Saqlash:** Bot har bir foydalanuvchi bilan bo'lgan suhbat tarixini eslab qoladi va suhbatni tabiiy ravishda davom ettiradi.
-   **Uzun Javoblarni Bo'lib Yuborish:** Telegramning 4096 belgi cheklovini chetlab o'tish uchun uzun javoblar avtomatik ravishda bir nechta xabarlarga bo'linadi.
-   **Moslashuvchan Model Tanlovi:** `.env` fayli orqali suhbat va ovoz uchun ishlatiladigan AI modellarini (masalan, `llama3-70b`, `qwen2-72b`, `gemma-7b`) kodni o'zgartirmasdan almashtirish mumkin.
-   **Toza Arxitektura:** Dastur `Controller`, `Service`, `Repository` kabi qatlamlarga bo'lingan bo'lib, uni kengaytirish va qo'llab-quvvatlash oson.
-   **Xavfsiz Konfiguratsiya:** Barcha maxfiy kalitlar va tokenlar `.env` fayli orqali boshqariladi va `.gitignore` yordamida repozitoriyga chiqib ketishidan himoyalangan.

## 🛠️ Texnologiyalar Staki

-   **Dasturlash tili:** Java 21 (Virtual Threads bilan)
-   **Loyihani boshqarish:** Apache Maven
-   **Telegram API:** TelegramBots kutubxonasi
-   **AI Platformasi:** Groq API (LLM + Whisper)
-   **JSON bilan ishlash:** Jackson Databind
-   **Konfiguratsiya:** Java Dotenv

## 🏗️ Loyiha Arxitekturasi

Loyiha ko'p qatlamli arxitektura tamoyillariga asoslangan:
-   **`Controller`**: Telegramdan kelgan so'rovlarni qabul qiladi va `Service`ga yo'naltiradi.
-   **`Service`**: Asosiy biznes-mantiq (suhbatni boshqarish, Groq API bilan ishlash) shu yerda joylashgan.
-   **`Repository`**: Ma'lumotlarni saqlash (hozircha xotirada).
-   **`Config`**: Barcha sozlamalarni `.env` faylidan yuklaydi.
-   **`Util`**: Yordamchi funksiyalar (masalan, Markdown'ni HTML'ga o'girish).

## ⚙️ O'rnatish va Ishga Tushirish

1.  **Repozitoriyni klonlash:**
    ```bash
    git clone https://github.com/muhammadjonsaidov/intellij-groq-bot.git
    cd intellij-groq-bot
    ```

2.  **`.env` faylini yaratish:**
    Loyiha papkasida `.env.example` faylidan nusxa olib, `.env` nomli yangi fayl yarating.

3.  **Konfiguratsiyani to'ldirish:**
    Yaratilgan `.env` faylini o'zingizning maxfiy kalitlaringiz bilan to'ldiring.

4.  **Loyihani qurish (build):**
    ```bash
    mvn clean install
    ```

5.  **Botni ishga tushirish:**
    ```bash
    java -jar target/telegram-groq-bot-java21-1.0-SNAPSHOT.jar
    ```

## 📝 Konfiguratsiya (`.env` fayli)

| O'zgaruvchi             | Tavsif                                                                      | Namuna                                |
| ----------------------- | --------------------------------------------------------------------------- | ------------------------------------- |
| `TELEGRAM_BOT_TOKEN`    | @BotFather orqali olingan Telegram bot tokeni.                              | `123456:ABC-DEF1234ghIkl-zyx57W2v1`   |
| `TELEGRAM_BOT_USERNAME` | Botingizning username'i (@ belgisisiz).                                     | `MeningAqlliBotim_bot`                |
| `GROQ_API_KEY`          | GroqCloud platformasidan olingan API kaliti.                                | `gsk_aBCdEFGhijKlmNopQRstUvWXyZ`      |
| `SYSTEM_PROMPT`         | Botning shaxsiyati va javob berish uslubini belgilovchi ko'rsatma.            | `Sen 'Intellekt' nomli AI yordamchisan...` |
| `GROQ_CHAT_MODEL`       | Matnli suhbatlar uchun ishlatiladigan model.                                | `llama3-70b-8192`                     |
| `GROQ_AUDIO_MODEL`      | Ovozni matnga o'girish uchun ishlatiladigan model.                          | `whisper-large-v3`                    |


## 🎯 Kelajakdagi Rejalar

-   [ ] Suhbat tarixini fayl yoki ma'lumotlar bazasida saqlash (PostgreSQL/SQLite).
-   [ ] Foydalanuvchilar uchun `/settings` buyrug'i orqali model yoki `system_prompt`ni o'zgartirish imkoniyati.
-   [ ] Rasm bilan ishlash funksiyasini qo'shish (masalan, LLaVA modeli yordamida).

---
Bu loyiha ta'limiy maqsadlarda yaratilgan.