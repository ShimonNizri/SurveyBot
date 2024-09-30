package org.example;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class ProfileManager {

    private final MessageManager messageManager;

    public ProfileManager(MessageManager messageManager){
        this.messageManager = messageManager;
    }


    public void getMessage(User user, Update update) throws TelegramApiException {
        switch (user.getStatus()) {
            case User.UserStatus.EP_EditingProfile:
                handle_EditingProfile_Status(user,update);
                break;
            case User.UserStatus.EP_ProfileSettings:
                handle_ProfileSettings_Status(user,update);
                break;
            case User.UserStatus.EP_Set_alerts:
                handle_Set_alerts_Status(user,update);
                break;
            case User.UserStatus.EP_Set_account:
                handle_Set_account_Status(user,update);
                break;
            case User.UserStatus.EP_Set_Defaults:
                handle_Set_Defaults_Status(user,update);
                break;
            case User.UserStatus.EP_DefaultTimeDistribution:
                handle_DefaultTimeDistribution_Status(user,update);
                break;
            case User.UserStatus.EP_DefaultDurationOfActivity:
                handle_DefaultDurationOfActivity_Status(user,update);
                break;
        }
    }


    private void handle_DefaultDurationOfActivity_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()){
            if (user.getAnswer().equals("back-Edit-Defaults")){
                user.setStatus(User.UserStatus.EP_Set_Defaults);
                EditMessageText EMessage = (EditMessageText) getSet_DefaultsEPMessage(user, update,false);
                messageManager.sendMessage(EMessage);
            }
        }else if (update.hasMessage() && update.getMessage().hasText()){
            Double time = getNumberFromUser(user);
            if (time != null){
                user.setStatus(User.UserStatus.EP_Set_Defaults);
                user.setDefaultDurationOfActivity(time);
                String text = "♻️ הסטטוס למשך זמן פעילות הסקר שונה ל: *" + time +" דקות*.";
                messageManager.sendMessageToUser(user,update,text,null,true);
                SendMessage message = (SendMessage) getSet_DefaultsEPMessage(user,update,true);
                messageManager.sendMessage(message);
            }
        }
    }



    private void handle_DefaultTimeDistribution_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()){
            if (user.getAnswer().equals("Immediate dispatch") || user.getAnswer().equals("back-Edit-Defaults")){
                user.setStatus(User.UserStatus.EP_Set_Defaults);
                if (user.getAnswer().equals("Immediate dispatch")) {
                    user.setDefaultDistributionTime(0);
                    String text = "♻️ הסטטוס להוספת הסקר לתור שונה ל: *באופן מיידי!*";
                    messageManager.sendMessageToUser(user,update,text,null,true);
                    SendMessage message = (SendMessage) getSet_DefaultsEPMessage(user,update,true);
                    messageManager.sendMessage(message);
                }else {
                    EditMessageText EMessage = (EditMessageText) getSet_DefaultsEPMessage(user, update,false);
                    messageManager.sendMessage(EMessage);
                }
            }
        }else if (update.hasMessage() && update.getMessage().hasText()){
            Double time = getNumberFromUser(user);
            if (time != null){
                user.setStatus(User.UserStatus.EP_Set_Defaults);
                user.setDefaultDistributionTime(time);
                String text = "♻️ הסטטוס להוספת הסקר לתור שונה ל: *" + time +" דקות.*";
                messageManager.sendMessageToUser(user,update,text,null,true);
                user.setStatus(User.UserStatus.EP_Set_Defaults);
                SendMessage message = (SendMessage) getSet_DefaultsEPMessage(user,update,true);
                messageManager.sendMessage(message);
            }
        }
    }

    private void handle_Set_Defaults_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            switch (user.getAnswer()) {
                case "Edit-distribution-time" -> {
                    user.setStatus(User.UserStatus.EP_DefaultTimeDistribution);
                    String text = """
                            *📌 הגדר את זמן הוספת הסקר לתור ההפצה בקהילה.*
                                                    
                            ☑️ סטטוס נוכחי:
                            """ + (user.getDefaultDistributionTime() == 0.0 ? "*באופן מיידי.*" : "*" + user.getDefaultDistributionTime() + "*" + " דקות מיצרת הסקר.") + "\n\n⏳ להוספת הסקר מיידית לתור, לחץ על הכפתור. כדי לתזמן את ההוספה למועד מאוחר יותר, שלח את מספר הדקות עד להוספה.";
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);

                }
                case "Editing-activity-duration" -> {
                    user.setStatus(User.UserStatus.EP_DefaultDurationOfActivity);
                    String text = """
                            *📌 קבע את משך הזמן שבו ניתן יהיה לענות על הסקר מהרגע שהוא נשלח בקהילה.*
                                                    
                            ☑️ סטטוס נוכחי:
                            """ + "*" + user.getDefaultDurationOfActivity() + "* דקות.\n\n⏳ לשינוי משך הזמן, שלח את מספר הדקות המבוקש.";

                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);
                }
                case "New-Status-Survey" -> {
                    user.setDefaultAnonymousSurvey(!user.isDefaultAnonymousSurvey());
                    String text = "✅ ברירת מחדל עבור הצבעה בסקרים שונתה ל: " + (user.isDefaultAnonymousSurvey() ? "אנונימית." : "גלויה.");
                    this.messageManager.sendAlert(user, update, text, false);
                    text = """
                            *⚙ ניהול ברירות מחדל לסקר:*
                                                    
                            ➖ הסקרים שתיצור יתחילו עם ההגדרות האלו כברירת מחדל, וניתן לשנות אותן לפי הצורך.
                            """;
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    this.messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);
                }
                case "Detail-Survey" -> {
                    String text = """
                            *⚙ ניהול ברירות מחדל לסקר:*
                                                    
                            ➖ הסקרים שתיצור יתחילו עם ההגדרות האלו כברירת מחדל, וניתן לשנות אותן לפי הצורך.
                                                    
                            *● סוג הסקר :*
                                                    
                            *👣 סקר אנונימי : *
                            יוצר הסקר לא יוכל לראות מי הצביע בסקר.
                                                    
                            *👁 סקר גלוי : *
                            יוצר הסקר יוכל לראות מי הצביע בסקר.
                            """;
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    this.messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);
                }
                case "Back-Menu-ProfileSettings" -> {
                    user.setStatus(User.UserStatus.EP_ProfileSettings);
                    EditMessageText EMessage = (EditMessageText) getProfileSettingsEPMessage(user, update, false);
                    messageManager.sendMessage(EMessage);
                }
            }
        }
    }

    private void handle_EditingProfile_Status(User user, Update update) throws TelegramApiException {
        if (update.hasMessage() &&  update.getMessage().hasText()) {
            user.setStatus(User.UserStatus.EP_ProfileSettings);
            SendMessage message = (SendMessage) getProfileSettingsEPMessage(user,update,true);
            messageManager.sendMessage(message);
            int messageID = messageManager.sendMessageToUser(user,"ㅤ",null).getMessageId();
            messageManager.deleteMessage(user,messageID);
        }
    }

    private void handle_ProfileSettings_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            switch (user.getAnswer()) {
                case "Edit-Alerts" -> {
                    user.setStatus(User.UserStatus.EP_Set_alerts);
                    String text = """
                            📢 *בחר את סוג ההתראה שברצונך לערוך:*
                                                    
                            🔊 - לקבלת התראות
                            🔇 - לביטול התראות
                                                    
                            לחץ על "סקר חדש" לדוגמה, כדי לקבל מידע נוסף על סוג ההתראה.
                            """;

                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);
                }
                case "Edit-Privacy" -> {
                    user.setStatus(User.UserStatus.EP_Set_account);
                    String text = "*🔒 הגדרות פרטיות:*" + "\n\nכעת החשבון שלך מוגדר כ: " + (user.isAnonymousAccount() ? "*אנונימי.*" : "*גלוי.*");
                    text = text + "\n\n" + """           
                            👣 *חשבון אנונימי:*
                               ● לא ניתן ליצור סקר חדש.
                               ● לא ניתן לחפש משתמשים בקהילה.
                               ● אי אפשר למצוא אותך בחיפוש משתמשים.
                               ● ניתן להצביע לסקרים בצורה אנונימית.
                                                    
                            👁 *חשבון גלוי:*
                               ● ניתן ליצור סקרים חדשים.
                               ● ניתן לחפש משתמשים בקהילה.
                               ● אפשר למצוא אותך בחיפוש משתמשים.
                               ● ניתן להצביע לסקרים, והמשתמש הגלוי יוצג כמי שענה לסקר.
                                                    
                            ⚠️ שינוי סוג החשבון אפשרי אחת ל-24 שעות מהשינוי הקודם.
                                                    
                            ⏳ כדי לדעת אם אתה יכול לשנות כרגע את את סטטוס החשבון הינך יכול ללחוץ על "סטטוס חשבון".
                            """;
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);

                }
                case "Edit-Defaults" -> {
                    user.setStatus(User.UserStatus.EP_Set_Defaults);
                    EditMessageText EMessage = (EditMessageText) getSet_DefaultsEPMessage(user, update, false);
                    messageManager.sendMessage(EMessage);
                }
            }
        }else if (update.hasMessage() && update.getMessage().hasText()){
            if (user.getAnswer().equals("Edit-Privacy")){
                user.setStatus(User.UserStatus.EP_Set_account);
                String text = "*🔒 הגדרות פרטיות:*" + "\n\nכעת החשבון שלך מוגדר כ: " + (user.isAnonymousAccount() ? "*אנונימי.*" : "*גלוי.*");
                text = text + "\n\n" + """           
                            👣 *חשבון אנונימי:*
                               ● לא ניתן ליצור סקר חדש.
                               ● לא ניתן לחפש משתמשים בקהילה.
                               ● אי אפשר למצוא אותך בחיפוש משתמשים.
                               ● ניתן להצביע לסקרים בצורה אנונימית.
                                                    
                            👁 *חשבון גלוי:*
                               ● ניתן ליצור סקרים חדשים.
                               ● ניתן לחפש משתמשים בקהילה.
                               ● אפשר למצוא אותך בחיפוש משתמשים.
                               ● ניתן להצביע לסקרים, והמשתמש הגלוי יוצג כמי שענה לסקר.
                                                    
                            ⚠️ שינוי סוג החשבון אפשרי אחת ל-24 שעות מהשינוי הקודם.
                                                    
                            ⏳ כדי לדעת אם אתה יכול לשנות כרגע את את סטטוס החשבון הינך יכול ללחוץ על "סטטוס חשבון".
                            """;
                InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, true);
                int messageID = messageManager.sendMessageToUser(user, "ㅤ", null).getMessageId();
                messageManager.deleteMessage(user, messageID);

            }

        }
    }

    private void handle_Set_account_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            switch (user.getAnswer()) {
                case "New-Status-Account" -> {
                    String text;
                    if (user.isCanChangeAccountStatus()) {
                        user.setAnonymousAccount(!user.isAnonymousAccount());
                        text = "🔐 סטטוס החשבון שלך שונה ל" + (user.isAnonymousAccount() ? "אנונימי." : "גלוי.") + "\n⌛תוכל לשנות את סטטוס החשבון שלך שוב בעוד 24 שעות!";
                    } else {
                        text = "⚠ אין באפשרות לשנות את סטטוס החשבון מכיוון שעדין לא עברו 24 שעות מהשינוי האחרון !";
                    }

                    messageManager.sendAlert(user, update, text, true);

                    text = "*🔒 הגדרות פרטיות:*" + "\n\nכעת החשבון שלך מוגדר כ: " + (user.isAnonymousAccount() ? "*אנונימי.*" : "*גלוי.*");
                    text = text + "\n\n" + """  
                            👣 *חשבון אנונימי:*
                               ● לא ניתן ליצור סקר חדש.
                               ● לא ניתן לחפש משתמשים בקהילה.
                               ● אי אפשר למצוא אותך בחיפוש משתמשים.
                               ● ניתן להצביע לסקרים בצורה אנונימית.
                                                    
                            👁 *חשבון גלוי:*
                               ● ניתן ליצור סקרים חדשים.
                               ● ניתן לחפש משתמשים בקהילה.
                               ● אפשר למצוא אותך בחיפוש משתמשים.
                               ● ניתן להצביע לסקרים, והמשתמש הגלוי יוצג כמי שענה לסקר.
                                                    
                            ⚠️ שינוי סוג החשבון אפשרי אחת ל-24 שעות מהשינוי הקודם.
                                                    
                            ⏳ כדי לדעת אם אתה יכול לשנות כרגע את את סטטוס החשבון הינך יכול ללחוץ על "סטטוס חשבון".
                            """;
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);

                }
                case "Detail-Account" -> {
                    String text;

                    if (user.isCanChangeAccountStatus()) {
                        text = "✅ עברו 24 שעות אתה יכול לשנות סטטוס.";
                    } else {
                        Duration duration = Duration.ofHours(24).minus(Duration.between(user.getAccountStatusChangeTime(), Instant.now()));
                        long hours = duration.toHours();
                        long minutes = duration.toMinutes() % 60;
                        text = "☑ כדי לשנות את הסטוס חשבון אתה צריך לחכות עוד -> " + hours + ":" + minutes;

                    }
                    messageManager.sendAlert(user, update, text, false);

                }
                case "Back-Menu-ProfileSettings" -> {
                    user.setStatus(User.UserStatus.EP_ProfileSettings);
                    EditMessageText EMessage = (EditMessageText) getProfileSettingsEPMessage(user, update, false);
                    messageManager.sendMessage(EMessage);
                }
            }
        }


    }

    private void handle_Set_alerts_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            switch (user.getAnswer()) {
                case "New-Survey-Alert", "New-Member-Alert" -> {
                    String text;
                    if (user.getAnswer().equals("New-Survey-Alert")) {
                        user.setNewSurveyAlert(!user.isNewSurveyAlert());
                        text = user.isNewSurveyAlert() ? "התראה על סקר חדש הופעלה 🔊" : "התראה על סקר חדש כובתה 🔇";
                    } else {
                        user.setNewMemberAlert(!user.isNewMemberAlert());
                        text = user.isNewMemberAlert() ? "התראה על משתמש חדש הופעלה 🔊" : "התראה על משתמש חדש כובתה 🔇";
                    }
                    messageManager.sendAlert(user, update, text, false);

                    text = """
                            📢 *בחר את סוג ההתראה שברצונך לערוך:*
                                                    
                            🔊 - לקבלת התראות
                            🔇 - לביטול התראות
                                                    
                            לחץ על "סקר חדש" לדוגמה, כדי לקבל מידע נוסף על סוג ההתראה.
                            """;
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);

                }
                case "Detail-SurveyAlert", "Detail-MemberAlert" -> {
                    String text = """
                            📢 *בחר את סוג ההתראה שברצונך לערוך:*
                                                    
                            🔊 - לקבלת התראות
                            🔇 - לביטול התראות
                                                    
                            """;
                    if (user.getAnswer().equals("Detail-SurveyAlert")) {
                        text = text + "💬 *סקר חדש:*" + "\n" + "בחר האם לקבל התראות על סקרים חדשים בקהילה.";
                    } else {
                        text = text + "💬 *משתמש חדש:*" + "\n" + "בחר האם לקבל התראות על משתמשים חדשים בקהילה.";
                    }

                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);
                }
                case "Back-Menu-ProfileSettings" -> {
                    user.setStatus(User.UserStatus.EP_ProfileSettings);
                    EditMessageText EMessage = (EditMessageText) getProfileSettingsEPMessage(user, update, false);
                    messageManager.sendMessage(EMessage);
                }
            }
        }
    }

    private BotApiMethod<?> getSet_DefaultsEPMessage(User user, Update update, boolean newMessage) {
        String chatId = user.getChatId();
        String text = """
                        *⚙ ניהול ברירות מחדל לסקר:*
                                                
                        ➖ הסקרים שתיצור יתחילו עם ההגדרות האלו כברירת מחדל, וניתן לשנות אותן לפי הצורך.
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

    private BotApiMethod<?> getProfileSettingsEPMessage(User user, Update update, boolean newMessage) {
        String chatId = user.getChatId();
        String text = """
                    *🔆 פאנל ניהול פרופיל:*
                    ⚙️ אנא בחר את האופציה שברצונך לערוך.
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

    private Double getNumberFromUser(User user) throws TelegramApiException {
        double timeToSend;
        SendMessage message = new SendMessage();
        message.setChatId(user.getChatId());
        try {
            timeToSend = Double.parseDouble(user.getAnswer());
            if (timeToSend < 0) {
                message.setText("""
                        ⚠️ הקלט שהתקבל שגוי.
                        שים לב אתה צריך לשלוח מספר חיובי בלבד❗
                        ☑️ לדוגמא : 1 או 2.""");
                messageManager.sendMessage(message);
                return null;
            }
        } catch (Exception e) {
            message.setText("""
                    ⚠️ הקלט שהתקבל שגוי.
                    שים לב אתה צריך לשלוח רק את מספר דקות בלבד❗
                    ☑️ לדוגמא : 1 או 2.""");
            messageManager.sendMessage(message);
            return null;
        }
        return timeToSend;
    }

    private InlineKeyboardMarkup getInlineKeyboardMarkupByStatus(User user){
        List<List<String[]>> rows = new ArrayList<>();
        switch (user.getStatus()) {
            case EP_ProfileSettings:
                MessageManager.addFButtonToNewRow(rows ,"ניהול התראות 🔊", "Edit-Alerts");
                MessageManager.addFButtonToNewRow(rows , "הגדרות פרטיות 🔒", "Edit-Privacy");
                MessageManager.addFButtonToNewRow(rows , "ניהול ברירות מחדל לסקר ⚙", "Edit-Defaults");
                return MessageManager.createsFloatingButtons(rows);
            case EP_Set_alerts:
                List<String[]> newRow1 = List.of(
                        new String[]{user.isNewSurveyAlert() ? "🔊" : "🔇","New-Survey-Alert"},
                        new String[]{"סקר חדש - >", "Detail-SurveyAlert"}
                );
                List<String[]> newRow2 = List.of(
                        new String[]{user.isNewMemberAlert() ? "🔊" : "🔇","New-Member-Alert"},
                        new String[]{"משתמש חדש - >", "Detail-MemberAlert"}
                );

                MessageManager.addFButtonsToNewRow(rows ,newRow1);
                MessageManager.addFButtonsToNewRow(rows ,newRow2);
                MessageManager.addFButtonToNewRow(rows,"⬅️ חזור", "Back-Menu-ProfileSettings");
                return MessageManager.createsFloatingButtons(rows);
            case EP_Set_account:
                List<String[]> newRow = List.of(
                        new String[]{user.isAnonymousAccount() ? "אנונימי" : "גלוי","New-Status-Account"},
                        new String[]{"סטטוס חשבון - >", "Detail-Account"}
                );

                MessageManager.addFButtonsToNewRow(rows ,newRow);
                MessageManager.addFButtonToNewRow(rows,"⬅️ חזור", "Back-Menu-ProfileSettings");
                return MessageManager.createsFloatingButtons(rows);

            case EP_Set_Defaults:
                MessageManager.addFButtonToNewRow(rows ,"⏲ עריכת זמן הפצה", "Edit-distribution-time");
                MessageManager.addFButtonToNewRow(rows , "🌐 עריכת משך זמן פעילות", "Editing-activity-duration");
                List<String[]> newRow3 = List.of(
                        new String[]{user.isDefaultAnonymousSurvey() ? "אנונימי" : "גלוי","New-Status-Survey"},
                        new String[]{" סוג הסקר - >", "Detail-Survey"}
                );

                MessageManager.addFButtonsToNewRow(rows,newRow3);
                MessageManager.addFButtonToNewRow(rows,"⬅️ חזור", "Back-Menu-ProfileSettings");
                return MessageManager.createsFloatingButtons(rows);
            case EP_DefaultTimeDistribution:
                MessageManager.addFButtonToNewRow(rows, "📩 שלח באופן מיידי!", "Immediate dispatch");
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-Edit-Defaults");
                return MessageManager.createsFloatingButtons(rows);
            case EP_DefaultDurationOfActivity:
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-Edit-Defaults");
                return MessageManager.createsFloatingButtons(rows);
            default:
                return null;
        }
    }

}