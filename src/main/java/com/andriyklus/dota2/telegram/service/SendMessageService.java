package com.andriyklus.dota2.telegram.service;


import com.andriyklus.dota2.domain.Match;
import com.andriyklus.dota2.domain.GameinsideNewsPost;
import com.andriyklus.dota2.telegram.messagesender.MessageSender;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


@Service
public class SendMessageService {

    private final MessageSender messageSender;


    public SendMessageService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void postGameInsideNews(GameinsideNewsPost gameinsideNewsPost) {
        var message = SendPhoto.builder()
                .chatId(-1002029412738L)
                //.chatId(358029493L)
                .caption(formatMessageForGameinsideNews(gameinsideNewsPost))
                .photo(new InputFile(gameinsideNewsPost.getImageUrl()))
                .parseMode(ParseMode.HTML)
                .build();

        messageSender.sendPhoto(message);

    }

    private String formatMessageForGameinsideNews(GameinsideNewsPost gameinsideNewsPost) {
        return "<b>" +
                gameinsideNewsPost.getHeader() +
                "</b>" +
                "\n\n" +
                gameinsideNewsPost.getBody() +
                "\n" +
                "<a href=\"" +
                gameinsideNewsPost.getNewsUrl() +
                "\">" +
                "Прочитати повну статтю" +
                "</a>" +
                "\n\n" +
                "#" +
                gameinsideNewsPost.getTags();
    }

    public void postTodayGames(List<Match> matches) {
        var message = SendMessage.builder()
                .chatId(-1002029412738L)
                //.chatId(358029493L)
                .text(formatMessageForTodayGames(matches))
                .parseMode(ParseMode.HTML)
                .build();

        messageSender.sendMessage(message);
    }

    public void postStartedMatch(Match match) {
        var message = SendMessage.builder()
                .chatId(-1002029412738L)
                //.chatId(358029493L)
                .text(formatMessageForStartedMatch(match))
                .parseMode(ParseMode.HTML)
                .build();

        messageSender.sendMessage(message);
    }

    private String formatMessageForTodayGames(List<Match> matches) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\uD83D\uDCFA Матчі українських команд на ")
                .append(getFormattedDate())
                .append("\n\n");
        for(Match match : matches) {
            stringBuilder.append("\uD83D\uDFE2 Матч: <b>")
                    .append(match.getTeamOne().getName())
                    .append("</b> \uD83C\uDD9A ")
                    .append("<b>")
                    .append(match.getTeamTwo().getName())
                    .append("</b> (Bo")
                    .append(match.getFormat())
                    .append(")\n")
                    .append("\uD83C\uDFC6 Турнір: <b>")
                    .append(match.getTournament().getName())
                    .append("</b>\n")
                    .append("⏰ Початок: ")
                    .append(match.getTime())
                    .append("\n\n");
        }
        return stringBuilder.toString();
    }

    private String getFormattedDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM", new Locale("uk", "UA"));

        return currentDate.format(formatter);
    }

    private String formatMessageForStartedMatch(Match match) {
        return new StringBuilder().append("⏰ Розпочинається матч: <b>")
                .append(match.getTeamOne().getName())
                .append("</b> vs <b>")
                .append(match.getTeamTwo().getName())
                .append("</b> ")
                .append(match.getFormat())
                .append("\n\uD83C\uDFC6 Турнір: <b>")
                .append(match.getTournament().getName())
                .append("</b>\n")
                .append("\uD83D\uDC65 Склади команд\n<b>")
                .append(match.getTeamOne().getName())
                .append("</b>: ")
                .append(String.join(", ", match.getTeamOne().getPlayers()))
                .append("\n<b>")
                .append(match.getTeamTwo().getName())
                .append("</b>: ")
                .append(String.join(", ", match.getTeamTwo().getPlayers()))
                .toString();
    }


}
