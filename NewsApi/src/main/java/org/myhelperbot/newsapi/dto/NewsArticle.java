package org.myhelperbot.newsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NewsArticle {
    private final String title;
    private final String description;
    private final String url;

}
