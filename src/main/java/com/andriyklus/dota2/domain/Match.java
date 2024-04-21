package com.andriyklus.dota2.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Match {

    @Id
    private String id;
    private Team teamOne;
    private Team teamTwo;
    private Tournament tournament;
    private String time;
    private int format;
    private List<TwitchChannel> streams;
}
