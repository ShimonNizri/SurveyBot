package org.example;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class UserSearchManager {

    private final MessageManager messageManager;
    private final DataLoader dataLoader;


    public UserSearchManager(MessageManager messageManager, DataLoader dataLoader){
        this.messageManager = messageManager;
        this.dataLoader = dataLoader;
    }

    public void getMessage(User user, Update update) throws TelegramApiException {
        switch (user.getStatus()) {
            case US_searchType:
                handle_searchType_Status(user,update);
                break;
            case US_SearchSelection:
                handle_SearchSelection_Status(user,update);
                break;
            case US_UserList:
                handle_UserList_Status(user,update);
                break;
            case US_UserInformation:
                handle_UserInformation_Status(user,update);
                break;
        }
    }
    private void handle_searchType_Status(User user, Update update) throws TelegramApiException{
        if (update.hasMessage() && update.getMessage().hasText()){
            if (!user.isAnonymousAccount()) {
                user.setStatus(User.UserStatus.US_SearchSelection);
                SendMessage message = (SendMessage) getSearchSelectionUSMessage(user, update, true);
                messageManager.sendMessage(message);
                int messageID = messageManager.sendMessageToUser(user, "ㅤ", null).getMessageId();
                messageManager.deleteMessage(user, messageID);
            }else {
                String text = """
                            🛂* החשבון שלך מוגדר כאנונימי.*
                                                    
                            ➖ כדי לערוך חיפוש משתמשים עליך ליהות בפרופיל גלוי.
                            ⚡️ למעבר מהיר לשינוי חשבון [לחץ כאן!](https://t.me/SurveyDevelopmentBot?start=change_account)
                            """;
                messageManager.sendMessageToUser(user,update,text,null,true);
            }
        }
    }

    private void handle_SearchSelection_Status(User user, Update update) throws TelegramApiException{
        if (update.hasCallbackQuery()){
            if (user.getAnswer().startsWith("User-list")){
                user.setStatus(User.UserStatus.US_UserList);
                String text = """
                        *🗂 רשימת משתמשים 🗂*
                                                
                        💠 לפניך מופיעה רשימת כל המשתמשים בבוט.
                                                
                        ● תוכל לנווט בין המשתמשים באמצעות הכפתורים ▶️◀️.
                        """;

                InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                this.messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);
            }
        }
    }
    private void handle_UserList_Status(User user, Update update) throws TelegramApiException{
        if (update.hasCallbackQuery()){

            if (user.getAnswer().startsWith("user")) {
                user.setStatus(User.UserStatus.US_UserInformation);
                String idUser = user.getAnswer().split(":")[1];
                User user1 = dataLoader.getUsersAsMap().get(idUser);
                String text = getUserInformation(user1);
                InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);

                this.messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);

            } else if (user.getAnswer().startsWith("User-list")){
                String text = """
                            *🗂 רשימת משתמשים 🗂*
                                                    
                            💠 לפניך מופיעה רשימת כל המשתמשים בבוט.
                                                    
                            ● תוכל לנווט בין המשתמשים באמצעות הכפתורים ▶️◀️.
                            """;

                InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                this.messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);

            }else if (user.getAnswer().equals("Anonymous-account")) {
                String text = "🔒 זהו משתמש אנונימי, פרטי המשתמש חסויים!";
                messageManager.sendAlert(user,update,text,true);
            }else if (user.getAnswer().equals("back-main")){
                user.setStatus(User.UserStatus.US_SearchSelection);
                EditMessageText EMessage = (EditMessageText) getSearchSelectionUSMessage(user,update,false);
                messageManager.sendMessage(EMessage);
            }
        }
    }

    private void handle_UserInformation_Status(User user, Update update) throws TelegramApiException{
        if (update.hasCallbackQuery()){
            if (user.getAnswer().equals("back-List")){
                user.setStatus(User.UserStatus.US_UserList);
                user.setAnswer("User-list:0");
                getMessage(user,update);
            }
        }
    }











    private BotApiMethod<?> getSearchSelectionUSMessage(User user, Update update, boolean newMessage) {
        String chatId = user.getChatId();
        String text = """
                    *🔍 חיפוש משתמשים 🔍*
                                        
                    🌐 עומדות בפניך שתי אפשרויות לחיפוש משתמשים:
                                        
                    🗂 *רשימת משתמשים:*
                    ניתן לעיין בכל המשתמשים הגלויים בבוט.
                                        
                    *🆔 חיפוש לפי ID:*
                    תוכל לבצע חיפוש על פי הזנת ה-ID של המשתמש.
                    """;
        InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);

        if (newMessage) {
            SendMessage message = new SendMessage(chatId, text);
            message.setParseMode("Markdown");
            message.setReplyMarkup(inlineKeyboardMarkup);
            return message;
        } else {
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            EditMessageText EMessage = new EditMessageText(text);
            EMessage.setParseMode("Markdown");
            EMessage.setMessageId(messageId);
            EMessage.setChatId(chatId);
            EMessage.setReplyMarkup(inlineKeyboardMarkup);
            return EMessage;
        }
    }


    private String getUserInformation(User user){
        String text = "*📑 מידע על המשתמש 📑*";
        text = text + "\n\n*🗣 שם :* " + (user.getDetails() == null ?  "- אין  מידע -" : user.getDetails().getFirstName() + (user.getDetails().getLastName() == null ? "": user.getDetails().getLastName()));
        text = text + "\n*💬 שם משתמש :* "
                + (user.getDetails() == null
                ? "- אין מידע -"
                : (user.getDetails().getUserName() == null
                ? "- אין -"
                : ("@" + MessageManager.escapeMarkdown(user.getDetails().getUserName()))));
        text = text + "\n*🆔 מזהה משתמש :* [" + (user.getChatId() + "](tg://user?id=" + user.getChatId() + ")");

        text = text + "\n\n*🗓 הצטרף לקהילה בתאריך : *" + user.getDateOfJoining().subSequence(0,10);
        text = text + "\n*📊 סקרים שנוצרו :* " + user.getSurveys().size();
        text = text + "\n*🗳 השתתפות בסקרים :* " + user.getParticipationInSurveys();

        return text;
    }



    private InlineKeyboardMarkup getInlineKeyboardMarkupByStatus(User user){
        List<List<String[]>> rows = new ArrayList<>();
        switch (user.getStatus()) {
            case US_SearchSelection:
                MessageManager.addFButtonToNewRow(rows, "🗂 רשימת משתמשים", "User-list:0");
                MessageManager.addFButtonToNewRow(rows, "🆔 חיפוש ID", "ID-search");
                return MessageManager.createsFloatingButtons(rows);
            case US_UserList:
                int currentPage = Integer.parseInt(user.getAnswer().split(":")[1]);
                List<User> users = dataLoader.getUsersAsList();
                int startIndex = (users.size() / 5.0) > currentPage ? 5 * currentPage : users.size() - (users.size() % 5);

                for (int i = startIndex; i - (startIndex) < 5 && i < users.size() && i >= 0; i++) {
                    if (users.get(i).isAnonymousAccount()) {
                        MessageManager.addFButtonToNewRow(rows, "- אנונימי -", "Anonymous-account");
                    } else {
                        MessageManager.addFButtonToNewRow(rows, users.get(i).getDetails() == null ? users.get(i).getChatId() : users.get(i).getDetails().getFirstName() + (users.get(i).getDetails().getLastName() != null ? users.get(i).getDetails().getLastName() : ""), "user:" + users.get(i).getChatId());
                    }
                }

                List<String[]> newRow = List.of(
                        new String[]{"◀️", "User-list:" + (currentPage - (currentPage - 1 < 0 ? 0 : 1)) + ":0"},
                        new String[]{"▶️", "User-list:" + (currentPage + ((users.size() / 5.0) <= currentPage + 1 ? 0 : 1)) + ":1"}

                );

                MessageManager.addFButtonsToNewRow(rows, newRow);
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-main");
                return MessageManager.createsFloatingButtons(rows);
            case US_UserInformation:
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-List");
                return MessageManager.createsFloatingButtons(rows);
            default:
                return null;
        }
    }
}
