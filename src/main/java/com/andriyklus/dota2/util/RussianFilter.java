package com.andriyklus.dota2.util;

import com.andriyklus.dota2.domain.Transfer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RussianFilter {

    private static List<String> russianTeams;


    @PostConstruct
    private void init() {
        russianTeams = List.of("1win",
                "9Pandas",
                "BetBoom Team",
                "L1GA TEAM",
                "Nemiga Gaming",
                "One Move",
                "SIBE Team",
                "Team Spirit",
                "Virtus.pro",
                "Marlerino eSports",
                "Pavaga Gaming");
    }

    public boolean isNotRussianTeam(Transfer transfer) {
        return !russianTeams.contains(transfer.getNewTeam()) && !russianTeams.contains(transfer.getOldTeam());
    }

    public boolean isNotRussianPlayer(Transfer transfer) {
        transfer.getPlayers().removeIf(player -> player.getFlag().equals("\uD83C\uDDF7\uD83C\uDDFA"));
        return transfer.getPlayers().size() > 0;
    }

    public boolean isNotRussianPlayer(String flag) {
        return !flag.equals("\uD83C\uDDF7\uD83C\uDDFA");
    }

}