package com.andriyklus.dota2.repository;

import com.andriyklus.dota2.domain.Transfer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransferRepository extends MongoRepository<Transfer, String> {
}
