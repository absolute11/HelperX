package org.myhelperbot.foodapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Recipe implements Serializable {
    private int id;
    private String title;
    private String imageUrl;
    private String sourceUrl;
}