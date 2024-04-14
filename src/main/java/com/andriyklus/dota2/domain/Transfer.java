package com.andriyklus.dota2.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document("last_transfer")
public class Transfer {

    @Id
    private String id;
    private List<Player> players;
    private String oldTeam;
    private String newTeam;
    private String newTeamPosition;
    private String newsLink;

}
