package com.andriyklus.dota2.telegram.messagesender;

import com.andriyklus.dota2.telegram.GameinsideTelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Service
public class MessageSenderImpl implements MessageSender {

    private GameinsideTelegramBot gameinsideTelegramBot;


    @Override
    public void sendPhoto(SendPhoto sendPhoto){
        try{
            gameinsideTelegramBot.execute(sendPhoto);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }


    @Autowired
    public void setWinnerGuessBot(GameinsideTelegramBot gameinsideTelegramBot) {
        this.gameinsideTelegramBot = gameinsideTelegramBot;
    }
}
