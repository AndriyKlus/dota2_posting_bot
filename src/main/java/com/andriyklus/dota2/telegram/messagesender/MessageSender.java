package com.andriyklus.dota2.telegram.messagesender;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;


public interface MessageSender {

    void sendPhoto(SendPhoto sendPhoto);

}
