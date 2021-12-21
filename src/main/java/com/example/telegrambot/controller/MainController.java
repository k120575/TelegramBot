package com.example.telegrambot.controller;

import com.example.telegrambot.model.TelegramMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class MainController {

    @PostMapping("/receive")
    public void receive(@RequestBody TelegramMessage telegramMessage){

    }
}
