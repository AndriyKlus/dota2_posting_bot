package com.andriyklus.dota2.parcer;

import com.andriyklus.dota2.domain.GameinsideNewsPost;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;


public class GameInsideParser {

    private static final String PAGE_URL = "https://gameinside.ua/news/";

    public static List<GameinsideNewsPost> parseDota2AndCS2News() {
        Document matchesPage;
        try {
            matchesPage = Jsoup.parse(new URL(PAGE_URL), 30000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Elements news = matchesPage.getElementsByClass("newsList__article");
        return parseNews(news);
    }

    private static List<GameinsideNewsPost> parseNews(Elements news) {
        return news.
                stream()
                .map(GameInsideParser::parseNews)
                .filter(gameinsideNewsPost -> Strings.isNotEmpty(gameinsideNewsPost.getImageUrl()))
                .collect(Collectors.toList());
    }

    private static GameinsideNewsPost parseNews(Element news) {
        String header = news.getElementsByClass("newsList__textWrap").get(0)
                .getElementsByTag("h2").get(0).text();
        String tags = news.getElementsByClass("cat-links").get(0)
                .getElementsByTag("a").stream().map(Element::text).filter(tag -> !tag.contains(" ")).collect(Collectors.joining(" #"));
        String body = news.getElementsByClass("newsList__text").get(0).
                getElementsByTag("p").get(0).text();
        String imageUrlStyle = news.getElementsByClass("newsList__imageWrap").get(0).attr("style");
        String imageUrl = parseImageStyle(imageUrlStyle);
        String newsUrl = news.getElementsByClass("newsList__textWrap").get(0)
                .getElementsByTag("h2").get(0).getElementsByTag("a").get(0).attr("href");
        return GameinsideNewsPost.builder()
                .header(header)
                .tags(tags)
                .body(body)
                .imageUrl(imageUrl)
                .newsUrl(newsUrl)
                .build();
    }

    private static String parseImageStyle(String style) {
        int firstIndex = style.indexOf("https:");
        int secondIndex = style.indexOf("') ");
        if(firstIndex < 1 || secondIndex < 1)
            return "";
        return style.substring(firstIndex, secondIndex);
    }


}
