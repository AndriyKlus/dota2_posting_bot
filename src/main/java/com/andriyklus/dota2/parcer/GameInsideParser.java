package com.andriyklus.dota2.parcer;

import com.andriyklus.dota2.domain.GameinsideNewsPost;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameInsideParser {

    private static final String PAGE_URL = "https://gameinside.ua/news/";

    private static final Logger logger = LoggerFactory.getLogger(GameInsideParser.class);


    public List<GameinsideNewsPost> parseDOTA2News() {
        Document matchesPage;
        try {
            matchesPage = Jsoup.parse(new URL(PAGE_URL), 30000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Elements news = matchesPage.getElementsByClass("newsList__article");
        return parseNews(news);
    }

    private List<GameinsideNewsPost> parseNews(Elements news) {
        return news.
                stream()
                .map(this::parseNews)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(gameinsideNewsPost -> Strings.isNotEmpty(gameinsideNewsPost.getImageUrl()))
                .filter(gameinsideNewsPost -> gameinsideNewsPost.getTags().contains("DoTA2") || gameinsideNewsPost.getTags().contains("CS"))
                .collect(Collectors.toList());
    }

    private Optional<GameinsideNewsPost> parseNews(Element news) {
        String imageUrlStyle = news.getElementsByClass("newsList__imageWrap").get(0).attr("style");
        String imageUrl = parseImageStyle(imageUrlStyle);
        if(Strings.isEmpty(imageUrl))
            return Optional.empty();

        String id = news.attr("id");
        String header = news.getElementsByClass("newsList__textWrap").get(0)
                .getElementsByTag("h2").get(0).text();
        String tags = news.getElementsByClass("cat-links").get(0)
                .getElementsByTag("a").stream().map(Element::text).filter(tag -> !tag.contains(" ")).collect(Collectors.joining(" #"));
        String body = news.getElementsByClass("newsList__text").get(0).
                getElementsByTag("p").get(0).text();
        String newsUrl = news.getElementsByClass("newsList__textWrap").get(0)
                .getElementsByTag("h2").get(0).getElementsByTag("a").get(0).attr("href");
        return Optional.of(GameinsideNewsPost.builder()
                .id(id)
                .header(header)
                .tags(tags)
                .body(body)
                .imageUrl(imageUrl)
                .newsUrl(newsUrl)
                .build());
    }

    private String parseImageStyle(String style) {
        try {
            return style.substring(style.indexOf("https:"), style.indexOf("') "));
        } catch (IndexOutOfBoundsException e) {
            logger.error("Couldn't parse image url");
        }
        return "";
    }


}
