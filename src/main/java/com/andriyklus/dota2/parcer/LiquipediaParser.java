package com.andriyklus.dota2.parcer;

import com.andriyklus.dota2.domain.Match;
import com.andriyklus.dota2.domain.Team;
import com.andriyklus.dota2.domain.Tournament;
import com.andriyklus.dota2.service.db.TeamService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

@Service
public class LiquipediaParser {

    private static final String MATCHES_PAGE_URL = "https://liquipedia.net/dota2/Liquipedia:Upcoming_and_ongoing_matches";


    private final TeamService teamService;

    public LiquipediaParser(TeamService teamService) {
        this.teamService = teamService;
    }

    public List<Match> parseDayMatches() {
        Document matchesPage;
        try {
            matchesPage = Jsoup.parse(new URL(MATCHES_PAGE_URL), 30000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return parseMatches(matchesPage);
    }

    private List<Match> parseMatches(Document matchesPage) {
        List<Match> matches = new ArrayList<>();
        Elements matchesBoxes = matchesPage.getElementsByClass("infobox_matches_content");
        List<Element> elements = matchesBoxes.subList(0, matchesBoxes.size()/4);

        for(Element element : elements) {
            parseUpcomingMatch(element).ifPresent(matches::add);
        }
        return matches;
    }

    private Optional<Match> parseUpcomingMatch(Element matchBox) {

        if(filterStartedMatch(matchBox))
            return Optional.empty();

        String teamOneName = matchBox.getElementsByClass("team-left").get(0).text();
        String teamTwoName = matchBox.getElementsByClass("team-right").get(0).text();

        if(filterUncertainMatches(teamOneName, teamTwoName) || !filterUkrainianTeams(teamOneName, teamTwoName))
            return Optional.empty();

        String tournament = matchBox.getElementsByClass("tournament-text").get(0).text();
        String time;
        try {
            String p = matchBox.getElementsByClass("timer-object-countdown-only").get(0).text();
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
                .build());
    }

    private boolean filterStartedMatch(Element matchBox) {
        return !matchBox.getElementsByClass("versus").get(0).text().contains("vs");
    }

    private boolean filterUncertainMatches(String teamOneName, String teamTwoName) {
        return teamOneName.equals("TBD") || teamTwoName.equals("TBD");
    }

    private boolean filterUkrainianTeams(String teamOneName, String teamTwoName) {
        List<String> ukrainianTeams = teamService.getUkrainianTeams().stream().map(Team::getName).toList();
        return ukrainianTeams.contains(teamOneName) || ukrainianTeams.contains(teamTwoName);
    }

    private Optional<String> formatTime(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy - HH:mm z", Locale.ENGLISH);

        LocalDateTime parseDateTime = LocalDateTime.parse(dateTimeString, formatter);

        LocalDateTime currentDateTime = LocalDateTime.now();

        if(currentDateTime.getDayOfMonth() != parseDateTime.getDayOfMonth())
            return Optional.empty();

        LocalTime resultTime = parseDateTime.toLocalTime().plusHours(2);


        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");
        return Optional.of(resultTime.format(formatter2));
    }

}
