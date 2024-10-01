package org.example;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class SurveyManager {
    private final MessageManager messageManager;
    private final VoteManager voteManager;
    private final DataLoader dataLoader;


    public SurveyManager(MessageManager messageManager, VoteManager voteManager,DataLoader dataLoader){
        this.messageManager = messageManager;
        this.voteManager = voteManager;
        this.dataLoader = dataLoader;
    }

    public void getMessage(User user, Update update) throws TelegramApiException{
        switch (user.getStatus()) {
            case CS_CreateSurvey:
                handle_CreateSurvey_Status(user,update);
                break;
            case CS_getName:
                handle_getName_Status(user,update);
                break;
            case CS_ANSWER_GET_QUESTION:
                handle_AnswerGetQuestion_Status(user, update);
                break;
            case CS_ANSWER_GET_OPTIONS:
                handle_AnswerGetOptions_Status(user, update);
                break;
            case CS_mainMenu:
                handle_Menu_Status(user, update);
                break;
            case CS_SendingTimeDistribution:
                handle_SendingTimeDistribution_Status(user,update);
                break;
            case CS_Select_question:
                handle_SelectQuestion_Status(user,update);
                break;
            case CS_Edit_question_menu:
                handle_EditQuestionMenu_Status(user,update);
                break;
            case CS_EditingQuestionItself:
                handle_editingQuestionItself_Status(user,update);
                break;
            case CS_Editing_answers:
                handle_editingAnswers_Status(user,update);
                break;
            case CS_surveySettings:
                handle_SurveySettings_Status(user,update);
                break;
            case CS_SendingDurationOfActivity:
                handle_SendingDurationOfActivity_Status(user,update);
                break;
            case CS_MySurveys:
                handle_MySurveys_Status(user,update);
                break;
            case CS_SurveySelection:
                handle_CS_SurveySelection_Status(user,update);
                break;
            case CS_ViewingSurvey:
                handle_ViewingSurvey_Status(user,update);
                break;
            case CS_ViewingResults:
                handle_ViewingResults_Status(user,update);
                break;
            case CS_voting:
                handle_voting_Status(user,update);
                break;
            case CS_voter:
                handle_voter_Status(user,update);
                break;
            case CS_UserInformation:
                handle_UserInformation_Status(user,update);
                break;
        }
    }

    private void handle_MySurveys_Status(User user, Update update) throws TelegramApiException {
        if (update.hasMessage() &&  update.getMessage().hasText()) {
            if (!user.getSurveys().isEmpty()) {
                user.setStatus(User.UserStatus.CS_SurveySelection);
                String text = """
                        *🗂 בחר את אחד מהסקרים שלך כדי להמשיך.*
                                            
                        ● ניתן לצפות בתוצאות או לערוך את הסקר.
                        """;
                text = text + "● יש לך סה\"כ* " + user.getSurveys().size() + "* סקרים.";
                InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, true);
                int messageID = messageManager.sendMessageToUser(user, "ㅤ", null).getMessageId();
                messageManager.deleteMessage(user, messageID);
            }else {
                user.setStatus(User.UserStatus.FREE);
                String text;
                if (!user.isAnonymousAccount()) {
                    text = "📭 **נראה שאין לך סקרים פעילים.**\n" +
                            "⚡️ [צור סקר חדש כאן!](https://t.me/SurveyDevelopmentBot?start=create_survey)";

                }else {
                    text = """
                            🛂* החשבון שלך מוגדר כאנונימי.*
                                                    
                            ➖ כדי ליצור סקר חדש עליך ליהות מוגדר כחשבון גלוי.
                            ⚡️ למעבר מהיר לשינוי חשבון [לחץ כאן!](https://t.me/SurveyDevelopmentBot?start=change_account)
                            """;
                }
                messageManager.sendMessageToUser(user, update, text, null, true);
            }

        }
    }

    private void handle_CS_SurveySelection_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            if (user.getAnswer().startsWith("NAME")) {
                user.setStatus(User.UserStatus.CS_ViewingSurvey);
                String surveyID = user.getAnswer().split(":")[1];
                Survey survey = user.getSurveyByID(surveyID);

                StringBuilder text = new StringBuilder("📊 *שם הסקר :* " + survey.getName() + "." + "\n" + "🔗  *ID Survey :* " + survey.getId() + "." + "\n\n");
                text.append("📋 *פרטי הסקר :*");
                text.append("\n  ● כמות שאלות : ").append(survey.getSurveyQuestions().size()).append(".");
                text.append("\n  ● סוג סקר : ").append(survey.isAnonymousSurvey() ? "אנונימי" : "גלוי").append(".");
                text.append("\n  ● זמן פעילות : ").append(survey.getDurationOfActivity()).append(" דקות.").append(survey.getDurationOfActivity() == user.getDefaultDurationOfActivity() ? " (ברירת מחדל)" : "");
                text.append("\n  ● תיזמון הסקר : ").append(survey.getTimeToAdd() == 0.0 ? "באופן מיידי!" : survey.getTimeToAdd() + " דקות.").append(survey.getTimeToAdd() == user.getDefaultDistributionTime() ? " (ברירת מחדל)" : "");
                text.append("\n  ● סטטוס : ").append(survey.isHasResults() ? "הופץ, והתקבלו תוצאות!" : survey.isDistributed() ? "הופץ!" : survey.isPublished() ? "פורסם!" : "בשלבי עריכה!");

                text.append("""
                                        
                                        
                        ⚠️ במידה ותערוך את הסקר אז הסקר יוסר מתור ההפצה ותצטרך לפרסם אותו מחדש.
                                        
                        🗃 תוצאות יתקבלו רק לאחר סיום ההפצה של הסקר בקהילה.
                        """);

                text.append("\n🗂 *תוכן הסקר :*");
                for (SurveyQuestion question : survey.getSurveyQuestions()){
                    text.append("\n\n📄 *השאלה :* ").append(question.getQuestionText()).append("\n📂 *התשובות :*");
                    for (String answer : question.getAnswerOptions()){
                        text.append("\n  ● ").append(answer);
                    }
                }
                InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                messageManager.sendMessageToUser(user,update, text.toString(),inlineKeyboardMarkup,false);
            }
        }
    }

    private void handle_ViewingSurvey_Status(User user, Update update) throws TelegramApiException{
        if (update.hasCallbackQuery()){
            if (user.getAnswer().startsWith("results")){
                String surveyID = user.getAnswer().split(":")[1];
                Survey survey = user.getSurveyByID(surveyID);
                if (survey.isHasResults()) {
                    user.setStatus(User.UserStatus.CS_ViewingResults);
                    String text =   "📊 *שם הסקר :* "+ survey.getName() + "." + "\n" + "🔗  *ID Survey :* " + survey.getId() + "." + "\n\n";
                    text = text + survey.getResultsAsMessage().getText();
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);
                }else {
                    String text = "⏳ עדיין לא התקבלו תוצאות.";
                    messageManager.sendAlert(update,text,true);
                }

            }else if (user.getAnswer().startsWith("edited")){
                String surveyID = user.getAnswer().split(":")[1];
                Survey survey = user.getSurveyByID(surveyID);
                if (!survey.isDistributed()) {
                    user.getSurveys().remove(survey);
                    user.getSurveys().add(survey);
                    if (survey.isPublished()){
                        voteManager.removeSurveys(survey);
                    }
                    survey.setPublished(false);
                    user.setStatus(User.UserStatus.CS_surveySettings);
                    user.setAnswer("Back-Main-Menu");
                    getMessage(user,update);

                } else {
                    String text = "🚫 הסקר הופץ, עריכה אינה אפשרית כעת.";
                    messageManager.sendAlert(update,text,true);
                }
            }else if (user.getAnswer().startsWith("Delete")){
                String surveyID = user.getAnswer().split(":")[1];
                Survey survey = user.getSurveyByID(surveyID);
                if (!survey.isDistributed() || survey.isHasResults()) {
                    user.getSurveys().remove(survey);
                    if (survey.isPublished()){
                        voteManager.removeSurveys(survey);
                    }
                    user.setStatus(User.UserStatus.CS_ViewingSurvey);
                    user.removeOneToSurveysHeCreated();
                    user.setAnswer("back-surveys");
                    getMessage(user,update);
                    String text = "✅ הסקר נמחק בהצלחה!";
                    messageManager.sendAlert(update,text,false);


                } else {
                    String text = "🚫 הסקר הופץ, מחיקה תתאפשר שתהליך ההפצה יגמר.";
                    messageManager.sendAlert(update,text,true);
                }

            } else if (user.getAnswer().equals("back-surveys")){
                if (!user.getSurveys().isEmpty()) {
                    user.setStatus(User.UserStatus.CS_SurveySelection);
                    String text = """
                            *🗂 בחר את אחד מהסקרים שלך כדי להמשיך.*
                                                
                            ● ניתן לצפות בתוצאות או לערוך את הסקר.
                            """;
                    text = text + "● יש לך סה\"כ* " + user.getSurveys().size() + "* סקרים.";
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);
                }else {
                    user.setStatus(User.UserStatus.FREE);
                    String text = "📭 **נראה שאין לך סקרים פעילים.**\n" +
                            "✨ [צור סקר חדש כאן!](https://t.me/SurveyDevelopmentBot?start=create_survey)";

                    messageManager.sendMessageToUser(user,update,text,null,false);
                }

            }

        }else if (update.hasMessage() && update.getMessage().hasText()){
            if (user.getAnswer().startsWith("results")){
                String surveyID = user.getAnswer().split(":")[1];
                Survey survey = user.getSurveyByID(surveyID);
                if (survey.isHasResults()) {
                    user.setStatus(User.UserStatus.CS_ViewingResults);
                    String text =   "📊 *שם הסקר :* "+ survey.getName() + "." + "\n" + "🔗  *ID Survey :* " + survey.getId() + "." + "\n\n";
                    text = text + survey.getResultsAsMessage().getText();
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, true);
                    int messageID = messageManager.sendMessageToUser(user, "ㅤ", null).getMessageId();
                    messageManager.deleteMessage(user, messageID);
                }else {
                    String text = "⏳ עדיין לא התקבלו תוצאות.";
                    messageManager.sendAlert(update,text,true);
                }
            }
        }
    }

    private void handle_ViewingResults_Status(User user, Update update) throws TelegramApiException{
        if (update.hasCallbackQuery()){
            if (user.getAnswer().startsWith("voting")){
                String surveyID = user.getAnswer().split(":")[1];
                Survey survey = user.getSurveyByID(surveyID);
                if (!survey.isAnonymousSurvey()) {
                    user.setStatus(User.UserStatus.CS_voting);
                    String text =   "📊 *שם הסקר :* "+ survey.getName() + "." + "\n" + "🔗  *ID Survey :* " + survey.getId() + "." + "\n\n";
                    text = text + "רשימת מצביעים :";
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user, update, text, inlineKeyboardMarkup, false);
                }else {
                    String text = "🔒 סקר זה אנונימי, ולכן אין גישה לרשימת המצביעים!";
                    messageManager.sendAlert(update,text,true);
                }

            }else if (user.getAnswer().startsWith("back-survey")){
                user.setStatus(User.UserStatus.CS_SurveySelection);
                String answer = "NAME:"+user.getAnswer().split(":")[1];
                user.setAnswer(answer);
                getMessage(user,update);
            }
        }
    }

    private void handle_voting_Status(User user, Update update) throws TelegramApiException{
        if (update.hasCallbackQuery()){
            if (user.getAnswer().equals("Anonymous-Account")){
                String text = "🔒 זהו משתמש אנונימי, פרטי המשתמש חסויים!";
                messageManager.sendAlert(update,text,true);
            }else if (user.getAnswer().startsWith("voter")){
                user.setStatus(User.UserStatus.CS_voter);
                String surveyID = user.getAnswer().split(":")[1];
                String userID = user.getAnswer().split(":")[2];
                Survey survey = user.getSurveyByID(surveyID);
                User user1 = this.dataLoader.getUsersAsMap().get(userID);
                String text =   "📊 *שם הסקר :* "+ survey.getName() + "." + "\n" + "🔗  *ID Survey :* " + survey.getId() + "." + "\n\n";
                text = text + survey.voterInformation(user1);
                InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
            }else if (user.getAnswer().startsWith("back-survey-results")){
                user.setStatus(User.UserStatus.CS_ViewingSurvey);
                String name = user.getAnswer().split(":")[1];
                String answer = "results:"+name;
                user.setAnswer(answer);
                getMessage(user,update);
            }
        }
    }

    private void handle_voter_Status(User user, Update update) throws TelegramApiException{
        if (update.hasCallbackQuery()){
            if (user.getAnswer().startsWith("information")){
                String id = user.getAnswer().split(":")[2];
                User user1 = dataLoader.getUsersAsMap().get(id);
                user.setStatus(User.UserStatus.CS_UserInformation);
                String text = UserSearchManager.getUserInformation(user1);
                InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
            }else if (user.getAnswer().startsWith("back-voting")){
                user.setStatus(User.UserStatus.CS_ViewingResults);
                String name = user.getAnswer().split(":")[1];
                String answer = "voting:"+name;
                user.setAnswer(answer);
                getMessage(user,update);
            }
        }
    }
    private void handle_UserInformation_Status(User user, Update update) throws TelegramApiException{
        if (update.hasCallbackQuery()){
             if (user.getAnswer().startsWith("back-voter")){
                 user.setStatus(User.UserStatus.CS_voting);
                String name = user.getAnswer().split(":")[1];
                String id = user.getAnswer().split(":")[2];
                String answer = "voter:"+name+":"+id;
                user.setAnswer(answer);
                getMessage(user,update);
            }
        }
    }

    private void handle_CreateSurvey_Status(User user, Update update) throws TelegramApiException {
        if (update.hasMessage() &&  update.getMessage().hasText()) {
            if (!user.isAnonymousAccount()) {
                user.setStatus(User.UserStatus.CS_getName);

                String text = "מצוין! 📋 בבקשה, שלח את שם הסקר שברצונך ליצור.";
                messageManager.sendMessageToUser(user, text, null);
            }else {
                String text = """
                        🛂* החשבון שלך מוגדר כאנונימי.*
                                                
                        ➖ כדי ליצור סקר חדש עליך ליהות מוגדר כחשבון גלוי.
                        ⚡️ למעבר מהיר לשינוי חשבון [לחץ כאן!](https://t.me/SurveyDevelopmentBot?start=change_account)
                        """;
                messageManager.sendMessageToUser(user,update,text,null,true);
                user.setStatus(User.UserStatus.FREE);
            }
        }
    }

    private void handle_getName_Status(User user, Update update) throws TelegramApiException {
        if (update.hasMessage() &&  update.getMessage().hasText()) {
            String surveyID = user.getAnswer();
            if (user.getSurveyByID(surveyID) != null){
                String text = "יש לך כבר סקר בשם הזה! 🔄 נסה שוב.";
                messageManager.sendMessageToUser(user,update,text,null,true);
                return;
            }
            user.createNewSurvey(surveyID);
            user.setStatus(User.UserStatus.CS_ANSWER_GET_QUESTION);
            String text =   "📊 *שם הסקר :* "+ user.getLastSurvey().getName() + "." + "\n" + "🔗  *ID Survey :* " + user.getLastSurvey().getId() + "." + "\n\n";
            text = text + "יופי! 🙌 עכשיו, מה השאלה הראשונה לסקר שלך?";

            messageManager.sendMessageToUser(user,update,text,null,true);
        }
    }

    private void handle_AnswerGetQuestion_Status(User user, Update update) throws TelegramApiException {
        if (update.hasMessage() &&  update.getMessage().hasText()) {
            SurveyQuestion q1 = new SurveyQuestion(user.getAnswer(), user.getLastSurvey().getSurveyQuestions().size());
            q1.setSurvey(user.getLastSurvey());
            for (SurveyQuestion question : user.getLastSurvey().getSurveyQuestions()){
                if (question.equals(q1)){
                    String text = "קיימת כבר שאלה זהה בסקר! 🔄 נסה שוב!";
                    messageManager.sendMessageToUser(user,text,null);
                    return;
                }
            }
            user.getLastSurvey().getSurveyQuestions().add(q1);

            user.setStatus(User.UserStatus.CS_ANSWER_GET_OPTIONS);

            String text = "📑 כעת, אנא שלח את התשובות לשאלה!\n" + "➖ ניתן לשלוח לפחות 2 תשובות אפשריות. לסיום, שלח: ⛔ סיים ⛔ (מתוך המקלדת המחוברת).";
            List<List<String>> rows = new ArrayList<>();
            MessageManager.addKButtonToNewRow(rows,"⛔ סיים ⛔");
            ReplyKeyboardMarkup replyKeyboardMarkup = MessageManager.createsKeyboardButtons(rows);
            messageManager.sendMessageToUser(user,text,replyKeyboardMarkup);

        }else if (update.hasCallbackQuery()){
            if (user.getAnswer().equals("Back-Main-Menu")){
                user.setStatus(User.UserStatus.CS_surveySettings);
                user.setAnswer("Back-Main-Menu");
                getMessage(user,update);
            }
        }
    }

    private void handle_AnswerGetOptions_Status(User user, Update update) throws TelegramApiException {
        if (update.hasMessage() &&  update.getMessage().hasText()) {

            int numAnswer = user.getLastSurvey().getSurveyQuestions().size();
            SurveyQuestion q1 = user.getLastSurvey().getSurveyQuestions().get(numAnswer-1);


            if (user.getAnswer().equals("⛔ סיים ⛔") && q1.getSumOfAnswer() >= 2) {
                String text = "✅ השאלה עודכנה בהצלחה ונוספה לסקר!";
                messageManager.sendMessageToUser(user,text,null);

                user.setStatus(User.UserStatus.CS_mainMenu);
                text =   "📊 *שם הסקר :* "+ user.getLastSurvey().getName() + "." + "\n" + "🔗  *ID Survey :* " + user.getLastSurvey().getId() + "." + "\n\n";
                text = text + "🔰 אנא בחר אחת מהאפשרויות למטה להמשך עריכת הסקר:";
                InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,true);

            } else if (user.getAnswer().equals("⛔ סיים ⛔")) {
                String text = "🚫 שים לב, עליך לשלוח לפחות 2 תשובות אפשריות לכל שאלה!";
                messageManager.sendMessageToUser(user,update,text,null,true);
            } else {
                if (!q1.getAnswerOptions().contains(user.getAnswer())) {
                    q1.addAnswer(user.getAnswer());
                }
            }
        }
    }

    private void handle_Menu_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {

            switch (user.getAnswer()) {
                case "complete-Survey" -> {
                    if (!user.getLastSurvey().getSurveyQuestions().isEmpty()) {
                        user.setStatus(User.UserStatus.FREE);
                        user.getLastSurvey().setPublished(true);
                        String text =   "📊 *שם הסקר :* "+ user.getLastSurvey().getName() + "." + "\n" + "🔗  *ID Survey :* " + user.getLastSurvey().getId() + "." + "\n\n";
                        this.voteManager.addSurveyToTheSurveyQueue(user.getLastSurvey());
                        text = text + "✅ הסקר נוסף לתור ההפצה בקהילה בהצלחה.";
                        messageManager.sendMessageToUser(user, update, text, null, false);
                        text = """
                    🔑 לרשימת הפקודות המהירות שלח: /help
                    """;
                        ReplyKeyboardMarkup replyKeyboardMarkup = BotManager.getReplyKeyboardMarkupByStatus(user);
                        messageManager.sendMessageToUser(user,text,replyKeyboardMarkup);

                    }else {
                        String text = """
                                ⚠️ לא ניתן לפרסם את הסקר!
                                נראה שאין לך שאלות בסקר.
                                אנא הוסף שאלה אחת לפחות כדי לפרסם! ✏️
                                """;
                        messageManager.sendAlert(update,text,true);
                    }

                }
                case "add-question" -> {
                    user.setStatus(User.UserStatus.CS_ANSWER_GET_QUESTION);

                    String text = "✏️ מה השאלה החדשה שברצונך להוסיף לסקר ? ";
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                }
                case "Editing-questions" -> {
                    user.setStatus(User.UserStatus.CS_Select_question);

                    String text = "📑 בסקר שלך יש " + user.getLastSurvey().getSurveyQuestions().size() + " שאלות !\n🔘 אנא בחר שאלה לעריכה :";
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                }
                case "survey-settings" -> {
                    user.setStatus(User.UserStatus.CS_surveySettings);

                    String text = """
                *⚙ תפריט הגדרות הסקר : *
                                
                ☑️ בחר את האפשרות שברצונך לערוך.
                ➖ שים לב כל שינוי שתבצע יחול רק על הסקר הנוכחי.
                """;
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                }
                case "Delete-Survey" ->{
                    String text =   "📊 *שם הסקר :* "+ user.getLastSurvey().getName() + "." + "\n" + "🔗  *ID Survey :* " + user.getLastSurvey().getId() + "." + "\n\n";
                    text = text + "🗑 הסקר נמחק לצמיתות!";
                    user.getSurveys().removeLast();
                    user.removeOneToSurveysHeCreated();
                    messageManager.sendMessageToUser(user,update,text,null,false);
                    text = """
                    🔑 לרשימת הפקודות המהירות שלח: /help
                    """;
                    user.setStatus(User.UserStatus.FREE);
                    ReplyKeyboardMarkup replyKeyboardMarkup = BotManager.getReplyKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,text,replyKeyboardMarkup);
                }
                case "Save-for-editing" ->{
                    String text =   "📊 *שם הסקר :* "+ user.getLastSurvey().getName() + "." + "\n" + "🔗  *ID Survey :* " + user.getLastSurvey().getId() + "." + "\n\n";
                    text = text + "☑️ הסקר נשמר לעירכה.";
                    messageManager.sendMessageToUser(user,update,text,null,false);
                    text = """
                    🔑 לרשימת הפקודות המהירות שלח: /help
                    """;
                    user.setStatus(User.UserStatus.FREE);
                    ReplyKeyboardMarkup replyKeyboardMarkup = BotManager.getReplyKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,text,replyKeyboardMarkup);
                }
            }
        }
    }

    private void handle_SurveySettings_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()){
            switch (user.getAnswer()) {
                case "Edit-distribution-time" -> {
                    user.setStatus(User.UserStatus.CS_SendingTimeDistribution);

                    String text = """
                            *📌 הגדר את זמן הוספת הסקר לתור ההפצה בקהילה.*
                            
                            """+"➖ ברירת מחדל: " +(user.getDefaultDistributionTime() == 0.0 ? "באופן מיידי!" : user.getDefaultDistributionTime() + " דקות.")+"\n☑️ סטטוס נוכחי: *" + (user.getLastSurvey().getTimeToAdd() == user.getDefaultDistributionTime() ? "ברירת מחדל *" : ( user.getLastSurvey().getTimeToAdd() == 0.0 ? "באופן מיידי!" : user.getLastSurvey().getTimeToAdd()+"* דקות מיצירת הסקר."))
                            +"\n\n⏳ להוספת הסקר מיידית לתור, לחץ על הכפתור. כדי לתזמן את ההוספה למועד מאוחר יותר, שלח את מספר הדקות עד להוספה.";

                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                }
                case "Editing-activity-duration" -> {
                    user.setStatus(User.UserStatus.CS_SendingDurationOfActivity);

                    String text = """
                            *📌 קבע את משך הזמן שבו ניתן יהיה לענות על הסקר מהרגע שהוא נשלח בקהילה.*
                            
                            """+"➖ ברירת מחדל: " +user.getDefaultDurationOfActivity() + " דקות.\n☑️ סטטוס נוכחי: *" + (user.getLastSurvey().getDurationOfActivity() == user.getDefaultDurationOfActivity() ? "ברירת מחדל *" : user.getLastSurvey().getDurationOfActivity()+" דקות.*") + "\n\n⏳ לשינוי משך הזמן, שלח את מספר הדקות המבוקש.";

                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                }
                case "New-Status-Survey" ->{
                    user.getLastSurvey().setAnonymousSurvey(!user.getLastSurvey().isAnonymousSurvey());
                    String text = "✅ ברירת מחדל עבור הצבעה בסקר שונתה ל: " + (user.getLastSurvey().isAnonymousSurvey() ? "אנונימית." : "גלויה.");
                    this.messageManager.sendAlert(update,text,false);
                    text = """
                *⚙ תפריט הגדרות הסקר : *
                                
                ☑️ בחר את האפשרות שברצונך לערוך.
                ➖ שים לב כל שינוי שתבצע יחול רק על הסקר הנוכחי.
                """;
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    this.messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                }
                case "Detail-Survey" -> {
                    String text = """
                       *⚙ תפריט הגדרות הסקר : *
                                
                ☑️ בחר את האפשרות שברצונך לערוך.
                ➖ שים לב כל שינוי שתבצע יחול רק על הסקר הנוכחי.
                
                *● סוג הסקר :*
                                                
                *👣 סקר אנונימי : *
                יוצר הסקר לא יוכל לראות מי הצביע בסקר.
                                                
                *👁 סקר גלוי : *
                יוצר הסקר יוכל לראות מי הצביע בסקר.
                """;
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    this.messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                }
                case "Reset-survey-settings-by-default" -> {
                    user.getLastSurvey().applyDefaultSettings();
                    String text = "♻️ ההגדרות עודכנו לעריכת ברירת מחדל!";
                    messageManager.sendAlert(update,text,false);
                    text = """
                *⚙ תפריט הגדרות הסקר : *
                                
                ☑️ בחר את האפשרות שברצונך לערוך.
                ➖ שים לב כל שינוי שתבצע יחול רק על הסקר הנוכחי.
                """;
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    this.messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                }
                case "Back-Main-Menu" -> {
                    user.setStatus(User.UserStatus.CS_mainMenu);
                    String text =   "📊 *שם הסקר :* "+ user.getLastSurvey().getName() + "." + "\n" + "🔗  *ID Survey :* " + user.getLastSurvey().getId() + "." + "\n\n";
                    text = text + "🔰 אנא בחר אחת מהאפשרויות למטה להמשך עריכת הסקר:";
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                }
            }
        }
    }

    private void handle_SelectQuestion_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            if (user.getAnswer().startsWith("Select question:")) {
                String[] details = user.getAnswer().split(":");
                user.getLastSurvey().setLastEditedQuestion(Integer.parseInt(details[1]));
                user.getLastSurvey().getSurveyQuestions().get(user.getLastSurvey().getLastEditedQuestion()).setLastAnswerAdded(user.getLastSurvey().getSurveyQuestions().get(user.getLastSurvey().getLastEditedQuestion()).getAnswerOptions().size() - 1);
                user.setStatus(User.UserStatus.CS_Edit_question_menu);

                EditMessageText EMessage = (EditMessageText) getEditQuestionMenuCSMessage(user, update, false);
                messageManager.sendMessage(EMessage);

            } else if (user.getAnswer().equals("Back-Main-Menu")) {
                user.setStatus(User.UserStatus.CS_surveySettings);
                user.setAnswer("Back-Main-Menu");
                getMessage(user,update);
            }
        }
    }

    private void handle_EditQuestionMenu_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            switch (user.getAnswer()) {
                case "delete", "Back menu" -> {
                    if (user.getAnswer().equals("delete")) {
                        for (int i = user.getLastSurvey().getLastEditedQuestion() + 1; i < user.getLastSurvey().getSurveyQuestions().size(); i++) {
                            SurveyQuestion q = user.getLastSurvey().getSurveyQuestions().get(i);
                            q.setQuestionNumber(q.getQuestionNumber() - 1);
                        }
                        user.getLastSurvey().getSurveyQuestions().remove(user.getLastSurvey().getLastEditedQuestion());
                        String text = "🗑 השאלה הוסרה מהסקר!";
                        this.messageManager.sendAlert(update, text, false);
                    }

                    user.setStatus(User.UserStatus.CS_Select_question);
                    String text = "📑 בסקר שלך יש " + user.getLastSurvey().getSurveyQuestions().size() + " שאלות !\n🔘 אנא בחר שאלה לעריכה :";
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);

                }
                case "Edit-question" -> {
                    user.setStatus(User.UserStatus.CS_EditingQuestionItself);
                    String text = "📄 *השאלה הנוכחית : *" + user.getLastSurvey().getSurveyQuestions().get(user.getLastSurvey().getLastEditedQuestion()).getQuestionText()+".\n";
                    text = text + "✏️ מה תיהיה השאלה החדשה ? ";
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);

                }
                case "Edit-answers" -> {
                    user.setStatus(User.UserStatus.CS_Editing_answers);
                    String text = "📑 כעת שלח את התשובות לשאלה " + (user.getLastSurvey().getLastEditedQuestion() + 1) + "! \n" +
                            "➖ ניתן לשלוח לפחות 2 תשובות אפשריות. לסיום, שלח : /finish .";
                    InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
                    messageManager.sendMessageToUser(user,update,text,inlineKeyboardMarkup,false);
                }
            }
        }

    }

    private void handle_editingAnswers_Status(User user, Update update) throws TelegramApiException {
        int numAnswer = user.getLastSurvey().getLastEditedQuestion();
        SurveyQuestion q1 = user.getLastSurvey().getSurveyQuestions().get(numAnswer);

        if (update.hasCallbackQuery()){
            if (user.getAnswer().equals("Back-edit-menu")){
                for (int i = q1.getAnswerOptions().size()-1; i > q1.getLastAnswerAdded(); i--) {
                    q1.removeAnswerByIndex(i);
                }
                user.setStatus(User.UserStatus.CS_Edit_question_menu);
                EditMessageText EMessage = (EditMessageText) getEditQuestionMenuCSMessage(user,update,false);
                this.messageManager.sendMessage(EMessage);
            }
        }else if (update.hasMessage() && update.getMessage().hasText()){
            if (user.getAnswer().equals("/finish") && ((q1.getSumOfAnswer()-1)-q1.getLastAnswerAdded()) >= 2){
                for (int i = q1.getLastAnswerAdded(); i >= 0; i--) {
                    q1.removeAnswerByIndex(i);
                }

                user.setStatus(User.UserStatus.CS_Edit_question_menu);
                String text = "✅ השאלה עודכנה בהצלחה ונוספה לסקר!";
                messageManager.sendMessageToUser(user,text,null);

                SendMessage EMessage = (SendMessage) getEditQuestionMenuCSMessage(user,update,true);
                this.messageManager.sendMessage(EMessage);

            }else if (user.getAnswer().equals("/finish")) {
                String text = "🚫 שים לב, עליך לשלוח לפחות 2 תשובות אפשריות לכל שאלה!";
                messageManager.sendMessageToUser(user,text,null);
            }else {
                List<String> newAnswers = new ArrayList<>(q1.getAnswerOptions());
                newAnswers.subList(0, q1.getLastAnswerAdded()+1).clear();

                if (!newAnswers.contains(user.getAnswer())) {
                    q1.addAnswer(user.getAnswer());
                }
            }
        }
    }

    private void handle_editingQuestionItself_Status(User user, Update update) throws TelegramApiException {
        if (update.hasMessage() && update.getMessage().hasText()){
            for (SurveyQuestion question : user.getLastSurvey().getSurveyQuestions()){
                if (question.getQuestionText().equals(user.getAnswer())){
                    String text = "קיימת כבר שאלה זהה בסקר! 🔄 נסה שוב!";
                    messageManager.sendMessageToUser(user,text,null);
                    return;
                }
            }
            user.setStatus(User.UserStatus.CS_Edit_question_menu);
            user.getLastSurvey().getSurveyQuestions().get(user.getLastSurvey().getLastEditedQuestion()).setQuestionText(user.getAnswer());
            String text = "✅ השאלה עודכנה בהצלחה ונוספה לסקר! ";
            messageManager.sendMessageToUser(user,text,null);

            SendMessage EMessage = (SendMessage) getEditQuestionMenuCSMessage(user,update,true);
            messageManager.sendMessage(EMessage);

        }else if (update.hasCallbackQuery()){
            if (user.getAnswer().equals("Back-edit-menu")){
                user.setStatus(User.UserStatus.CS_Edit_question_menu);
                EditMessageText EMessage = (EditMessageText) getEditQuestionMenuCSMessage(user,update,false);
                messageManager.sendMessage(EMessage);
            }
        }
    }

    private Double getNumberFromUser(User user) throws TelegramApiException {
        double timeToSend;

        try {
            timeToSend = Double.parseDouble(user.getAnswer());
            if (timeToSend < 0) {
                String text = """
                        ⚠️ הקלט שהתקבל שגוי.
                        שים לב אתה צריך לשלוח מספר חיובי בלבד❗
                        ☑️ לדוגמא : 1 או 2.""";
                messageManager.sendMessageToUser(user,text,null);
                return null;
            }
        } catch (Exception e) {
            String text = """
                    ⚠️ הקלט שהתקבל שגוי.
                    שים לב אתה צריך לשלוח רק את מספר דקות בלבד❗
                    ☑️ לדוגמא : 1 או 2.""";
            messageManager.sendMessageToUser(user,text,null);
            return null;
        }
        return timeToSend;
    }

    private void handle_SendingDurationOfActivity_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            if (user.getAnswer().equals("back-survey-settings")) {
                user.setStatus(User.UserStatus.CS_surveySettings);
                EditMessageText EMessage = (EditMessageText) getSurveySettingsCSMessage(user, update, false);
                messageManager.sendMessage(EMessage);
            }
        }else if (update.hasMessage() && update.getMessage().hasText()){

            Double timeToSend = getNumberFromUser(user);
            if (timeToSend == null) {
                return;
            }
            user.getLastSurvey().setDurationOfActivity(timeToSend);

            user.setStatus(User.UserStatus.CS_surveySettings);
            String text = "♻️ הסטטוס למשך זמן פעילות הסקר שונה ל : " + (user.getLastSurvey().getDurationOfActivity() == 5 ? "ברירת מחדל." : "משך זמן של " + user.getLastSurvey().getDurationOfActivity() + " דקות.");
            messageManager.sendMessageToUser(user,text,null);
            SendMessage message = (SendMessage) getSurveySettingsCSMessage(user, update, true);
            messageManager.sendMessage(message);
        }
    }

    private void handle_SendingTimeDistribution_Status(User user, Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            if (user.getAnswer().equals("back-survey-settings")) {
                user.setStatus(User.UserStatus.CS_surveySettings);
                EditMessageText EMessage = (EditMessageText) getSurveySettingsCSMessage(user, update, false);
                messageManager.sendMessage(EMessage);
            } else if (user.getAnswer().equals("Immediate dispatch")) {
                user.getLastSurvey().setTimeToAdd(0);

                user.setStatus(User.UserStatus.CS_surveySettings);
                String text = "♻️ הסטטוס להוספת הסקר לתור שונה ל : " + (user.getLastSurvey().getTimeToAdd() == 0 ? "ברירת מחדל. (באופן מיידי)" : user.getLastSurvey().getTimeToAdd() + " דקות מיצרת הסקר.");
                messageManager.sendMessageToUser(user,text,null);
                //
                SendMessage message = (SendMessage) getSurveySettingsCSMessage(user, update, true);
                this.messageManager.sendMessage(message);
            }
        }else if (update.hasMessage() && update.getMessage().hasText()) {
            Double timeToSend = getNumberFromUser(user);
            if (timeToSend == null) {
                return;
            }
            user.getLastSurvey().setTimeToAdd(timeToSend);

            user.setStatus(User.UserStatus.CS_surveySettings);
            String text = "♻️ הסטטוס להוספת הסקר לתור שונה ל : " + (user.getLastSurvey().getTimeToAdd() == 0 ? "ברירת מחדל. (באופן מיידי)" : user.getLastSurvey().getTimeToAdd() + " דקות מיצרת הסקר.");
            messageManager.sendMessageToUser(user,text,null);
            //
            SendMessage message = (SendMessage) getSurveySettingsCSMessage(user, update, true);
            this.messageManager.sendMessage(message);
        }
    }

    private BotApiMethod<?> getSurveySettingsCSMessage(User user, Update update, boolean newMessage) {
        String chatId = user.getChatId();
        String text = """
                *⚙ תפריט הגדרות הסקר : *
                                
                ☑️ בחר את האפשרות שברצונך לערוך.
                ➖ שים לב כל שינוי שתבצע יחול רק על הסקר הנוכחי.
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

    private BotApiMethod<?> getEditQuestionMenuCSMessage(User user, Update update ,boolean newMessage) {
        String chatId = user.getChatId();
        String text = user.getLastSurvey().getSurveyQuestions().get(user.getLastSurvey().getLastEditedQuestion()).getQuestionAndAnswerAsText();
        InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupByStatus(user);
        if (newMessage){
            SendMessage message = new SendMessage(chatId, text);
            message.setReplyMarkup(inlineKeyboardMarkup);
            message.setParseMode("Markdown");
            return message;
        }else {
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            EditMessageText EMessage = new EditMessageText(text);
            EMessage.setMessageId(messageId);
            EMessage.setChatId(chatId);
            EMessage.setReplyMarkup(inlineKeyboardMarkup);
            EMessage.setParseMode("Markdown");
            return EMessage;
        }
    }

    private InlineKeyboardMarkup getInlineKeyboardMarkupByStatus(User user){
        List<List<String[]>> rows = new ArrayList<>();
        switch (user.getStatus()) {
            case CS_mainMenu:
                MessageManager.addFButtonToNewRow(rows, "🌐 פרסם סקר", "complete-Survey");
                MessageManager.addFButtonToNewRow(rows, "➕ הוסף שאלה", "add-question");
                MessageManager.addFButtonToNewRow(rows, "🗂 ערוך שאלות", "Editing-questions");
                MessageManager.addFButtonToNewRow(rows,"⚙ הגדרות סקר","survey-settings");
                MessageManager.addFButtonToNewRow(rows,"🗑 מחק סקר","Delete-Survey");
                MessageManager.addFButtonToNewRow(rows,"☑️ שמור לעריכה","Save-for-editing");
                return MessageManager.createsFloatingButtons(rows);
            case CS_Edit_question_menu:
                MessageManager.addFButtonToNewRow(rows, "✏️ ערוך שאלה", "Edit-question");
                MessageManager.addFButtonToNewRow(rows, "📝 ערוך תשובות", "Edit-answers");
                MessageManager.addFButtonToNewRow(rows, "🗑 מחק שאלה", "delete");
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "Back menu");
                return MessageManager.createsFloatingButtons(rows);
            case CS_SendingTimeDistribution:
                MessageManager.addFButtonToNewRow(rows, "📩 שלח באופן מיידי!", "Immediate dispatch");
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-survey-settings");
                return MessageManager.createsFloatingButtons(rows);
            case CS_ANSWER_GET_QUESTION:
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "Back-Main-Menu");
                return MessageManager.createsFloatingButtons(rows);
            case CS_Select_question:
                for (int i = 0; i < user.getLastSurvey().getSurveyQuestions().size(); i++){
                    MessageManager.addFButtonToNewRow(rows,user.getLastSurvey().getSurveyQuestions().get(i).getQuestionText(),("Select question:"+i));
                }
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "Back-Main-Menu");
                return MessageManager.createsFloatingButtons(rows);
            case CS_EditingQuestionItself:
            case CS_Editing_answers:
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "Back-edit-menu");
                return MessageManager.createsFloatingButtons(rows);
            case CS_surveySettings:
                MessageManager.addFButtonToNewRow(rows,"⏲ עריכת זמן הפצה","Edit-distribution-time");
                MessageManager.addFButtonToNewRow(rows,"🌐 עריכת משך זמן פעילות","Editing-activity-duration");
                List<String[]> newRow = List.of(
                        new String[]{user.getLastSurvey().isAnonymousSurvey() ? "אנונימי" : "גלוי","New-Status-Survey"},
                        new String[]{" סוג הסקר - >", "Detail-Survey"}
                );

                MessageManager.addFButtonsToNewRow(rows,newRow);
                MessageManager.addFButtonToNewRow(rows,"♻️ אפס לעריכת ברירת מחדל","Reset-survey-settings-by-default");
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "Back-Main-Menu");
                return MessageManager.createsFloatingButtons(rows);
            case CS_SendingDurationOfActivity:
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-survey-settings");
                return MessageManager.createsFloatingButtons(rows);
            case CS_SurveySelection:
                for (Survey survey : user.getSurveys()){
                    MessageManager.addFButtonToNewRow(rows, survey.getName(), "NAME:"+survey.getId());
                }
                return MessageManager.createsFloatingButtons(rows);
            case CS_ViewingSurvey:
                MessageManager.addFButtonToNewRow(rows,  "🗃 תוצאות", "results:"+user.getAnswer().split(":")[1]);
                MessageManager.addFButtonToNewRow(rows, "✏️ ערוך", "edited:"+user.getAnswer().split(":")[1]);
                MessageManager.addFButtonToNewRow(rows, "🗑 מחק", "Delete:"+user.getAnswer().split(":")[1]);
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-surveys");
                return MessageManager.createsFloatingButtons(rows);
            case CS_ViewingResults:
                MessageManager.addFButtonToNewRow(rows, "🗳️ מצביעים", "voting:"+user.getAnswer().split(":")[1]);
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-survey:"+user.getAnswer().split(":")[1]);
                return MessageManager.createsFloatingButtons(rows);
            case CS_voting:
                String name = user.getAnswer().split(":")[1];
                Survey survey = user.getSurveyByID(name);
                for (User user1 : survey.getAllVoting()){
                    String text = user1.isAnonymousAccount() ?  "אנונימי" : user1.getDetails().getFirstName() +  (user1.getDetails().getLastName() == null ? "" : user1.getDetails().getLastName());
                    MessageManager.addFButtonToNewRow(rows, text,user1.isAnonymousAccount() ? "Anonymous-Account" : "voter:"+name+":"+user1.getChatId());
                }
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-survey-results:"+name);
                return MessageManager.createsFloatingButtons(rows);
            case CS_voter:
                MessageManager.addFButtonToNewRow(rows, "👤 מידע על המשתמש 👤", "information:"+user.getAnswer().split(":")[1]+":"+user.getAnswer().split(":")[2]);
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-voting:"+user.getAnswer().split(":")[1]+":"+user.getAnswer().split(":")[2]);
                return MessageManager.createsFloatingButtons(rows);
            case CS_UserInformation:
                MessageManager.addFButtonToNewRow(rows, "⬅️ חזור", "back-voter:"+user.getAnswer().split(":")[1]+":"+user.getAnswer().split(":")[2]);
                return MessageManager.createsFloatingButtons(rows);
            default:
                return null;
        }
    }
}