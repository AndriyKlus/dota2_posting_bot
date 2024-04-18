package com.andriyklus.dota2.service.db;

import com.andriyklus.dota2.domain.Quest;
import com.andriyklus.dota2.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;

    public Optional<Quest> getQuest() {
        return questRepository.findAll().stream().findAny();
    }

    public void deleteQuestById(String id) {
        questRepository.deleteById(id);
    }

}
