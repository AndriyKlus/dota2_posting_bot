package com.andriyklus.dota2.parcer;

import com.andriyklus.dota2.domain.Match;
import com.andriyklus.dota2.domain.Team;
import com.andriyklus.dota2.domain.Tournament;
import com.andriyklus.dota2.service.db.TeamService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LiquipediaParser {

    private final TeamService teamService;

    public LiquipediaParser(TeamService teamService) {
        this.teamService = teamService;
    }

    public List<Match> parseDayMatches() {
        System.setProperty("webdriver.chrome.driver", "selenium\\chromedriver.exe");
        WebDriver webDriver = new ChromeDriver();
        webDriver.get("https://liquipedia.net/dota2/Liquipedia:Upcoming_and_ongoing_matches");
        return parseMatches(webDriver);
    }

    private List<Match> parseMatches(WebDriver webDriver) {
        List<Match> matches = new ArrayList<>();
        List<WebElement> matchesBoxes = webDriver.findElements(By.className("infobox_matches_content"));

        for(WebElement matchBox : matchesBoxes) {
            parseUpcomingMatch(matchBox).ifPresent(matches::add);
        }

        webDriver.close();
        return matches;
    }

    private Optional<Match> parseUpcomingMatch(WebElement matchBox) {

        if(filterStartedMatch(matchBox))
            return Optional.empty();

        String teamOneName = matchBox.findElement(By.className("team-left")).getText();
        String teamTwoName = matchBox.findElement(By.className("team-right")).getText();

        if(filterUncertainMatches(teamOneName, teamTwoName) || !filterUkrainianTeams(teamOneName, teamTwoName))
            return Optional.empty();

        String tournament = matchBox.findElement(By.className("tournament-text")).getText();
        String time;
        try {
            time = formatTime(matchBox.findElement(By.className("timer-object-countdown-only")).getText()).orElseThrow();
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

    private boolean filterStartedMatch(WebElement matchBox) {
        return !matchBox.findElement(By.className("versus")).getText().contains("vs");
    }

    private boolean filterUncertainMatches(String teamOneName, String teamTwoName) {
        return teamOneName.equals("TBD") || teamTwoName.equals("TBD");
    }

    private boolean filterUkrainianTeams(String teamOneName, String teamTwoName) {
        List<String> ukrainianTeams = teamService.getUkrainianTeams().stream().map(Team::getName).toList();
        return ukrainianTeams.contains(teamOneName) || ukrainianTeams.contains(teamTwoName);
    }

    private Optional<String> formatTime(String timeString) {
        String[] parts = timeString.split("\\s+");
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        for (String part : parts) {
            if (part.endsWith("h")) {
                hours = Integer.parseInt(part.substring(0, part.length() - 1));
            } else if (part.endsWith("m")) {
                minutes = Integer.parseInt(part.substring(0, part.length() - 1));
            } else if (part.endsWith("s")) {
                seconds = Integer.parseInt(part.substring(0, part.length() - 1));
            }
        }

        if(hours > 15)
            return Optional.empty();

        LocalTime currentTime = LocalTime.now();

        LocalTime resultTime = currentTime.plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return Optional.of(resultTime.format(formatter));
    }

}
