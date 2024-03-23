package com.andriyklus.dota2.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class Team {

    @Id
    private String id;
    private String name;

}
