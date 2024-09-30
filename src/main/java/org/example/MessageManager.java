package org.example;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MessageManager {
    private Bot bot;

    public MessageManager(Bot bot){
        this.bot = bot;
    }

    public static InlineKeyboardMarkup createsFloatingButtons(List<List<String[]>> rows){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (List<String[]> row :rows){
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            for (String[] buttons :row){
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(buttons[0]);
                button.setCallbackData(buttons[1]);
                keyboardButtonsRow.add(button);
            }
            rowList.add(keyboardButtonsRow);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup createsKeyboardButtons(List<List<String>> rows){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true); // משנה את גודל המקלדת לפי הכפתורים
        keyboardMarkup.setOneTimeKeyboard(true); // המקלדת נעלמת לאחר הלחיצה על כפתור

        List<KeyboardRow> keyboard = new ArrayList<>();
        for (List<String> row :rows){
            KeyboardRow rowList = new KeyboardRow();
            for (String buttons :row){
                rowList.add(buttons);
            }
            keyboard.add(rowList);
        }
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }


    // פונקציה ליצירת כפתור מקלדת ולהוספתו לשורה חדשה
    public static void addKButtonToNewRow(List<List<String>> rows, String text) {
        addKButtonsToNewRow(rows, List.of(text));
    }
    // פונקציה ליצירת כפתורי מקלדת ולהוספתו לשורה חדשה
    public static void addKButtonsToNewRow(List<List<String>> rows, List<String> rowButtons) {
        List<String> row = new LinkedList<>(rowButtons);
        rows.add(row);
    }


    // פונקציה ליצירת כפתור צף ולהוספתו לשורה חדשה
    public static void addFButtonToNewRow(List<List<String[]>> rows, String text, String callbackData) {
        addFButtonsToNewRow(rows, List.<String[]>of(new String[]{text, callbackData}));
    }
    // פונקציה ליצירת כפתורים צפים ולהוספתו לשורה חדשה
    public static void addFButtonsToNewRow(List<List<String[]>> rows, List<String[]> rowButtons) {
        List<String[]> row = new LinkedList<>(rowButtons);
        rows.add(row);
    }

    public Message sendMessageToUser(User user,Update update,String text,InlineKeyboardMarkup inlineKeyboardMarkup ,boolean newMessage) throws TelegramApiException {
        String chatId = user.getChatId();
        if (newMessage) {
            SendMessage message = new SendMessage(chatId, text);
            message.setParseMode("Markdown");
            message.setDisableWebPagePreview(true);
            message.setReplyMarkup(inlineKeyboardMarkup);
            return bot.execute(message);
        } else {
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            EditMessageText EMessage = new EditMessageText(text);
            EMessage.setParseMode("Markdown");
            EMessage.setDisableWebPagePreview(true);
            EMessage.setMessageId(messageId);
            EMessage.setChatId(chatId);
            EMessage.setReplyMarkup(inlineKeyboardMarkup);
            bot.execute(EMessage);
            return update.getMessage();
        }
    }
    public Message sendMessageToUser(User user , String text, ReplyKeyboardMarkup keyboardMarkup) throws TelegramApiException {
        String chatId = user.getChatId();
        SendMessage message = new SendMessage(chatId, text);
        message.setParseMode("Markdown");
        message.setDisableWebPagePreview(true);
        if (keyboardMarkup != null) {
            message.setReplyMarkup(keyboardMarkup);
        }else {
            ReplyKeyboardRemove keyboardMarkup2 = new ReplyKeyboardRemove();
            keyboardMarkup2.setRemoveKeyboard(true); // מגדיר שהמקלדת תוסר
            message.setReplyMarkup(keyboardMarkup2);
        }
        return bot.execute(message);
    }


    public void sendMessage(BotApiMethod<?> message) throws TelegramApiException{
        bot.execute(message);
    }


    public void sendAlert(User user, Update update, String text, boolean showAlert) throws TelegramApiException {
        AnswerCallbackQuery message = new AnswerCallbackQuery();
        message.setCallbackQueryId(update.getCallbackQuery().getId());
        message.setShowAlert(showAlert);
        message.setText(text);
        bot.execute(message);
    }

    public void deleteMessage(User user, int messageId) throws TelegramApiException {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(user.getChatId()); // מזהה הצ'אט
        deleteMessage.setMessageId(messageId); // מזהה ההודעה למחיקה
        bot.execute(deleteMessage);
    }

    public static String escapeMarkdown(String text) {
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }


}