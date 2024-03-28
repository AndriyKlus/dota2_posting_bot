package com.andriyklus.dota2.repository;

import com.andriyklus.dota2.domain.Match;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchRepository extends MongoRepository<Match, String> {
}
