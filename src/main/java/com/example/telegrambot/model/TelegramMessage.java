package com.example.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelegramMessage {

    @JsonProperty("updateId")
    private String updateId;

    @JsonProperty("Message")
    private Message message;
}
