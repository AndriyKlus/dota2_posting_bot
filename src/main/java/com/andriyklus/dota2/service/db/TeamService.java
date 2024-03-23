package com.andriyklus.dota2.service.db;

import com.andriyklus.dota2.domain.Team;
import com.andriyklus.dota2.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> getUkrainianTeams() {
        return teamRepository.findAll();
    }


}
