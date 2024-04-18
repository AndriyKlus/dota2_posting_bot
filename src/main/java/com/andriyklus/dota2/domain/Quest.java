package com.andriyklus.dota2.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("quest")
public class Quest {

    @Id
    private String id;
    private String question;
    private List<String> choices;
    private String feedback;
    private String rightAnswer;

}
