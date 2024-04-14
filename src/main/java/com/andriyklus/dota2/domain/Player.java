package com.andriyklus.dota2.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Player {

    private String name;
    private String flag;
    private String yearOfBirth;
    private String link;

}
