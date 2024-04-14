package com.andriyklus.dota2.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DayInDota {

    private List<Player> playersBirths;
    private Map<Tournament, Team> tournamentWinners;

}
