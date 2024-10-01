package org.example;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VoteManager {
    private final DataLoader dataLoader;
    private final List<Survey> surveys;
    private Survey currentSurvey;
    private final MessageManager messageManager;

    public VoteManager(DataLoader dataLoader, MessageManager messageManager){
        this.surveys = new ArrayList<>();
        this.messageManager = messageManager;
        this.dataLoader = dataLoader;
        sendSurveysToAllSubscribers();
    }
    public void answerASurvey(User user, Update update) throws TelegramApiException {
        String[] details = update.getCallbackQuery().getData().split(":");

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());

        if ((!surveys.isEmpty()) && (currentSurvey.getId() + "").equals(details[0])) {
            int questionIndex = Integer.parseInt(details[1]);
            if (!currentSurvey.getAllVoting().contains(user)){
                user.addOneToParticipationInSurveys();
            }

            currentSurvey.getSurveyQuestions().get(questionIndex).addVote(Integer.parseInt(details[2]), user);
            String text = "✅ ההצבעה בוצעה בהצלחה!";
            messageManager.sendAlert(update,text,false);

        } else if (details.length == 3){
            String text = "🚫️ פג תוקף סקר זה!";
            messageManager.sendAlert(update,text,true);
        }else {
            String text = "⛔️ כפתור זה יצא משימוש";
            messageManager.sendAlert(update,text,true);
        }
    }

    private void sendSurveysToAllSubscribers() throws RuntimeException {
        new Thread(()->{
            while (true){
                try {

                    try {
                        this.currentSurvey = surveys.getFirst();
                    } catch (Exception e) {
                        Thread.sleep(1000);
                        this.currentSurvey = null;
                        continue;
                    }

                    currentSurvey.setDistributed(true);

                    String text = """
                            *📩 סקר חדש זמין בקהילה!*
                                                        
                            🗳 נשמח שתצביעו.
                            ⚡️ למעבר מהיר לסקר [לחץ כאן!](https://t.me/SurveyDevelopmentBot?start=active_survey)
                            """;

                    List<User> usersList = dataLoader.getUsersAsList();
                    for (int i = 0; i < usersList.size(); i++) {
                        if (usersList.get(i) == currentSurvey.getSurveyCreator()) {
                            String text2 = "📩 הסקר שלך נשלח כרגע לכל חברי הקהילה!";
                            messageManager.sendMessageToUser(usersList.get(i),null,text2,null,true);
                        } else if (usersList.get(i).isNewSurveyAlert()) {
                            messageManager.sendMessageToUser(usersList.get(i),null,text,null,true);
                        }
                    }

                    int timeActivityToMil = (int) (currentSurvey.getDurationOfActivity() * 60 * 1000);


                    while (timeActivityToMil >= 0) {

                        List<User> userVotersList = new ArrayList<>(currentSurvey.getUsersThatParticipateInAll());
                        userVotersList.remove(currentSurvey.getSurveyCreator());

                        if (userVotersList.size() == dataLoader.getUsersAsList().size()-1) {
                            String text3 = "*📊 התקבלו תוצאות עבור הסקר '" + currentSurvey.getName() + "' שפירסמת.*\n\n" ;
                            text3 = text3 + "⚡️ למעבר מהיר לצפייה בתוצאות הסקר [לחץ כאן!](https://t.me/SurveyDevelopmentBot?start=SurveyResults_" + currentSurvey.getId()+")";
                            messageManager.sendMessageToUser(currentSurvey.getSurveyCreator(),null,text3,null,true);
                            break;
                        }
                        Thread.sleep(1000);
                        timeActivityToMil -= 1000;
                    }


                    if (timeActivityToMil <= 0) {
                        messageManager.sendMessage(currentSurvey.getResultsAsMessage());
                    }


                    currentSurvey.setHasResults(true);
                    surveys.removeFirst();
                }catch (InterruptedException  | TelegramApiException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void addSurveyToTheSurveyQueue(Survey survey) {
        new Thread(()->{
            int timeToMil =(int)(survey.getTimeToAdd()*60*1000);

            try {
                Thread.sleep(timeToMil);
                surveys.add(survey);
                dataLoader.addOneToSumOfSurvey();
            } catch (InterruptedException | IOException e ) {
                e.printStackTrace();
            }
        }).start();
    }

    public void printTheCurrentSurvey(User user ,Update update) throws TelegramApiException{
        if (this.currentSurvey != null) {
            for (SendMessage message : currentSurvey.getQuestionsAsMessage()) {
                message.setChatId(user.getChatId());
                String text2 = message.getText();
                String text1 = "📊 *שם הסקר :* " + currentSurvey.getName() + "." + "\n" + "🔗  *ID Survey :* " + currentSurvey.getId() + "." + "\n\n";
                message.setText(text1 + text2);
                message.setParseMode("Markdown");
                messageManager.sendMessage(message);
            }
            int messageID = messageManager.sendMessageToUser(user, "ㅤ", null).getMessageId();
            messageManager.deleteMessage(user, messageID);
        } else {
            String text = "🚫 אין סקר פעיל כרגע בקהילה.\n" +
                    "⚡️ [צור סקר חדש כאן!](https://t.me/SurveyDevelopmentBot?start=create_survey)";

            user.setStatus(User.UserStatus.FREE);
            messageManager.sendMessageToUser(user, update,text, null,true);
        }
    }
    public void removeSurveys(Survey survey){
        if (surveys.contains(survey)){
            if (!currentSurvey.equals(survey)){
                surveys.remove(survey);
            }
        }
    }

}