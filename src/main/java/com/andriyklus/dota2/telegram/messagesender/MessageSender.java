package com.andriyklus.dota2.telegram.messagesender;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;


public interface MessageSender {

    void sendPhoto(SendPhoto sendPhoto);

    void sendMessage(SendMessage sendMessage);

}
