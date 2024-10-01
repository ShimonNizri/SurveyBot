package org.example;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.*;


public class BotManager {
    private final DataLoader dataLoader;
    private final MessageManager messageManager;
    private final VoteManager voteManager;
    private final ProfileManager profileManager;
    private final SurveyManager surveyManager;
    private final Map<String,User> users;
    private final UserSearchManager userSearchManager;
    private final Bot bot;

    public BotManager(Bot bot) throws IOException{
        this.bot = bot;
        this.dataLoader = new DataLoader();
        this.messageManager = new MessageManager(this.bot);
        this.profileManager = new ProfileManager(messageManager);
        this.voteManager = new VoteManager(dataLoader,messageManager);
        this.surveyManager = new SurveyManager(messageManager,voteManager,dataLoader);
        this.userSearchManager = new UserSearchManager(messageManager,dataLoader);
        this.dataLoader.initializeData();
        this.users = dataLoader.getUsersAsMap();
    }

    public void messageManagement(Update update){
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {

                User user = users.get(update.getMessage().getChatId().toString());
                if (user != null){
                    user.setDetails(update.getMessage().getFrom());
                }

                if (update.getMessage().getText().equals("/start") || user == null) {
                    user = loginMessage(update);
                    handle_FREE_Status(user,update);
                } else {
                    user.setAnswer(update.getMessage().getText());
                    if (user.getAnswer().startsWith("/start ")) {
                        String[] parts = user.getAnswer().split(" ");
                        if (parts.length > 1) {
                            String payload = parts[1]; // הפרמטר שנשלח בקישור העמוק
                            if (payload.equals("change_account")){
                                user.setStatus(User.UserStatus.EP_ProfileSettings);
                                user.setAnswer("Edit-Privacy");
                            }else if (payload.equals("create_survey")){
                                user.setAnswer("/"+payload);
                            }else if (payload.equals("active_survey")){
                                user.setAnswer("/"+payload);
                            }else if (payload.startsWith("SurveyResults")){
                                String idSurvey = payload.split("_")[1];
                                user.setStatus(User.UserStatus.CS_ViewingSurvey);
                                user.setAnswer("results:" +idSurvey);
                            }
                        }
                    }

                    switch (user.getAnswer()) {
                        case "/create_survey" -> {
                            user.setStatus(User.UserStatus.CS_CreateSurvey);
                            this.surveyManager.getMessage(user, update);
                        }
                        case "/search_users" -> {
                            user.setStatus(User.UserStatus.US_searchType);
                            this.userSearchManager.getMessage(user, update);
                        }
                        case "/active_survey" -> {
                            user.setStatus(User.UserStatus.voting);
                            this.voteManager.printTheCurrentSurvey(user, update);
                        }
                        case "/my_survey" -> {
                            user.setStatus(User.UserStatus.CS_MySurveys);
                            this.surveyManager.getMessage(user, update);
                        }
                        case "/help" -> printAListOfCommands(user);
                        case "/about" -> printAbout(user);
                        case "/my_profile" -> {
                            user.setStatus(User.UserStatus.EP_EditingProfile);
                            this.profileManager.getMessage(user, update);
                        }
                        default -> {
                            if (user.getStatus().toString().startsWith("CS_")) {
                                this.surveyManager.getMessage(user, update);
                            } else if (user.getStatus().toString().startsWith("EP_")) {
                                this.profileManager.getMessage(user, update);
                            } else if (user.getStatus().toString().startsWith("US_")) {
                                this.userSearchManager.getMessage(user, update);
                            } else if (user.getStatus().equals(User.UserStatus.FREE)) {
                                switch (user.getAnswer()) {
                                    case "📊 יצירת סקר 📊" -> {
                                        user.setStatus(User.UserStatus.CS_CreateSurvey);
                                        this.surveyManager.getMessage(user, update);
                                    }
                                    case "👤 ניהול פרופיל 👤" -> {
                                        user.setStatus(User.UserStatus.EP_EditingProfile);
                                        this.profileManager.getMessage(user, update);
                                    }
                                    case "🌐 מידע על הקהילה 🌐" -> printAbout(user);
                                    case "📤 סקר בהפצה 📤" -> {
                                        user.setStatus(User.UserStatus.voting);
                                        this.voteManager.printTheCurrentSurvey(user, update);
                                    }
                                    case "📁 הסקרים שלי 📁" -> {
                                        user.setStatus(User.UserStatus.CS_MySurveys);
                                        this.surveyManager.getMessage(user, update);
                                    }
                                    case "🔍 חיפוש משתמשים 🔍" -> {
                                        user.setStatus(User.UserStatus.US_searchType);
                                        this.userSearchManager.getMessage(user, update);
                                    }
                                }
                            }
                        }
                    }
                }
            }else if (update.hasCallbackQuery()){
                User user = users.get(update.getCallbackQuery().getMessage().getChatId().toString());
                if (user != null){
                    user.setDetails(update.getCallbackQuery().getFrom());
                }
                if (user == null){
                    user = loginMessage(update);
                    handle_FREE_Status(user,update);
                }else {
                    user.setAnswer(update.getCallbackQuery().getData());
                    if (user.getStatus() == User.UserStatus.voting) {
                        this.voteManager.answerASurvey(user, update);
                    } else if (user.getStatus().toString().startsWith("CS_")) {
                        this.surveyManager.getMessage(user, update);
                    }else if (user.getStatus().toString().startsWith("EP_")) {
                        this.profileManager.getMessage(user, update);
                    }else if (user.getStatus().toString().startsWith("US_")) {
                        this.userSearchManager.getMessage(user, update);
                    }

                }
            }
        }catch (Exception e){
            //ignore
        }
    }

    private void handle_FREE_Status(User user,Update update) throws TelegramApiException {
        if (update.hasMessage() &&  update.getMessage().hasText()) {
            String text = """
                    🔑 לרשימת הפקודות המהירות שלח: /help
                    """;
            ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkupByStatus(user);
            messageManager.sendMessageToUser(user,text,replyKeyboardMarkup);
        }
    }

    public User loginMessage(Update update) throws TelegramApiException,IOException{
        User newUser = new User(update.getMessage().getFrom());
        if (!users.containsKey(newUser.getChatId())) {
            dataLoader.addNewUser(newUser);
            for (User user : users.values()){
                if (user.isNewMemberAlert() && !user.equals(newUser)){
                    String text = "📩 משתמש חדש הצטרף לקהילה !";
                    messageManager.sendMessageToUser(user,update,text,null,true);
                }
            }
        }
        newUser = users.get(newUser.getChatId());
        newUser.setStatus(User.UserStatus.FREE);
        String text = "*ברוך הבא " + (newUser.getFullName()) +" לקהילת הסקרים שלנו! 🎉✨*\n\n" ;
        text = text + """                                            
                📌 בקהילה הזו תוכל :
                                
                📊 ליצור סקרים ולשתפם עם כל חברי הקהילה.
                🗳️ ולהשתתף בסקרים אחרים.
                                
                בכל פעם יופץ רק סקר אחד בקהילה 🔄, לפי סדר יצירתם, כך שכולם יוכלו להשתתף בצורה מסודרת ✅.
                                
                אנחנו שמחים שהצטרפת 🙌 ומחכים לראות את הסקרים המעניינים שתיצור! 📋✏️
                """;

        messageManager.sendMessageToUser(newUser,text,null);
        return newUser;
    }

    public void printAListOfCommands(User user) throws TelegramApiException{
        String text = """
                📋 *רשימת הפקודות המהירות: *
                                
                🔗 ליצירת סקר חדש שלח : /create\\_survey
                                
                🔗 לסקרים שלי שלח : /my\\_survey
                                
                🔗 לסקר בהפצה שלח : /active\\_survey
                                
                🔗 לחיפוש משתמשים : /search\\_users
                                
                🔗 לניהול הפרופיל שלי שלח : /my\\_profile
                                
                🔗 לקבל מידע על הקהילה שלח : /about
                """;

        this.messageManager.sendMessageToUser(user,null,text,null,true);

    }

    public void printAbout(User user) throws TelegramApiException{
        String text = "\uD83D\uDCCB נתונים על הבוט :\n" +
                "\n" +
                "\uD83D\uDC65 בקהילה שלנו קיימים "+ users.size()+ " חברים.\n" +
                "\uD83D\uDCCA עד כה נשלחו "+ dataLoader.getSumOfSurvey() +" סקרים בקהילה.";
        messageManager.sendMessageToUser(user,null,text,null,true);
    }

    public static ReplyKeyboardMarkup getReplyKeyboardMarkupByStatus(User user){
        List<List<String>> rows = new ArrayList<>();
        switch (user.getStatus()) {
            case FREE:
                MessageManager.addKButtonToNewRow(rows , "📤 סקר בהפצה 📤");
                MessageManager.addKButtonToNewRow(rows ,"📊 יצירת סקר 📊");
                MessageManager.addKButtonToNewRow(rows,"📁 הסקרים שלי 📁");
                MessageManager.addKButtonToNewRow(rows ,"👤 ניהול פרופיל 👤");
                MessageManager.addKButtonToNewRow(rows , "🔍 חיפוש משתמשים 🔍");
                MessageManager.addKButtonToNewRow(rows , "🌐 מידע על הקהילה 🌐");
                return MessageManager.createsKeyboardButtons(rows);
            default:
                return null;
        }
    }


}