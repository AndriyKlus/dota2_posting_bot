package com.andriyklus.dota2.telegram.service;


import com.andriyklus.dota2.domain.NewsPost;
import com.andriyklus.dota2.telegram.messagesender.MessageSender;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;


@Service
public class SendMessageService {

    private final MessageSender messageSender;


    public SendMessageService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void post(NewsPost newsPost) {
        var message = SendPhoto.builder()
                .chatId(-1002029412738L)
                .caption(formatMessage(newsPost))
                .photo(new InputFile(newsPost.getImageUrl()))
                .parseMode(ParseMode.HTML)
                .build();

        messageSender.sendPhoto(message);

    }

    private static String formatMessage(NewsPost newsPost) {
        return "<b>" +
                newsPost.getHeader() +
                "</b>" +
                "\n\n" +
                newsPost.getBody() +
                "\n" +
                "<a href=\"" +
                newsPost.getNewsUrl() +
                "\">" +
                "Прочитати повну статтю" +
                "</a>" +
                "\n\n" +
                "#" +
                newsPost.getTags();
    }


}
