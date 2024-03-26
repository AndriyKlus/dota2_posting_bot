package com.andriyklus.dota2.repository;

import com.andriyklus.dota2.domain.GameinsideNewsPost;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameinsideNewsPostRepository extends MongoRepository<GameinsideNewsPost, String> {
}
