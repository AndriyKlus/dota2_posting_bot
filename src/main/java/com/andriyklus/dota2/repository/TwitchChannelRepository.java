package com.andriyklus.dota2.repository;

import com.andriyklus.dota2.domain.TwitchChannel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TwitchChannelRepository extends MongoRepository<TwitchChannel, String> {


}
