package com.andriyklus.dota2.repository;

import com.andriyklus.dota2.domain.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeamRepository extends MongoRepository<Team, String> {
}
