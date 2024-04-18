package com.andriyklus.dota2.repository;

import com.andriyklus.dota2.domain.Quest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuestRepository extends MongoRepository<Quest, String> {
}
