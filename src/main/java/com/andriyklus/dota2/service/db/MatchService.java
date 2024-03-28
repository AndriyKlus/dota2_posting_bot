package com.andriyklus.dota2.service.db;

import com.andriyklus.dota2.domain.Match;
import com.andriyklus.dota2.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public List<Match> getStartedMatches() {
        return matchRepository.findAll();
    }

    public Match save(Match match) {
        return matchRepository.save(match);
    }

}