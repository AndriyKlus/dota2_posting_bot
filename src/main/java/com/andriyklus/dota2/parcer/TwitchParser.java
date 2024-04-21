package com.andriyklus.dota2.parcer;

import com.andriyklus.dota2.domain.TwitchChannel;
import com.andriyklus.dota2.domain.UkrainianTeam;
import com.andriyklus.dota2.service.db.TwitchChannelService;
import com.andriyklus.dota2.service.db.UkrainianTeamService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TwitchParser {

    private final TwitchChannelService twitchChannelService;
    private final UkrainianTeamService ukrainianTeamService;


    public List<TwitchChannel> parseStreamChannels(String firstTeamName, String secondTeamName) {
        List<TwitchChannel> twitchChannels = twitchChannelService.getAllTwitchChannels();
        return twitchChannels.stream()
                .filter(twitchChannel -> isChannelStreamingMatch(twitchChannel, firstTeamName, secondTeamName))
                .collect(Collectors.toList());
    }

    private boolean isChannelStreamingMatch(TwitchChannel twitchChannel, String firstTeamName, String secondTeamName) {
        Document twitchPage;
        try {
            twitchPage = Jsoup.parse(new URL(twitchChannel.getUrl()), 30000);
            String streamTitle = twitchPage.getElementsByAttributeValue("property", "og:description").get(0)
                    .attr("content");
            return streamTitleContainsName(streamTitle, firstTeamName, secondTeamName);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean streamTitleContainsName(String streamTitle, String firstTeamName, String secondTeamName) {
        Optional<UkrainianTeam> ukrainianTeam = ukrainianTeamService.getUkrainianTeam(firstTeamName);
        if(ukrainianTeam.isPresent()) {
            if (ukrainianTeam.get().getPossibleNames().stream()
                    .anyMatch(streamTitle::contains))
                return true;
        }
        ukrainianTeam = ukrainianTeamService.getUkrainianTeam(secondTeamName);
        return ukrainianTeam.map(team -> team.getPossibleNames().stream()
                .anyMatch(streamTitle::contains))
                .orElse(false);
    }


}
