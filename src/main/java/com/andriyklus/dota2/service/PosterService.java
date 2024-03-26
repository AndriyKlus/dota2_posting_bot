package com.andriyklus.dota2.service;

import com.andriyklus.dota2.domain.Match;
import com.andriyklus.dota2.domain.GameinsideNewsPost;
import com.andriyklus.dota2.parcer.LiquipediaParser;
import com.andriyklus.dota2.service.db.GameinsideNewsPostService;
import com.andriyklus.dota2.telegram.service.SendMessageService;
import org.apache.logging.log4j.util.Strings;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.andriyklus.dota2.parcer.GameInsideParser.parseDota2AndCS2News;

@Component
@EnableScheduling
public class PosterService {

    @Autowired
    private SendMessageService sendMessageService;
    @Autowired
    private LiquipediaParser liquipediaParser;
    @Autowired
    private GameinsideNewsPostService gameinsideNewsPostService;

    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void postGameInsideNews() {
        List<GameinsideNewsPost> news = parseDota2AndCS2News();
        news = getNewPosts(news);
        news.forEach(sendMessageService::postGameInsideNews);
        if(news.size()>0)
            gameinsideNewsPostService.saveLastNewsPost(news.get(0));
    }

    private List<GameinsideNewsPost> getNewPosts(List<GameinsideNewsPost> news) {
        Optional<GameinsideNewsPost> lastNewsPost = gameinsideNewsPostService.getLastNewsPost();
        if(lastNewsPost.isEmpty())
            return news;
        List<GameinsideNewsPost> newPosts = new ArrayList<>();
        String header = lastNewsPost.get().getHeader();
        for (GameinsideNewsPost gameinsideNewsPost : news) {
            if (gameinsideNewsPost.getHeader().equals(header)) {
                break;
            }
            newPosts.add(gameinsideNewsPost);
        }
        return newPosts;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void postTodayMatches() {
        List<Match> matches = liquipediaParser.parseDayMatches();
        if(matches.size() == 0)
            return;

        sendMessageService.postTodayGames(matches);
    }


}
