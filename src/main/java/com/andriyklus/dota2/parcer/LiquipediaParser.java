package com.andriyklus.dota2.parcer;

import com.andriyklus.dota2.domain.*;
import com.andriyklus.dota2.service.db.TransferService;
import com.andriyklus.dota2.service.db.UkrainianTeamService;
import com.andriyklus.dota2.util.RussianFilter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.andriyklus.dota2.util.FlagConverterUtil.getFlag;

@Service
@RequiredArgsConstructor
public class LiquipediaParser {

    private static final String MATCHES_PAGE_URL = "https://liquipedia.net/dota2/Liquipedia:Upcoming_and_ongoing_matches";
    private static final String MATCHES_STATS_URL = "https://liquipedia.net/dota2/Special:Stream/twitch/";
    private static final String TRANSFERS_PAGE_URL = "https://liquipedia.net/dota2/Portal:Transfers";
    private static final String MAIN_PAGE_URL = "https://liquipedia.net/dota2/Main_Page";
    private static final String LIQUIPEDIA_URL = "https://liquipedia.net";

    private final UkrainianTeamService teamService;
    private final TransferService transferService;
    private final RussianFilter russianFilter;

    private final Logger logger = LoggerFactory.getLogger(LiquipediaParser.class);

    public List<Match> parseDayMatches() {
        Document matchesPage;
        try {
            matchesPage = Jsoup.parse(new URL(MATCHES_PAGE_URL), 30000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Match> parsedMatches = parseMatches(matchesPage);

        if (parsedMatches.size() > 0) {
            logger.info("Parsed day matches from Liquipedia: " + parsedMatches);
        }
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
        String teamOneName = formatTeamName(matchBox.getElementsByClass("team-left").get(0).attr("title"));
        String teamTwoName = formatTeamName(matchBox.getElementsByClass("team-right").get(0).attr("title"));
        int matchFormat;
        try {
            matchFormat = Integer.parseInt(matchBox.getElementsByClass("versus-lower").get(0).text().substring(3, 4));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        if (filterUncertainMatches(teamOneName, teamTwoName) || filterUkrainianTeams(teamOneName, teamTwoName))
            return Optional.empty();

        Tournament tournament = parseTournament(matchBox);

        String time;
        try {
            time = formatTime(matchBox.getElementsByClass("timer-object-countdown-only").get(0).text()).orElseThrow();
        } catch (Exception e) {
            logger.error("Couldn't parse time");
            return Optional.empty();
        }

        return Optional.ofNullable(Match.builder()
                .teamOne(Team.builder().
                        name(teamOneName)
                        .build())
                .teamTwo(Team.builder()
                        .name(teamTwoName)
                        .build())
                .tournament(tournament)
                .time(time)
                .format(matchFormat)
                .build());
    }

    private String formatTeamName(String teamName) {
        if (teamName.contains("(page does not exist)"))
            return teamName.substring(0, teamName.indexOf("(page does not exist)"));
        return teamName;
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy - HH:mm z", Locale.ENGLISH);
        LocalDateTime parseDateTime = LocalDateTime.parse(dateTimeString, formatter);
        LocalDateTime currentDateTime = LocalDateTime.now();

        if (currentDateTime.getDayOfMonth() != parseDateTime.getDayOfMonth())
            return Optional.empty();

        LocalTime resultTime = parseDateTime.toLocalTime().plusHours(1);

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
        if (startedMatches.size() > 0) {
            logger.info("Parsed started matches from Liquipedia: " + startedMatches);
        }

        return startedMatches;
    }

    private Optional<Match> parseStartedMatch(Element matchBox) {

        String teamOneName = formatTeamName(matchBox.getElementsByClass("team-left").get(0).attr("title"));
        String teamTwoName = formatTeamName(matchBox.getElementsByClass("team-left").get(0).attr("title"));
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

        Tournament tournament = parseTournament(matchBox);

        Match match = Match.builder()
                .teamOne(Team.builder()
                        .name(teamOneName)
                        .score(firstTeamScore)
                        .build())
                .teamTwo(Team.builder()
                        .name(teamTwoName)
                        .score(secondTeamScore)
                        .build())
                .tournament(tournament)
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

    private Tournament parseTournament(Element matchBox) {
        String tournament = matchBox.getElementsByClass("tournament-text").get(0).text();
        String tournamentLink = matchBox.getElementsByClass("tournament-text").get(0).attr("href");
        return Tournament.builder()
                .name(tournament)
                .link(LIQUIPEDIA_URL + tournamentLink)
                .build();
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
        match.getTeamOne().setPlayers(tables.get(0).getElementsByAttributeValue("id", "player").stream()
                .map(Element::text)
                .map(player -> {
                    if (player.contains("_(Ukrainian_player)"))
                        return player.substring(0, player.length() - 19);
                    else
                        return player;
                })
                .collect(Collectors.toList()));
        match.getTeamTwo().setPlayers(tables.get(1).getElementsByAttributeValue("id", "player").stream()
                .map(Element::text)
                .map(player -> {
                    if (player.contains("_(Ukrainian_player)"))
                        return player.substring(0, player.length() - 19);
                    else
                        return player;
                })
                .collect(Collectors.toList()));
    }

    public List<Match> parseEndedMatches() {
        Document matchesPage;
        try {
            matchesPage = Jsoup.parse(new URL(MATCHES_PAGE_URL), 30000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return parseEndedMatches(matchesPage);
    }

    private List<Match> parseEndedMatches(Document matchesPage) {
        Elements endedMatchesBoxes = matchesPage.getElementsByAttributeValue("data-toggle-area-content", "3")
                .get(2)
                .getElementsByTag("table");

        return endedMatchesBoxes.stream()
                .map(this::parseEndedMatch)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Match parseEndedMatch(Element endedMatchBox) {
        String firstTeamName = formatTeamName(endedMatchBox.getElementsByClass("team-left").get(0).attr("title"));
        String secondTeamName = formatTeamName(endedMatchBox.getElementsByClass("team-left").get(0).attr("title"));

        if (filterUkrainianTeams(firstTeamName, secondTeamName))
            return null;

        int firstTeamScore, secondTeamScore;
        try {
            firstTeamScore = Integer.parseInt(endedMatchBox.getElementsByClass("versus").get(0)
                    .getElementsByTag("div").get(0).text().substring(0, 1));
            secondTeamScore = Integer.parseInt(endedMatchBox.getElementsByClass("versus").get(0)
                    .getElementsByTag("div").get(0).text().substring(2, 3));
        } catch (NumberFormatException e) {
            //logger.error("One of the team forfeited");
            return null;
        }
        Tournament tournament = parseTournament(endedMatchBox);
        String time;
        try {
            time = formatTime(endedMatchBox.getElementsByClass("match-countdown").get(0).text()).orElseThrow();
        } catch (Exception e) {
            return null;
        }

        return Match.builder()
                .teamOne(Team.builder()
                        .name(firstTeamName)
                        .score(firstTeamScore)
                        .build())
                .teamTwo(Team.builder()
                        .name(secondTeamName)
                        .score(secondTeamScore)
                        .build())
                .tournament(tournament)
                .time(time)
                .build();
    }


    public List<Transfer> parseTransfers() {
        Document matchesPage;
        try {
            matchesPage = Jsoup.parse(new URL(TRANSFERS_PAGE_URL), 30000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return parseTransfers(matchesPage);
    }

    private List<Transfer> parseTransfers(Document matchPage) {
        Elements transferBoxes = matchPage.getElementsByClass("divRow");
        List<Transfer> transfers = new ArrayList<>();
        for (Element element : transferBoxes) {
            Optional<Transfer> optionalTransfer = parseTransfer(element);
            if (optionalTransfer.isPresent()) {
                transfers.add(optionalTransfer.get());
            } else {
                break;
            }
        }
        return transfers.stream()
                .filter(transfer -> Strings.isNotEmpty(transfer.getOldTeam()))
                .filter(transfer -> Objects.nonNull(transfer.getNewTeamPosition()))
                .toList();
    }

    private Optional<Transfer> parseTransfer(Element element) {
        String newsLink = parseNewsLink(element.getElementsByClass("Ref").get(0));
        if (isOldPost(newsLink))
            return Optional.empty();

        List<Player> players = parseTransferPlayers(element.getElementsByClass("Name").get(0));
        String oldTeam = parseOldTeam(element.getElementsByClass("OldTeam").get(0));
        String newTeam = parseNewTeam(element.getElementsByClass("NewTeam").get(0));
        String position = parseNewTeamPosition(element.getElementsByClass("NewTeam").get(0));
        return Optional.of(Transfer.builder()
                .players(players)
                .oldTeam(oldTeam)
                .newTeam(newTeam)
                .newTeamPosition(position)
                .newsLink(newsLink)
                .build());
    }

    private boolean isOldPost(String newsLink) {
        if (transferService.getLastTransfer().isEmpty())
            return false;
        return transferService.getLastTransfer().get().getNewsLink().equals(newsLink);
    }

    private List<Player> parseTransferPlayers(Element element) {
        List<Player> players = new ArrayList<>();
        Elements playersTags = element.getElementsByTag("a");
        for (int w = 0; w < playersTags.size(); w = w + 2) {
            players.add(Player.builder()
                    .flag(getFlag(playersTags.get(w).attr("title")))
                    .name(playersTags.get(w + 1).text())
                    .build());
        }
        return players;
    }

    private String parseOldTeam(Element element) {
        try {
            String value = element.getElementsByTag("span").get(0).text();
            if (value.equals("None"))
                return "None";
            if (value.equals("Retired"))
                return "Retired";
        } catch (Exception ignored) {
        }

        if (element.getElementsByClass("team-template-team-icon").size() > 1)
            return "";

        try {
            String value = element.getElementsByTag("span").get(element.getElementsByTag("span").size() - 1).text();
            if (value.equals("(Inactive)") || value.equals("(Coach)") || value.equals("(Manager)"))
                return "";
        } catch (Exception ignored) {
        }

        return element.getElementsByClass("team-template-team-icon")
                .get(0)
                .attr("data-highlightingclass");
    }

    private String parseNewTeam(Element element) {
        try {
            String value = element.getElementsByTag("span").get(0).text();
            if (value.equals("None"))
                return "None";
            if (value.equals("Retired"))
                return "Retired";
        } catch (Exception ignored) {
        }
        return element.getElementsByClass("team-template-team-icon")
                .get(0)
                .attr("data-highlightingclass");
    }

    private String parseNewTeamPosition(Element element) {
        try {
            String value = element.getElementsByTag("span").get(element.getElementsByTag("span").size() - 1).text();
            if (value.equals("(Inactive)") || value.equals("(Coach)"))
                return value;
            if (value.equals("(Analyst)") || value.equals("(Manager)"))
                return null;
        } catch (Exception ignored) {
        }
        return "";
    }

    private String parseNewsLink(Element element) {
        return element.getElementsByTag("a").get(0).attr("href");
    }

    public DayInDota parseDayInDota() {
        Document dota2Page;
        try {
            dota2Page = Jsoup.parse(new URL(MAIN_PAGE_URL), 30000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return parseDayInDota(dota2Page);
    }

    private DayInDota parseDayInDota(Document document) {
        Element thisDayBox = document.getElementsByAttributeValue("id", "this-day-facts").get(0);
        Map<Tournament, Team> tournamentWinners = new LinkedHashMap<>();
        parseTournamentWinners(thisDayBox, tournamentWinners);
        List<Player> players = parsePlayers(thisDayBox);
        return DayInDota.builder()
                .tournamentWinners(tournamentWinners)
                .playersBirths(players)
                .build();
    }

    private void parseTournamentWinners(Element thisDayBox, Map<Tournament, Team> tournamentWinners) {
        try {
            Elements yearBoxes = thisDayBox.getElementsByTag("h4");
            for (int w = 0; w < yearBoxes.size(); w++) {
                String year = thisDayBox.getElementsByTag("h4").get(w).text();
                Element tournamentBox = thisDayBox.getElementsByTag("ul").get(w);
                parseTournamentWinner(tournamentBox, year, tournamentWinners);
            }
        } catch (Exception ignored) {
        }
    }

    private void parseTournamentWinner(Element element, String year, Map<Tournament, Team> tournamentWinners) {
        String tournamentName = element.getElementsByTag("a").get(1).text();
        String tournamentLink = LIQUIPEDIA_URL + element.getElementsByTag("a").get(1).attr("href");
        String teamName = element.getElementsByClass("team-template-text").get(0)
                .getElementsByTag("a").get(0).text();
        String teamLink = LIQUIPEDIA_URL + element.getElementsByClass("team-template-text").get(0)
                .getElementsByTag("a").get(0).attr("href");

        tournamentWinners.put(Tournament.builder()
                        .name(tournamentName)
                        .year(year)
                        .link(tournamentLink)
                        .build(),
                Team.builder()
                        .name(teamName)
                        .link(teamLink)
                        .build());
    }

    private List<Player> parsePlayers(Element thisDayBox) {
        try {
            Elements pBoxes = thisDayBox.getElementsByTag("p");
            for (Element pBox : pBoxes) {
                if (pBox.text().equals("There are no birthdays today"))
                    return Collections.emptyList();
            }
        } catch (Exception ignored) {
        }
        int num = 0;
        try {
            num = thisDayBox.getElementsByTag("h4").size();
        } catch (Exception ignored) {
        }

        return thisDayBox.getElementsByTag("ul").get(num)
                .getElementsByTag("li").stream()
                .map(this::parsePlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Player parsePlayer(Element liBox) {
        String playerName = liBox.getElementsByClass("inline-player").get(0)
                .getElementsByTag("a")
                .get(0)
                .text();
        String playerLink = LIQUIPEDIA_URL + liBox.getElementsByClass("inline-player").get(0)
                .getElementsByTag("a")
                .get(0)
                .attr("href");
        String playerFlag = liBox.getElementsByTag("img").get(0).attr("alt");
        if (!russianFilter.isNotRussianPlayer(playerFlag)) {
            return null;
        }
        String yearOfBirth = liBox.text().substring(liBox.text().indexOf(" - ") + 3, liBox.text().indexOf(" ("));
        return Player.builder()
                .flag(getFlag(playerFlag))
                .name(playerName)
                .yearOfBirth(yearOfBirth)
                .link(playerLink)
                .build();
    }

}
