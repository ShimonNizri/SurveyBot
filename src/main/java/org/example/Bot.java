package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Bot extends TelegramLongPollingBot {
    BotManager botManager;
    public Bot(){

    }
    public void setBotManager(BotManager botManager){
        this.botManager = botManager;
    }
    @Override
    public void onUpdateReceived(Update update) {
        botManager.messageManagement(update);
    }
    @Override
    public String getBotUsername() {
        return "@SurveyDevelopmentBot";
    }
    public String getBotToken(){
        return "7426129452:AAFPEggK4wDVwCc_NQZ4nCjzdvv-nX3LDUc";
    }
}