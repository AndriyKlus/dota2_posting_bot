package com.andriyklus.dota2.service.db;

import com.andriyklus.dota2.domain.UkrainianTeam;
import com.andriyklus.dota2.repository.UkrainianTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UkrainianTeamService {

    private final UkrainianTeamRepository teamRepository;
    private final MongoOperations mongoOperations;

    public List<UkrainianTeam> getUkrainianTeams() {
        return teamRepository.findAll();
    }

    public Optional<UkrainianTeam> getUkrainianTeam(String name) {
        Query query = new Query(Criteria.where("name").is(name));
        return mongoOperations.find(query, UkrainianTeam.class).stream().findAny();
    }

}
