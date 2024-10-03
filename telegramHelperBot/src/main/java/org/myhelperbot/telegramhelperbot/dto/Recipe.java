package org.myhelperbot.telegramhelperbot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Recipe {
    private int id;
    private String title;
    private String imageUrl;
    private String sourceUrl;
}