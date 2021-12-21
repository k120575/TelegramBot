package com.example.telegrambot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class From {

    private String id;

    private Boolean isBot;

    private String firstName;

    private String lastName;

    private String userName;

    private String languageCode;
}
