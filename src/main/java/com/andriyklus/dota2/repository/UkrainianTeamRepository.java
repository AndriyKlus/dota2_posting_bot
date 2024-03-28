package com.andriyklus.dota2.repository;

import com.andriyklus.dota2.domain.UkrainianTeam;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UkrainianTeamRepository extends MongoRepository<UkrainianTeam, String> {

}
