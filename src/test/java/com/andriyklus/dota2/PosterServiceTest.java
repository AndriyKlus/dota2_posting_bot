package com.andriyklus.dota2;

import com.andriyklus.dota2.domain.Match;
import com.andriyklus.dota2.domain.Team;
import com.andriyklus.dota2.domain.Tournament;
import com.andriyklus.dota2.parcer.LiquipediaParser;
import com.andriyklus.dota2.service.PosterService;
import com.andriyklus.dota2.service.db.MatchService;
import com.andriyklus.dota2.telegram.service.SendMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class PosterServiceTest {

    /*@InjectMocks
    private PosterService posterService;
    @Mock
    private LiquipediaParser liquipediaParser;
    @Mock
    private SendMessageService sendMessageService;
    @Mock
    private MatchService matchService;

    @BeforeEach
    public void setup() {
        posterService = new PosterService(sendMessageService, liquipediaParser, null, matchService, null, null);
    }

    @Test
    public void testPostingNewStartedMatch() {
        List<Match> startedMatches = new ArrayList<>();
        when(matchService.getStartedMatches()).thenReturn(startedMatches);

        List<Match> parsedMatches = new ArrayList<>();
        parsedMatches.add(getStartedMatch());
        when(liquipediaParser.parseStartedMatches()).thenReturn(parsedMatches);

        posterService.postStartedMatch();

        verify(sendMessageService, times(1)).postStartedMatch(parsedMatches.get(0));
    }

    @Test
    public void testPostingExistingStartedMatch() {
        List<Match> startedMatches = new ArrayList<>();
        Match match = getStartedMatch();
        match.setId("3424322");
        startedMatches.add(match);
        when(matchService.getStartedMatches()).thenReturn(startedMatches);

        List<Match> parsedMatches = new ArrayList<>();
        parsedMatches.add(getStartedMatch());
        when(liquipediaParser.parseStartedMatches()).thenReturn(parsedMatches);

        posterService.postStartedMatch();

        verify(sendMessageService, times(0)).postStartedMatch(parsedMatches.get(0));
    }

    @Test
    public void testPostingOngoingMatch() {
        List<Match> startedMatches = new ArrayList<>();
        Match match = getStartedMatch();
        match.setId("3424322");
        startedMatches.add(match);
        when(matchService.getStartedMatches()).thenReturn(startedMatches);

        List<Match> parsedMatches = new ArrayList<>();
        parsedMatches.add(getOngoingMatch());
        when(liquipediaParser.parseStartedMatches()).thenReturn(parsedMatches);

        posterService.postOngoingMatch();

        verify(matchService, times(1)).save(parsedMatches.get(0));
    }

    @Test
    public void testPostingOngoingMatchNotUpdated() {
        List<Match> startedMatches = new ArrayList<>();
        Match match = getOngoingMatch();
        match.setId("3424322");
        startedMatches.add(match);
        when(matchService.getStartedMatches()).thenReturn(startedMatches);

        List<Match> parsedMatches = new ArrayList<>();
        parsedMatches.add(getOngoingMatch());
        when(liquipediaParser.parseStartedMatches()).thenReturn(parsedMatches);

        posterService.postOngoingMatch();

        verify(matchService, times(0)).save(parsedMatches.get(0));
    }

    private Match getStartedMatch() {
        return Match.builder()
                .teamOne(Team.builder()
                        .name("Monte")
                        .score(0)
                        .build())
                .teamTwo(Team.builder()
                        .name("FAZE")
                        .score(0)
                        .build())
                .tournament(Tournament.builder()
                        .name("Some team")
                        .build())
                .time("123312")
                .format(3)
                .build();
    }

    private Match getOngoingMatch() {
        return Match.builder()
                .teamOne(Team.builder()
                        .name("Monte")
                        .score(1)
                        .build())
                .teamTwo(Team.builder()
                        .name("FAZE")
                        .score(0)
                        .build())
                .tournament(Tournament.builder()
                        .name("Some team")
                        .build())
                .time("123312")
                .format(3)
                .build();
    }


}
