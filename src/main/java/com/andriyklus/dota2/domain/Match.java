package com.andriyklus.dota2.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Match {

    @Id
    private Long id;
    private Team teamOne;
    private Team teamTwo;
    private Tournament tournament;
    private String time;


}
