package com.andriyklus.dota2.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "gameinside_last_post")
public class GameinsideNewsPost {

    @Id
    private String id;
    private String tags;
    private String header;
    private String imageUrl;
    private String body;
    private String newsUrl;


}
