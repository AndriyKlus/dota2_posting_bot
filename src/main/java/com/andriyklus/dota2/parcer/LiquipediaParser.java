package com.andriyklus.dota2.parcer;

import com.andriyklus.dota2.domain.Match;
import com.andriyklus.dota2.domain.Team;
import com.andriyklus.dota2.domain.Tournament;
import com.andriyklus.dota2.domain.UkrainianTeam;
import com.andriyklus.dota2.service.db.UkrainianTeamService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiquipediaParser {

    private static final String MATCHES_PAGE_URL = "https://liquipedia.net/dota2/Liquipedia:Upcoming_and_ongoing_matches";
    private static final String MATCHES_STATS_URL = "https://liquipedia.net/dota2/Special:Stream/twitch/";

    private final UkrainianTeamService teamService;

    Logger logger = LoggerFactory.getLogger(LiquipediaParser.class);


    public List<Match> parseDayMatches() {
        Document matchesPage;
        try {
            matchesPage = Jsoup.parse(new URL(MATCHES_PAGE_URL), 30000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Match> parsedMatches = parseMatches(matchesPage);
        logger.info("Parsed day matches from Liquipedia: " + parsedMatches);
        return parsedMatches;
    }

    private List<Match> parseMatches(Document matchesPage) {
        List<Match> matches = new ArrayList<>();
        Elements matchesBoxes = matchesPage.getElementsByClass("infobox_matches_content");
        List<Element> elements = matchesBoxes.subList(0, matchesBoxes.size() / 4);

        for (Element element : elements) {
            parseUpcomingMatch(element).ifPresent(matches::add);
        }
        return matches;
    }

    private Optional<Match> parseUpcomingMatch(Element matchBox) {

        if (filterStartedMatch(matchBox))
            return Optional.empty();

        String teamOneName = matchBox.getElementsByClass("team-left").get(0).text();
        String teamTwoName = matchBox.getElementsByClass("team-right").get(0).text();
        int matchFormat;
        try {
            matchFormat = Integer.parseInt(matchBox.getElementsByClass("versus-lower").get(0).text().substring(3, 4));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        if (filterUncertainMatches(teamOneName, teamTwoName) || filterUkrainianTeams(teamOneName, teamTwoName))
            return Optional.empty();

        String tournament = matchBox.getElementsByClass("tournament-text").get(0).text();
        String time;
        try {
            time = formatTime(matchBox.getElementsByClass("timer-object-countdown-only").get(0).text()).orElseThrow();
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.ofNullable(Match.builder()
                .teamOne(Team.builder().
                        name(teamOneName)
                        .build())
                .teamTwo(Team.builder()
                        .name(teamTwoName)
                        .build())
                .tournament(Tournament.builder().name(tournament).build())
                .time(time)
                .format(matchFormat)
                .build());
    }

    private boolean filterStartedMatch(Element matchBox) {
        return !matchBox.getElementsByClass("versus").get(0).text().contains("vs");
    }

    private boolean filterUncertainMatches(String teamOneName, String teamTwoName) {
        return teamOneName.equals("TBD") || teamTwoName.equals("TBD");
    }

    private boolean filterUkrainianTeams(String teamOneName, String teamTwoName) {
        List<String> ukrainianTeams = teamService.getUkrainianTeams().stream().map(UkrainianTeam::getName).toList();
        return !ukrainianTeams.contains(teamOneName) && !ukrainianTeams.contains(teamTwoName);
    }

    private Optional<String> formatTime(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy - HH:mm z", Locale.ENGLISH);
        LocalDateTime parseDateTime = LocalDateTime.parse(dateTimeString, formatter);
        LocalDateTime currentDateTime = LocalDateTime.now();

        if (currentDateTime.getDayOfMonth() != parseDateTime.getDayOfMonth())
            return Optional.empty();

        LocalTime resultTime = parseDateTime.toLocalTime().plusHours(2);

        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");
        return Optional.of(resultTime.format(formatter2));
    }

    public List<Match> parseStartedMatches() {
        Document matchesPage;
        try {
            matchesPage = Jsoup.parse(new URL(MATCHES_PAGE_URL), 30000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return parseStartedMatches(matchesPage);
    }

    private List<Match> parseStartedMatches(Document matchesPage) {
        List<Match> startedMatches = new ArrayList<>();
        Elements matchesBoxes = matchesPage.getElementsByClass("infobox_matches_content");
        List<Element> elements = matchesBoxes.subList(0, matchesBoxes.size() / 4);

        for (Element element : elements) {
            if (!filterStartedMatch(element))
                break;

            parseStartedMatch(element).ifPresent(startedMatches::add);
        }
        logger.info("Parsed started matches from Liquipedia: " + startedMatches);
        return startedMatches;
    }

    private Optional<Match> parseStartedMatch(Element matchBox) {

        String teamOneName = matchBox.getElementsByClass("team-left").get(0).text();
        String teamTwoName = matchBox.getElementsByClass("team-right").get(0).text();
        int matchFormat;
        try {
            matchFormat = Integer.parseInt(matchBox.getElementsByClass("versus-lower").get(0).text().substring(3, 4));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        int firstTeamScore = Integer.parseInt(matchBox.getElementsByClass("versus").get(0)
                .getElementsByTag("div").get(0)
                .getElementsByTag("div").get(0).text().substring(0, 1));
        int secondTeamScore = Integer.parseInt(matchBox.getElementsByClass("versus").get(0)
                .getElementsByTag("div").get(0)
                .getElementsByTag("div").get(0).text().substring(2, 3));

        if (filterUncertainMatches(teamOneName, teamTwoName) || filterUkrainianTeams(teamOneName, teamTwoName))
            return Optional.empty();

        String tournament = matchBox.getElementsByClass("tournament-text").get(0).text();

        Match match = Match.builder()
                .teamOne(Team.builder()
                        .name(teamOneName)
                        .score(firstTeamScore)
                        .build())
                .teamTwo(Team.builder()
                        .name(teamTwoName)
                        .score(secondTeamScore)
                        .build())
                .tournament(Tournament.builder().name(tournament).build())
                .format(matchFormat)
                .build();
        try {
            String twitchChannel = matchBox.getElementsByClass("timer-object-countdown-only").get(0)
                    .attr("data-stream-twitch");
            parseMatchStats(twitchChannel, match);
        } catch (Exception e) {
            logger.error("Cannot parse players of teams");
        }
        return Optional.of(match);
    }

    private void parseMatchStats(String twitchChannel, Match match) {
        Document statsPage;
        try {
            statsPage = Jsoup.parse(new URL(MATCHES_STATS_URL + twitchChannel), 30000);
            parsePlayers(statsPage, match);
        } catch (IOException e) {
            logger.error("Couldn't reach url: " + MATCHES_STATS_URL + twitchChannel);
        }
    }

    private void parsePlayers(Document document, Match match) {
        Elements tables = document.getElementsByClass("wikitable");
        match.getTeamOne().setPlayers(tables.get(0).getElementsByAttributeValue("id", "player").stream().map(Element::text).collect(Collectors.toList()));
        match.getTeamTwo().setPlayers(tables.get(1).getElementsByAttributeValue("id", "player").stream().map(Element::text).collect(Collectors.toList()));
    }


}
