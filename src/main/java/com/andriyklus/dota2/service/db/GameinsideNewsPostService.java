package com.andriyklus.dota2.service.db;

import com.andriyklus.dota2.domain.GameinsideNewsPost;
import com.andriyklus.dota2.repository.GameinsideNewsPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameinsideNewsPostService {

    private final GameinsideNewsPostRepository gameinsideNewsPostRepository;

    @Autowired
    public GameinsideNewsPostService(GameinsideNewsPostRepository gameinsideNewsPostRepository) {
        this.gameinsideNewsPostRepository = gameinsideNewsPostRepository;
    }

    public Optional<GameinsideNewsPost> getLastNewsPost() {
        return gameinsideNewsPostRepository.findAll().stream().findAny();
    }

    public GameinsideNewsPost saveLastNewsPost(GameinsideNewsPost gameinsideNewsPost) {
        gameinsideNewsPostRepository.deleteAll();
        return gameinsideNewsPostRepository.insert(gameinsideNewsPost);
    }


}
