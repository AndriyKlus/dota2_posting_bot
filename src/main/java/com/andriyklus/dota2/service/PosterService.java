package com.andriyklus.dota2.service;

import com.andriyklus.dota2.domain.Match;
import com.andriyklus.dota2.domain.NewsPost;
import com.andriyklus.dota2.parcer.LiquipediaParser;
import com.andriyklus.dota2.telegram.service.SendMessageService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.andriyklus.dota2.filemanager.FileManager.getLastVideoHeader;
import static com.andriyklus.dota2.filemanager.FileManager.writeLastHeaderToTheFile;
import static com.andriyklus.dota2.parcer.GameInsideParser.parseDota2AndCS2News;

@Component
@EnableScheduling
public class PosterService {

    @Autowired
    private SendMessageService sendMessageService;
    @Autowired
    private LiquipediaParser liquipediaParser;

    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void postGameInsideNews() {
        List<NewsPost> news = parseDota2AndCS2News();
        news = getNewPosts(news);
        news.forEach(sendMessageService::postGameInsideNews);
        if(news.size()>0)
            writeLastHeaderToTheFile(news.get(0).getHeader());
    }

    private List<NewsPost> getNewPosts(List<NewsPost> news) {
        String lastHeader = getLastVideoHeader();
        if(Strings.isEmpty(lastHeader))
            return news;
        List<NewsPost> newPosts = new ArrayList<>();
        for (NewsPost newsPost : news) {
            if (newsPost.getHeader().equals(lastHeader)) {
                break;
            }
            newPosts.add(newsPost);
        }
        return newPosts;
    }

    //@Scheduled(cron = "0 0 11 * * *")
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void postTodayMatches() {
        List<Match> matches = liquipediaParser.parseDayMatches();
        if(matches.size() == 0)
            return;

        sendMessageService.postTodayGames(matches);
    }


}
