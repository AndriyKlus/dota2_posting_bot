package com.andriyklus.dota2.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Player {

    String name;
    String flag;

}
