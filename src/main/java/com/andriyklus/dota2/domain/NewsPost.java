package com.andriyklus.dota2.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsPost {

    private String tags;
    private String header;
    private String imageUrl;
    private String body;
    private String newsUrl;


}
