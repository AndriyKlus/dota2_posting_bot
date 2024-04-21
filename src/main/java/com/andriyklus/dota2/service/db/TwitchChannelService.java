package com.andriyklus.dota2.service.db;

import com.andriyklus.dota2.domain.TwitchChannel;
import com.andriyklus.dota2.repository.TwitchChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TwitchChannelService {

    private final TwitchChannelRepository twitchChannelRepository;

    public List<TwitchChannel> getAllTwitchChannels() {
        return twitchChannelRepository.findAll();
    }

}
