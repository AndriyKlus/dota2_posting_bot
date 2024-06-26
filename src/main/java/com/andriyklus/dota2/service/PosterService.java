package com.andriyklus.dota2.service;

import com.andriyklus.dota2.domain.*;
import com.andriyklus.dota2.parcer.GameInsideParser;
import com.andriyklus.dota2.parcer.LiquipediaParser;
import com.andriyklus.dota2.service.db.*;
import com.andriyklus.dota2.telegram.service.SendMessageService;
import com.andriyklus.dota2.util.RussianFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
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
    private final RussianFilter russianFilter;
    private final TransferService transferService;
    private final QuestService questService;

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

    @Scheduled(fixedRate = 3 * 60 * 1000)
    public void postStartedMatch() {
        List<Match> startedMatchesDB = matchService.getStartedMatches();
        List<Match> matchesToPost = liquipediaParser.parseStartedMatches().stream()
                .filter(match -> match.getTeamOne().getScore() == 0 && match.getTeamTwo().getScore() == 0)
                .filter(match -> !filterMatchFromDB(match, startedMatchesDB))
                .toList();

        matchesToPost.forEach(sendMessageService::postStartedMatch);
        matchesToPost.forEach(matchService::save);
    }

    @Scheduled(fixedRate = 3 * 60 * 1000)
    public void postOngoingMatch() {
        List<Match> startedMatchesDB = matchService.getStartedMatches();
        List<Match> matchesToPost = liquipediaParser.parseStartedMatches().stream()
                .filter(match -> filterOldMatchFromDB(match, startedMatchesDB))
                .toList();

        matchesToPost.forEach(this::chooseTypeOfMessageForGameEnd);
        matchesToPost.forEach(matchService::save);
    }

    @Scheduled(fixedRate = 3 * 60 * 1000)
    private void postEndedMatches() {
        List<Match> startedMatchesDB = matchService.getStartedMatches();
        List<Match> matchesToPost = liquipediaParser.parseEndedMatches().stream()
                .filter(match -> filterOldMatchFromDB(match, startedMatchesDB))
                .toList();

        matchesToPost.forEach(this::chooseTypeOfMessageForMatchEnd);
    }

    @Scheduled(cron = "0 45 20 * * *")
    private void postDayResults() {
        List<Match> matches = liquipediaParser.parseEndedMatches();
        Collections.reverse(matches);
        if (matches.size() == 0)
            return;
        while (true) {
            if (matchService.getStartedMatches().size() == 0) {
                sendMessageService.postDayResults(matches);
                return;
            }
            try {
                Thread.sleep(200000);
            } catch (Exception ignored) {
            }
        }
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    private void postTransfers() {
        List<Transfer> transfers = liquipediaParser.parseTransfers();
        transfers.stream()
                .filter(russianFilter::isNotRussianTeam)
                .filter(russianFilter::isNotRussianPlayer)
                .forEach(this::chooseTransferMessage);
        if (transfers.size() > 0) {
            transferService.saveLastTransfer(transfers.get(0));
        }
    }

    @Scheduled(cron = "0 30 6 * * *")
    private void postDayInDota() {
        DayInDota dayInDota = liquipediaParser.parseDayInDota();
        if (dayInDota.getPlayersBirths().size() > 0 || dayInDota.getTournamentWinners().size() > 0)
            sendMessageService.sendThisDayInDotaMessage(dayInDota);
    }

    @Scheduled(cron = "0 0 6 * * *")
    private void postDayQuest() {
        Optional<Quest> quest = questService.getQuest();
        if(quest.isPresent()) {
            sendMessageService.postDayQuest(quest.get());
            questService.deleteQuestById(quest.get().getId());
        }
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
                    default -> sendMessageService.postTwoUkrainianTeamsGame(match, gameWinner.get(match));
                }
            }
            case 2 -> {
                switch (getUkrainianTeam(match)) {
                    case 1 -> sendMessageService.postUkrainianTeamLostGame(match, gameWinner.get(match));
                    case 2 -> sendMessageService.postUkrainianTeamWonGame(match, gameWinner.get(match));
                    default -> sendMessageService.postTwoUkrainianTeamsGame(match, gameWinner.get(match));
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

    private void chooseTransferMessage(Transfer transfer) {
        if (transfer.getOldTeam().equals("None") && Strings.isEmpty(transfer.getNewTeamPosition()))
            sendMessageService.sendMessageTransferNoneToTeam(transfer);
        else if (transfer.getOldTeam().equals("None") && transfer.getNewTeamPosition().equals("(Coach)"))
            sendMessageService.sendMessageTransferNoneToTeamCoach(transfer);
        else if (transfer.getOldTeam().equals("Retired") && Strings.isEmpty(transfer.getNewTeamPosition()))
            sendMessageService.sendMessageTransferRetiredToTeam(transfer);
        else if (transfer.getOldTeam().equals("Retired") && transfer.getNewTeamPosition().equals("(Coach)"))
            sendMessageService.sendMessageTransferRetiredToTeamCoach(transfer);
        else if (transfer.getNewTeam().equals("None"))
            sendMessageService.sendMessageTransferFromTeamToNone(transfer);
        else if (transfer.getNewTeamPosition().equals("(Inactive)"))
            sendMessageService.sendMessageFromTeamToInactive(transfer);
        else
            sendMessageService.sendMessageServiceFromTeamToTeam(transfer);
    }


}
