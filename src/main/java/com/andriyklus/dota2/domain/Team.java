package com.andriyklus.dota2.domain;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class Team {

    private String name;
    private int score;
    private List<String> players;
}
