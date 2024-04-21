package com.andriyklus.dota2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection =  "ukrainian_team")
public class UkrainianTeam {
    @Id
    private String id;
    private String name;
    private List<String> possibleNames;

}
