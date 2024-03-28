package com.andriyklus.dota2.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Builder
@Document(collection =  "ukrainian_team")
public class Team {

    @Id
    private String id;
    private String name;
    private int score;
    private List<String> players;

}
