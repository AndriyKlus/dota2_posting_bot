package com.andriyklus.dota2.service;

import com.andriyklus.dota2.domain.Match;
import com.andriyklus.dota2.domain.GameinsideNewsPost;
import com.andriyklus.dota2.domain.Team;
import com.andriyklus.dota2.domain.UkrainianTeam;
import com.andriyklus.dota2.parcer.GameInsideParser;
import com.andriyklus.dota2.parcer.LiquipediaParser;
import com.andriyklus.dota2.service.db.GameinsideNewsPostService;
import com.andriyklus.dota2.service.db.MatchService;
import com.andriyklus.dota2.service.db.UkrainianTeamService;
import com.andriyklus.dota2.telegram.service.SendMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class PosterService {

    private final SendMessageService sendMessageService;
    private final LiquipediaParser liquipediaParser;
    private final GameinsideNewsPostService gameinsideNewsPostService;
    private final MatchService matchService;
    private final UkrainianTeamService teamService;
    private final GameInsideParser gameInsideParser;

    Logger logger = LoggerFactory.getLogger(PosterService.class);


    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void postGameInsideNews() {
        List<GameinsideNewsPost> news = gameInsideParser.parseDOTA2News();
        news = getNewPosts(news);
        news.forEach(sendMessageService::postGameInsideNews);
        if (news.size() > 0)
            gameinsideNewsPostService.saveLastNewsPost(news.get(0));
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void postTodayMatches() {
        List<Match> matches = liquipediaParser.parseDayMatches();
        if (matches.size() == 0)
            return;

        sendMessageService.postTodayGames(matches);
    }
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void postStartedMatch() {
        List<Match> startedMatchesDB = matchService.getStartedMatches();
        List<Match> matchesToPost = liquipediaParser.parseStartedMatches().stream()
                .filter(match -> match.getTeamOne().getScore() == 0 && match.getTeamTwo().getScore() == 0)
                .filter(match -> !filterMatchFromDB(match, startedMatchesDB))
                .toList();

        matchesToPost.forEach(sendMessageService::postStartedMatch);
        matchesToPost.forEach(matchService::save);
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void postOngoingMatch() {
        List<Match> startedMatchesDB = matchService.getStartedMatches();
        List<Match> matchesToPost = liquipediaParser.parseStartedMatches().stream()
                .filter(match -> filterOldMatchFromDB(match, startedMatchesDB))
                .toList();

        matchesToPost.forEach(this::chooseTypeOfMessageForGameEnd);
        matchesToPost.forEach(matchService::save);
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    private void postEndedMatches() {
        List<Match> startedMatchesDB = matchService.getStartedMatches();
        List<Match> matchesToPost = liquipediaParser.parseEndedMatches().stream()
                .filter(match -> filterOldMatchFromDB(match, startedMatchesDB))
                .toList();

        matchesToPost.forEach(this::chooseTypeOfMessageForMatchEnd);
    }

    private List<GameinsideNewsPost> getNewPosts(List<GameinsideNewsPost> news) {
        Optional<GameinsideNewsPost> lastNewsPost = gameinsideNewsPostService.getLastNewsPost();
        if (lastNewsPost.isEmpty())
            return news;
        List<GameinsideNewsPost> newPosts = new ArrayList<>();
        String id = lastNewsPost.get().getId();
        for (GameinsideNewsPost gameinsideNewsPost : news) {
            if (gameinsideNewsPost.getId().equals(id)) {
                break;
            }
            newPosts.add(gameinsideNewsPost);
        }
        return newPosts;
    }

    private boolean filterMatchFromDB(Match match, List<Match> matches) {
        return matches.stream()
                .anyMatch(matchDB -> matchDB.getTeamOne().getName().equals(match.getTeamOne().getName()) &&
                        matchDB.getTeamTwo().getName().equals(match.getTeamTwo().getName()));
    }

    private boolean filterOldMatchFromDB(Match match, List<Match> matches) {
        return matches.stream()
                .anyMatch(matchDB -> matchDB.getTeamOne().getName().equals(match.getTeamOne().getName()) &&
                        matchDB.getTeamTwo().getName().equals(match.getTeamTwo().getName()) &&
                        (matchDB.getTeamOne().getScore() != match.getTeamOne().getScore() ||
                                matchDB.getTeamTwo().getScore() != match.getTeamTwo().getScore()));
    }

    private void chooseTypeOfMessageForGameEnd(Match match) {
        Map<Match, Team> gameWinner = new HashMap<>();
        switch (getGameWinner(match, gameWinner)) {
            case 1 -> {
                switch (getUkrainianTeam(match)) {
                    case 1 -> sendMessageService.postUkrainianTeamWonGame(match, gameWinner.get(match));
                    case 2 -> sendMessageService.postUkrainianTeamLostGame(match, gameWinner.get(match));
                    default -> sendMessageService.postTwoUkrainianTeamsGame(match);
                }
            }
            case 2 -> {
                switch (getUkrainianTeam(match)) {
                    case 1 -> sendMessageService.postUkrainianTeamLostGame(match, gameWinner.get(match));
                    case 2 -> sendMessageService.postUkrainianTeamWonGame(match, gameWinner.get(match));
                    default -> sendMessageService.postTwoUkrainianTeamsGame(match);
                }
            }
        }
    }

    private void chooseTypeOfMessageForMatchEnd(Match match) {
        Map<Match, Team> matchWinner = new HashMap<>();
        switch (getMatchResult(match, matchWinner)) {
            case 1 -> {
                switch (getUkrainianTeam(match)) {
                    case 1 -> sendMessageService.postUkrainianTeamWonMatch(match, matchWinner.get(match));
                    case 2 -> sendMessageService.postUkrainianTeamLostMatch(match, matchWinner.get(match));
                    default -> sendMessageService.postTwoUkrainianTeamsMatch(match);
                }
            }
            case 2 -> {
                switch (getUkrainianTeam(match)) {
                    case 1 -> sendMessageService.postUkrainianTeamLostMatch(match, matchWinner.get(match));
                    case 2 -> sendMessageService.postUkrainianTeamWonMatch(match, matchWinner.get(match));
                    default -> sendMessageService.postTwoUkrainianTeamsMatch(match);
                }
            }
        }
    }

    private int getUkrainianTeam(Match match) {
        List<String> ukrainianTeams = teamService.getUkrainianTeams().stream()
                .map(UkrainianTeam::getName)
                .toList();
        if (ukrainianTeams.contains(match.getTeamOne().getName()) && ukrainianTeams.contains(match.getTeamTwo().getName())) {
            return 0;
        } else if (ukrainianTeams.contains(match.getTeamOne().getName()))
            return 1;
        return 2;
    }

    private int getGameWinner(Match match, Map<Match, Team> gameWinner) {
        Optional<Match> matchDB = matchService.findMatch(match);
        if (matchDB.isEmpty()) {
            logger.error("Match: " + match + "wasn't found in database");
            return -1;
        }
        match.setId(matchDB.get().getId());
        if (match.getTeamOne().getScore() != matchDB.get().getTeamOne().getScore()) {
            gameWinner.put(match, match.getTeamOne());
            return 1;
        } else
            gameWinner.put(match, match.getTeamTwo());
        return 2;
    }

    private int getMatchResult(Match match, Map<Match, Team> matchWinner) {
        matchService.deleteByTeamsNames(match.getTeamOne().getName(), match.getTeamTwo().getName());
        if (match.getTeamOne().getScore() > match.getTeamTwo().getScore()) {
            matchWinner.put(match, match.getTeamOne());
            return 1;
        } else if (match.getTeamOne().getScore() < match.getTeamTwo().getScore()) {
            matchWinner.put(match, match.getTeamTwo());
            return 2;
        }
        return 0;
    }


}
