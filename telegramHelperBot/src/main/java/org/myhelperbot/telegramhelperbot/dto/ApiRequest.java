package org.myhelperbot.telegramhelperbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiRequest<T> implements Serializable {
    private Long chatId;
    private T requestData;
}
