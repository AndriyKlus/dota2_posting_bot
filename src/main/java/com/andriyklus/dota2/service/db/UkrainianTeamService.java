package com.andriyklus.dota2.service.db;

import com.andriyklus.dota2.domain.UkrainianTeam;
import com.andriyklus.dota2.repository.UkrainianTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UkrainianTeamService {

    private final UkrainianTeamRepository teamRepository;

    public List<UkrainianTeam> getUkrainianTeams() {
        return teamRepository.findAll();
    }


}
