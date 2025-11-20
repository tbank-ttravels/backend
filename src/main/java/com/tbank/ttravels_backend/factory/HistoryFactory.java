package com.tbank.ttravels_backend.factory;

import com.tbank.ttravels_backend.entity.History;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.enums.HistoryType;


public class HistoryFactory {

    // Фабричный метод создания истории
    public static History create(Travel travel, User author,
                                 String description, HistoryType type) {

        return History.builder()
                .travel(travel)
                .author(author)
                .description(description)
                .type(type)
                .build();
    }
}
