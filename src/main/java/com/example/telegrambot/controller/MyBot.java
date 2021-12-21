package com.example.telegrambot.controller;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MyBot extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId());
           if (update.getMessage().getText().equals("測試") || update.getMessage().getText().toLowerCase().equals("test")){
               message.setText("測試成功");
           } else if (update.getMessage().getText().equals("嗨") || update.getMessage().getText().equals("hi") || update.getMessage().getText().equals("hello")){
               message.setText("你好啊");
           }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "mykevinbot";
    }

    @Override
    public String getBotToken() {
        return "5085724282:AAFZG3NuPhAOBRroO_lUkCViaRMcARJZp2Q";
    }
}
