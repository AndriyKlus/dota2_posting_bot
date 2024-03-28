package com.andriyklus.dota2.service.db;

import com.andriyklus.dota2.domain.Match;
import com.andriyklus.dota2.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MongoOperations mongoOperations;


    public List<Match> getStartedMatches() {
        return matchRepository.findAll();
    }

    public void save(Match match) {
        matchRepository.save(match);
    }

    public Optional<Match> findMatch(Match match) {
        Query query = new Query(Criteria.where("teamOne.name").is(match.getTeamOne().getName())
                .and("teamTwo.name").is(match.getTeamTwo().getName()));
        return mongoOperations.find(query, Match.class).stream().findAny();
    }

}